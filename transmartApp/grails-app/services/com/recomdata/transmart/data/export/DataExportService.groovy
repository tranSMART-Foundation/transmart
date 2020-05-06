package com.recomdata.transmart.data.export

import com.recomdata.snp.SnpData
import com.recomdata.transmart.data.export.exception.DataNotFoundException
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.quartz.JobDataMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.Assert
import org.transmart.authorization.QueriesResourceAuthorizationDecorator
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.users.User
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService
import org.transmartproject.db.ontology.StudiesResourceService

import javax.sql.DataSource

import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.EXPORT

@Slf4j('logger')
class DataExportService {

    //Hard-coded subsets to count 2
    private static final List<String> subsets = ['subset1', 'subset2'].asImmutable()

    @Autowired private ACGHDataService ACGHDataService
    @Autowired private AdditionalDataService additionalDataService
    @Autowired private ClinicalExportService clinicalExportService
    @Autowired private DataSource dataSource
    @Autowired private GeneExpressionDataService geneExpressionDataService
    @Autowired private GrailsApplication grailsApplication
    @Autowired private HighDimensionResourceService highDimensionResourceService
    @Autowired private HighDimExportService highDimExportService
    @Autowired private I2b2ExportHelperService i2b2ExportHelperService
    QueriesResourceAuthorizationDecorator queriesResourceAuthorizationDecorator
    @Autowired private RNASeqDataService RNASeqDataService
    @Autowired private SnpDataService snpDataService
    @Autowired private StudiesResourceService studiesResourceService
    @Autowired private VcfDataService vcfDataService

    @Value('${com.recomdata.analysis.data.file.dir:}')
    private String dataFileDir

    @Value('${com.recomdata.transmart.data.export.jobTmpDirectory:}')
    private String exportJobTmpDirectory

    @Transactional(readOnly = true)
    def exportData(JobDataMap jobDataMap) {
	def checkboxList = jobDataMap.checkboxList

	Assert.isTrue((checkboxList.getClass().isArray() && checkboxList) ||
		      (checkboxList instanceof List && checkboxList), 'Please select the data to Export.')

	String jobName = jobDataMap.jobName
	String jobTmpDirectory = jobDataMap.jobTmpDirectory
	Map<String, String> resultInstanceIdMap = jobDataMap.result_instance_ids
	Map<String, List<String>> subsetSelectedFilesMap = jobDataMap.subsetSelectedFilesMap
        def subsetSelectedPlatformsByFiles = jobDataMap.subsetSelectedPlatformsByFiles
        def highDimDataTypes = jobDataMap.highDimDataTypes

	String study = null
	File studyDir = null
	Map<String, Boolean> filesDoneMap = [:]
	Map selection = jobDataMap.selection ? new JsonSlurper().parseText(jobDataMap.selection) : [:]

	if (!jobTmpDirectory) {
	    jobTmpDirectory = exportJobTmpDirectory
	    Assert.hasLength jobTmpDirectory, 'Job temp directory must be specified'
        }

	for (String subset in subsets) {
	    List<String> selectedFilesList = subsetSelectedFilesMap[subset] ?: []
	    if (selectedFilesList) {
                //Prepare Study dir
		List<String> studyList = null
		if (resultInstanceIdMap[subset]) {
                    studyList = i2b2ExportHelperService.findStudyAccessions([resultInstanceIdMap[subset]])
		    if (studyList) {
			study = studyList[0]
                        studyDir = new File(jobTmpDirectory, subset + (studyList.size() == 1 ? '_' + study : ''))
                        studyDir.mkdir()
                    }
                }

                //Pull the data pivot parameter out of the data map.
		def pivotDataValueDef = jobDataMap.pivotData
		boolean pivotData = true
		if (pivotDataValueDef == false) {
		    pivotData = false
		}

                if (resultInstanceIdMap[subset]) {
                    // Construct a list of the URL objects we're running, submitted to the pool
		    for (String selectedFile in selectedFilesList) {
			List gplIds = subsetSelectedPlatformsByFiles?.get(subset)?.get(selectedFile)
			def retVal
                        switch (selectedFile) {
                            case 'CLINICAL':
				clinicalExportService.exportClinicalData(jobName, resultInstanceIdMap[subset] as Long,
									 selection[subset][selectedFile.toLowerCase()].selector, studyDir)
                                break
                            case highDimensionResourceService.knownTypes:
                                // For now we ignore the information about the platforms to 
                                // export. All data that matches the selected concepts
                                // is exported
				for (format in highDimDataTypes[subset][selectedFile].keySet()) {
				    retVal = highDimExportService.exportHighDimData(jobName,
										    resultInstanceIdMap[subset] as long, selection[subset][selectedFile].selector,
										    selectedFile, format, studyDir)
                                }

                                //filesDoneMap is used for building the Clinical Data query
				filesDoneMap['MRNA.TXT'] = true
                                break
                            case 'MRNA_DETAILED.TXT':
				String pathway = jobDataMap.gexpathway
				String timepoint = jobDataMap.gextime
				String sampleType = jobDataMap.gexsample
				String tissueType = jobDataMap.gextissue
				String gplString = jobDataMap.gexgpl

				if (tissueType == ',') {
				    tissueType = ''
				}
				if (sampleType == ',') {
				    sampleType = ''
				}
				if (timepoint == ',') {
				    timepoint = ''
				}

                                if (gplIds != null) {
                                    gplIds = gplString.tokenize(',')
                                }
                                else {
                                    gplIds = []
                                }

                                //adding String to a List to make it compatible to the type expected
                                //if gexgpl contains multiple gpl(s) as single string we need to convert that to a list
				retVal = geneExpressionDataService.getData(studyList, studyDir, 'mRNA.trans',
									   jobName, resultInstanceIdMap[subset], pivotData, gplIds, pathway,
									   timepoint, sampleType, tissueType, true)
				if (jobDataMap.analysis != 'DataExport') {
                                    //if geneExpressionDataService was not able to find data throw an exception.
                                    if (!retVal) {
					throw new DataNotFoundException('There are no patients that meet the ' +
									'criteria selected therefore no gene expression data was returned.')
                                    }
                                }
                                break
                            case 'ACGH_REGIONS.TXT':
                                if (studyList.size() != 1) {
				    throw new Exception('Only one study allowed per analysis; list given was : ' + studyList)
				}
				ACGHDataService.writeRegions studyList[0], studyDir, 'regions.txt',
				    jobName, resultInstanceIdMap[subset]
				// currently the interface does not allow filtering, so don't implement it here was well
                                break
                            case 'RNASEQ.TXT':
                                if (studyList.size() != 1) {
				    throw new Exception('Only one study allowed per analysis; list given was : ' + studyList)
				}
				RNASeqDataService.writeRegions studyList[0], studyDir, 'RNASeq.txt',
				    jobName, resultInstanceIdMap[subset]
				// currently the interface does not allow filtering, so don't implement it here was well
                                break
                            case 'MRNA.CEL':
				geneExpressionDataService.downloadCELFiles resultInstanceIdMap[subset], studyList,
				    studyDir, jobName, null, null, null, null
                                break
                            case 'GSEA.GCT & .CLS':
				geneExpressionDataService.getGCTAndCLSData studyList, studyDir, 'mRNA.GCT',
				    jobName, resultInstanceIdMap, pivotData, gplIds
                                break
                            case 'SNP.PED, .MAP & .CNV':
				retVal = snpDataService.getData(studyDir, 'snp.trans', jobName, resultInstanceIdMap[subset])
				snpDataService.getDataByPatientByProbes(studyDir, resultInstanceIdMap[subset], jobName)
                                break
                            case 'SNP.CEL':
				snpDataService.downloadCELFiles(studyList, studyDir, resultInstanceIdMap[subset], jobName)
                                break
                            case 'SNP.TXT':
                                //In this case we need to get a file with Patient ID, Probe ID, Gene, Genotype, Copy Number
                                //We need to grab some inputs from the jobs data map.
				String pathway = jobDataMap.snppathway
				String sampleType = jobDataMap.snpsample
				String timepoint = jobDataMap.snptime
				String tissueType = jobDataMap.snptissue

				// row processor which handles the writing to the SNP text file.
                                SnpData snpData = new SnpData()
                                //Make sure the directory we want to write the file to is created.
				new File(jobTmpDirectory, 'subset1_' + study + '/SNP').mkdir()
                                //This is the exact path of the file to write.
				String fileLocation = jobTmpDirectory + '/subset1_' + study + '/SNP/snp.trans'
                                //Call our service which writes the SNP data to a file.
				boolean gotData = snpDataService.getSnpDataByResultInstanceAndGene(
				    resultInstanceIdMap[subset], study, pathway, sampleType, timepoint,
				    tissueType, snpData, fileLocation, true, true)
				if (jobDataMap.analysis != 'DataExport') {
                                    //if SNPDataService was not able to find data throw an exception.
                                    if (!gotData) {
					throw new DataNotFoundException('There are no patients that meet the ' +
									'criteria selected therefore no SNP data was returned.')
                                    }
                                }
                                break
                            case 'ADDITIONAL':
				additionalDataService.downloadFiles(resultInstanceIdMap[subset], studyList, studyDir, jobName)
                                break
                            case 'IGV.VCF':

				String selectedGenes = jobDataMap.selectedGenes
				String chromosomes = jobDataMap.chroms
				String selectedSNPs = jobDataMap.selectedSNPs

				logger.trace 'VCF Parameters; selectedGenes:{}, chromosomes:{}, selectedSNPs:{}',
				    selectedGenes, chromosomes, selectedSNPs

				String webRootName = jobDataMap.appRealPath
				if (!webRootName.endsWith(File.separator)) {
                                    webRootName += File.separator
				}
				String prefix = 'S1'
				if ('subset2' == subset) {
                                    prefix = 'S2'
				}
				vcfDataService.getDataAsFile webRootName + dataFileDir, jobName, null,
				    resultInstanceIdMap[subset], selectedSNPs, selectedGenes, chromosomes, prefix
                                break
                        }
                    }
                }
            }
        }
    }

    boolean isUserAllowedToExport(final User user, final List<Long> resultInstanceIds) {
        assert user
        assert resultInstanceIds

        // check that the user has export access in the studies of patients

	List<QueryResult> queryResults = []
	for (long id in resultInstanceIds.findAll()) {
	    queryResults << queriesResourceAuthorizationDecorator.getQueryResultFromId(id)
	}

	Set<Patient> patients = queryResults*.patients.flatten() as Set<Patient> // merge two patient sets into one
	Set<String> trials = patients*.trial as Set
	Set<Study> studies = []
	for (String trial in trials) {
	    studies << studiesResourceService.getStudyById(trial)
	}
	
	for (Study study in studies) {
            if (!user.canPerform(EXPORT, study)) {
		return false
            }
        }

	true
    }
}
