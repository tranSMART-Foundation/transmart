package com.recomdata.transmart.data.export

import de.DeVariantDataSet
import de.DeVariantSubjectDetail
import groovy.sql.Sql
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.sql.DataSource

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

@CompileStatic
@Slf4j('logger')
class VcfDataService {

    private static final String VCF_V4_1_HEADER_LINE = '#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT'

    static transactional = false

    SnpRefDataService snpRefDataService
    DataSource dataSource

    boolean getDataAsFile(String outputDir, String jobName, List studyList, String resultInstanceId1,
	                  String selectedSNPs, String selectedGenes, String selectedChromosomes, String subjectPrefix) {

	List<String> rsList = []
	List<String> chrList = []
	List<String> geneNameList = []
	if (selectedGenes?.trim()) {
	    // ToDo- parse gene list
	    //def geneList = parseGeneList(selectedGenes)
	    //	def geneList = parseGeneList(['BRCA1'])
	    List<Long> geneSearchIdList = []
	    geneNameList = parseGeneList(selectedGenes, geneSearchIdList)
	    rsList.addAll snpRefDataService.findRsIdByGeneNames(geneNameList)
        }

        // create query to find snps
	if (selectedSNPs?.trim()) {
	    rsList.addAll parseRsList(selectedSNPs)
        }

	if (selectedChromosomes && 'ALL' != selectedChromosomes) {
	    chrList.addAll parseChrList(selectedChromosomes)
        }

	List<String> datasets = findDataset(resultInstanceId1)

        // locate and retrieve variant

	List<DeVariantSubjectDetail> variants = retrieveVariantDetail(rsList, chrList, datasets)

	Map<String, List<Integer>> dsSubjectIdxMap = [:]
	Map<String, List<String>> dsSubHeaderColMap = [:]
        // create subset 1 subject id query

	Map<String, Map<String, Integer>> s1 = [:]
	if (resultInstanceId1 != null) {
            s1 = findSubjectIdx(resultInstanceId1)
        }

	// create vcf sample and header list by dataset id; each dataset has a vcf file
	s1.each { String k, Map<String, Integer> v ->
	    if (dsSubjectIdxMap[k] == null) {
		dsSubjectIdxMap[k] = []
		dsSubHeaderColMap[k] = []
            }
	    v.each { String key, int value ->
		dsSubjectIdxMap[k] << value
                // construct key
		dsSubHeaderColMap[k] << subjectPrefix + '_' + key
            }
        }

        // construct VCFdata file
	constructVCFfile variants, datasets, dsSubjectIdxMap, dsSubHeaderColMap, outputDir, jobName, subjectPrefix
	constructVCFParamFile outputDir, geneNameList, chrList, jobName

	true
    }

    private void constructVCFParamFile(String outputDir, List<String> geneNameList, List<String> chrList, String jobName) {
        // create a filter file so that we can get the right params
	Writer writer
        try {
	    writer = new File(outputDir, jobName + '_vcf.params').newWriter()
            // if there is a gene use the first gene
	    if (geneNameList) {
		writer.writeLine 'Gene=' + geneNameList[0]
            }
	    if (chrList) {
		writer.writeLine 'Chr=' + 'chr' + chrList[0]
            }
        }
	catch (e) {
	    logger.error e.message, e
        }
        finally {
	    writer?.flush()
	    writer?.close()
        }
    }

    /**
     * construct multiple VCF files - each dataset has a file
     */
    private void constructVCFfile(List<DeVariantSubjectDetail> variants, List<String> datasetIds, Map<String, List<Integer>> dsSubjectIdxMap, Map<String, List<String>> dsSubHeaderColMap, String outputDir, String jobName, String subjectPrefix) {

        // create writers, each dataset get a writer
	Map<String, BufferedWriter> dsWriterMap = [:]
        // each dataset has a new files
	for (String id in datasetIds) {
	    File file = new File(outputDir, jobName + '_' + id + '_' + subjectPrefix + '.vcf')
	    logger.debug 'writing data file for dataset:{} at :{}', id, file.absolutePath

	    BufferedWriter writer = file.newWriter()
	    dsWriterMap[id] = writer
            // write metadata and header
	    writer.write DeVariantDataSet.get(id).metadata
            // header

	    StringBuilder sb = new StringBuilder(VCF_V4_1_HEADER_LINE)
	    for (String s in dsSubHeaderColMap[id]) {
		sb << '\t' << s
            }
	    writer.writeLine sb.toString()
	    writer.flush()
        }

	// loop through variant data and use the dataset writer to output data

	for (DeVariantSubjectDetail v in variants) {
            StringBuilder variant = new StringBuilder()

	    variant << v.chromosome << '\t'
	    variant << v.position << '\t'
	    variant << v.rsID << '\t'
	    variant << v.ref << '\t'
	    variant << v.alt << '\t'
	    variant << v.quality << '\t'
	    variant << v.filter << '\t'
	    variant << v.info << '\t'
	    variant << v.format

            // using a split to get the value in array by tab
	    String[] valueArray = v.variant.split('\\t')
	    int total = valueArray.length
	    List<Integer> indexList = dsSubjectIdxMap[v.dataset]
            // 1 based index list

	    for (int i in indexList) {
		int idx = i - 1
                if (idx <= total) {
		    variant << '\t' << valueArray[idx]
                }
                else {
		    throw new Exception('variant size :' + total + ' do not match variant index:' + i)
                }
            }
	    dsWriterMap[v.dataset].writeLine variant.toString()
        }

	for (Writer w in dsWriterMap.values()) {
            try {
		w.flush()
		w.close()
            }
	    catch (e) {
		logger.error e.message, e
	    }
        }
    }

    /**
     * retrieve variant by rsid, chr and dataset. dataset is required
     */
    private List<DeVariantSubjectDetail> retrieveVariantDetail(List<String> rsList, List<String> chrList, List<String> datasetList) {
	String hql = 'FROM DeVariantSubjectDetail dvd WHERE dvd.dataset IN (:ds) '
	Map params = [ds: datasetList]

	if (rsList) {
	    hql += ' AND dvd.rsID IN (:rsids) ' // this could be an issue for more than 1000 rs ids
	    params.rsids = rsList
        }

	if (chrList) {
	    hql += ' AND dvd.chromosome IN (:chrNums) '
	    params.chrNums = chrList
        }
	hql += ' ORDER BY dvd.chromosome, dvd.position'

	DeVariantSubjectDetail.findAll hql, params
    }

    private List<String> parseRsList(String rsIds) {
	rsIds.split(',')*.trim()
    }

    private List<String> parseChrList(String chrs) {
	[chrs]
    }

    @CompileDynamic
    private List<String> findDataset(String resultInstanceId1) {
	checkQueryResultAccess resultInstanceId1

	String sql = '''
		SELECT DISTINCT a.dataset_id
		from DEAPP.DE_VARIANT_SUBJECT_IDX a
		INNER JOIN DEAPP.de_subject_sample_mapping b on a.SUBJECT_ID = b.SUBJECT_ID 
		INNER JOIN I2B2DEMODATA.qt_patient_set_collection sc ON sc.result_instance_id in (?) AND b.patient_id = sc.patient_num
'''

	List params = []
	if (resultInstanceId1) {
	    params << resultInstanceId1
        }

	List<String> datasetList = []

	new Sql(dataSource).eachRow(sql, params, { row ->
            if (row.dataset_id != null) {
		datasetList << row.dataset_id
            }
        })

	datasetList
    }

    @CompileDynamic
    private Map<String, Map<String, Integer>> findSubjectIdx(String resultInstanceId) {

	String sql = '''
			SELECT distinct a.DATASET_ID, a.SUBJECT_ID, a.POSITION
			from DEAPP.DE_VARIANT_SUBJECT_IDX a
			INNER JOIN DEAPP.de_subject_sample_mapping b on a.SUBJECT_ID = b.SUBJECT_ID
			INNER JOIN I2B2DEMODATA.qt_patient_set_collection sc ON sc.result_instance_id in (?) AND b.patient_id = sc.patient_num
			ORDER BY a.POSITION
		'''

	Map<String, Map<String, Integer>> datasetIdxMap = [:]

	new Sql(dataSource).eachRow(sql, [resultInstanceId], { row ->
	    Map<String, Integer> sIdx = datasetIdxMap[row.DATASET_ID]
            if (sIdx == null) {
		sIdx = [:]
		datasetIdxMap[row.DATASET_ID] = sIdx
            }
	    sIdx[row.SUBJECT_ID] = row.POSITION
        })

	datasetIdxMap
    }

    /**
     * Parse the ','-separated gene string like 'Gene>MET', and return a list of gene search ID and a list of matching gene names.
     */
    private List<String> parseGeneList(String genes, List<Long> geneSearchIdList) {
	if (!genes) {
            return null
	}

	List<String> geneNameList = []
	Map<String, Long> geneIdMap = [:]

	for (String geneStr in genes.split(',')) {
            geneStr = geneStr.trim()
	    geneSearchIdList << geneIdMap[geneStr.trim()]
	    if (geneStr.startsWith('Gene>')) {
                geneStr = geneStr.substring('Gene>'.length())
            }
	    geneNameList << geneStr.trim()
        }

	geneNameList
    }
}
