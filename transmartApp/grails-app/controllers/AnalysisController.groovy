import com.recomdata.asynchronous.GenePatternService
import com.recomdata.export.IgvFiles
import com.recomdata.export.PlinkFiles
import com.recomdata.export.SnpViewerFiles
import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import grails.converters.JSON
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.genepattern.webservice.JobResult
import org.genepattern.webservice.WebServiceException
import org.json.JSONException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.transmart.CohortInformation
import org.transmart.HeatmapValidator
import org.transmart.plugin.shared.SecurityService
import org.transmartproject.db.log.AccessLogService

import javax.sql.DataSource

/**
 * @author mkapoor
 */
@Slf4j('logger')
class AnalysisController {

    private static final String geneInputPrefix = 'Gene>'
    private static
    final List<String> chroms = ['ALL', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13',
	                         '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y'].asImmutable()

    AccessLogService accessLogService
    AnalysisService analysisService
    DataSource dataSource
    GenePatternService genePatternService
    I2b2HelperService i2b2HelperService
    IgvService igvService
    PlinkService plinkService
    SnpService snpService
    SolrService solrService
    SecurityService securityService

    @Value('${com.recomdata.datasetExplorer.genePatternURL:}')
    private String genePatternUrl

    @Value('${com.recomdata.solr.baseURL:}')
    private String solrUrl

    @Value('${com.recomdata.solr.maxRows:0}')
    private int solrMaxRows

    @Value('${com.recomdata.analysis.genepattern.file.dir:}')
    private String genePatternFileDir

    @Value('${com.recomdata.datasetExplorer.enableGenePattern:false}')
    private boolean enableGenePattern

    def index() {}

    def heatmapvalidate(String result_instance_id1, String result_instance_id2, String analysis) {
 	logger.debug 'Received heatmap validation request'
	result_instance_id1 = result_instance_id1 ?: null
	result_instance_id2 = result_instance_id2 ?: null
	logger.debug 'analysis type: {}', analysis

	logger.debug '\tresult_instance_id1: {}', result_instance_id1
	logger.debug '\tresult_instance_id2: {}', result_instance_id2
	accessLogService.report 'DatasetExplorer-Before Heatmap',
	    'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2

	String markerType = ''

        List<String> subjectIds1
        List<String> concepts1
        HeatmapValidator hv1 = new HeatmapValidator()
        CohortInformation ci1 = new CohortInformation()

        if (result_instance_id1 != null) {
	    subjectIds1 = i2b2HelperService.getSubjectsAsList(result_instance_id1)
	    concepts1 = i2b2HelperService.getConceptsAsList(result_instance_id1)
	    i2b2HelperService.fillHeatmapValidator(subjectIds1, concepts1, hv1)
	    i2b2HelperService.fillCohortInformation(subjectIds1, concepts1, ci1, CohortInformation.TRIALS_TYPE)
	    i2b2HelperService.fillDefaultGplInHeatMapValidator(hv1, ci1, concepts1)
	    i2b2HelperService.fillDefaultRbmpanelInHeatMapValidator(hv1, ci1, concepts1)
	    markerType = i2b2HelperService.getMarkerTypeFromConceptCd(concepts1[0])
        }

        List<String> subjectIds2
        List<String> concepts2
        HeatmapValidator hv2 = new HeatmapValidator()
        CohortInformation ci2 = new CohortInformation()
        if (result_instance_id2 != null) {
	    subjectIds2 = i2b2HelperService.getSubjectsAsList(result_instance_id2)
	    concepts2 = i2b2HelperService.getConceptsAsList(result_instance_id2)
	    i2b2HelperService.fillHeatmapValidator(subjectIds2, concepts2, hv2)
	    i2b2HelperService.fillCohortInformation(subjectIds2, concepts2, ci2, CohortInformation.TRIALS_TYPE)
	    i2b2HelperService.fillDefaultGplInHeatMapValidator(hv2, ci2, concepts2)
	    i2b2HelperService.fillDefaultRbmpanelInHeatMapValidator(hv2, ci2, concepts2)
        }

	Map result = [defaultPlatforms      : [hv1.getFirstPlatform(), hv2.getFirstPlatform()],
		      defaultPlatformLabels : [hv1.getFirstPlatformLabel(), hv2.getFirstPlatformLabel()],
		      trials                : [ci1.getAllTrials(), ci2.getAllTrials()],
		      defaultTimepoints     : [hv1.getAllTimepoints(), hv2.getAllTimepoints()],
		      defaultTimepointLabels: [hv1.getAllTimepointLabels(), hv2.getAllTimepointLabels()],
		      defaultSamples        : [hv1.getAllSamples(), hv2.getAllSamples()],
		      defaultSampleLabels   : [hv1.getAllSampleLabels(), hv2.getAllSampleLabels()],
		      defaultGpls           : [hv1.getAll('gpls'), hv2.getAll('gpls')],
		      defaultGplLabels      : [hv1.getAll('gplLabels'), hv2.getAll('gplLabels')],
		      defaultTissues        : [hv1.getAll('tissues'), hv2.getAll('tissues')],
		      defaultTissueLabels   : [hv1.getAll('tissueLabels'), hv2.getAll('tissueLabels')],
		      defaultRbmpanels      : [hv1.getAll('rbmpanels'), hv2.getAll('rbmpanels')],
		      defaultRbmpanelLabels : [hv1.getAll('rbmpanelsLabels'), hv2.getAll('rbmpanelsLabels')],
		      markerType            : markerType]
	render result as JSON
    }

    def getCohortInformation() {
	String infoType = params.INFO_TYPE
	String platform = params.PLATFORM
	String rbmpanels = params.RBMPANEL
	String gpls = params.GPL
	String trial = params.TRIAL
	String tissues = params.TISSUE
	String samples = params.SAMPLES

	CohortInformation ci = new CohortInformation()
	ci.platforms.add platform

	if (rbmpanels) {
	    ci.rbmpanels.addAll Arrays.asList(rbmpanels.split(','))
	}

	if (trial) {
	    ci.trials.addAll Arrays.asList(trial.split(','))
	}

	if (samples) {
	    ci.samples.addAll Arrays.asList(samples.split(','))
	}

	if (tissues) {
	    ci.tissues.addAll Arrays.asList(tissues.split(','))
	}

	if (gpls) {
	    ci.gpls.addAll Arrays.asList(gpls.split(','))
	}

	int ifT = infoType ? Integer.parseInt(infoType) : 0

	i2b2HelperService.fillCohortInformation(null, null, ci, ifT)

	Map result
	switch (ifT) {
	    case CohortInformation.GPL_TYPE: result = [rows: ci.gpls]; break
	    case CohortInformation.TISSUE_TYPE: result = [rows: ci.tissues]; break
	    case CohortInformation.TIMEPOINTS_TYPE: result = [rows: ci.timepoints]; break
	    case CohortInformation.SAMPLES_TYPE: result = [rows: ci.samples]; break
	    case CohortInformation.PLATFORMS_TYPE: result = [rows: ci.platforms]; break
	    case CohortInformation.RBM_PANEL_TYPE: result = [rows: ci.rbmpanels]; break
	    default: result = [rows: []]
	}

	render params.callback + '(' + (result as JSON) + ')'
    }

    def showSNPViewer(String result_instance_id1, String result_instance_id2, String chroms,
	              String genes, String geneAndIdList, String snps) {
	JSONObject result = new JSONObject()

	WorkflowStatus wfstatus = initWorkflowStatus('SnpViewer')

	try {
	    logger.debug 'Received SNPViewer rendering request: {}', request

	    logger.debug '\tresult_instance_id1: {}', result_instance_id1
	    logger.debug '\tresult_instance_id2: {}', result_instance_id2

	    chroms = chroms ?: 'ALL'

	    if (!hasValue(result_instance_id1)) {
		logger.debug '\tresult_instance_id2 == undefined or null'
		result_instance_id1 = null
	    }
	    if (!hasValue(result_instance_id2)) {
		logger.debug '\tresult_instance_id2 == undefined or null'
		result_instance_id2 = null
	    }

	    accessLogService.report 'DatasetExplorer-ShowSNPViewer',
		'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2

	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	    String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)

	    if (!subjectIds1 && !subjectIds2) {
		result.put('error', 'No subject was selected')
		response.outputStream << result.toString()
		return
	    }

	    List<Long> geneSearchIdList = []
	    List<String> geneNameList = []
	    if (genes) {
		getGeneSearchIdListFromRequest genes, geneAndIdList, geneSearchIdList, geneNameList
	    }

	    List<String> snpNameList = snps ? snps.split(',') as List : null

	    boolean isByPatient = !geneSearchIdList && !snpNameList

	    SnpViewerFiles snpFiles = new SnpViewerFiles()
	    StringBuilder geneSnpPageBuf = new StringBuilder()
	    try {
		if (isByPatient) {
		    i2b2HelperService.getSNPViewerDataByPatient subjectIds1, subjectIds2, chroms, snpFiles
		}
		else {
		    i2b2HelperService.getSNPViewerDataByProbe subjectIds1, subjectIds2, geneSearchIdList, geneNameList,
			snpNameList, snpFiles, geneSnpPageBuf
		}
	    }
	    catch (e) {
		result.put('error', e.message)
		return
	    }

	    JobResult[] jresult
	    try {
		jresult = genePatternService.snpViewer(snpFiles.dataFile, snpFiles.sampleFile)
	    }
	    catch (WebServiceException e) {
		result.put('error', 'WebServiceException: ' + e.message)
		return
	    }

	    String viewerURL
	    try {
		result.put('jobNumber', jresult[1].jobNumber)
		viewerURL = genePatternUrl +
		    '/gp/jobResults/' +
		    jresult[1].jobNumber +
		    '?openVisualizers=true'
		logger.debug 'URL for viewer: {}', viewerURL
		result.put('viewerURL', viewerURL)
		result.put('snpGeneAnnotationPage', geneSnpPageBuf.toString())

		logger.debug 'result: {}', result
	    }
	    catch (JSONException e) {
		logger.error 'JSON Exception: {}', e.message
		result.put('error', 'JSON Exception: ' + e.message)
	    }
	}
	finally {
	    wfstatus.result = result
	    wfstatus.setCompleted()
	}
    }

    def showSNPViewerSample(String chroms, String genes, String geneAndIdList, String snps,
	                    String result_instance_id1, String result_instance_id2) {
	JSONObject result = new JSONObject()

	//Set the workflow status that gets show in the status popup.
	WorkflowStatus wfstatus = initWorkflowStatus('SnpViewer')

	try {
	    logger.debug 'Received SNPViewer rendering request: {}', request

	    //The JSON we received should be [1:[category:[]]]
	    def subsetListJSON = request.JSON.SearchJSON

	    //We need to get an ID list per subset. Build the subset from the JSON Data.
	    Map subsetList = solrService.buildSubsetList(subsetListJSON)

	    //Change the chroms text if the param was empty.
	    if (!chroms?.trim()) {
		chroms = 'ALL'
	    }

	    accessLogService.report 'DatasetExplorer-ShowSNPViewer',
		'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2

	    List<List<Long>> patientNumList

	    boolean foundPatients = false

	    for (Map.Entry subsetItem in subsetList) {
		def subsetSampleList = subsetItem.value

		//Don't add a subset if there are no items in the subset.
		if (subsetSampleList) {
		    //Add the list to the list of lists.
		    List<Long> tempPatientList = i2b2HelperService.getSubjectsAsListFromSampleLong(subsetSampleList)

		    //If we found patients, add them to the list and set our boolean to indicate we found some.
		    if (tempPatientList) {
			foundPatients = true
			patientNumList << tempPatientList
		    }
		}
	    }

	    //If we didn't find any patients, send a message to the output stream.
	    if (!foundPatients) {
		result.put('error', 'No subject was selected')
		response.outputStream << result.toString()
		return
	    }

	    //Parse the gene list passed to this action to get the actual list of genes.
	    List<Long> geneSearchIdList = []
	    List<String> geneNameList = []
	    if (genes) {
		getGeneSearchIdListFromRequest genes, geneAndIdList, geneSearchIdList, geneNameList
	    }

	    List<String> snpNameList = snps ? snps.split(',')*.trim() : null

	    // whether to retrieve SNP data by patient or by probe.
	    boolean isByPatient = !geneSearchIdList && !snpNameList

	    // the files to write the SNP data to.
	    SnpViewerFiles snpFiles = new SnpViewerFiles()

	    //A page is used to display the items the user selected? I think?
	    // This buffer holds a page, it gets filled in during a helper method call within the SNP service.
	    StringBuilder geneSnpPageBuf = new StringBuilder()

	    try {
		if (isByPatient) {
		    snpService.getSNPViewerDataByPatient(patientNumList as List<Long>[], chroms, snpFiles)
		}
		else {
		    snpService.getSNPViewerDataByProbe(patientNumList as List<Long>[], geneSearchIdList, geneNameList, snpNameList, snpFiles, geneSnpPageBuf)
		}
	    }
	    catch (Exception e) {
		result.put('error', e.message)
		return
	    }

	    JobResult[] jresult

	    try {
		jresult = genePatternService.snpViewer(snpFiles.getDataFile(), snpFiles.getSampleFile())
	    }
	    catch (WebServiceException e) {
		result.put('error', 'WebServiceException: ' + e.message)
		return
	    }

	    String viewerURL
	    try {
		result.put('jobNumber', jresult[1].getJobNumber())
		viewerURL = genePatternUrl +
		    '/gp/jobResults/' +
		    jresult[1].getJobNumber() +
		    '?openVisualizers=true'
		logger.debug 'URL for viewer: {}', viewerURL
		result.put('viewerURL', viewerURL)

		result.put('snpGeneAnnotationPage', geneSnpPageBuf.toString())

		logger.debug 'result: {}', result
	    }
	    catch (JSONException e) {
		logger.error 'JSON Exception: {}', e.message
		result.put('error', 'JSON Exception: ' + e.message)
	    }
	}
	finally {
	    wfstatus.result = result
	    wfstatus.setCompleted()
	}
    }

    def showIgv(String result_instance_id1, String result_instance_id2, String chroms, String genes,
	        String geneAndIdList, String snps) {
	JSONObject result = new JSONObject()

	WorkflowStatus wfstatus = initWorkflowStatus('IGV')

	try {
	    logger.debug 'Received IGV rendering request: {}', request

	    logger.debug '\tresult_instance_id1: {}', result_instance_id1
	    logger.debug '\tresult_instance_id2: {}', result_instance_id2

	    chroms = chroms ?: 'ALL'

	    if (!hasValue(result_instance_id1)) {
		logger.debug '\tresult_instance_id1 == undefined or null'
		result_instance_id1 = null
	    }
	    if (!hasValue(result_instance_id2)) {
		logger.debug '\tresult_instance_id2 == undefined or null'
		result_instance_id2 = null
	    }

	    accessLogService.report 'DatasetExplorer-ShowIgv',
		'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2

	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	    String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)

	    if (!subjectIds1 && !subjectIds2) {
		result.put('error', 'No subject was selected')
		return
	    }

	    int snpDatasetNum1 = 0
	    int snpDatasetNum2 = 0
	    if (result_instance_id1?.trim()) {
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1)
		if (idList != null) {
		    snpDatasetNum1 = idList.size()
		}
	    }
	    if (result_instance_id2?.trim()) {
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2)
		if (idList != null) {
		    snpDatasetNum2 = idList.size()
		}
	    }
	    if (snpDatasetNum1 == 0 && snpDatasetNum2 == 0) {
		result.put('error', 'No SNP dataset was selected')
		return
	    }

	    List<Long> geneSearchIdList = []
	    List<String> geneNameList = []
	    if (genes) {
		getGeneSearchIdListFromRequest genes, geneAndIdList, geneSearchIdList, geneNameList
	    }
	    List<String> snpNameList = null
	    if (snps) {
		snpNameList = []
		String[] snpNameArray = snps.split(',')
		for (String snpName : snpNameArray) {
		    snpNameList.add(snpName.trim())
		}
	    }

	    boolean isByPatient = !geneSearchIdList && !snpNameList

	    String newIGVLink = createLink(controller: 'analysis', action: 'getGenePatternFile', absolute: true)

	    IgvFiles igvFiles = new IgvFiles(getGenePatternFileDirName(), newIGVLink)
	    StringBuilder geneSnpPageBuf = new StringBuilder()
	    try {
		if (isByPatient) {
		    igvService.getIgvDataByPatient(subjectIds1, subjectIds2, chroms, igvFiles)
		}
		else {
		    igvService.getIgvDataByProbe(subjectIds1, subjectIds2, geneSearchIdList, geneNameList, snpNameList, igvFiles, geneSnpPageBuf)
		}
	    }
	    catch (Exception e) {
		logger.error e.message, e
		result.put('error', e.message)
		return
	    }

	    JobResult[] jresult

	    try {
		jresult = genePatternService.igvViewer(igvFiles, null, null,
						       securityService.currentUsername())
	    }
	    catch (WebServiceException e) {
		logger.error e.message, e
		result.put('error', 'WebServiceException: ' + e.message)
		return
	    }

	    String viewerURL
	    String altviewerURL

	    try {
		result.put('jobNumber', jresult[1].getJobNumber())
		viewerURL = genePatternUrl +
		    '/gp/jobResults/' +
		    jresult[1].getJobNumber() +
		    '?openVisualizers=true'
		logger.debug 'URL for viewer: {}', viewerURL
		result.put('viewerURL', viewerURL)

		result.put('snpGeneAnnotationPage', geneSnpPageBuf.toString())

		logger.debug 'result: {}', result
	    }
	    catch (JSONException e) {
		logger.error 'JSON Exception: {}', e.message
		result.put('error', 'JSON Exception: ' + e.message)
	    }
	}
	finally {
	    wfstatus.result = result
	    wfstatus.setCompleted()
	}
    }

    def showIgvSample(String genes, String geneAndIdList, String snps, String chroms) {
	JSONObject result = new JSONObject()

	WorkflowStatus wfstatus = initWorkflowStatus('IGV')

	try {
	    logger.debug 'Received IGV rendering request: {}', request

	    //The JSON we received should be [1:[category:[]]]
	    def subsetListJSON = request.JSON.SearchJSON

	    //We need to get an ID list per subset. Build the subset from the JSON Data.
	    def subsetList = solrService.buildSubsetList(subsetListJSON)

	    chroms = chroms ?: 'ALL'

//			accessLogService.report 'DatasetExplorer-ShowIgv',
//					'RID1:' + resultInstanceID1 + ' RID2:' + resultInstanceID2

	    //Gather subjects from Sample IDs.
	    //Form a list of lists of longs. We will convert the outside list to an array in a later method.
	    List<List<Long>> patientNumList

	    //We will use this to determine if our queries return ANY patients.
	    boolean foundPatients = false
	    boolean foundSNPData = false

	    //For each subset get a list of subjects.
	    for (subsetItem in subsetList) {

		def subsetSampleList = subsetItem.value

		//Don't add a subset if there are no items in the subset.
		if (subsetSampleList) {
		    //Add the list to the list of lists.
		    List<Long> tempPatientList = i2b2HelperService.getSubjectsAsListFromSampleLong(subsetSampleList)

		    //If we found patients, add them to the list and set our boolean to indicate we found some.
		    if (tempPatientList) {
			foundPatients = true
			patientNumList.add(tempPatientList)
		    }

		    //TODO: This needs to be a string.
		    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(tempPatientList)
		    if (idList) {
			foundSNPData = true
		    }
		}
	    }

	    //If we didn't find SNP data, add an error message to the results.
	    if (!foundSNPData) {
		result.put('error', 'No SNP dataset was selected')
		return
	    }

	    //If we didn't find any patients, send a message to the output stream.
	    if (!foundPatients) {
		result.put('error', 'No subject was selected')
		response.outputStream << result.toString()
		return
	    }

	    List<Long> geneSearchIdList = []
	    List<String> geneNameList = []
	    if (genes) {
		getGeneSearchIdListFromRequest(genes, geneAndIdList, geneSearchIdList, geneNameList)
	    }
	    List<String> snpNameList = null
	    if (snps) {
		snpNameList = snps.split(',')*.trim()
	    }

	    boolean isByPatient = !geneSearchIdList && !snpNameList

	    String newIGVLink = createLink(controller: 'analysis', action: 'getGenePatternFile', absolute: true)

	    IgvFiles igvFiles = new IgvFiles(getGenePatternFileDirName(), newIGVLink)
	    StringBuilder geneSnpPageBuf = new StringBuilder()
	    try {
		if (isByPatient) {
		    igvService.getIgvDataByPatientSample(patientNumList as List<Long>[], chroms, igvFiles)
		}
		else {
		    igvService.getIgvDataByProbeSample(patientNumList as List<Long>[], geneSearchIdList, geneNameList, snpNameList, igvFiles, geneSnpPageBuf)
		}
	    }
	    catch (Exception e) {
		result.put('error', e.message)
		return
	    }

	    JobResult[] jresult

	    try {
		jresult = genePatternService.igvViewer(igvFiles, null, null,
						       securityService.currentUsername())
	    }
	    catch (WebServiceException e) {
		result.put('error', 'WebServiceException: ' + e.message)
		return
	    }

	    String viewerURL
	    String altviewerURL

	    try {
		result.put('jobNumber', jresult[1].getJobNumber())
		viewerURL = genePatternUrl +
		    '/gp/jobResults/' +
		    jresult[1].getJobNumber() +
		    '?openVisualizers=true'
		logger.debug 'URL for viewer: {}', viewerURL
		result.put('viewerURL', viewerURL)

		result.put('snpGeneAnnotationPage', geneSnpPageBuf.toString())

		logger.debug 'result: {}', result
	    }
	    catch (JSONException e) {
		logger.error 'JSON Exception: {}', e.message
		result.put('error', 'JSON Exception: ' + e.message)
	    }
	}
	finally {
	    wfstatus.result = result
	    wfstatus.setCompleted()
	}
    }

    def showPlink(String result_instance_id1, String result_instance_id2, String chroms) {
	JSONObject result = new JSONObject()

	WorkflowStatus wfstatus = initWorkflowStatus('PLINK')

	try {
	    logger.debug 'Received PLINK rendering request: {}', request

	    logger.debug '\tresult_instance_id1: {}', result_instance_id1
	    logger.debug '\tresult_instance_id2: {}', result_instance_id2

	    chroms = chroms ?: 'ALL'

	    List<String> conceptCodeList1
	    if (!hasValue(result_instance_id1)) {
		logger.debug '\tresult_instance_id2 == undefined or null'
		result_instance_id1 = null
	    }
	    else {
		conceptCodeList1 = i2b2HelperService.getConceptsAsList(result_instance_id1)
	    }

	    List<String> conceptCodeList2
	    if (!hasValue(result_instance_id2)) {
		logger.debug '\tresult_instance_id2 == undefined or null'
		result_instance_id2 = null
	    }
	    else {
		conceptCodeList2 = i2b2HelperService.getConceptsAsList(result_instance_id2)
	    }

	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	    String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)

	    accessLogService.report 'DatasetExplorer-ShowPlink',
		'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2

	    if (!subjectIds1 || !subjectIds2) {
		result.put('error', 'No subject was selected')
		return
	    }

	    PlinkFiles plinkFiles = new PlinkFiles()

	    plinkService.getMapDataByChromosome(subjectIds1, chroms, plinkFiles.mapFile)

	    // set the subset1 as 'unaffected' or 'control' and the subset2 as 'affected' or 'case'
	    plinkService.getSnpDataBySujectChromosome(subjectIds1, chroms, plinkFiles.pedFile, conceptCodeList1, '1')
	    plinkService.getSnpDataBySujectChromosome(subjectIds2, chroms, plinkFiles.pedFile, conceptCodeList2, '2')

	    String viewerUrl

	    try {
		logger.debug 'URL for viewer: {}', viewerUrl
		result.put('viewerURL', viewerUrl)
//		result.put('snpGeneAnnotationPage', geneSnpPageBuf.toString())
		logger.debug 'result: {}', result
	    }
	    catch (JSONException e) {
		logger.error 'JSON Exception: {}', e.message
		result.put('error', 'JSON Exception: ' + e.message)
	    }
	}
	finally {
	    wfstatus.result = result
	    wfstatus.setCompleted()
	}
    }

    //This function parses the ','-separated gene strings like 'Gene>MET', and returns a list of gene search IDs and a list of matching gene names.
    void getGeneSearchIdListFromRequest(String genes, String geneAndIdListStr, List<Long> geneSearchIdList, List<String> geneNameList) {
	if (!genes || !geneAndIdListStr || !geneSearchIdList || !geneNameList) {
	    return
	}

	Map<String, Long> geneIdMap = [:]
	String[] geneAndIdList = geneAndIdListStr.split('\\|\\|\\|')
	for (String geneAndIdStr : geneAndIdList) {
	    String[] geneIdPair = geneAndIdStr.split('\\|\\|')
	    geneIdMap[geneIdPair[0].trim()] = Long.valueOf(geneIdPair[1].trim())
	}
	String[] geneValues = genes.split(',')
	for (String geneStr : geneValues) {
	    geneStr = geneStr.trim()
	    Long geneId = geneIdMap.get(geneStr.trim())
	    geneSearchIdList.add(geneId)
	    if (geneStr.startsWith(geneInputPrefix)) {
		geneStr = geneStr.substring(geneInputPrefix.length())
	    }
	    geneNameList.add(geneStr.trim())
	}
    }

    def showHaploviewGeneSelector(String result_instance_id1, String result_instance_id2) {
	Set<String> combined = []
	if (result_instance_id1) {
	    combined.addAll i2b2HelperService.getGenesForHaploviewFromResultInstanceId(result_instance_id1)
	}
	if (result_instance_id2) {
	    combined.addAll i2b2HelperService.getGenesForHaploviewFromResultInstanceId(result_instance_id2)
	}
	render template: 'haploviewGeneSelector', model: [genes: combined.sort()]
    }

    //Use the search JSON to get the list of samples. Find the Genes associated with those samples.
    def showHaploviewGeneSelectorSample() {

	//We need to first retrieve the list of Sample ID's for the dataset we have selected.

	//Get the list of Sample ID's based on the criteria in the JSON object.
	//We need to get an ID list per subset. The JSON we received should be [1:[category:[]]]
	def subsetList = request.JSON.SearchJSON

	//Build the subset from the JSON Data.
	def result = solrService.buildSubsetList(solrUrl, solrMaxRows, subsetList)

	render template: 'haploviewGeneSelector', model: [
	    genes: analysisService.getGenesForHaploviewFromSampleId(result)]
    }

    def showSNPViewerSelection(String result_instance_id1, String result_instance_id2) {
	int snpDatasetNum1 = 0
	if (result_instance_id1) {
	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1)
	    if (idList) {
		snpDatasetNum1 = idList.size()
	    }
	}

	int snpDatasetNum2 = 0
	if (result_instance_id2) {
	    String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)
	    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2)
	    if (idList) {
		snpDatasetNum2 = idList.size()
	    }
	}

	String warningMsg = null
	if (snpDatasetNum1 + snpDatasetNum2 > 10) {
	    warningMsg = 'Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.'
	}
	
	[chroms: chroms, snpDatasetNum_1: snpDatasetNum1, snpDatasetNum_2: snpDatasetNum2, warningMsg: warningMsg, chromDefault: 'ALL']
    }

    //Get the data for the display elements in the SNP selection window.
    def showSNPViewerSelectionSample() {

	//We need to first retrieve the list of Sample ID's for the dataset we have selected.

	//Get the list of Sample ID's based on the criteria in the JSON object.
	//We need to get an ID list per subset. The JSON we received should be [1:[category:[]]]
	def subsetList = request.JSON.SearchJSON

	//Build the subset from the JSON Data.
	def result = solrService.buildSubsetList(solrUrl, solrMaxRows, subsetList)

	//We need to show the users a count of how many datasets exist for each subset. As we gather the lists, stash the count in a map.
	def datasetCount = [:]

	//Keep track of the total number of datasets so we can warn the user if > 10 datasets are available.
	int datasetCounter = 0

	//For each subset we need to get a list of the Dataset Ids.
	for (subsetItem in result) {

	    def subsetSampleList = subsetItem.value

	    //Verify we have samples in this subset.
	    if (subsetSampleList) {

		//Get the list of subjects from the subject sample mapping table based on Sample_ID
		String subjectIds = i2b2HelperServer.getSubjectsAsListFromSample(subsetItem.value)

		//Use the list of subjects to get the dataset ID List.
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds)

		//Make sure we retrieved Data Set Ids.
		if (idList != null) {
		    //Put the count of dataset items in the map.
		    datasetCount[subseyItem.key] = idList.size()

		    //Add the number of datasets to our total counter.
		    datasetCounter += idList.size()
		}
	    }
	}

	//Warn the user if there are over 10 SNP Datasets selected.
	String warningMsg = null
	if (datasetCounter > 10) {
	    warningMsg = 'Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.'
	}

	//Render the showSNPViewerSelectionSample template.
	[chroms: chroms, snpDatasets: datasetCount, warningMsg: warningMsg, chromDefault: 'ALL']
    }

    def showIgvSelection(String result_instance_id1, String result_instance_id2) {
	int snpDatasetNum1 = 0
	if (result_instance_id1?.trim()) {
	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1)
	    if (idList != null) {
		snpDatasetNum1 = idList.size()
	    }
	}

	int snpDatasetNum2 = 0
	if (result_instance_id2?.trim()) {
	    String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)
	    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2)
	    if (idList != null) {
		snpDatasetNum2 = idList.size()
	    }
	}

	String warningMsg = null
	if (snpDatasetNum1 + snpDatasetNum2 > 10) {
	    warningMsg = 'Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.'
	}

	[chroms: chroms, snpDatasetNum_1: snpDatasetNum1, snpDatasetNum_2: snpDatasetNum2, warningMsg: warningMsg, chromDefault: 'ALL']
    }

    def showPlinkSelection(String result_instance_id1, String result_instance_id2) {
	int snpDatasetNum1 = 0
	int snpDatasetNum2 = 0
	if (result_instance_id1?.trim()) {
	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1)
	    if (idList != null) {
		snpDatasetNum1 = idList.size()
	    }
	}
	if (result_instance_id2?.trim()) {
	    String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)
	    List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2)
	    if (idList != null) {
		snpDatasetNum2 = idList.size()
	    }
	}

	String warningMsg = null
	if (snpDatasetNum1 + snpDatasetNum2 > 10) {
	    warningMsg = 'Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.'
	}

	[chroms         : chroms,
	 snpDatasetNum_1: snpDatasetNum1,
	 snpDatasetNum_2: snpDatasetNum2,
	 warningMsg     : warningMsg,
	 chromDefault   : 'ALL']
    }

    //* Obtain the pathway for biomarker comparison when using the heatmap in dataset explorer.
    def ajaxGetPathwaySearchBoxData(String query, String callback) {
	logger.info 'Obtaining pathways for {}', query

	List<Map> pathways = []

	Sql sql = new Sql(dataSource)
	String sqlt = '''
		SELECT * FROM (
			select * from DEAPP.de_pathway
			where upper(name) like upper(?) ORDER BY LENGTH(name)
		)
		WHERE ROWNUM<=40'''

	sql.eachRow(sqlt, [query + '%'], { row ->
	    pathways << [name: row.name, type: row.type, source: row.source, uid: row.pathway_uid]
	})

	render callback + '(' + ([rows: pathways] as JSON) + ')'
    }

    def gplogin() {
	if (enableGenePattern) {
	    [userName: securityService.currentUsername()]
	}
	else {
	    render view: 'nogp'
	}
    }

    protected String getGenePatternFileDirName() {
	String webRootName = servletContext.getRealPath('/')
	if (!webRootName.endsWith(File.separator)) {
	    webRootName += File.separatorChar
	}
	webRootName + genePatternFileDir
    }

    private WorkflowStatus initWorkflowStatus(String job) {
	WorkflowStatus wfstatus = new WorkflowStatus()
	wfstatus.setCurrentJobStatus new JobStatus(name: 'Initializing Workflow', status: 'C')
	wfstatus.setCurrentJobStatus new JobStatus(name: 'Retrieving Data', status: 'R')
	wfstatus.addNewJob 'ConvertLineEndings'
	wfstatus.addNewJob job
	session.workflowstatus = wfstatus
	wfstatus
    }

    private boolean hasValue(String s) {
	s && s != 'undefined' && s != 'null'
    }
}
