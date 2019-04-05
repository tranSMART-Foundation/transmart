import com.recomdata.export.IgvFiles
import i2b2.GeneWithSnp
import i2b2.SnpDataByProbe
import i2b2.SnpDataset
import i2b2.SnpDatasetListByProbe
import i2b2.SnpProbeSortedDef
import i2b2.StringLineReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.Assert

class IgvService {

    static transactional = false

    @Autowired private SnpService snpService

    /**
     * IGV launched from a session file will read the data file URLs defined in the session file. However, the data file will be
     * splitted into tracks, and the each track is displayed independently. If a data URL has multiple datasets/tracks, this data
     * URL will be read multiple times, and will cause an OutOfMemory exception.
     * The solution is to have one data URL for each dataset/track.
     */
    void getIgvDataByPatient(String subjectIds1, String subjectIds2, String chroms, IgvFiles igvFiles) {
	Assert.notNull igvFiles, 'The IgvFiles object is not instantiated'

        // For the patient numbers selected by users in subset 1 and subset 2
	List<Long>[] patientNumListArray = [
	    getPatientNumListFromSubjectIdStr(subjectIds1),
	    getPatientNumListFromSubjectIdStr(subjectIds2)]

        // Get SQL query String for all the subject IDs
        String subjectListStr = ''
	if (subjectIds1) {
	    subjectListStr += subjectIds1
	}
	if (subjectIds2) {
	    if (subjectListStr) {
		subjectListStr += ', '
	    }
            subjectListStr += subjectIds2
        }

	Map<Long, SnpDataset[]> snpDatasetBySubjectMap = [:]
	snpService.getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr

	StringBuilder sampleInfoBuf = new StringBuilder()
	List<SnpDataset> datasetList = []
	List<String> datasetNameForSNPViewerList = []
	snpService.getSnpSampleInfo datasetList, datasetNameForSNPViewerList,
	    patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf

	Map<Long, Map<String, String>> snpDataByDatasetByChrom =
	    snpService.getSNPDataByDatasetByChrom(subjectListStr, chroms)

        List<String> neededChroms = snpService.getSortedChromList(chroms)
	Map<String, SnpProbeSortedDef> probeDefMap = snpService.getSNPProbeDefMap(
	    datasetList[0].platformName, chroms)

        // Instantiate the dataWrite, one for each dataset
        List<File> dataFileList = igvFiles.getCopyNumberFileList()
	for (SnpDataset dataset in datasetList) {
            File cnFile = igvFiles.createCopyNumberFile()
	    dataFileList << cnFile
            BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile))
            // Write the header column
	    dataWriter.write 'SNP\tChromosome\tPhysicalPosition\t' + dataset.datasetName
	    dataWriter.write '\n'

	    Map<String, String> dataByChromMap = snpDataByDatasetByChrom[dataset.id]
	    for (String chrom in neededChroms) {
		StringLineReader dataReader = new StringLineReader(dataByChromMap[chrom])

		SnpProbeSortedDef probeDef = probeDefMap[chrom]
                StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef)
		int numProbe = probeDef.numProbe
		for (int idx = 0; idx < numProbe; idx++) {
                    String probeLine = probeReader.readLine()
		    if (!probeLine?.trim()) {
			throw new Exception('The number ' + idx +
					    ' line in probe definition file for chromosome ' + chrom + ' is empty')
		    }
		    dataWriter.write probeLine

		    dataWriter.write '\t' + dataReader.readLine().split('\t')[0] + '\n'
                }
            }

            dataWriter.close()
        }
    }

    void getIgvDataByProbe(String subjectIds1, String subjectIds2, List<Long> geneSearchIdList, List<String> geneNameList,
	                   List<String> snpNameList, IgvFiles igvFiles, StringBuilder geneSnpPageBuf) {
	Assert.notNull igvFiles, 'The IgvFiles object is not instantiated'
	Assert.notNull geneSnpPageBuf , 'The geneSnpPageBuf object is not instantiated'

        SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe()

        // For the patient numbers selected by users in subset 1 and subset 2
	List<Long>[] patientNumListArray = [
	    getPatientNumListFromSubjectIdStr(subjectIds1),
	    getPatientNumListFromSubjectIdStr(subjectIds2)]
        allDataByProbe.patientNumList_1 = patientNumListArray[0]
        allDataByProbe.patientNumList_2 = patientNumListArray[1]

        // Get SQL query String for all the subject IDs
        String subjectListStr = ''
	if (subjectIds1) {
	    subjectListStr += subjectIds1
	}
	if (subjectIds2) {
	    if (subjectListStr) {
		subjectListStr += ', '
	    }
            subjectListStr += subjectIds2
        }

        // Get the gene-snp map, and the snp set related to all the user-input genes.
        // Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = [:]
	Map<Long, GeneWithSnp> geneEntrezIdMap = [:]
	Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = [:]
	snpService.getGeneWithSnpMapForGenes geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList

        // Get the gene-snp map for the user-selected SNPs.
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = [:]
	snpService.getGeneWithSnpMapForSnps geneSnpMapForSnp, snpNameList

	Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = [
	    geneSnpMapForGene, geneSnpMapForSnp]
        Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = snpService.mergeGeneWithSnpMap(geneSnpMapList)

	if (!allGeneSnpMap) {
            throw new Exception('There is no SNP data for selected genes and SNP IDs')
	}

        // Generate the web page to display the Gene and SNP selected by User
	snpService.getSnpGeneAnnotationPage geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap,
	    geneNameToGeneWithSnpMap, geneNameList, snpNameList

        Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap
	snpService.getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr

	StringBuilder sampleInfoBuf = new StringBuilder()
        List<SnpDataset> datasetList = allDataByProbe.datasetList
        List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList
	snpService.getSnpSampleInfo datasetList, datasetNameForSNPViewerList, patientNumListArray,
	    snpDatasetBySubjectMap, sampleInfoBuf

        // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
        Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap

        Set<Long> allSnpIdSet = snpService.getSnpSet(allGeneSnpMap)
	snpService.getSNPDataByProbeByChrom datasetList, snpDataByChromMap, allSnpIdSet

        List<String> neededChroms = snpService.getSortedChromList(snpDataByChromMap.keySet())

        // Write the sample info text file for SNPViewer
	igvFiles.getSampleFile() << sampleInfoBuf.toString()

        List<File> dataFileList = igvFiles.getCopyNumberFileList()
        for (int i = 0; i < datasetList.size(); i++) {
            File cnFile = igvFiles.createCopyNumberFile()
	    dataFileList << cnFile
            BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile))
            // Write the header column
	    dataWriter.write 'SNP\tChromosome\tPhysicalPosition\t' + datasetList[i].datasetName
	    dataWriter.write '\n'

	    for (String chrom in neededChroms) {
		for (SnpDataByProbe snpDataByProbe in snpDataByChromMap[chrom]) {
		    dataWriter.write snpDataByProbe.snpName + '\t' + chrom + '\t' + snpDataByProbe.chromPos
		    dataWriter.write '\t' + snpDataByProbe.dataArray[i][0].trim() + '\n'
                }
            }

            dataWriter.close()
        }
    }

    /**
     * IGV launched from a session file will read the data file URLs defined in the session file. However, the data file will be
     * splitted into tracks, and the each track is displayed independently. If a data URL has multiple datasets/tracks, this data
     * URL will be read multiple times, and will cause an OutOfMemory exception.
     * The solution is to have one data URL for each dataset/track.
     */
    void getIgvDataByPatientSample(List<Long>[] patientNumListArray, String chroms, IgvFiles igvFiles) {
	Assert.notNull igvFiles, 'The IgvFiles object is not instantiated'

        // Get SQL query String for all the subject IDs
        String subjectListStr = ''

        //Loop through the array of Lists.
        for (int i = 0; i < patientNumListArray.length; i++) {
            //Add a comma to seperate the lists.
	    if (subjectListStr) {
		subjectListStr += ','
	    }

            //This is a list of patients, add it to our string.
            subjectListStr += patientNumListArray[i].join(',')
        }

	Map<Long, SnpDataset[]> snpDatasetBySubjectMap = [:]
	snpService.getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr

	StringBuilder sampleInfoBuf = new StringBuilder()
	List<SnpDataset> datasetList = []
	List<String> datasetNameForSNPViewerList = []
	snpService.getSnpSampleInfo datasetList, datasetNameForSNPViewerList,
	    patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf

	Map<Long, Map<String, String>> snpDataByDatasetByChrom =
	    snpService.getSNPDataByDatasetByChrom(subjectListStr, chroms)

        List<String> neededChroms = snpService.getSortedChromList(chroms)
	Map<String, SnpProbeSortedDef> probeDefMap = snpService.getSNPProbeDefMap(
	    datasetList[0].platformName, chroms)

        // Instantiate the dataWrite, one for each dataset
        List<File> dataFileList = igvFiles.getCopyNumberFileList()
	for (SnpDataset dataset in datasetList) {
            File cnFile = igvFiles.createCopyNumberFile()
	    dataFileList << cnFile
            BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile))
            // Write the header column
	    dataWriter.write 'SNP\tChromosome\tPhysicalPosition\t' + dataset.datasetName
	    dataWriter.write '\n'

	    Map<String, String> dataByChromMap = snpDataByDatasetByChrom[dataset.id]
	    for (String chrom in neededChroms) {
		StringLineReader dataReader = new StringLineReader(dataByChromMap[chrom])

		SnpProbeSortedDef probeDef = probeDefMap[chrom]
                StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef)
		int numProbe = probeDef.numProbe
		for (int idx = 0; idx < numProbe; idx++) {
                    String probeLine = probeReader.readLine()
		    if (!probeLine?.trim()) {
			throw new Exception('The number ' + idx +
					    ' line in probe definition file for chromosome ' + chrom + ' is empty')
		    }
		    dataWriter.write probeLine

		    dataWriter.write '\t' + dataReader.readLine().split('\t')[0] + '\n'
                }
            }

            dataWriter.close()
        }
    }

    void getIgvDataByProbeSample(List<Long>[] patientNumListArray, List<Long> geneSearchIdList, List<String> geneNameList,
	                         List<String> snpNameList, IgvFiles igvFiles, StringBuilder geneSnpPageBuf) {
	Assert.notNull igvFiles, 'The IgvFiles object is not instantiated'
	Assert.notNull geneSnpPageBuf, 'The geneSnpPageBuf object is not instantiated'

        SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe()

        // Get SQL query String for all the subject IDs
	StringBuilder subjectListStr = new StringBuilder()

        //Loop through the array of Lists.
        for (int i = 0; i < patientNumListArray.length; i++) {
            //Add a comma to seperate the lists.
	    if (subjectListStr) {
		subjectListStr << ','
	    }

            //This is a list of patients, add it to our string.
	    subjectListStr << patientNumListArray[i].join(',')
        }

        // Get the gene-snp map, and the snp set related to all the user-input genes.
        // Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = [:]
	Map<Long, GeneWithSnp> geneEntrezIdMap = [:]
	Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = [:]
	snpService.getGeneWithSnpMapForGenes geneSnpMapForGene, geneEntrezIdMap,
	    geneNameToGeneWithSnpMap, geneSearchIdList

        // Get the gene-snp map for the user-selected SNPs.
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = [:]
	snpService.getGeneWithSnpMapForSnps geneSnpMapForSnp, snpNameList

	Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = []
	geneSnpMapList << geneSnpMapForGene
	geneSnpMapList << geneSnpMapForSnp
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap =
	    snpService.mergeGeneWithSnpMap(geneSnpMapList)

	if (!allGeneSnpMap) {
            throw new Exception('There is no SNP data for selected genes and SNP IDs')
	}

        // Generate the web page to display the Gene and SNP selected by User
	snpService.getSnpGeneAnnotationPage geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap,
	    geneNameToGeneWithSnpMap, geneNameList, snpNameList

        Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap
	snpService.getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr.toString()

	StringBuilder sampleInfoBuf = new StringBuilder()
        List<SnpDataset> datasetList = allDataByProbe.datasetList
        List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList
	snpService.getSnpSampleInfo datasetList, datasetNameForSNPViewerList,
	    patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf

        // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
        Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap

        Set<Long> allSnpIdSet = snpService.getSnpSet(allGeneSnpMap)
	snpService.getSNPDataByProbeByChrom datasetList, snpDataByChromMap, allSnpIdSet

        List<String> neededChroms = snpService.getSortedChromList(snpDataByChromMap.keySet())

        // Write the sample info text file for SNPViewer
	igvFiles.getSampleFile() << sampleInfoBuf.toString()

        List<File> dataFileList = igvFiles.getCopyNumberFileList()
        for (int i = 0; i < datasetList.size(); i++) {
	    SnpDataset dataset = datasetList[i]
            File cnFile = igvFiles.createCopyNumberFile()
	    dataFileList << cnFile
            BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile))
            // Write the header column
	    dataWriter.write 'SNP\tChromosome\tPhysicalPosition\t' + dataset.datasetName
	    dataWriter.write '\n'

	    for (String chrom in neededChroms) {
		for (SnpDataByProbe snpDataByProbe in snpDataByChromMap[chrom]) {
		    dataWriter.write snpDataByProbe.snpName + '\t' + chrom + '\t' + snpDataByProbe.chromPos
		    dataWriter.write '\t' + snpDataByProbe.dataArray[i][0].trim() + '\n'
                }
            }

            dataWriter.close()
        }
    }

    List<Long> getPatientNumListFromSubjectIdStr(String subjectIdStr) {
	if (!subjectIdStr) {
	    return null
	}

	List<Long> patientNumList = []
        String[] subjectArray = subjectIdStr.split(',')
	for (String subjectId in subjectArray) {
	    patientNumList << Long.valueOf(subjectId.trim())
        }

	patientNumList
    }
}
