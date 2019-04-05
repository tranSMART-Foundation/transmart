import com.recomdata.export.SnpViewerFiles
import groovy.sql.Sql
import i2b2.GeneWithSnp
import i2b2.SnpDataByProbe
import i2b2.SnpDataset
import i2b2.SnpDatasetListByProbe
import i2b2.SnpInfo
import i2b2.SnpProbeSortedDef
import i2b2.StringLineReader
import org.springframework.util.Assert

import javax.sql.DataSource
import java.sql.Clob

class SnpService {

    static transactional = false

    DataSource dataSource

    private static final char QUOTE = "'"
    private static final Map<String, String[]> chromEndProbeLineMap =
	['1' : ['SNP_A-8575125\t1\t564621', 'SNP_A-8391333\t1\t249198692'],
	 '2' : ['SNP_A-8615982\t2\t15703', 'SNP_A-8304446\t2\t243048760'],
	 '3' : ['SNP_A-2100278\t3\t66866', 'SNP_A-8336753\t3\t197856433'],
	 '4' : ['SNP_A-8661350\t4\t45410', 'SNP_A-8713585\t4\t190921709'],
	 '5' : ['SNP_A-8392711\t5\t36344', 'SNP_A-2186029\t5\t180692833'],
	 '6' : ['SNP_A-8533260\t6\t203249', 'SNP_A-8608599\t6\t170918031'],
	 '7' : ['SNP_A-8539824\t7\t43259', 'SNP_A-8436508\t7\t159119220'],
	 '8' : ['SNP_A-8325516\t8\t161222', 'SNP_A-2094900\t8\t146293414'],
	 '9' : ['SNP_A-8574568\t9\t37747', 'SNP_A-8302801\t9\t141071475'],
	 '10': ['SNP_A-8435658\t10\t104427', 'SNP_A-4271863\t10\t135434551'],
	 '11': ['SNP_A-8300213\t11\t198510', 'SNP_A-2246844\t11\t134944770'],
	 '12': ['SNP_A-8434276\t12\t161382', 'SNP_A-4219877\t12\t133777645'],
	 '13': ['SNP_A-8687595\t13\t19045720', 'SNP_A-8587371\t13\t115106996'],
	 '14': ['SNP_A-8430270\t14\t20211644', 'SNP_A-2127677\t14\t107285437'],
	 '15': ['SNP_A-8429754\t15\t20071673', 'SNP_A-8685263\t15\t102400037'],
	 '16': ['SNP_A-1807459\t16\t86671', 'SNP_A-1841720\t16\t90163275'],
	 '17': ['SNP_A-8398136\t17\t6689', 'SNP_A-8656409\t17\t81049726'],
	 '18': ['SNP_A-8496414\t18\t11543', 'SNP_A-8448011\t18\t78015057'],
	 '19': ['SNP_A-8509279\t19\t260912', 'SNP_A-8451148\t19\t59095126'],
	 '20': ['SNP_A-8559313\t20\t61795', 'SNP_A-8480501\t20\t62912463'],
	 '21': ['SNP_A-4217519\t21\t9764385', 'SNP_A-8349060\t21\t48084820'],
	 '22': ['SNP_A-8656401\t22\t16055171', 'SNP_A-8313387\t22\t51219006'],
	 'X' : ['SNP_A-8572888\tX\t119805', 'SNP_A-8363487\tX\t154925045'],
	 'Y' : ['SNP_A-8655052\tY\t2722506', 'SNP_A-8433021\tY\t28758193']]

    private static final String[] allChroms = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12',
	                                       '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y']

    void getSNPViewerDataByPatient(List<Long>[] patientNumListArray, String chroms, SnpViewerFiles snpFiles) {
	Assert.notNull snpFiles, 'The SNPViewerFiles object is not instantiated'

	StringBuilder sb = new StringBuilder()

        for (int i = 0; i < patientNumListArray.length; i++) {
	    if (sb) {
		sb << ','
	    }

            //This is a list of patients, add it to our string.
	    sb << patientNumListArray[i].join(',')
        }

	String subjectListStr = sb

	Map<Long, SnpDataset[]> snpDatasetBySubjectMap = [:]
	getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr
	if (!snpDatasetBySubjectMap) {
            throw new Exception('Error: The selected cohorts do not have SNP data.')
        }

	StringBuilder sampleInfoBuf = new StringBuilder()
	List<SnpDataset> datasetList = []
	List<String> datasetNameForSNPViewerList = []
        getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf)

        Map<Long, Map<String, String>> snpDataByDatasetByChrom = getSNPDataByDatasetByChrom(subjectListStr, chroms)

        /** There is a bug in GenePattern SNPViewer. If there is no probe position information for previous chrom,
         * The display of chroms becomes erratic.
         * The work-around is to enter dummy data for starting and ending probes of the absent chrom, so
         SNPViewer can display the chrom number correctly. Need to build a list of chroms to the last used chrom*/

        List<String> neededChroms = getSortedChromList(chroms)
	Map<String, String[]> allChromEndProbeLines = chromEndProbeLineMap
	String lastChrom = neededChroms[-1]

	String platform = datasetList[0].platformName
        Map<String, SnpProbeSortedDef> probeDefMap = getSNPProbeDefMap(platform, chroms)

        BufferedWriter dataWriter = new BufferedWriter(new FileWriter(snpFiles.dataFile))

        // Write the header column
        dataWriter.write('SNP\tChromosome\tPhysicalPosition')
	for (String datasetName in datasetNameForSNPViewerList) {
            dataWriter.write('\t' + datasetName + '\t' + datasetName + ' Call')
        }
        dataWriter.write('\n')

	for (String chrom in neededChroms) {
	    SnpProbeSortedDef probeDef = probeDefMap[chrom]
	    if (probeDef) { // This chrom is selected by user
                // Create the list of BufferedReader for SNP data for each dataset for this chrom
		List<StringLineReader> snpDataReaderList = []
		for (SnpDataset dataset in datasetList) {
		    Map<String, String> snpDataByChrom = snpDataByDatasetByChrom[dataset.id]
		    snpDataReaderList << new StringLineReader(snpDataByChrom[chrom])
                }

		int numProbe = probeDef.numProbe
		StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef)
		for (int idx = 0; idx < numProbe; idx++) {
                    String probeLine = probeReader.readLine()
		    if (!probeLine) {
                        throw new Exception('The number ' + idx + ' line in probe definition file for chromosome ' + chrom + ' is empty')
					}
                    dataWriter.write(probeLine)

		    for (StringLineReader dataReader in snpDataReaderList) {
                        dataWriter.write('\t' + dataReader.readLine())
                    }
                    dataWriter.write('\n')
                }
	    }
	    else { // This chrom need dummy data for the starting and ending probes
		String[] endProbeLines = allChromEndProbeLines[chrom]
                dataWriter.write(endProbeLines[0])
		for (SnpDataset dataset in datasetList) {
                    dataWriter.write('\t2.0\tNC')
                }
                dataWriter.write('\n')

                dataWriter.write(endProbeLines[1])
		for (SnpDataset dataset in datasetList) {
                    dataWriter.write('\t2.0\tNC')
                }
                dataWriter.write('\n')
            }
	    if (chrom == lastChrom) {
		// Stop at the last needed chrom
		break
	    }
        }
	snpFiles.sampleFile << sampleInfoBuf
	dataWriter.close()
    }

    /**
     * For now the patients have to be in the same trial, for the sake of simplicity.
     */
    void getSNPViewerDataByProbe(List<Long>[] patientNumListArray, List<Long> geneSearchIdList, List<String> geneNameList,
	                         List<String> snpNameList, SnpViewerFiles snpFiles, StringBuilder geneSnpPageBuf) {
	Assert.notNull snpFiles, 'The SNPViewerFiles object is not instantiated'
	Assert.notNull geneSnpPageBuf, 'The geneSnpPageBuf object is not instantiated'

        //This object is seemingly used to initialize objects which get assigned to local variables later.
        SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe()

	StringBuilder sb = new StringBuilder()

        for (int i = 0; i < patientNumListArray.length; i++) {
	    if (sb) {
		sb << ','
	    }

            //This is a list of patients, add it to our string.
	    sb << patientNumListArray[i].join(',')
        }
	String subjectListStr = sb

        // Get the gene-snp map, and the snp set related to all the user-input genes.
        // Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = [:]
	Map<Long, GeneWithSnp> geneEntrezIdMap = [:]
	Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = [:]
        getGeneWithSnpMapForGenes(geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList)

        // Get the gene-snp map for the user-selected SNPs.
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = [:]
        getGeneWithSnpMapForSnps(geneSnpMapForSnp, snpNameList)

	Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = []
	geneSnpMapList << geneSnpMapForGene
	geneSnpMapList << geneSnpMapForSnp
        Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = mergeGeneWithSnpMap(geneSnpMapList)

	if (!allGeneSnpMap) {
            throw new Exception('There is no SNP data for selected genes and SNP IDs')
	}

        // Generate the web page to display the Gene and SNP selected by User
        getSnpGeneAnnotationPage(geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneNameList, snpNameList)

        Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap
        //Fill the SnpDatasets. We use the map to keep track of the patients.
        getSnpDatasetBySubjectMap(snpDatasetBySubjectMap, subjectListStr)

	StringBuilder sampleInfoBuf = new StringBuilder()
        List<SnpDataset> datasetList = allDataByProbe.datasetList
        List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList
        getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf)

        // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
        Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap

        Set<Long> allSnpIdSet = getSnpSet(allGeneSnpMap)
        getSNPDataByProbeByChrom(datasetList, snpDataByChromMap, allSnpIdSet)

        // Write the sample info text file for SNPViewer
	File sampleFile = snpFiles.sampleFile
        sampleFile << sampleInfoBuf.toString()

        // Write the xcn file
	File dataFile = snpFiles.dataFile
        BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile))
        // Write the header column
        dataWriter.write('SNP\tChromosome\tPhysicalPosition')
	for (String datasetName in datasetNameForSNPViewerList) {
            dataWriter.write('\t' + datasetName + '\t' + datasetName + ' Call')
        }
        dataWriter.write('\n')
        // Write the data section, by chrom. Stop at the last used chrom in snpDataByChromMap
        List<String> sortedChromList = getSortedChromList(snpDataByChromMap.keySet())
	String lastChrom = sortedChromList[-1]
	for (String chrom in allChroms) {
	    List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap[chrom]
	    if (snpDataByProbeList) {
                // SNPViewer has problem rendering single SNP without boundary blank SNPs.
		String[] chromEndProbeLine = chromEndProbeLineMap[chrom]
                dataWriter.write(chromEndProbeLine[0])
                for (int i = 0; i < datasetList.size(); i++) {
                    dataWriter.write('\t2.0\tNC')
                }
                dataWriter.write('\n')

		for (SnpDataByProbe snpDataByProbe in snpDataByProbeList) {
		    dataWriter.write(snpDataByProbe.snpName + '\t' + chrom + '\t' + snpDataByProbe.chromPos)
                    String[][] dataArray = snpDataByProbe.dataArray
                    for (int i = 0; i < datasetList.size(); i++) {
                        dataWriter.write('\t' + dataArray[i][0].trim() + '\t' + dataArray[i][1])
                    }
                    dataWriter.write('\n')
                }

                dataWriter.write(chromEndProbeLine[1])
                for (int i = 0; i < datasetList.size(); i++) {
                    dataWriter.write('\t2.0\tNC')
                }
                dataWriter.write('\n')
	    }
	    else { // There is no snp data needed for this chrom
		String[] chromEndProbeLine = chromEndProbeLineMap[chrom]
                for (int idxEndProbe = 0; idxEndProbe < 2; idxEndProbe++) {
                    dataWriter.write(chromEndProbeLine[idxEndProbe])
                    for (int i = 0; i < datasetList.size(); i++) {
                        dataWriter.write('\t2.0\tNC')
                    }
                    dataWriter.write('\n')
                }
            }

	    if (chrom == lastChrom) {
		break
	    }
        }
        dataWriter.close()
    }

    /** ******************************************************************/
    //These are all helper methods.
    /** ******************************************************************/

    void getSnpDatasetBySubjectMap(Map<Long, SnpDataset[]> snpDatasetBySubjectMap, String subjectListStr) {
	if (snpDatasetBySubjectMap == null || !subjectListStr) {
	    return
	}

        // The display concept name like 'Normal Blood Lymphocyte' for dataset with conceptId of '1222211'
	Map<String, String> conceptIdToDisplayNameMap = [:]

        // Get the dataset list from subject lists, and organize them in pairs for each patient.
	String commonPlatformName = null // To make sure there is noly one platform among all the datasets
	String commonTrialName = null // For now only one trial is allowed.

	String sql = '''
			select * from DEAPP.de_subject_snp_dataset
			where patient_num in (''' + subjectListStr + ')'
	new Sql(dataSource).eachRow(sql) { row ->
	    SnpDataset snpDataset = new SnpDataset(
		id: row.subject_snp_dataset_id,
		datasetName: row.dataset_name,
		conceptId: row.concept_cd,
		conceptName: getConceptDisplayName(snpDataset.conceptId, conceptIdToDisplayNameMap),
		platformName: row.platform_name,
		patientNum: row.patient_num,
		timePoint: row.timepoint,
		subjectId: row.subject_id,
		sampleType: row.sample_type,
		pairedDatasetId: row.paired_dataset_id,
		patientGender: row.patient_gender)

            if (commonPlatformName == null) {
                commonPlatformName = snpDataset.platformName
            }
	    else if (commonPlatformName != snpDataset.platformName) {
		throw new Exception('The platform for SnpDataset ' + snpDataset.datasetName + ', ' +
				    snpDataset.platformName + ', is different from previous platform ' + commonPlatformName)
            }

            snpDataset.trialName = row.trial_name
	    if (commonTrialName == null) {
		commonTrialName = snpDataset.trialName
	    }
	    else if (commonTrialName != snpDataset.trialName) {
		throw new Exception('The trial for SnpDataset ' + snpDataset.datasetName + ', ' +
				    snpDataset.trialName + ', is different from previous trial ' + commonTrialName)
	    }

	    SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap[snpDataset.patientNum]
            if (snpDatasetPair == null) {
                snpDatasetPair = new SnpDataset[2]
		snpDatasetBySubjectMap[snpDataset.patientNum] = snpDatasetPair
            }
	    if (snpDataset.sampleType == SnpDataset.SAMPLE_TYPE_NORMAL) {
                snpDatasetPair[0] = snpDataset
            }
            else {
                snpDatasetPair[1] = snpDataset
            }
        }
    }

    void getSnpSampleInfo(List<SnpDataset> datasetList, List<String> datasetNameForSNPViewerList,
	                  List<Long>[] patientNumListArray, Map<Long, SnpDataset[]> snpDatasetBySubjectMap,
	                  StringBuilder sampleInfoBuf) {

	Assert.notNull datasetList, 'The datasetList is null'
	Assert.notNull patientNumListArray, 'The patient number list for two subsets cannot be null'
	Assert.notNull sampleInfoBuf, 'The StringBuilder for sample info text needs to instantiated'

        // Organize the datasetList and SNPViewer dataset name List, also generate the SNPViewer sample info text in this pass
	sampleInfoBuf << 'Array\tSample\tType\tPloidy(numeric)\tGender\tPaired'
        for (int idxSubset = 0; idxSubset < 1; idxSubset++) {
            if (patientNumListArray[idxSubset] != null) {
		for (Long patientNum in patientNumListArray[idxSubset]) {
		    SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap[patientNum.longValue()]
                    if (snpDatasetPair != null) {
                        String datasetNameForSNPViewer_1 = null
			String datasetNameForSNPViewer_2

                        if (snpDatasetPair[0] != null) {    // Has the control dataset
                            SnpDataset snpDataset_1 = snpDatasetPair[0]
                            datasetNameForSNPViewer_1 = 'S' + (idxSubset + 1) + '_' + snpDataset_1.datasetName
			    datasetList << snpDataset_1
			    datasetNameForSNPViewerList << datasetNameForSNPViewer_1
			    sampleInfoBuf << '\n' << datasetNameForSNPViewer_1 << '\t' << datasetNameForSNPViewer_1
			    sampleInfoBuf << '\t' << snpDataset_1.conceptName << '\t2\t' << snpDataset_1.patientGender << '\t'

			    if (snpDatasetPair[1] != null) {
				// Paired
				sampleInfoBuf << 'Yes'
			    }
			    else {
				sampleInfoBuf << 'No'
			    }
                        }

                        if (snpDatasetPair[1] != null) {    // Has the control dataset
                            SnpDataset snpDataset_2 = snpDatasetPair[1]
                            datasetNameForSNPViewer_2 = 'S' + (idxSubset + 1) + '_' + snpDataset_2.datasetName
			    datasetList << snpDataset_2
			    datasetNameForSNPViewerList << datasetNameForSNPViewer_2
			    sampleInfoBuf << '\n' << datasetNameForSNPViewer_2 << '\t' << datasetNameForSNPViewer_2
			    sampleInfoBuf << '\t' << snpDataset_2.conceptName << '\t2\t' << snpDataset_2.patientGender << '\t'

			    if (snpDatasetPair[0] != null) {
				// Paired
				sampleInfoBuf << datasetNameForSNPViewer_1
			    }
			    else {
				sampleInfoBuf << 'No'
			    }
                        }
                    }
                }
            }
        }
    }

    List<String> getSortedChromList(String chromListStr) {
        String[] chromArray = chromListStr.split(',')
	Set<String> chromSet = []
	for (String chrom in chromArray) {
	    chromSet << chrom.trim()
        }
	getSortedChromList chromSet
    }

    Map<Long, Map<String, String>> getSNPDataByDatasetByChrom(String subjectIds, String chroms) {
	if (!subjectIds) {
	    return null
	}
	Map<Long, Map<String, String>> snpDataByDatasetByChrom = [:]
        // Map<[datasetId], Map<chrom, data>>

        // Get the list of dataset first, SNP data will be fetched later
	String sql = '''
			SELECT a.*, b.chrom as chrom, b.data_by_patient_chr as data
			FROM DEAPP.de_subject_snp_dataset a, DEAPP.de_snp_data_by_patient b
			WHERE a.subject_snp_dataset_id = b.snp_dataset_id
			  and a.patient_num in (''' + subjectIds + ') ' +
			' and b.chrom in (' + getSqlStrFromChroms(chroms) + ')'

	new Sql(dataSource).eachRow(sql) { row ->
            Long datasetId = row.subject_snp_dataset_id
            String chrom = row.chrom
	    String data = ((Clob) row.data).asciiStream.text

	    Map<String, String> dataByChrom = snpDataByDatasetByChrom[datasetId]
	    if (dataByChrom == null) {
		dataByChrom = [:]
		snpDataByDatasetByChrom[datasetId] = dataByChrom
            }
	    dataByChrom[chrom] = data
        }

	snpDataByDatasetByChrom
    }

    String[] getAllChromArray() {
	allChroms
    }

    Map<String, String[]> getChromEndProbeLineMap() {
	chromEndProbeLineMap
    }

    /**
     * Original example data files for SNPViewer and IGV use probe name such as 'SNP_A-1780419'.
     * It is better to use the target SNP id like 'rs6576700' in the data file, so the tooltip in IGV will show the SNP rs id.
     */
    Map<String, SnpProbeSortedDef> getSNPProbeDefMap(String platformName, String chroms) {
	if (!platformName?.trim()) {
	    return null
	}

	Map<String, SnpProbeSortedDef> snpProbeDefMap = [:]
	String sql = '''
			SELECT snp_probe_sorted_def_id, platform_name, num_probe, chrom, snp_id_def
			FROM DEAPP.de_snp_probe_sorted_def
			WHERE platform_name = ?
			and chrom in (''' + getSqlStrFromChroms(chroms) + ') order by chrom'
	new Sql(dataSource).eachRow(sql, [platformName]) { row ->
	    snpProbeDefMap[probeDef.chrom] = new SnpProbeSortedDef(
		id: row.snp_probe_sorted_def_id,
		platformName: row.platform_name,
		numProbe: row.num_probe,
		chrom: row.chrom,
		snpIdDef: ((Clob) row.snp_id_def).asciiStream.text)
        }
	snpProbeDefMap
    }

    void getGeneWithSnpMapForGenes(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom,
	                           Map<Long, GeneWithSnp> geneEntrezIdMap, Map<String, GeneWithSnp> geneNameToGeneWithSnpMap,
	                           List<Long> geneSearchIdList) {
	if (!geneSearchIdList) {
	    return
	}

	Assert.notNull geneSnpMapByChrom, 'geneSnpMapByChrom is not instantiated'
	Assert.notNull geneEntrezIdMap, 'geneEntrezIdMap is not instantiated'

        String geneSearchIdListStr = getStringFromCollection(geneSearchIdList)

        // Get the gene entrez id
	String sqlStr = '''
			select unique_id, keyword
			from SEARCHAPP.search_keyword
			where data_category = 'GENE'
			  and search_keyword_id in (''' + geneSearchIdListStr + ') '
	StringBuilder geneEntrezIds = new StringBuilder()
	Sql sql = new Sql(dataSource)
        sql.eachRow(sqlStr) { row ->
	    String uniqueId = row.unique_id
	    String geneEntrezIdStr = uniqueId.substring(uniqueId.indexOf(':') + 1).trim()
	    if (geneEntrezIds) {
		geneEntrezIds << ','
	    }
	    geneEntrezIds << geneEntrezIdStr
	    GeneWithSnp gene = new GeneWithSnp(entrezId: Long.valueOf(geneEntrezIdStr), name: row.keyword)
	    geneEntrezIdMap[gene.entrezId] = gene
	    geneNameToGeneWithSnpMap[gene.name] = gene
        }

        // Get the snp association and chrom mapping
	sqlStr = '''
			select a.entrez_gene_id, b.*
			from DEAPP.de_snp_gene_map a, DEAPP.de_snp_info b
			where a.snp_id = b.snp_info_id
			and a.entrez_gene_id in (''' + geneEntrezIds + ') '
        sql.eachRow(sqlStr) { row ->
            Long snpId = row.snp_info_id
            String snpName = row.name
            String chrom = row.chrom
            Long chromPos = row.chrom_pos
            Long entrezId = row.entrez_gene_id

	    GeneWithSnp gene = geneEntrezIdMap[entrezId]
            if (gene.chrom == null) {
                gene.chrom = chrom
            }
            else {
		if (gene.chrom != chrom) {
		    throw new Exception('Inconsistant SNP-Gene mapping in database: The Gene ' + gene.name +
					', with Entrez ID of ' + gene.entrezId + ', is mapped to chromosome ' +
					gene.chrom + ' and ' + chrom)
                }
            }

	    gene.snpMap[chromPos] = new SnpInfo(id: snpId, name: snpName, chrom: chrom, chromPos: chromPos)
        }

        // Organize the GeneWithSnp by chrom
	for (GeneWithSnp gene in geneEntrezIdMap.values()) {
	    if (gene.chrom == null || !gene.snpMap) {
		continue
	    }
	    SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom[gene.chrom]
            if (genes == null) {
                genes = new TreeMap<Long, Map<Long, GeneWithSnp>>()
		geneSnpMapByChrom[gene.chrom] = genes
            }
            Long chromPosGene = gene.snpMap.firstKey()
	    Map<Long, GeneWithSnp> geneMap = genes[chromPosGene]
            if (geneMap == null) {
		geneMap = [:]
		genes[chromPosGene] = geneMap
            }
	    geneMap[gene.entrezId] = gene
        }
    }

    void getGeneWithSnpMapForSnps(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom, List<String> snpNameList) {
	if (!snpNameList) {
	    return
	}
	Assert.notNull geneSnpMapByChrom, 'geneSnpMapByChrom is not instantiated'

	Map<Long, GeneWithSnp> geneEntrezIdMap = [:]
        // Get the snp association and chrom mapping
	Sql sql = new Sql(dataSource)
	String sqlStr = '''
			select a.entrez_gene_id, b.*
			from DEAPP.de_snp_gene_map a, DEAPP.de_snp_info b
			where a.snp_id = b.snp_info_id
			  and b.name in (''' + getStringFromCollection(snpNameList) + ')'
        sql.eachRow(sqlStr) { row ->
            Long snpId = row.snp_info_id
            String snpName = row.name
            String chrom = row.chrom
            Long chromPos = row.chrom_pos
            Long entrezId = row.entrez_gene_id

	    GeneWithSnp gene = geneEntrezIdMap[entrezId]
            if (gene == null) {
		gene = new GeneWithSnp(entrezId: entrezId)
		geneEntrezIdMap[gene.entrezId] = gene
            }
            if (gene.chrom == null) {
                gene.chrom = chrom
            }
            else {
		if (gene.chrom != chrom) {
		    throw new Exception('The Gene ' + gene.name + ', with Entrez ID of ' + gene.entrezId +
					', is on chromosome ' + gene.chrom + ' and ' + chrom)
                }
            }

	    gene.snpMap[chromPos] = new SnpInfo(id: snpId, name: snpName, chrom: chrom, chromPos: chromPos)
        }

        // Construct the unique_id list from Entrez IDs
	StringBuilder geneSearchStr = new StringBuilder()
	for (String key in geneEntrezIdMap.keySet()) {
	    if (geneSearchStr) {
		geneSearchStr << ','
            }
	    geneSearchStr << "'GENE:" << key << QUOTE
        }

        // Get the gene name from search_keyword table
	sqlStr = '''
			select unique_id, keyword
			from SEARCHAPP.search_keyword
			where data_category = 'GENE'
			  and unique_id in (''' + geneSearchStr + ')'
        sql.eachRow(sqlStr) { row ->
	    String uniqueId = row.unique_id
	    String geneEntrezIdStr = uniqueId.substring(uniqueId.indexOf(':') + 1).trim()
	    GeneWithSnp gene = geneEntrezIdMap[Long.valueOf(geneEntrezIdStr)]
            gene.name = row.keyword
        }

        // Organize the GeneWithSnp by chrom
	for (GeneWithSnp gene in geneEntrezIdMap.values()) {
	    if (gene.chrom == null || !gene.snpMap) {
		continue
	    }
	    SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom[gene.chrom]
            if (genes == null) {
                genes = new TreeMap<Long, Map<Long, GeneWithSnp>>()
		geneSnpMapByChrom[gene.chrom] = genes
            }
            Long chromPosGene = gene.snpMap.firstKey()
	    Map<Long, GeneWithSnp> geneMap = genes[chromPosGene]
            if (geneMap == null) {
		geneMap = [:]
		genes[chromPosGene] = geneMap
            }
	    geneMap[gene.entrezId] = gene
        }
    }

    /* This function merge the sorted snp in sorted gene, organized by chromosome
     * In the rare case that snp are merged into a same gene, the chrom position of the gene may change. Organize gene first. */

    Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergeGeneWithSnpMap(Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> mapList) {

	Map<Long, GeneWithSnp> geneMap = new TreeMap<>()
	for (Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> map in mapList) {
	    if (!map) {
		continue
	    }
	    for (SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMap in map.values()) {
		for (Map<Long, GeneWithSnp> entrezIdGeneMap in geneWithSnpMap.values()) {
		    for (Map.Entry<Long, GeneWithSnp> entrezIdGeneMapEntry in entrezIdGeneMap) {
			Long entrezId = entrezIdGeneMapEntry.key
			GeneWithSnp geneWithSnp = entrezIdGeneMapEntry.value
			GeneWithSnp geneWithSnpInMap = geneMap[entrezId]
                        if (geneWithSnpInMap == null) {
                            // First time to have this entrezId, use the existing gene structure
                            geneWithSnpInMap = geneWithSnp
			    geneMap[entrezId] = geneWithSnpInMap
			}
			else { // The gene structure and associated snp list already exist
			    geneWithSnpInMap.snpMap.putAll geneWithSnp.snpMap
                        }
                    }
                }
            }
        }

	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergedMap = [:]
	for (GeneWithSnp gene in geneMap.values()) {
	    SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapByChrom = mergedMap[gene.chrom]
            if (geneWithSnpMapByChrom == null) {
                geneWithSnpMapByChrom = new TreeMap<Long, Map<Long, GeneWithSnp>>()
		mergedMap[gene.chrom] = geneWithSnpMapByChrom
            }
            Long chromPosGene = gene.snpMap.firstKey()
	    Map<Long, GeneWithSnp> entrezIdgeneMap = geneWithSnpMapByChrom[chromPosGene]
            if (entrezIdgeneMap == null) {
		entrezIdgeneMap = [:]
		geneWithSnpMapByChrom[chromPosGene] = entrezIdgeneMap
            }
	    entrezIdgeneMap[gene.entrezId] = gene
        }

	mergedMap
    }

    void getSnpGeneAnnotationPage(StringBuilder geneSnpPageBuf, Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap,
                                  Map<Long, GeneWithSnp> geneEntrezIdMap, Map<String, GeneWithSnp> geneNameToGeneWithSnpMap,
                                  List<String> geneNameList, List<String> snpNameList) {
	geneSnpPageBuf << '<html><header></hearder><body><p>Selected Genes and SNPs</p>'
	geneSnpPageBuf << "<table width='100%' border='1' cellpadding='4' cellspacing='3'>"
	geneSnpPageBuf << "<tr align='center'><th>Gene</th><th>SNP</th><th>Chrom</th><th>Position</th></tr>"
	Set<String> snpNameSet = []
	if (snpNameList) {
	    snpNameSet.addAll snpNameList
	}

	Set<String> geneNotUsedNameSet = []
	if (geneNameList) {
	    for (String geneName in geneNameList) {
		if (!geneNameToGeneWithSnpMap.containsKey(geneName)) {
		    geneNotUsedNameSet << geneName
		}
            }
        }

	Set<String> snpUsedNameSet = []

	for (String chrom in allChroms) {
	    SortedMap<Long, Map<Long, GeneWithSnp>> geneMapChrom = allGeneSnpMap[chrom]
	    for (Map<Long, GeneWithSnp> geneMap in geneMapChrom.values()) {
		for (GeneWithSnp gene in geneMap.values()) {
                    SortedMap<Long, SnpInfo> snpMap = gene.snpMap
                    String geneDisplay = gene.name
		    if (geneEntrezIdMap && geneEntrezIdMap[gene.entrezId] != null) {
                        // This gene is selected by user
			geneDisplay = "<font color='red'>" + gene.name + '</font>'
                    }
		    geneSnpPageBuf << "<tr align='center' valign='top'><td rowspan='" << snpMap.size() << "'>" << geneDisplay << '</td>'
                    boolean firstEntry = true
		    for (SnpInfo snp in snpMap.values()) {
                        String snpDisplay = snp.name
			snpUsedNameSet << snpDisplay
			if (snpNameSet?.contains(snp.name)) { // This SNP is entered by user
			    snpDisplay = "<font color='red'>" + snp.name + '</font>'
                        }
			if (firstEntry) {
			    geneSnpPageBuf << '<td>' << snpDisplay << '</td><td>' << snp.chrom << '</td><td>' << snp.chromPos << '</td></tr>'
                        }
                        else {
			    geneSnpPageBuf << "<tr align='center'><td>" << snpDisplay << '</td><td>' << snp.chrom << '</td><td>' << snp.chromPos << '</td></tr>'
                        }
                        firstEntry = false
                    }
                }
            }
        }
	geneSnpPageBuf << '</table>'

	if (geneNotUsedNameSet) {
	    geneSnpPageBuf << '<p>The user-selected genes that do not have matching SNP data: ' << geneNotUsedNameSet.join(', ') << '</p>'
        }

	if (snpNameList) {
	    Set<String> snpNotUsedNameSet = []
            // Need to get the list of SNPs that do not have data
	    for (String snpName in snpNameList) {
		if (snpUsedNameSet) {
		    if (!snpUsedNameSet.contains(snpName)) {
			snpNotUsedNameSet << snpName
                    }
                }
		else {
		    snpNotUsedNameSet << snpName
		}
            }
	    if (snpNotUsedNameSet) {
		geneSnpPageBuf << '<p>The user-selected SNPs that do not have data: ' << snpNotUsedNameSet.join(', ') << '</p>'
            }
        }

	geneSnpPageBuf << '</body></html>'
    }

    Set<Long> getSnpSet(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap) {
	if (!allGeneSnpMap) {
	    return null
        }

	Set<Long> allSnpSet = []
	for (SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapChrom in allGeneSnpMap.values()) {
	    for (Map<Long, GeneWithSnp> geneMap in geneWithSnpMapChrom.values()) {
		for (GeneWithSnp gene in geneMap.values()) {
		    for (SnpInfo snp in gene.snpMap.values()) {
			allSnpSet << snp.id
                    }
		}
            }
	}

	allSnpSet
    }

    void getSNPDataByProbeByChrom(List<SnpDataset> datasetList, Map<String, List<SnpDataByProbe>> snpDataByChromMap,
                                  Collection snpIds) {
	if (!datasetList) {
	    throw new Exception('The datasetList is empty')
	}
	Assert.notNull snpDataByChromMap, 'The snpDataByChromMap is null'
	if (!snpIds) {
	    throw new Exception('The snpIds is empty')
	}

	Sql sql = new Sql(dataSource)

	String trialName = datasetList[0].trialName
        // Get the order of each dataset in the compacted data String
	Map<Long, Integer> datasetCompactLocationMap = [:]
	String sqlStr = 'select snp_dataset_id, location from DEAPP.de_snp_data_dataset_loc where trial_name = ?'
        sql.eachRow(sqlStr, [trialName]) { row ->
            Long datasetId = row.snp_dataset_id
            Integer order = row.location
	    datasetCompactLocationMap[datasetId] = order
        }

        String snpIdListStr = getStringFromCollection(snpIds)
        // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
	sqlStr = '''
			select b.name, b.chrom, b.chrom_pos, c.snp_data_by_probe_id, c.snp_id, c.probe_id, c.probe_name, c.trial_name, c.data_by_probe
			from DEAPP.de_snp_info b, DEAPP.de_snp_data_by_probe c
			where b.snp_info_id = c.snp_id
			  and c.trial_name = ?
			  and b.snp_info_id in (''' + snpIdListStr + ') order by b.chrom, b.chrom_pos'
        sql.eachRow(sqlStr, [trialName]) { row ->
	    String dataByProbe = ((Clob) row.data_by_probe).asciiStream.text
	    SnpDataByProbe snpDataByProbe = new SnpDataByProbe(
		snpDataByProbeId: row.snp_data_by_probe_id,
		snpInfoId: row.snp_id,
		snpName: row.name,
		probeId: row.probe_id,
		probeName: row.probe_name,
		trialName: row.trial_name,
		chrom: row.chrom,
		chromPos: row.chrom_pos,
		dataArray: getSnpDataArrayFromCompactedString(datasetList, datasetCompactLocationMap, dataByProbe))

	    List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap[snpDataByProbe.chrom]
            if (snpDataByProbeList == null) {
		snpDataByProbeList = []
		snpDataByChromMap[snpDataByProbe.chrom] = snpDataByProbeList
            }
	    snpDataByProbeList << snpDataByProbe
        }
    }

    List<Long> getSNPDatasetIdList(String subjectIds) {
	if (!subjectIds) {
	    return null
	}

        List<Long> idList = null
	String sql = '''
			SELECT subject_snp_dataset_id as id
			FROM DEAPP.de_subject_snp_dataset
			WHERE patient_num in (''' + subjectIds + ')'
	new Sql(dataSource).eachRow(sql) { row ->
            Long id = row.id
            if (idList == null) {
		idList = []
            }
	    idList << id
        }
	idList
    }

    String getConceptDisplayName(String conceptId, Map<String, String> conceptIdToDisplayNameMap) {
	if (!conceptId || conceptIdToDisplayNameMap == null) {
	    return null
	}

	String conceptDisplayName = conceptIdToDisplayNameMap[conceptId]
        if (conceptDisplayName == null) {
	    new Sql(dataSource).eachRow('select name_char from I2B2DEMODATA.concept_dimension where concept_cd = ?', [conceptId]) { row ->
                conceptDisplayName = row.name_char
            }
        }

	conceptDisplayName
    }

    String getStringFromCollection(Collection inCollection) {
	if (!inCollection) {
	    return null
	}

	StringBuilder buf = new StringBuilder()
	for (obj in inCollection) {
	    if (buf) {
		buf << ', '
	    }
            if (obj instanceof Long || obj instanceof Integer || obj instanceof Float || obj instanceof Double) {
		buf << obj
            }
            else {
		buf << QUOTE << obj << QUOTE
            }
        }
	buf
    }

    String getSqlStrFromChroms(String chroms) {
	if (!chroms?.trim()) {
            return "'ALL'"
	}

        String[] values = chroms.split(',')
	StringBuilder buf = new StringBuilder()
        for (int i = 0; i < values.length; i++) {
	    if (i) {
		buf << ','
	    }
	    buf << QUOTE << values[i] << QUOTE
        }
	buf
    }

    List<String> getSortedChromList(Set<String> chromSet) {
	if (!chromSet) {
	    return null
        }

	if (chromSet.size() == 1) {
	    return chromSet
        }

	SortedMap<Integer, String> chromIndexMap = new TreeMap<>()
	for (String chrom in chromSet) {
            for (int i = 0; i < allChroms.length; i++) {
		if (chrom == allChroms[i]) {
		    chromIndexMap[i] = chrom
		}
            }
        }

	chromIndexMap.values()
    }

    String[][] getSnpDataArrayFromCompactedString(List<SnpDataset> datasetList,
                                                  Map<Long, Integer> datasetCompactLocationMap, String dataByProbe) {
        String[][] dataArray = new String[datasetList.size()][2]

        for (int i = 0; i < datasetList.size(); i++) {
	    SnpDataset snpDataset = datasetList[i]
	    int location = datasetCompactLocationMap[snpDataset.id]
            // The snp data is compacted in the format of [##.##][AB] for copy number and genotype
	    String copyNumber = dataByProbe.substring(location * 7, location * 7 + 5)
	    String genotype = dataByProbe.substring(location * 7 + 5, location * 7 + 7)
            dataArray[i][0] = copyNumber
            dataArray[i][1] = genotype
        }

	dataArray
    }
}
