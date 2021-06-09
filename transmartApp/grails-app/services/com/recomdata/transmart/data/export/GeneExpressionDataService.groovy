package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import com.recomdata.transmart.util.FileDownloadService
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.rosuda.REngine.Rserve.RConnection
import org.springframework.beans.factory.annotation.Value
import org.transmart.searchapp.SearchKeyword

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

@Slf4j('logger')
class GeneExpressionDataService {

    static transactional = false

    private static final List<String> controlSampleList = ['Unknown_GPL96', 'Unknown_GPL97'].asImmutable()

    private static final char separator = '\t'
    private String valueDelimiter = '\t'
    private int flushInterval = 5000

    DataSource dataSource
    FileDownloadService fileDownloadService

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

    boolean getData(List studyList, File studyDir, String fileName, String jobName, String resultInstanceId,
	            boolean pivot, List gplIds, String pathway, String timepoint, String sampleTypes,
	            String tissueTypes, boolean splitAttributeColumn) {

	boolean includePathwayInfo = false

        boolean dataFound = false

        try {
            pathway = derivePathwayName(pathway)
	    if (pathway) {
		includePathwayInfo = true
	    }

	    for (String study in studyList) {
		String sqlQuery = null
		String sampleQuery = null

                //Create a query for the Subset.
		if (resultInstanceId) {
		    sqlQuery = createMRNAHeatmapPathwayQuery(study, gplIds, pathway, timepoint, sampleTypes, tissueTypes)
		    sampleQuery = createStudySampleAssayQuery(gplIds, timepoint, sampleTypes, tissueTypes)
                }

		String filename = studyList?.size() > 1 ? study + '_' + fileName : fileName
                //The writeData method will return a map that tells us if data was found, and the name of the file that was written.
		Map writeDataStatusMap = writeData(resultInstanceId, sqlQuery, sampleQuery, studyDir, filename, jobName,
						   includePathwayInfo, splitAttributeColumn, gplIds, study)

		String outFile = writeDataStatusMap.outFile
		dataFound = writeDataStatusMap.dataFound
		if (outFile && dataFound && pivot) {
		    pivotData(studyList?.size() > 1, study, outFile)
                }
            }
        }
	catch (e) {
	    logger.error e.message, e
        }

	dataFound
    }

    private String createStudySampleAssayQuery(List gplIds, String timepoint, String sampleTypes, String tissueTypes) {
	StringBuilder sql = new StringBuilder()
	sql << '''
				SELECT DISTINCT ssm.assay_id, ssm.sample_type, ssm.timepoint, ssm.tissue_type,
				                ssm.sample_cd, ssm.trial_name, ssm.GPL_ID
				FROM DEAPP.de_subject_sample_mapping ssm
				INNER JOIN I2B2DEMODATA.qtm_patient_set_collection sc
				ON sc.result_instance_id = ? AND ssm.patient_id = sc.patient_num
				WHERE ssm.trial_name = ?'''

	if (sampleTypes) {
	    sql << ' AND ssm.sample_type_cd IN ' << convertStringToken(sampleTypes)
        }

	if (timepoint?.trim()) {
	    sql << ' AND ssm.timepoint_cd IN ' << convertStringToken(timepoint)
        }

	if (tissueTypes?.trim()) {
	    sql << ' AND ssm.tissue_type_cd IN ' << convertStringToken(tissueTypes)
        }

	if (gplIds) {
	    sql << ' AND ssm.GPL_ID IN (' << toListString(gplIds) << ')'
        }

	sql
    }

    /**
     * Creates a query to gather sample information for a given list of subject ids.
     * @param subsetName The subset name is hardcoded as a column into the result set so we know which subset the sample should be a part of.
     * @param ids This is a CSV list of subject IDs that we are retrieving data for.
     * @param pathwayName This is optionally the name of a pathway that we use to filter which genes we gather data for.
     * @param timepoint This is optionally a list of timepoints to gather data for.
     * @param sampleTypes When de_subject_sample_mapping is queried, we use the sample type to determine which assay ID's to retrieve data for.
     */
    private String createMRNAHeatmapPathwayQuery(String study, List gplIds, String pathwayName,
	                                         String timepoint, String sampleTypes, String tissueTypes) {

        StringBuilder sSelect = new StringBuilder()
        StringBuilder sTables = new StringBuilder()

        //create select table - we don't get sample mapping in this query - minimize network traffic from db...

	sSelect << '''
			SELECT a.PATIENT_ID, a.RAW_INTENSITY, a.ZSCORE, a.LOG_INTENSITY, a.assay_id, b.probe_id,
			       b.probeset_id, b.GENE_SYMBOL, b.GENE_ID, pd.sourcesystem_cd, ssm.gpl_id '''

	sTables << '''
		   FROM DEAPP.de_subject_microarray_data a
		   INNER JOIN DEAPP.de_subject_sample_mapping ssm ON ssm.assay_id = A.assay_id
		   INNER JOIN DEAPP.de_mrna_annotation b ON a.probeset_id = b.probeset_id and ssm.gpl_id = b.gpl_id
		   INNER JOIN I2B2DEMODATA.qtm_patient_set_collection sc ON sc.result_instance_id = ? AND ssm.PATIENT_ID = sc.patient_num
         INNER JOIN I2B2DEMODATA.PATIENT_DIMENSION pd on ssm.patient_id = pd.patient_num
	   '''

	//If a list of genes was entered, look up the gene ids and add them to the query.
	// If a gene signature or list was supplied then we modify the query to join on the tables that link the list to the gene ids.
	if (pathwayName && !(pathwayName.startsWith('GENESIG') || pathwayName.startsWith('GENELIST'))) {
            // insert distinct
            sSelect.insert(6, ' DISTINCT ')

            String keywordTokens = convertStringToken(pathwayName)

	    sSelect << ', sk.SEARCH_KEYWORD_ID '

            //Include the tables we join on to get the unique_id.
	    sTables << '''
		   INNER JOIN BIOMART.bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(b.GENE_ID)
		   INNER JOIN BIOMART.bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
		   INNER JOIN SEARCHAPP.search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
		   '''

	    sTables << " WHERE SSM.trial_name = '" << study << "' "
	    sTables << ' AND sk.unique_id IN ' << keywordTokens << ' '
        }
        else if (pathwayName?.startsWith('GENESIG') || pathwayName?.startsWith('GENELIST')) {
            //If we are querying by a pathway, we need to include that id in the final output.
	    sSelect << ', sk.SEARCH_KEYWORD_ID '

            //Include the tables we join on to filter by the pathway.
	    sTables << '''
		   INNER JOIN BIOMART.bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(b.GENE_ID)
		   INNER JOIN SEARCHAPP.SEARCH_BIO_MKR_CORREL_VIEW sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
		   INNER JOIN SEARCHAPP.search_keyword sk ON sk.bio_data_id = sbm.domain_object_id
		   '''

            //Include the normal filter.
	    sTables << " WHERE SSM.trial_name = '" << study << "' "
	    sTables << ' AND sk.unique_id IN ' << convertStringToken(pathwayName) << ' '
        }
        else {
	    sTables << " WHERE SSM.trial_name = '" << study << "' "
        }

	if (sampleTypes) {
	    sTables << ' AND ssm.sample_type_cd IN ' << convertStringToken(sampleTypes)
        }

	if (timepoint?.trim()) {
	    sTables << ' AND ssm.timepoint_cd IN ' << convertStringToken(timepoint)
        }

	if (tissueTypes?.trim()) {
	    sTables << ' AND ssm.tissue_type_cd IN ' << convertStringToken(tissueTypes)
        }

	if (gplIds) {
	    sTables << ' AND ssm.GPL_ID IN (' << toListString(gplIds) << ')'
        }

	sTables << ' ORDER BY probe_id, patient_id, gpl_id'

	sSelect << sTables

	sSelect
    }

    private String createDownloadCELFilesQuery(String resultInstanceId, studyList, String timepoint,
	                                       String sampleTypes, String tissueTypes) {
        String assayIds = getAssayIds(resultInstanceId, sampleTypes, timepoint, tissueTypes)
	if (!assayIds) {
	    return ''
        }

	'''
		SELECT DISTINCT a.PATIENT_ID, a.sample_type, a.timepoint, a.tissue_type, a.sample_cd, a.trial_name, pd.sourcesystem_cd
		FROM DEAPP.de_subject_sample_mapping a
		INNER JOIN I2B2DEMODATA.PATIENT_DIMENSION pd on a.patient_id = pd.patient_num
		WHERE a.trial_name in (''' + convertList(studyList, true, 1000) + ')' + '''
		  AND a.assay_id IN (''' + assayIds + ')' + '''
		  AND a.platform like 'MRNA%'
		ORDER BY patient_id'''
    }

    /**
     * Retrieves the Assay Ids for the given paramters from the de_subject_sample_mapping table.
     * @param resultInstanceId Result Instance Id.
     * @param sampleTypes Sample type
     * @param timepoint List of timepoints.
     */
    private String getAssayIds(String resultInstanceId, String sampleTypes, String timepoint, String tissueTypes) {
        checkQueryResultAccess resultInstanceId

        //Sql command used to retrieve Assay IDs.
	Sql sql = new Sql(dataSource)

        //SQL Query string.
        StringBuilder assayS = new StringBuilder()

	assayS << '''
			SELECT DISTINCT s.assay_id
			FROM DEAPP.de_subject_sample_mapping s, I2B2DEMODATA.qtm_patient_set_collection qtm
			WHERE qtm.patient_num = s.patient_id
			  AND qtm.result_instance_id = ? '''

	if (sampleTypes) {
	    assayS << ' AND s.sample_type_cd IN ' << convertStringToken(sampleTypes)
        }

	if (timepoint?.trim()) {
	    assayS << ' AND s.timepoint_cd IN ' << convertStringToken(timepoint)
        }

	if (tissueTypes?.trim()) {
	    assayS << ' AND s.tissue_type_cd IN ' << convertStringToken(tissueTypes)
        }

	assayS << ' ORDER BY s.assay_id'

	logger.debug 'getAssayIds used this query: {}', assayS

	List assayIds = []

        sql.eachRow(assayS.toString(), [resultInstanceId], { row ->
	    if (row.assay_id) {
		assayIds << row.assay_id
            }
        })

        //TODO: Why is there a max here?
        //Make a string of the assay IDs.
	convertList(assayIds, false, 1000)
    }

    private String convertStringToken(String t) {
        String[] ts = t.split(',')
        StringBuilder s = new StringBuilder('(')
        for (int i = 0; i < ts.length; i++) {
            if (ts[i]) {
		if (i) {
		    s << ','
		}
		s << "'"
		s << ts[i]
		s << "'"
            }
	}
	s << ')'
	
	s
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
		    s << "'"
                }
		s << id
                if (isString) {
		    s << "'"
                }
            }
            else {
                break
            }
            i++
        }

	s
    }

    private Map writeData(String resultInstanceId, String sqlQuery, String sampleQuery, File studyDir, String fileName,
	                  String jobName, includePathwayInfo, splitAttributeColumn, gplIds, String study) {

	String dataTypeName = 'mRNA'
	String dataTypeFolder = 'Processed_Data'
	boolean dataFound = false

	Connection con = dataSource.connection
	PreparedStatement stmt = con.prepareStatement(sqlQuery)
	stmt.setString 1, resultInstanceId
	stmt.fetchSize = stmtFetchSize

        // sample query
	PreparedStatement stmt1 = con.prepareStatement(sampleQuery)
	stmt1.setString 1, resultInstanceId
	stmt1.setString 2, study
	stmt1.fetchSize = stmtFetchSize

	logger.debug 'started file writing'

	FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator)
	File outFile = writerUtil.outputFile
	Writer output = outFile.newWriter(true)
        output << 'PATIENT ID\t'

	if (splitAttributeColumn) {
            output << 'SAMPLE TYPE\tTIMEPOINT\tTISSUE TYPE\tGPL ID\tASSAY ID\tVALUE\tZSCORE\tLOG2ED\tPROBE ID\tPROBESET ID\tGENE_ID\tGENE_SYMBOL'
	}
	else {
            output << 'SAMPLE\tASSAY ID\tVALUE\tZSCORE\tLOG2ED\tPROBE ID\tPROBESET ID\tGENE_ID\tGENE_SYMBOL'
	}

	if (includePathwayInfo) {
            output << '\tSEARCH_ID\n'
	}
	else {
            output << '\n'
	}

        String log2 = null
	Map<String, String> sttMap = [:]

	long startTime = System.currentTimeMillis()
        // performance optimization - reduce latency by eliminating sample type data from mRNA result
        //we retrieve the results in 2 queries
        // first one gets the sample, patient and timepoint from subject sample mapping table
        // create the sample type\t timepoint\tTissue type string and put it in a map with assay id as key

        // second query goes to the mrna table and gets the intensity data
        // use the assay id to look up the sample\tm\tissue type from the map created in the first query
        // and writes to the writer

	logger.debug 'start sample retrieving query; Sample Query : {}', sampleQuery
	ResultSet rs = stmt1.executeQuery()

        try {
            while (rs.next()) {
		String sampleType = rs.getString('SAMPLE_TYPE')
		String timepoint = rs.getString('TIMEPOINT')
		String tissueType = rs.getString('TISSUE_TYPE')
		String assayID = rs.getString('ASSAY_ID')
		String gplId = rs.getString('GPL_ID')

		String sql
                if (splitAttributeColumn) {
		    sql = (sampleType ?: '') + valueDelimiter + (timepoint ?: '') + valueDelimiter +
			(tissueType ?: '') + valueDelimiter + (gplId ?: '')
                }
                else {
		    sql = (sampleType ?: '') + (timepoint ? '_' + timepoint : '') + (tissueType ? '_' +
										     tissueType : '') + (gplId ? '_' + gplId : '')
                }
		sttMap[assayID] = sql
            }
        }
        finally {
            rs?.close()
            stmt1?.close()
        }
	logger.debug 'finished sample retrieving query'

        //Run the query.
	logger.debug 'begin data retrieving query: {}', sqlQuery
        rs = stmt.executeQuery()
	logger.debug 'query completed'

	ResultSetMetaData metaData = rs.metaData
	Map<String, Integer> nameIndexMap = [:]
	int count = metaData.columnCount
        for (int i = 1; i <= count; i++) {
	    nameIndexMap[metaData.getColumnName(i).toUpperCase()] = i
        }

	int rawIntensityRSIdx = nameIndexMap.RAW_INTENSITY
	int zScoreRSIdx = nameIndexMap.ZSCORE
	int ptIDIdx = nameIndexMap.PATIENT_ID
	int sourceSystemCodeIdx = nameIndexMap.SOURCESYSTEM_CD
	int assayIDIdx = nameIndexMap.ASSAY_ID
	int probeIDIdx = nameIndexMap.PROBE_ID
	int probesetIDIdx = nameIndexMap.PROBESET_ID
	int logIntensityRSIdx = nameIndexMap.LOG_INTENSITY
	int geneIDIdx = nameIndexMap.GENE_ID
	int geneSymbolIdx = nameIndexMap.GENE_SYMBOL
	int searchKeywordIdIdx = nameIndexMap.SEARCH_KEYWORD_ID

        int flushCount = 0
        long recCount = 0

        //A workaround for using only GPL96 values. I don't like the way we have to hard-code GPL96 here.
	String platformToUse = 'GPL96'
	Map<String, String> patientProbePlatformValueMap = [:]
	int gplIDIdx = nameIndexMap.GPL_ID

        try {
            //Iterate over the record set object.
            while (rs.next()) {
                //Pull the values we need from the record set object.
		String rawIntensityRS = rs.getString(rawIntensityRSIdx)
		String zScoreRS = rs.getString(zScoreRSIdx)
		String patientID = rs.getString(ptIDIdx)
		String sourceSystemCode = rs.getString(sourceSystemCodeIdx)
		String assayID = rs.getString(assayIDIdx)
		String probeID = rs.getString(probeIDIdx)
		String probesetID = rs.getString(probesetIDIdx)
		String logIntensityRS = rs.getString(logIntensityRSIdx)
		String geneID = rs.getString(geneIDIdx)
		String geneSymbolId = rs.getString(geneSymbolIdx)

                dataFound = true

                //To use only GPL96 when same probe present in both platforms
                if (gplIds.size() > 1) { // when there are more than one platforms
		    String gplID = rs.getString(gplIDIdx)
		    if (gplID == platformToUse) { // compared with the hard-coded value GPL96
			patientProbePlatformValueMap[patientID + '_' + probeID + '_' + gplID] = logIntensityRS
                    }
                    else {
			if (patientProbePlatformValueMap.containsKey(patientID + '_' + probeID + '_' + platformToUse)) {
			    continue
			}
                    }
                }

		writeNotEmptyString output, getActualPatientId(sourceSystemCode)
		output.write valueDelimiter
                // sample attribute, time point, tissue type
		output.write determineSampleAttribute(sttMap[assayID])
		output.write valueDelimiter
		writeNotEmptyString output, assayID
		output.write valueDelimiter

		// If the data is Global Normalized log_intensity is the value to output.
		// If the log_intensity is NULL, data-load process has to be corrected to always have a value.
		if (logIntensityRS) {
		    output.write logIntensityRS
                    log2 = '1'
                } /*
		 //Don't do the below for Global Normalized data as log_intensity must be present
		 else if(rawIntensityRS) { // calculate log 2
		 rawIntensity =  Double.valueOf(rawIntensityRS)
		 output.write((Math.log(rawIntensity)/Math.log(2)).toString())
		 log2 ='1'
	     } else if(zScoreRS) { // use zscore
		 output.write(zScoreRS)
		 log2 ='0'
	     }*/

                output.write(valueDelimiter)
                writeNotEmptyString(output, zScoreRS)
                output.write(valueDelimiter)
                output.write(log2)
                output.write(valueDelimiter)
                writeNotEmptyString(output, probeID)
                output.write(valueDelimiter)
                writeNotEmptyString(output, probesetID)
                output.write(valueDelimiter)
                writeNotEmptyString(output, geneID)
                output.write(valueDelimiter)
                writeNotEmptyString(output, geneSymbolId)

                if (includePathwayInfo) {
                    output.write(valueDelimiter)
		    writeNotEmptyString(output, rs.getString(searchKeywordIdIdx))
                }
                output.newLine()

                flushCount++
                if (flushCount >= flushInterval) {
                    output.flush()
                    recCount += flushCount
                    flushCount = 0
                }
            }
            if (!dataFound) {
		outFile?.delete()
                writeNotEmptyString(output, 'No data found to add to file.')
            }
        }
	catch (e) {
	    logger.error e.message, e
        }
        finally {
            output?.flush()
            output?.close()
            if (!dataFound) {
		outFile?.delete()
            }
	    logger.debug 'completed file writing'
            stmt?.close()
            con?.close()
        }

	logger.debug '\n \t total seconds:{}\n\n', ((System.currentTimeMillis() - startTime) / 1000)

	[outFile: outFile?.absolutePath, dataFound: dataFound]
    }

    private String determineSampleAttribute(String sample) {
        if (controlSampleList.contains(sample)) {
            sample = 'Unknown_GPL96_GPL97'
        }
	sample
    }

    private void pivotData(boolean multipleStudies, String study, String inputFileLoc) {
        //TODO pass the boolean param for deletion of the mRNA.trans file
	logger.debug 'Pivot File started'
	if (!inputFileLoc) {
	    return
	}

        File inputFile = new File(inputFileLoc)
	RConnection c = new RConnection(rmodulesHost, rmodulesPort)

        //Set the working directory to be our temporary location.
	String workingDirectoryCommand = "setwd('${inputFile.parent}')".replace('\\', '\\\\')

	logger.debug 'Attempting following R Command : {}', workingDirectoryCommand

        //Run the R command to set the working directory to our temp directory.
	c.eval workingDirectoryCommand

	String compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotGeneExprData.R')".replace('\\', '\\\\')

	logger.debug 'Attempting following R Command : {}', compilePivotDataCommand.replace('\\', '\\\\')

	c.eval compilePivotDataCommand

        //Prepare command to call the PivotGeneExprData.R script
	String pivotDataCommand = "PivotGeneExprData.pivot('$inputFile.name', '$multipleStudies', '$study')".replace('\\', '\\\\')

	logger.debug 'Attempting following R Command : {}', pivotDataCommand

	c.eval pivotDataCommand

        c.close()
    }

    private String derivePathwayName(String pathwayName) {
	if (!pathwayName || pathwayName == 'null') {
	    pathwayName = null
        }

	boolean nativeSearch = genepathway == 'native'
	if (!nativeSearch && pathwayName) {
            //If we have multiple genes they will be comma separated. We need to split the string and find the unique ID for each.
            //For each gene, get the long ID.
	    pathwayName = pathwayName.split(',').collect { SearchKeyword.get(it).uniqueId }.join(',')
        }

	logger.debug 'pathway_name has been set to a keyword ID: {}', pathwayName
	pathwayName
    }

    private List getCELFiles(String studyName, String sampleCd) {
	String sql = 'SELECT * FROM BIOMART.bio_content WHERE study_name = ? and file_name like ?'
	new Sql(dataSource).rows(sql, [studyName, sampleCd + '%'])
    }

    void downloadCELFiles(String resultInstanceId, studyList, File studyDir, String jobName, String pathway,
	                  String timepoint, String sampleTypes, String tissueTypes) {

	Map<String, String> sampleCdsMap = [:]

        try {
	    String sqlQuery = createDownloadCELFilesQuery(resultInstanceId, studyList, timepoint, sampleTypes, tissueTypes)
	    new Sql(dataSource).eachRow(sqlQuery, { row ->

		String sample = (row.SAMPLE_TYPE?.toString() ?: '')  +
		    (row.TIMEPOINT ? '_' + row.TIMEPOINT : '') +
		    (row.TISSUE_TYPE ? '_' + row.TISSUE_TYPE : '')

		String mapKey = (row.TRIAL_NAME?.toString() ?: '') + (row.SAMPLE_CD?.toString() ? '/' + row.SAMPLE_CD : '')
		String mapValue = getActualPatientId(row.sourcesystem_cd?.toString()) +
		    (sample.toString() ? '_' + sample : '')
		if (null == sampleCdsMap[mapKey]) {
		    sampleCdsMap[mapKey] = mapValue
		}
            })

	    if (sampleCdsMap) {
		File mRNADir = new FileWriterUtil().createDir(studyDir, 'mRNA')
		File rawDataDir = new FileWriterUtil().createDir(mRNADir, 'Raw_data')

		sampleCdsMap.each { String key, String value ->
		    File valueDir = new FileWriterUtil().createDir(rawDataDir, value)
                    // write files into that dir
                    // use service to download files by passing the folder and filesURLs
		    String[] keyList = key.tokenize('/')
		    String studyName = (keyList.size() == 2) ? keyList[0] : null
		    String sampleCd = (keyList.size() == 2) ? keyList[1] : null
                    if (studyName) {
			List<String> filesList = []
			for (file in getCELFiles(studyName, sampleCd)) {
			    filesList << file.CEL_LOCATION + file.FILE_NAME + file.CEL_FILE_SUFFIX
                        }

			fileDownloadService.getFiles filesList, valueDir.path
                    }
                }
            }
        }
	catch (e) {
	    logger.error e.message, e
        }
    }

    private void writeNotEmptyString(Writer writer, String str) {
	if (str) {
	    writer.write str
        }
    }

    private void validateCommonSubjectsIn2Subsets(Map resultInstanceIdMap) {
        checkQueryResultAccess(*resultInstanceIdMap.values())

	if (resultInstanceIdMap?.size() == 2) {
	    String sqlQuery = '''
					SELECT DISTINCT ssm.patient_id
					FROM DEAPP.de_subject_sample_mapping ssm
							INNER JOIN (SELECT DISTINCT patient_num 
					            FROM I2B2DEMODATA.qtm_patient_set_collection
							            WHERE result_instance_id = ?
							            INTERSECT
							            SELECT DISTINCT patient_num 
					            FROM I2B2DEMODATA.qtm_patient_set_collection
					            WHERE result_instance_id = ?
					            ) sc ON ssm.patient_id = sc.patient_num
							'''
	    def rows = new Sql(dataSource).rows(sqlQuery, resultInstanceIdMap.values() as List)
	    logger.debug 'Common subjects found :: {}', rows.size()
	    if (rows) {
		throw new Exception(' Common Subjects found in both Subsets. ')
	    }
        }
    }

    private void validateGSEAExport(Map resultInstanceIdMap) {
        try {
	    validateCommonSubjectsIn2Subsets resultInstanceIdMap
        }
	catch (e) {
            throw new Exception('GSEA Export validation failed.' + e.message)
        }
    }

    /**
     * GCT and CLS files are not created within Subset(1/2)_<StudyName> folder.
     * Instead they are created within GSEA folder within the jobTmpDirectory.
     */
    void getGCTAndCLSData(studyList, File studyDir, String fileName, String jobName,
	                  Map resultInstanceIdMap, boolean pivot, platformsList) {
	validateGSEAExport resultInstanceIdMap
	getGCTData studyList, studyDir, fileName, jobName, resultInstanceIdMap, pivot, platformsList
	getCLSData studyList, studyDir, fileName, jobName, resultInstanceIdMap, platformsList
    }

    private void getGCTData(List studyList, File studyDir, String fileName, String jobName,
                            Map resultInstanceIdMap, boolean pivot, List platformsList) {
	if (!resultInstanceIdMap) {
	    return
	}

        try {
	    String resultInstanceIds = getResultInstanceIdsAsStr(resultInstanceIdMap)
	    String sqlQuery = createGCTPathwayQuery(studyList, resultInstanceIds, platformsList)
	    String sampleQuery = createGCTStudySampleAssayQuery(studyList, resultInstanceIds, platformsList)

	    // Create the GCT.trans File in <currentJobDir>/GSEA folder, studyDir.parentDir == currentJobDir
	    // currentJobDir == <username_jobType_jobId>, ex: admin_DataExport_12345
	    String gctFilePath
            if (studyDir.isDirectory()) {
		File gseaDir = new FileWriterUtil().createDir(studyDir.parentFile, 'GSEA')
                gctFilePath = writeGCTData(sqlQuery, sampleQuery, gseaDir, fileName, jobName, platformsList)
            }

	    if (gctFilePath && pivot) {
		pivotGCTData gctFilePath
            }
        }
	catch (e) {
	    logger.error e.message, e
        }
    }

    private void getCLSData(List studyList, File studyDir, String fileName, String jobName,
                            Map resultInstanceIdMap, List platformsList) {

	if (!resultInstanceIdMap || !studyDir.isDirectory()) {
	    return
	}

        try {
	    // Create the GCT.trans File in <currentJobDir>/GSEA folder, studyDir.parentDir == currentJobDir
	    // currentJobDir == <username_jobType_jobId>, ex: admin_DataExport_12345
	    String resultInstanceIds = getResultInstanceIdsAsStr(resultInstanceIdMap)
	    String sqlQuery = createCLSDataQuery(studyList, resultInstanceIds, platformsList)
	    File gseaDir = new FileWriterUtil().createDir(studyDir.parentFile, 'GSEA')
	    writeCLSData sqlQuery, gseaDir, resultInstanceIdMap
        }
	catch (e) {
	    logger.error e.message, e
        }
    }

    private String createCLSDataQuery(List studyList, String resultInstanceIds, List platformsList) {
        checkQueryResultAccess(*resultInstanceIds.split(/,/)*.trim())

	'''
		SELECT DISTINCT ssm.patient_id, ssm.sample_type, ssm.timepoint, ssm.tissue_type, sc.result_instance_id
		FROM DEAPP.de_subject_sample_mapping ssm
		   INNER JOIN (SELECT DISTINCT patient_num, result_instance_id 
		            FROM I2B2DEMODATA.qtm_patient_set_collection
		            WHERE result_instance_id IN (''' + resultInstanceIds + ')' + '''
		           ) sc ON ssm.patient_id = sc.patient_num
		WHERE ssm.trial_name IN (''' + convertList(studyList, true, 100) + ')' + '''
		  AND ssm.gpl_id IN (''' + toListString(platformsList) + ')' + '''
		ORDER BY sc.result_instance_id desc'''
    }

    private String createGCTPathwayQuery(List studyList, String resultInstanceIds, List platformsList) {
        checkQueryResultAccess(*resultInstanceIds.split(/,/)*.trim())

	'''
	   	SELECT a.PATIENT_ID, a.LOG_INTENSITY, a.RAW_INTENSITY, a.assay_id, b.probe_id, b.probeset_id, pd.sourcesystem_cd, ssm.gpl_id
		FROM DEAPP.de_subject_microarray_data a
		   INNER JOIN (SELECT probe_id, probeset_id, min(gene_id) gene_id
		            FROM DEAPP.de_mrna_annotation
		            group by probe_id, probeset_id
		           ) b ON a.probeset_id = b.probeset_id
		INNER JOIN DEAPP.de_subject_sample_mapping ssm ON (ssm.trial_name = A.trial_name AND ssm.assay_id = A.assay_id)
		INNER JOIN I2B2DEMODATA.patient_dimension pd ON a.patient_id = pd.patient_num
	   		INNER JOIN (SELECT DISTINCT patient_num 
					                  FROM qtm_patient_set_collection 
		            WHERE result_instance_id IN (''' + resultInstanceIds + ')' + '''
		           ) sc ON ssm.patient_id = sc.patient_num
		WHERE ssm.trial_name IN (''' + toListString(studyList) + ')' + '''
		  AND ssm.gpl_id IN (''' + toListString(platformsList) + ')' + '''
		ORDER BY probe_id, patient_id, gpl_id'''
    }

    private String createGCTStudySampleAssayQuery(List studyList, String resultInstanceIds, List platformsList) {
        checkQueryResultAccess(*resultInstanceIds.split(/,/)*.trim())

	'''
	   	SELECT DISTINCT ssm.assay_id, ssm.sample_type, ssm.timepoint, ssm.tissue_type, ssm.sample_cd, ssm.trial_name, ssm.GPL_ID
      FROM DEAPP.de_subject_sample_mapping ssm
      INNER JOIN (SELECT DISTINCT patient_num
					                  FROM qtm_patient_set_collection 
                  WHERE result_instance_id IN (''' + resultInstanceIds + ')' + '''
		           ) sc ON ssm.patient_id = sc.patient_num
		WHERE ssm.trial_name IN (''' + toListString(studyList) + ')' + '''
		  AND ssm.gpl_id IN (''' + toListString(platformsList) + ')'
    }

    private String getResultInstanceIdsAsStr(Map resultInstanceIdMap) {
	StringBuilder str = new StringBuilder()
	for (val in resultInstanceIdMap.values()) {
	    if (val && ((String) val)?.trim()) {
		if (str) {
		    str << ','
		}
		str << val
	    }
	}

	str
    }

    private int getStmtFetchSize() {
	if (resultSize == -1) {
	    logger.warn 'com.recomdata.plugins.resultSize is not set!'
	    5000
        }
	else {
	    resultSize
        }
    }

    private Map<String, String> getSamplesMap(sampleQuery, resultInstanceId = null, splitAttributeColumn) {
	Map<String, String> sttMap = [:]
	Connection con
	PreparedStatement stmt
	ResultSet rs

        try {
            con = dataSource.getConnection()
            stmt = con.prepareStatement(sampleQuery)
	    if (resultInstanceId) {
		stmt.setString(1, resultInstanceId)
	    }
	    stmt.fetchSize = stmtFetchSize

	    logger.debug 'start sample retrieving query'
	    rs = stmt.executeQuery()
            while (rs.next()) {
		String sampleType = rs.getString('SAMPLE_TYPE')
		String timepoint = rs.getString('TIMEPOINT')
		String tissueType = rs.getString('TISSUE_TYPE')
		String assayID = rs.getString('ASSAY_ID')
		String gplId = rs.getString('GPL_ID')

		String sttSampleStr
                if (splitAttributeColumn) {
		    sttSampleStr = (sampleType ?: '') + valueDelimiter + (timepoint ?: '') + valueDelimiter +
			(tissueType ?: '') + (gplId ?: '')
                }
                else {
		    sttSampleStr = (sampleType ?: '') + (timepoint ? '_' + timepoint : '') +
			(tissueType ? '_' + tissueType : '') + (gplId ? '_' + gplId : '')
                }
		sttMap[assayID] = sttSampleStr
            }
        }
        finally {
            rs?.close()
            stmt?.close()
            con?.close()
        }
	logger.debug 'finished sample retrieving query'

	sttMap
    }

    private String writeCLSData(String sqlQuery, File gseaDir, Map resultInstanceIdMap) {
	Writer output
        try {
	    logger.debug 'started writing CLS file'
	    File outFile = new File(gseaDir, 'GSEA.CLS')
            output = outFile.newWriter(true)
	    def rows = new Sql(dataSource).rows(sqlQuery)
	    if (rows) {
		output.write rows.size().toString()
                output.write(valueDelimiter)
                output.write(resultInstanceIdMap?.subset1 && resultInstanceIdMap?.subset2 ? '2' : '1')
                output.write(valueDelimiter)
                output.write('1')
                output.newLine()

                output.write('#')
                output.write(valueDelimiter)
		if (resultInstanceIdMap?.subset1) {
		    output.write('Subset1')
		}
                output.write(valueDelimiter)
		if (resultInstanceIdMap?.subset2) {
		    output.write('Subset2')
		}
                output.newLine()

		for (row in rows) {
		    if (row.RESULT_INSTANCE_ID.toString() == resultInstanceIdMap?.subset1) {
			output.write('0')
		    }
		    else if (row.RESULT_INSTANCE_ID.toString() == resultInstanceIdMap?.subset2) {
			output.write('1')
		    }
                    output.write(valueDelimiter)
                }
                output.newLine()
            }
        }
	catch (e) {
	    logger.error e.message, e
        }
        finally {
            output?.flush()
            output?.close()
	    logger.debug 'completed writing CLS file'
        }
    }

    private String writeGCTData(String sqlQuery, String sampleQuery, File gseaDir, String fileName, String jobName, gplIds) {
	Map<String, String> sttMap = getSamplesMap(sampleQuery, null, false)

	Connection con = dataSource.connection
	PreparedStatement stmt = con.prepareStatement(sqlQuery)
	stmt.fetchSize = stmtFetchSize

	logger.debug 'started file writing'

	File outFile = new File(gseaDir, 'GCT.trans')
	Writer output = outFile.newWriter(true)
        output << 'PATIENT ID\tDescription\tSAMPLE\tASSAY ID\tVALUE\tPROBE ID\tPROBESET ID\n'

	long startTime = System.currentTimeMillis()
	logger.debug 'begin data retrieving query: {}', sqlQuery

	ResultSet rs = stmt.executeQuery()
	logger.debug 'query completed'

	ResultSetMetaData metaData = rs.metaData
	Map<String, Integer> nameIndexMap = [:]
	int count = metaData.columnCount
        for (int i = 1; i <= count; i++) {
	    nameIndexMap[metaData.getColumnName(i)] = i
        }

	int logIntensityRSIdx = nameIndexMap.LOG_INTENSITY
	int rawIntensityRSIdx = nameIndexMap.RAW_INTENSITY
	int ptIDIdx = nameIndexMap.PATIENT_ID
	int sourceSystemCodeIdx = nameIndexMap.SOURCESYSTEM_CD
	int assayIDIdx = nameIndexMap.ASSAY_ID
	int probeIDIdx = nameIndexMap.PROBE_ID
	int probesetIDIdx = nameIndexMap.PROBESET_ID

        //A workaround for using only GPL96 values. I don't like the way we have to hard-code GPL96 here.
	String platformToUse = 'GPL96'
	Map patientProbePlatformValueMap = [:]
	int gplIDIdx = nameIndexMap.GPL_ID

        int flushCount = 0
        long recCount = 0
        try {
            //Iterate over the record set object.
            while (rs.next()) {
		String logIntensityRS = rs.getString(logIntensityRSIdx)
		String rawIntensityRS = rs.getString(rawIntensityRSIdx)
		String patientID = rs.getString(ptIDIdx)
		String sourceSystemCode = rs.getString(sourceSystemCodeIdx)
		String assayID = rs.getString(assayIDIdx)
		String probeID = rs.getString(probeIDIdx)
		String probesetID = rs.getString(probesetIDIdx)

                //To use only GPL96 when same probe present in both platforms
                if (gplIds.size() > 1) { // when there are more than one platforms
		    String gplID = rs.getString(gplIDIdx)
		    if (gplID == platformToUse) { // compared with the hard-coded value GPL96
			patientProbePlatformValueMap[patientID + '_' + probeID + '_' + gplID] = logIntensityRS
                    }
                    else {
			if (patientProbePlatformValueMap.containsKey(patientID + '_' + probeID + '_' + platformToUse)) {
			    continue
			}
                    }
                }

		writeNotEmptyString(output, getActualPatientId(sourceSystemCode))
                output.write(valueDelimiter)

                // Row description
                writeNotEmptyString(output, 'NA')
                output.write(valueDelimiter)

                // sample attribute, time point, tissue type
		String sampleAttribute = determineSampleAttribute(sttMap[assayID])
		output.write(sampleAttribute ? sampleAttribute : writeNotEmptyString(output, ''))
                output.write(valueDelimiter)

                writeNotEmptyString(output, assayID)
                output.write(valueDelimiter)

		// If the data is Global Normalized log_intensity is the value to output.
		// If the log_intensity is NULL, data-load process has to be corrected to always have a value.
		if (logIntensityRS) {
                    output.write(logIntensityRS)
                }

                output.write(valueDelimiter)
                writeNotEmptyString(output, probeID)
                output.write(valueDelimiter)
                writeNotEmptyString(output, probesetID)

                output.newLine()

                flushCount++
                if (flushCount >= flushInterval) {
                    output.flush()
                    recCount += flushCount
                    flushCount = 0
                }
            }
        }
	catch (e) {
	    logger.error e.message, e
        }
        finally {
            output?.flush()
            output?.close()
	    logger.debug 'completed file writing'
            rs?.close()
            stmt?.close()
            con?.close()
        }

	logger.debug '\n \t total seconds:{}\n\n', (System.currentTimeMillis() - startTime) / 1000
	outFile?.absolutePath
    }

    private void pivotGCTData(String inputFileLoc) {
	logger.debug 'Pivot File started'
	if (!inputFileLoc) {
	    return
	}

        File inputFile = new File(inputFileLoc)
	RConnection c = new RConnection(rmodulesHost, rmodulesPort)

        //Set the working directory to be our temporary location.
	String workingDirectoryCommand = "setwd('${inputFile.parent}')".replace('\\', '\\\\')

	logger.debug 'Attempting following R Command : {}', workingDirectoryCommand

        //Run the R command to set the working directory to our temp directory.
	c.eval workingDirectoryCommand

	String compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotGSEAExportGCTData.R')".replace('\\', '\\\\')

	logger.debug 'Attempting following R Command : {}', compilePivotDataCommand

	c.eval compilePivotDataCommand

        //Prepare command to call the PivotGSEAExportGCTData.R script
	String pivotDataCommand = "PivotGSEAExportGCTData.pivot('$inputFile.name')".replace('\\', '\\\\')

	logger.debug 'Attempting following R Command : {}', pivotDataCommand

	c.eval pivotDataCommand

        c.close()
    }

    String toListString(List objList) {
	StringBuilder objToString = new StringBuilder()
	for (obj in objList) {
	    if (obj && obj?.toString()?.trim()) {
		if (obj instanceof String) {
		    objToString << "'" << obj << "'"
		}
		else {
		    objToString << obj
		}
		if (objToString) {
		    objToString << ','
		}
            }
	}

	objToString
    }

    String getActualPatientId(String sourceSystemCode) {
	sourceSystemCode.split(':')[-1]
    }
}
