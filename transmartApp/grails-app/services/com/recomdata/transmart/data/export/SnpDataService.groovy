package com.recomdata.transmart.data.export

import com.recomdata.dataexport.util.BiomarkerDataRowProcessor
import com.recomdata.transmart.data.export.util.FileWriterUtil
import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.rosuda.REngine.Rserve.RConnection
import org.springframework.beans.factory.annotation.Value
import org.transmart.searchapp.SearchKeyword
import org.transmartproject.db.support.DatabasePortabilityService

import javax.sql.DataSource
import java.sql.Clob
import java.sql.Statement

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

@Slf4j('logger')
class SnpDataService {

    private static final char QUOTE = "'"
    private static final char separator = '\t'
    private static final String[] HEADER = ['FAMILY ID', 'PATIENT ID', 'GENDER', 'MATERNAL ID', 'PATERNAL ID', 'CHROMOSOME DATA']
    private static final String lineSeparator = System.getProperty('line.separator')

    static transactional = false

    DatabasePortabilityService databasePortabilityService
    DataSource dataSource
    GeneExpressionDataService geneExpressionDataService
    def i2b2HelperService
    def plinkService
    SpringSecurityService springSecurityService

    @Value('${com.recomdata.search.genepathway:}')
    private String genepathway

    @Value('${com.recomdata.plugins.resultSize:-1}')
    private int resultSize

    @Value('${RModules.host:}')
    private String rmodulesHost

    @Value('${RModules.port:0}')
    private int rmodulesPort

    @Value('${com.recomdata.transmart.data.export.rScriptDirectory:}')
    private String rScriptDirectory

    Map getData(studyDir, fileName, jobName, resultInstanceId) {
	[PEDFiles: writePEDFiles(studyDir, fileName, jobName, resultInstanceId),
	 MAPFiles: writeMAPFiles(studyDir, fileName, jobName, resultInstanceId)]
    }

    private String getPatientId(subjectId) {
	def firstRow = new Sql(dataSource).firstRow(
	    'SELECT SOURCESYSTEM_CD FROM I2B2DEMODATA.PATIENT_DIMENSION WHERE PATIENT_NUM = ?', [subjectId])
	geneExpressionDataService.getActualPatientId firstRow?.SOURCESYSTEM_CD
    }

    private Map<Long, PatientData> getPatientData(resultInstanceId) {
	String query = '''
				SELECT DISTINCT patient_id, omic_patient_id, subject_id
				FROM DEAPP.de_subject_sample_mapping
				WHERE platform = 'SNP'
				  and patient_id in (
					SELECT DISTINCT patient_num 
						FROM I2B2DEMODATA.qtm_patient_set_collection
						WHERE result_instance_id = ?
				  )
			'''
	Map patientDataMap = [:]
	new Sql(dataSource).eachRow(query, [resultInstanceId]) { row ->
	    patientDataMap[row.PATIENT_ID] = new PatientData(
		patientId: row.PATIENT_ID,
		omicPatientId: row.OMIC_PATIENT_ID,
		subjectId: row.SUBJECT_ID)
        }

	patientDataMap
    }

    def getDataByPatientByProbes(File studyDir, String resultInstanceId, String jobName) {
        checkQueryResultAccess resultInstanceId

	String dataTypeName = 'SNP'
	String dataTypeFolder = 'Processed_data'
	int flushCount = 0
	int flushInterval = 5000
	Sql sql = new Sql(dataSource)

	List<String> subjectIds = i2b2HelperService.getSubjectsAsList(resultInstanceId)
	// Input params required for the R script are parentDir and subjectIdsStr
	String parentDir
	String subjectIdsStr = i2b2HelperService.getSubjects(resultInstanceId)

	Map<Long, PatientData> patientDataMap = getPatientData(resultInstanceId)

        //check if there are data for copy number. If not, don't create files for this
	String query = '''
		select dssm.patient_id, dssm.trial_name, dssd.snp_name, null as chrom, null as chrom_pos, dssd.copy_number
                from deapp.de_subject_sample_mapping dssm
		inner join deapp.DE_SAMPLE_SNP_DATA dssd on dssm.sample_cd = dssd.sample_id
                where dssm.patient_id in (
                    select distinct patient_num
                    from i2b2demodata.qtm_patient_set_collection
                    where result_instance_id = ?
                )'''
	def first = new Sql(dataSource).firstRow(query, Integer.valueOf(resultInstanceId))
	if (first) {
	    if (first[0] == 0) {
		logger.info 'No copy number data for these cohorts. Skip copy number export'
                return
            }
        }

	int fetchSize
	if (resultSize == -1) {
	    logger.warn 'com.recomdata.plugins.resultSize is not set!'
	    fetchSize = 10000
        }
	else {
	    fetchSize = resultSize
        }

	logger.debug 'Starting the long query to get cnv file information'

	for (subjectId in subjectIds) {
	    PatientData patientData = patientDataMap[subjectId.toLong()]

	    // Prepare the query to extract the records for this subject
	    query = '''
			select dssm.sample_cd as gsm_num, dssd.snp_name, dssd.copy_number
                        from deapp.de_subject_sample_mapping dssm
			inner join deapp.DE_SAMPLE_SNP_DATA dssd on dssm.sample_cd = dssd.sample_id
                        where dssm.patient_id = ?'''

	    String filename = subjectId + '.CNV'
	    FileWriterUtil writerUtil = new FileWriterUtil(studyDir, filename, jobName, dataTypeName, dataTypeFolder, separator)
	    BufferedWriter output = writerUtil.outputFile.newWriter(true)
            output << 'SAMPLE\tPATIENT ID\tPROBE ID\tCOPY NUMBER\n'

	    Sql s = new Sql(dataSource)
	    s.withStatement{ Statement stmt -> stmt.fetchSize = fetchSize }
	    s.eachRow query, [patientData?.omicPatientId], { row ->
		output.write row.GSM_NUM ?: ''
		output.write separator
		output.write patientData?.subjectId ?: ''
		output.write separator
		output.write row.SNP_NAME ?: ''
		output.write separator
		output.write row.COPY_NUMBER ?: ''
                output.newLine()

                flushCount++
                if (flushCount >= flushInterval) {
                    output.flush()
                    flushCount = 0
                }
            }

            parentDir = writerUtil.outputFile.parent
            output?.flush()
            output?.close()
        }

	logger.debug 'Finished the long query to get cnv file information; Starting the query to get platform'

        String platformQuery = '''
				SELECT dgi.title
				FROM DEAPP.de_subject_snp_dataset ssd
				INNER JOIN DEAPP.de_gpl_info dgi on dgi.platform=ssd.platform_name
				INNER JOIN DEAPP.de_subject_sample_mapping dssm on ssd.patient_num=dssm.omic_patient_id
				WHERE dssm.patient_id IN (
					SELECT DISTINCT patient_num
					FROM I2B2DEMODATA.qtm_patient_set_collection
					WHERE result_instance_id = ?
				)'''

        def firstRow = sql.firstRow(platformQuery, [resultInstanceId])
	String platformName = firstRow.title ?: 'Output'

	logger.debug 'Finished the query to get platform'

	// R script invocation starts here
	logger.debug 'Invoking R for transformations'
	RConnection c = new RConnection(rmodulesHost, rmodulesPort)
	String workingDirectoryCommand = "setwd('$parentDir')".replace('\\', '\\\\')
	c.eval workingDirectoryCommand

	String compilePivotDataCommand = "source('$rScriptDirectory/PivotData/PivotSNPCNVData.R')"
	c.eval compilePivotDataCommand

        parentDir = parentDir.replace('\\', '\\\\')

        String pivotDataCommand = "PivotSNPCNVData.pivot('$subjectIdsStr', ',', '$parentDir', '$platformName')"
	c.eval pivotDataCommand

	logger.debug 'Finished R transformations'

        c.close()
    }

    private Map writeMAPFiles(File studyDir, String fileName, jobName, resultInstanceId) {
	FileWriterUtil writerUtil = null
        try {
	    String platform = plinkService.getStudyInfoByResultInstanceId(resultInstanceId)[0]
	    if (platform) {
		String query = '''
				SELECT probe_def
				FROM DEAPP.de_snp_probe_sorted_def
				WHERE chrom != 'ALL'
				  and probe_def is not null
				  and platform_name=?'''

		List<String> clobStrings = []
		Sql sql = new Sql(dataSource)
		sql.eachRow(query, [platform]) { row ->
		    clobStrings << ((Clob) row.PROBE_DEF).asciiStream.text
                }
                //Since the file write takes a lot of time we close the connection once we have all the data for this patient
		sql.close()
		if (clobStrings) {
		    String dataTypeName = 'SNP'
		    String dataTypeFolder = 'Processed_data'
		    String snpFileName = 'SNPData.MAP'

                    writerUtil = new FileWriterUtil(studyDir, snpFileName, jobName, dataTypeName, dataTypeFolder, separator)
		    for (String s in clobStrings) {
                        // change probe_def format from 'SNP  chr  position' to 'chr  SNP position'
			s.eachLine { String line ->
			    String[] items = line.split()
			    if (items?.length == 3) {
				writerUtil.writeLine([items[1], items[0], items[2]] as String[])
                            }
			}
                    }
		}
            }
	}
	catch (e) {
	    logger.error 'Potential issue while exporting map file {}', e.message
        }
        finally {
            writerUtil?.finishWriting()
        }
    }

    private Map<String, String> writePEDFiles(File studyDir, String fileName, String jobName, String resultInstanceId) {
	List<String> subjectIds = i2b2HelperService.getSubjectsAsList(resultInstanceId)
	Map<String, String> patientConceptCdPEDFileMap = [:]
	FileWriterUtil writerUtil = null

	for (subjectId in subjectIds) {
	    String snpDataBySampleQry = '''
			SELECT t1.PATIENT_NUM, t1.CHROM, t1.PED_BY_PATIENT_CHR,
				case t2.PATIENT_GENDER
					 when 'M' then 1
					 when 'F' then 2
					 else 0
				          end as PATIENT_GENDER,
			       t2.CONCEPT_CD, t2.SUBJECT_ID
				FROM DEAPP.DE_SNP_DATA_BY_PATIENT t1
				INNER JOIN(SELECT DISTINCT PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, CONCEPT_CD, SUBJECT_ID
			           FROM DEAPP.DE_SUBJECT_SNP_DATASET) t2 on t1.patient_num=t2.patient_num
					INNER JOIN DEAPP.DE_SUBJECT_SAMPLE_MAPPING t3 on t1.patient_num=t3.omic_patient_id
					WHERE t1.TRIAL_NAME=t2.TRIAL_NAME
					and t1.CHROM != 'ALL'
					and t1.PED_BY_PATIENT_CHR is not null 
					and t3.patient_id = ?
                        		and t3.platform='SNP'
					ORDER BY t1.PATIENT_NUM, t2.CONCEPT_CD, t2.SUBJECT_ID
			'''
            try {
		String dataTypeName = 'SNP'
		List<Map> snpDataRows = []
		Sql sql = new Sql(dataSource)
                sql.eachRow(snpDataBySampleQry?.toString(), [subjectId]) { row ->
		    snpDataRows << [FAMILY_ID : row.SUBJECT_ID?.toString(),
				    PATIENT_NUM: row.PATIENT_NUM?.toString(),
				    CHROM : row.CHROM?.toString(),
				    PED_BY_PATIENT_CHR: (Clob) row.PED_BY_PATIENT_CHR,
				    PATIENT_GENDER : row.PATIENT_GENDER?.toString(),
				    CONCEPT_CD : row.CONCEPT_CD?.toString()]
                }
                //Since the file write takes a lot of time we close the connection once we have all the data for this patient
		sql.close()

		if (snpDataRows) {
		    String patientId = getPatientId(subjectId)
		    String dataTypeFolder = 'Processed_data'
		    String snpFileName = 'SNPData_' + patientId + '.PED'
                    writerUtil = new FileWriterUtil(studyDir, snpFileName, jobName, dataTypeName, dataTypeFolder, separator)
		    writerUtil.writeLine HEADER

		    for (row in snpDataRows) {
			String familyId = row.FAMILY_ID
			String patientNum = row.PATIENT_NUM
			Clob pedByPatientChrClob = row.PED_BY_PATIENT_CHR
			String patientGender = row.PATIENT_GENDER
			String conceptCd = row.CONCEPT_CD

                        //store the map between patient_conceptcd and the file created for it
			String key = patientNum + '_' + conceptCd
			if (null == patientConceptCdPEDFileMap[key]) {
			    patientConceptCdPEDFileMap[key] = writerUtil.outputFile.name
                        }

			writerUtil.writeLine([familyId, patientId, patientGender, '0', '0',
					      writerUtil.getClobAsString(pedByPatientChrClob)] as String[])
                    }
                }
            }
            finally {
                //Close existing file and flush out the contents
                writerUtil?.finishWriting()
	    }
	}

	patientConceptCdPEDFileMap
    }

    boolean getSnpDataByResultInstanceAndGene(resultInstanceId, String study, String pathway,
	                                      String sampleType, String timepoint, String tissueType,
	                                      BiomarkerDataRowProcessor rowProcessor, String fileLocation,
	                                      boolean genotype, boolean copyNumber) {

	boolean includePathwayInfo = false
	boolean retrievedData = false

        pathway = derivePathwayName(pathway)

        //These will be the two parts of the SQL statement. This SQL gets our SNP data by probe. We'll need to extract the actual genotypes/copynumber later.
        StringBuilder sSelect = new StringBuilder()
        StringBuilder sTables = new StringBuilder()

	sSelect << '''
				SELECT SNP.SNP_NAME AS SNP, DSM.PATIENT_ID, DSM.SUBJECT_ID, bm.BIO_MARKER_NAME AS GENE, DSM.sample_type,
				       DSM.timepoint, DSM.tissue_type, SNP.SNP_CALLS AS GENOTYPE, SNP.COPY_NUMBER AS COPYNUMBER,
				       PD.sourcesystem_cd, DSM.GPL_ID
		'''

        //This from statement needs to be in all selects.
	sTables << '''
				FROM DEAPP.DE_SUBJECT_SAMPLE_MAPPING DSM
				INNER JOIN I2B2DEMODATA.patient_dimension PD ON DSM.patient_id = PD.patient_num
				INNER JOIN I2B2DEMODATA.qtm_patient_set_collection qtm ON qtm.result_instance_id = ? AND qtm.PATIENT_NUM = DSM.PATIENT_ID
				INNER JOIN DEAPP.DE_SAMPLE_SNP_DATA SNP ON DSM.SAMPLE_CD = SNP.SAMPLE_ID
				INNER JOIN DEAPP.DE_SNP_GENE_MAP D2 ON D2.SNP_NAME = SNP.SNP_NAME
				INNER JOIN BIOMART.bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = ''' + databasePortabilityService.toChar('D2.ENTREZ_GENE_ID')

	// If a list of genes was entered, look up the gene ids and add them to the query.
	// If a gene signature or list was supplied then we modify the query to join on the tables that link the list to the gene ids.
	if (pathway && !(pathway.startsWith('GENESIG') || pathway.startsWith('GENELIST'))) {
	    sSelect << ', sk.SEARCH_KEYWORD_ID '

            //Include the tables we join on to get the unique_id.
	    sTables << """
				INNER JOIN BIOMART.bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
				INNER JOIN SEARCHAPP.search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
			"""

	    sTables << ' WHERE DSM.trial_name = ?'
	    sTables << ' AND sk.unique_id IN ' << convertStringToken(pathway) << ' '

            includePathwayInfo = true
        }
	else if (pathway && (pathway.startsWith('GENESIG') || pathway.startsWith('GENELIST'))) {
            //If we are querying by a pathway, we need to include that id in the final output.
	    sSelect << ', sk.SEARCH_KEYWORD_ID '

            //Include the tables we join on to filter by the pathway.
	    sTables << """
				INNER JOIN SEARCHAPP.search_bio_mkr_correl_fast_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
				INNER JOIN SEARCHAPP.search_keyword sk ON sk.bio_data_id = sbm.domain_object_id
			"""

            //Include the normal filter.
	    sTables << ' WHERE DSM.trial_name = ?'
	    sTables << ' AND sk.unique_id IN ' << convertStringToken(pathway) << ' '

            includePathwayInfo = true
        }
        else {
	    sTables << ' WHERE DSM.trial_name = ?'
        }

	if (sampleType) {
	    sTables << ' AND DSM.sample_type_cd IN ' << convertStringToken(sampleType)
        }

	if (timepoint?.trim()) {
	    sTables << ' AND DSM.timepoint_cd IN ' << convertStringToken(timepoint)
        }

	if (tissueType?.trim()) {
	    sTables << ' AND DSM.tissue_type_cd IN ' << convertStringToken(tissueType)
        }

	sSelect << sTables

	logger.debug 'SNP Query : {}',  sSelect

	int fetchSize
	if (resultSize == -1) {
	    logger.warn 'com.recomdata.plugins.resultSize is not set!'
	    fetchSize = 5000
        }
	else {
	    fetchSize = resultSize
        }

	new File(fileLocation).withWriterAppend { Writer out ->

            //Write the header line to the file.
	    if (includePathwayInfo) {
		out.write 'PATIENT.ID\tGENE\tPROBE.ID\tGENOTYPE\tCOPYNUMBER\tSAMPLE.TYPE\tTIMEPOINT\tTISSUE.TYPE\tGPL.ID\tSEARCH_ID'
            }
            else {
		out.write 'PATIENT.ID\tGENE\tPROBE.ID\tGENOTYPE\tCOPYNUMBER\tSAMPLE\tTIMEPOINT\tTISSUE.TYPE\tGPL.ID'
            }
	    out.write lineSeparator

	    Sql s = new Sql(dataSource)
	    s.withStatement{ Statement stmt -> stmt.fetchSize = fetchSize }
	    s.eachRow sSelect.toString(), [resultInstanceId, study], { row ->
		retrievedData = true

		SnpDataObject snpDataObject = new SnpDataObject(
		    patientNum: row.SUBJECT_ID,
		    probeName: row.SNP,
		    geneName: row.GENE,
		    sample: row.sample_type,
		    timepoint: row.timepoint,
		    tissue: row.tissue_type,
		    genotype: genotype ? row.GENOTYPE : 'NA',
		    copyNumber: copyNumber ? row.COPYNUMBER : 'NA',
		    gplId: row.GPL_ID)
                if (includePathwayInfo) {
		    snpDataObject.searchKeywordId = row.SEARCH_KEYWORD_ID ?: ''
                }

                //Write record.
		rowProcessor.processDataRow snpDataObject, out
            }
        }

	retrievedData
    }

    String convertStringToken(String t) {
        String[] ts = t.split(',')
        StringBuilder s = new StringBuilder('(')
        for (int i = 0; i < ts.length; i++) {
	    if (i > 0) {
		s << ','
	    }
	    s << QUOTE
	    s << ts[i]
	    s << QUOTE
        }
	s << ')'
	s
    }

    private String derivePathwayName(String pathwayName) {
	if (!pathwayName || pathwayName == 'null') {
	    pathwayName = null
        }

	boolean nativeSearch = genepathway == 'native'

	if (!nativeSearch && pathwayName) {
            //If we have multiple genes they will be comma separated. We need to split the string and find the unique ID for each.
	    String[] pathwayGeneList = pathwayName.split(',')

            //For each gene, get the long ID.
	    pathwayName = pathwayGeneList.collect { SearchKeyword.get(it).uniqueId }.join(',')
        }

	logger.debug 'pathway_name has been set to a keyword ID: {}', pathwayName
	return pathwayName
    }

    /**
     * Get the genes in a pathway based on the data in the search database.
     */
    String getGenes(String pathwayName) {

	String sql = '''
				select distinct bm.primary_external_id as gene_id
				from SEARCHAPP.search_keyword sk, BIOMART.bio_marker_correl_mv sbm, BIOMART.bio_marker bm
				where sk.bio_data_id = sbm.bio_marker_id
				  and sbm.asso_bio_marker_id = bm.bio_marker_id
				  and sk.unique_id IN ''' + convertStringToken(pathwayName)

        def genesArray = []
	new Sql(dataSource).eachRow sql, { row ->
            if (row.gene_id != null) {
		genesArray << row.gene_id
            }
        }

	convertList(genesArray, false, 1000)
    }

    String convertList(idList, boolean isString, int max) {
        StringBuilder s = new StringBuilder()
        int i = 0
        for (id in idList) {
            if (i < max) {
		if (s) {
		    s << ','
                }
                if (isString) {
		    s << QUOTE
                }
		s << id
                if (isString) {
		    s << QUOTE
                }
            }
            else {
                break
            }
            i++
        }
	s
    }
}

@CompileStatic
class SnpDataObject {
    String patientNum
    String probeName
    String genotype
    String copyNumber
    String geneName
    String searchKeywordId
    String sample
    String timepoint
    String tissue
    String gplId
}

@CompileStatic
class PatientData {
    def patientId
    def omicPatientId
    def subjectId
}
