package com.thomsonreuters.lsps.transmart

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.plugin.shared.SecurityService

@Slf4j('logger')
class MetacoreEnrichmentController {
	
    // Maximum loop count when creating temp directories
    private static final int TEMP_DIR_ATTEMPTS = 10000

    private static final List<String> JS = ['metacoreEnrichment', 'metacoreEnrichmentDisplay', 'raphael-min'].asImmutable()
    private static final List<String> CSS = ['metacore'].asImmutable()

    def dataExportService
    @Autowired private MetacoreEnrichmentService metacoreEnrichmentService
    def RModulesService
    @Autowired private SecurityService securityService
	
    @Value('${com.recomdata.plugins.tempFolderDirectory:}')
    private String tempFolderDirectory
	
    def index() {}
		
    def prepareData() {
	String jobName = securityService.currentUsername() + '-metacoreEnrichment-' + UUID.randomUUID()
	params.jobName = jobName
		
	Map jobData = RModulesService.prepareDataForExport(securityService.currentUsername(), params)
	jobData.subsetSelectedFilesMap = [subset1: ['CLINICAL.TXT', 'MRNA_DETAILED.TXT'],
		                          subset2: ['CLINICAL.TXT', 'MRNA_DETAILED.TXT']]
	jobData.pivotData = false
	jobData.jobTmpDirectory = tempFolderDirectory + '/' + jobName

	createDir new File(tempFolderDirectory), jobName

	def res = dataExportService.exportData(jobData)
	// for now, just first subset
	String mrnaFilename = jobData.jobTmpDirectory + '/' + res[0] + '_' + jobData.studyAccessions[0] + '/mRNA/Processed_Data/mRNA.trans'
	render([mrnaFilename: mrnaFilename] as JSON)
    }
	
    def runAnalysis(String mrnaFilename) {
	Map<String, String> metacoreParams = metacoreEnrichmentService.getMetacoreParams()

	// only cohort1 (first list in the first parameter is used in enrichment for now)
	double threshold = params.double('zThresholdAbs', 0)

	int i = 0
	Set geneList = []

	for (String line in new File(mrnaFilename).readLines()) {
	    if (i > 0) {
		String[] values = line.split('\t', 13)
		//If GENE_SYMBOL is empty need add limit to split or length line will be less 11
		if (values.size() < 12) {
		    continue
		}

		double zScore = 0
		try {
		    zScore = Double.parseDouble(values[7])
		}
		catch (ignored) {
		    logger.debug 'Can not parse z_score {}', values[7]
		}
		if (values[11] && Math.abs(zScore) >= threshold) {
		    geneList << values[11]
		}
	    }

	    i++
	}

	logger.info 'Running enrichment for {} genes; |z| >= {}', geneList.size(), threshold

	render(metacoreEnrichmentService.getEnrichmentByMaps(
	    [IdType: 'LOCUSLINK', Data  : [geneList as List]],
	    metacoreParams) as JSON)
    }
	
    def runAnalysisForMarkerSelection() {
	logger.info 'Running enrichment for {} genes (IdType passed: {})', params.IdList.size(), params.IdType
	render(metacoreEnrichmentService.getEnrichmentByMaps(
	    [IdType: params.IdType ?: 'AFFYMETRIX', Data: [params.IdList as List]],
	    metacoreEnrichmentService.getMetacoreParams()) as JSON)
    }
	
    def serverSettingsWindow() {
	String mode = metacoreEnrichmentService.metacoreSettingsMode()
	Map<String, String> settings = metacoreEnrichmentService.getMetacoreParams()

	render view: 'metacoreSettingsWindow', model: [
	    settingsConfigured: metacoreEnrichmentService.areSettingsConfigured(),
	    settingsMode: mode,
	    systemSettingsDefined: metacoreEnrichmentService.systemMetacoreSettingsDefined(),
	    settings: mode == 'user' ? settings : [baseUrl: settings?.baseUrl]]
    }
	
    def saveMetacoreSettings(String mode, String baseUrl, String login, String password) {
	if (mode) {
	    logger.info 'MC Settings - Setting mode: {}', mode
	    metacoreEnrichmentService.setMetacoreSettingsMode mode
	}	

	if (baseUrl) {
	    logger.info 'MC Settings - Setting baseUrl: {}', baseUrl
	    metacoreEnrichmentService.setMetacoreBaseUrl baseUrl
	}	

	if (login) {
	    logger.info 'MC Settings - Setting login: {}', login
	    metacoreEnrichmentService.setMetacoreLogin login
	}

	if (password) {
	    logger.info 'MC Settings - Setting new password'
	    metacoreEnrichmentService.setMetacorePassword password
	}

	render([result: 'success'] as JSON)
    }

    def loadScripts() {
	List<Map> rows = []

	for (String file in JS) {
	    rows << [path: resource(dir: 'js/metacore', file: file + '.js', plugin: 'transmart-metacore-plugin'), type: 'script']
        }

	for (String file in CSS) {
	    rows << [path: resource(dir: 'css', file: file + '.css', plugin: 'transmart-fractalis'), type: 'css']
	}

	render([success: true, totalCount: rows.size(), files: rows] as JSON)
    }

    // copied from com.recomdata.transmart.data.export.util.FileWriterUtil
    private File createDir(File baseDir, String name) {
	for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
	    File tempDir = new File(baseDir, name)
	    if (tempDir.mkdir()) {
		return tempDir
	    }
	    if (tempDir.exists()) {
		return tempDir
	    }
	}

	throw new IllegalStateException("Failed to create directory " + name + " within " + TEMP_DIR_ATTEMPTS)
    }
}
