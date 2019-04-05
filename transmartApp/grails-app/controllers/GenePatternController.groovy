import com.rdc.snp.haploview.PEDFormat
import com.recomdata.asynchronous.GenePatternService
import com.recomdata.asynchronous.JobResultsService
import com.recomdata.export.GenePatternFiles
import com.recomdata.export.GwasFiles
import com.recomdata.export.SurvivalAnalysisFiles
import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import com.recomdata.transmart.asynchronous.job.AsyncJobService
import grails.converters.JSON
import grails.gsp.PageRenderer
import groovy.util.logging.Slf4j
import org.hibernate.SessionFactory
import org.json.JSONObject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.beans.factory.annotation.Value
import org.transmart.CohortInformation
import org.transmart.ExperimentData
import org.transmart.HeatmapValidator
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.SearchKeyword
import org.transmartproject.db.log.AccessLogService

import javax.sql.DataSource
import java.sql.Connection

@Slf4j('logger')
class GenePatternController {

    private static final String GENE_PATTERN_WHITE_SPACE_DEFAULT = '0'
    private static final String GENE_PATTERN_WHITE_SPACE_EMPTY = ''
    private static final String TEMP_DIR = System.getProperty('java.io.tmpdir')
    private static final List<String> chroms = ['ALL', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13',
	                                        '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y'].asImmutable()

    AccessLogService accessLogService
    AsyncJobService asyncJobService
    DataSource dataSource
    PageRenderer groovyPageRenderer
    I2b2HelperService i2b2HelperService
    JobResultsService jobResultsService
    SampleInfoService sampleInfoService
    Scheduler quartzScheduler
    SecurityService securityService
    SessionFactory sessionFactory

    @Value('${com.recomdata.analysis.genepattern.file.dir:}')
    private String genePatternFileDir

    @Value('${com.recomdata.search.genepathway:}')
    private String genepathway

    /**
     * Called asynchronously from the datasetExplorer Javascript.
     * Determines the type of heatmap to run and then kicks off the heatmap job in genePatternService.
     */
    def runheatmap(String analysis, String rbmPanels1, String rbmPanels2, String sample1, String sample2,
	           String result_instance_id1, String result_instance_id2, String nclusters,
	           String timepoints1, String timepoints2, String resulttype, String datatype,
	           String jobName, String pathway_name) {
	logParams()

        //On the DataSet explorer side we build an i2b2 Query that we later reference the results by this result instance id.
	result_instance_id1 = nullCheck(result_instance_id1)
	result_instance_id2 = nullCheck(result_instance_id2)

	// resulttype may be deprecated but we will leave it in in case we want to support different result types in the future.
	// There used to be an 'Image' result type.

	// datatype tells us the mRNA platform or if the data type is RBM.

        //Build the status list that we display to the user as the job processes.
        // TODO: Stick this in JobResultsService as an enum
	List<String> statusList = ['Validating Parameters', 'Obtaining Query Definitions',
				   'Obtaining subject IDs', 'Obtaining concepts', 'Obtaining heatmap data',
				   'Triggering GenePattern job']
        if (analysis == 'Compare') {
	    statusList << 'Uploading file'
	    statusList << 'Running Heatmap Viewer'
        }
        else if (analysis == 'Cluster') {
	    statusList << 'Performing Hierarchical Clustering'
	    statusList << 'Running Hierarchical Clustering Viewer'
        }
        else if (analysis == 'KMeans') {
	    statusList << 'Performing KMeans Clustering'
	    statusList << 'Running KMeans Clustering Viewer'
        }
        else if (analysis == 'Select') {
	    statusList << 'Imputing Missing Value KNN'
	    statusList << 'Performing Comparative Marker Selection'
	    statusList << 'Extracting Comparative Marker Results'
	    statusList << 'Running Heatmap Viewer'
	    statusList << 'Running Comparative Marker Selection Viewer'
        }
        else if (analysis == 'PCA') {
	    statusList << 'Uploading file'
	    statusList << 'Running PCA'
	    statusList << 'Running PCA Viewer'
        }

        //Update our job object to have the list relevant to the job we are running.
        jobResultsService[jobName]['StatusList'] = statusList

        //This updates the status and checks to see if the job has been cancelled.
        if (asyncJobService.updateStatus(jobName, statusList[0])) {
            return
        }

        //Create the object which represents the gene pattern files we use to run the job.
        GenePatternFiles gpf = new GenePatternFiles()

	accessLogService.report "Heatmap Analysis: $analysis, Job: $jobName",
	    "result_instance_id1: $result_instance_id1, result_instance_id2: $result_instance_id2"

	logger.info 'Pathway Name set to {}', pathway_name

        //We once again validate to make sure two subsets were selected when we run a Comparative Marker Analysis.
	logger.debug 'Ensuring at least two subsets for comparative marker selection...'
	if (analysis == 'Select' && (!result_instance_id1 || !result_instance_id2)) {
	    String error = 'Comparative marker selection requires two subsets'
            jobResultsService[jobName]['Status'] = 'Error'
            jobResultsService[jobName]['Exception'] = error
	    logger.error error
            return
        }

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[1])) {
            return
        }

        StringWriter def1 = new StringWriter()
        StringWriter def2 = new StringWriter()

	i2b2HelperService.renderQueryDefinition(result_instance_id1, 'Subset1', def1)
	i2b2HelperService.renderQueryDefinition(result_instance_id2, 'Subset2', def2)

	logger.debug 'def1: {}', def1
	logger.debug 'def2: {}', def2

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[2])) {
            return
        }

	String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
	String subjectIds2 = i2b2HelperService.getSubjects(result_instance_id2)

	logger.debug 'subjectIds1: {}', subjectIds1
	logger.debug 'subjectIds2: {}', subjectIds2

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[3])) {
            return
        }

	String concepts1 = i2b2HelperService.getConcepts(result_instance_id1)
	String concepts2 = i2b2HelperService.getConcepts(result_instance_id2)

	logger.debug 'concepts1: {}', concepts1
	logger.debug 'concepts2: {}', concepts2

        //If we are doing a Heatmap we need to address a '*' in the subject heading.
        boolean fixlast = analysis == 'Compare'
        //We use the raw microarray data for the Comparative Marker Selection, otherwise we use LOG2.
        boolean rawdata = analysis == 'Select'

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[4])) {
            return
        }
        try {
	    i2b2HelperService.getHeatMapData pathway_name, subjectIds1, subjectIds2,
                concepts1, concepts2, timepoints1, timepoints2, sample1, sample2,
		rbmPanels1, rbmPanels2, datatype, gpf, fixlast, rawdata, analysis
	    String expfilename = TEMP_DIR + File.separator + 'datasetexplorer' + File.separator + gpf.CSVFileName
            session.expdsfilename = expfilename
	    logger.info 'Filename for export has been set to {}', expfilename
        }
	catch (e) {
	    handleException e
        }

	logger.debug 'Checking to see if the user cancelled the job prior to running it'
        if (jobResultsService[jobName]['Status'] == 'Cancelled') {
	    logger.warn '{} has been cancelled', jobName
            return
        }

        if (asyncJobService.updateStatus(jobName, statusList[5])) {
            return
        }

	String group = 'heatmaps'
	JobDetail jobDetail = new JobDetailImpl(jobName, group, GenePatternService)
	jobDetail.jobDataMap = new JobDataMap(
	    analysis: analysis,
	    gctFile: gpf.gctFile,
	    clsFile: gpf.clsFile,
	    resulttype: resulttype,
	    nclusters: nclusters,
	    userName: securityService.currentUsername())
	quartzScheduler.scheduleJob jobDetail, new SimpleTriggerImpl('triggerNow', group)

	render([jobName: jobName] as JSON)
    }

    /**
     * Method that is called asynchronously from the datasetExplorer Javascript
     * Will determine the type of heatmap to run and then kick off the heatmap job in genePatternService
     */
    def runheatmapsample(String sampleIdList, String analysis, String datatype, String pathway_name,
	                 String nclusters, String resulttype, String jobName) {

	//The sampleIdList will look like {"SampleIdList":{"subset1":["Sample1"],"subset2":[],"subset3":[]}}
        def sampleIdListJSON = JSON.parse(sampleIdList)

        // TODO: Put switch in based on analysis
	List<String> statusList = ['Validating Parameters', 'Obtaining heatmap data',
		                   'Writing GenePattern files', 'Triggering GenePattern job']

        if (analysis == 'Compare') {
	    statusList << 'Uploading file'
	    statusList << 'Running Heatmap Viewer'
        }
        else if (analysis == 'Cluster') {
	    statusList << 'Performing Hierarchical Clustering'
	    statusList << 'Running Hierarchical Clustering Viewer'
        }
        else if (analysis == 'KMeans') {
	    statusList << 'Performing KMeans Clustering'
	    statusList << 'Running KMeans Clustering Viewer'
        }
        else if (analysis == 'Select') {
	    statusList << 'Imputing Missing Value KNN'
	    statusList << 'Performing Comparative Marker Selection'
	    statusList << 'Extracting Comparative Marker Results'
	    statusList << 'Running Heatmap Viewer'
	    statusList << 'Running Comparative Marker Selection Viewer'
        }
        else if (analysis == 'PCA') {
	    statusList << 'Uploading file'
	    statusList << 'Running PCA'
	    statusList << 'Running PCA Viewer'
        }

        jobResultsService[jobName]['StatusList'] = statusList

        //This updates the status and checks to see if the job has been canceled.
        if (asyncJobService.updateStatus(jobName, statusList[0])) {
            return
        }

        //Set a flag based on the type of analysis we are doing.
        boolean fixlast = analysis == 'Compare'
        boolean rawdata = analysis == 'Select'

        //We need to convert from the Search_Keyword_id to the gene name.
        pathway_name = derivePathwayName(analysis, pathway_name)

        //For most cases, GenePattern server cannot accept gct file with empty expression ratio.
        //Use 0 rather than empty cell. However, Comparative Marker Select needs to use empty space
        String whiteString = GENE_PATTERN_WHITE_SPACE_DEFAULT
	if (analysis == 'Select') {
	    whiteString = GENE_PATTERN_WHITE_SPACE_EMPTY
	}

        //Create the gene patterns file object we use to pass to the gene pattern server.
        GenePatternFiles gpf = new GenePatternFiles()

        //This is the object we use to build the GenePatternFiles.
	ExperimentData experimentData = new ExperimentData(dataSource, sessionFactory, sampleInfoService)
        experimentData.gpf = gpf
        experimentData.dataType = datatype
        experimentData.analysisType = analysis
        experimentData.sampleIdList = sampleIdListJSON
        experimentData.whiteString = whiteString
        experimentData.fixlast = fixlast
        experimentData.rawdata = rawdata
        experimentData.pathwayName = pathway_name

        if (asyncJobService.updateStatus(jobName, statusList[1])) {
            return
        }

        experimentData.getHeatMapDataSample()

        if (asyncJobService.updateStatus(jobName, statusList[2])) {
            return
        }

        experimentData.writeGpFiles()

        //Verify user has not cancelled job.
	logger.debug 'Checking to see if the user cancelled the job prior to running it'
	if (jobResultsService[jobName + ':Status'] == 'Cancelled') {
	    logger.warn '{} has been cancelled', jobName
            return
        }

        if (asyncJobService.updateStatus(jobName, statusList[3])) {
            return
        }

	String group = 'heatmaps'
	JobDetail jobDetail = new JobDetailImpl(jobName, group, GenePatternService)
	jobDetail.jobDataMap = new JobDataMap(
	    analysis: analysis,
	    gctFile: experimentData.gpf.gctFile,
	    clsFile: gpf.clsFile,
	    resulttype: resulttype,
	    userName: securityService.currentUsername(),
	    nclusters: nclusters)
        quartzScheduler.scheduleJob jobDetail, new SimpleTriggerImpl('triggerNow', group)

	render([jobName: jobName] as JSON)
    }

    /**
     * Runs a survival analysis; called asynchronously from the datasetexplorer.
     */
    def runsurvivalanalysis(String result_instance_id1, String result_instance_id2,
	                    String querySummary1, String querySummary2, String jobName) {
	logParams()

	result_instance_id1 = nullCheck(result_instance_id1)
	result_instance_id2 = nullCheck(result_instance_id2)

	List<String> statusList = ['Validating Parameters', 'Obtaining Cohort Information',
				   'Obtaining Survival Analysis data', 'Triggering GenePattern job',
				   'Running Cox Regression', 'Calculating Survival Curve']

        jobResultsService[jobName]['StatusList'] = statusList

        asyncJobService.updateStatus(jobName, statusList[0])

	accessLogService.report "Survival Analysis, Job: $jobName",
	    "result_instance_id1: $result_instance_id1, result_instance_id2: $result_instance_id2"

	asyncJobService.updateStatus jobName, statusList[1]

        List<String> subjectIds1
        List<String> concepts1
	HeatmapValidator hv1 = new HeatmapValidator()
	CohortInformation ci1 = new CohortInformation()
	if (result_instance_id1) {
	    subjectIds1 = i2b2HelperService.getSubjectsAsList(result_instance_id1)
	    concepts1 = i2b2HelperService.getConceptsAsList(result_instance_id1)
            i2b2HelperService.fillHeatmapValidator(subjectIds1, concepts1, hv1)
            i2b2HelperService.fillCohortInformation(subjectIds1, concepts1, ci1, CohortInformation.TRIALS_TYPE)
        }

	List<String> subjectIds2
	List<String> concepts2
	HeatmapValidator hv2 = new HeatmapValidator()
	CohortInformation ci2 = new CohortInformation()
	if (result_instance_id2) {
	    subjectIds2 = i2b2HelperService.getSubjectsAsList(result_instance_id2)
	    concepts2 = i2b2HelperService.getConceptsAsList(result_instance_id2)
            i2b2HelperService.fillHeatmapValidator(subjectIds2, concepts2, hv2)
            i2b2HelperService.fillCohortInformation(subjectIds2, concepts2, ci2, CohortInformation.TRIALS_TYPE)
        }

	asyncJobService.updateStatus jobName, statusList[2]

	SurvivalAnalysisFiles saFiles = new SurvivalAnalysisFiles()
        try {
	    i2b2HelperService.getSurvivalAnalysisData concepts1, concepts2, subjectIds1, subjectIds2, saFiles
        }
	catch (e) {
	    handleException e
            return
        }

	logger.debug 'Checking to see if the user cancelled the job prior to running it'
        if (jobResultsService[jobName]['Status'] == 'Cancelled') {
	    logger.warn '{} has been cancelled', jobName
            return
        }

        asyncJobService.updateStatus(jobName, statusList[3])

	String imgTmpDir = '/images/datasetExplorer'
	String group = 'heatmaps'
	JobDetail jobDetail = new JobDetailImpl(jobName, group, GenePatternService)
	jobDetail.jobDataMap = new JobDataMap(
	    analysis: 'Survival',
	    gctFile: saFiles.dataFile,
	    clsFile: saFiles.clsFile,
	    imgTmpDir: imgTmpDir,
	    imgTmpPath: servletContext.getRealPath(imgTmpDir),
	    ctxtPath: servletContext.contextPath,
	    querySum1: querySummary1,
	    querySum2: querySummary2,
	    userName: securityService.currentUsername())
        quartzScheduler.scheduleJob jobDetail, new SimpleTriggerImpl('triggerNow', group)

	render([jobName: jobName] as JSON)
    }

    /**
     * Called asynchronously from the datasetExplorer Javascript.
     * Will run the haploviewer but does not use Quartz due to the need for the database connection
     */
    def runhaploviewer(String result_instance_id1, String result_instance_id2, String genes, String jobName) {
	logParams()

	result_instance_id1 = nullCheck(result_instance_id1)
	result_instance_id2 = nullCheck(result_instance_id2)

	List<String> statusList = ['Validating Parameters']

	int statusIndex = 1

	if (result_instance_id1) {
	    statusList << 'Creating haploview for subset 1'
        }

	if (result_instance_id2) {
	    statusList << 'Creating haploview for subset 2'
        }

        jobResultsService[jobName]['StatusList'] = statusList

        asyncJobService.updateStatus(jobName, statusList[0])

	accessLogService.report "Haploview Job: $jobName",
	    "result_instance_id1: $result_instance_id1, result_instance_id2: $result_instance_id2, Genes: $genes"

        StringBuilder sb = new StringBuilder()
	sb << "<a  href=\"javascript:showInfo('help/happloview.html');\"><img src=\"${resource(dir: 'images', file: 'information.png')}\"></a>"
	sb << '<b>Genes Selected: ' << genes << '</b>'
	sb << '<table><tr>'

	Connection con
        try {
            con = dataSource.getConnection()
	    if (result_instance_id1) {
                asyncJobService.updateStatus(jobName, statusList[statusIndex])
		sb << createHaploView(result_instance_id1, genes, con)
		statusIndex++
            }
	    if (result_instance_id2) {
                asyncJobService.updateStatus(jobName, statusList[statusIndex])
		sb << createHaploView(result_instance_id2, genes, con)
            }
        }
	catch (e) {
	    handleException e
        }
        finally {
            con?.close()
        }

	sb << '</tr></table>'
        jobResultsService[jobName]['Results'] = sb.toString()
    }

    /**
     * Called asynchronously from the sampleExplorer Javascript.
     * Will run the haploviewer but does not use Quartz due to the need for the database connection
     */
    def runhaploviewersample(String sampleIdList, String genes, String jobName) {
	logParams()

	//The sampleIdList will look like {"SampleIdList":{"subset1":["Sample1"],"subset2":[],"subset3":[]}}
        def sampleIdListJSON = JSON.parse(sampleIdList)

        //Initialize status list.
	List<String> statusList = ['Validating Parameters']

	for (subsetItem in sampleIdList) {

	    List subsetSampleList = subsetItem.value

            //Don't add a subset if there are no items in the subset.
	    if (subsetSampleList) {
		statusList << 'Creating haploview for subset ' + subsetItem.key
            }
        }

        jobResultsService[jobName]['StatusList'] = statusList

        asyncJobService.updateStatus(jobName, statusList[0])

	accessLogService.report "Haploview Job: $jobName",
	    "Sample IDs JSON: $sampleIdListJSON, Genes: $genes"

        //Add some links to the returned page.
        StringBuilder sb = new StringBuilder()
	sb << "<a  href=\"javascript:showInfo('help/happloview.html');\"><img src=\"${resource(dir: 'images', file: 'information.png')}\"></a>"
	sb << '<b>Genes Selected: ' << genes << '</b>'
	sb << '<table><tr>'

	// increment the status as we generate the Halplo data for each set.
	int statusIndex = 1

	Connection con
        try {
            //Create the connection we will use for our SQL statements.
	    con = dataSource.getConnection()

            //Get data for each subset.
	    for (subsetItem in sampleIdList) {

		List subsetSampleList = subsetItem.value

                //Don't use a subset if there are no items in the subset.
		if (subsetSampleList) {
                    //Make a note of which subset we are working on.
                    asyncJobService.updateStatus(jobName, statusList[statusIndex])

                    //Attach data from creating the haploview for our subset.
		    sb << createHaploViewSample(subsetSampleList, genes, con)

                    //Update our status index.
		    statusIndex++
                }
            }
        }
	catch (e) {
	    handleException e
        }
        finally {
            con?.close()
        }

	sb << '</tr></table>'
        jobResultsService[jobName]['Results'] = sb.toString()
    }

    private void callHaploText(List<String> args) {
        try {
            // yes, this class does all the work in the constructor and
            // it's instantiated for the collaterals. Don't believe it? See
            // https://github.com/jazzywhit/Haploview/blob/69f7ca282/edu/mit/wi/haploview/HaploText.java#L210
	    Class.forName('edu.mit.wi.haploview.HaploText').newInstance(args as String[])
        }
        catch (ClassNotFoundException e) {
	    logger.error 'Haploview class not found. It is not bundled anymore. You will need to add its jar as a dependency'
        }
    }

    /**
     * Helper method to create the haploview for each result instance ID
     *
     * @param rID - the result instance ID
     * @param genes - the list of genes
     * @param con - the database connection
     */
    private String createHaploView(String rID, String genes, Connection con) {

	String ids = i2b2HelperService.getSubjects(rID)

	String fileroot = TEMP_DIR
        File tempFile = File.createTempFile('haplo', '.tmp', new File(fileroot))

        if (!fileroot.endsWith(File.separator)) {
            fileroot = fileroot + File.separator
        }

	String filenamein = tempFile.name
	String pathin = fileroot + filenamein
	String pathinped = fileroot + filenamein + '.ped'
	String pathininfo = fileroot + filenamein + '.info'

	boolean s1 = new PEDFormat().createPEDFile(genes, ids, pathin, con)

	callHaploText(['-nogui', '-quiet', '-pedfile', pathinped, '-info', pathininfo, '-png'])
        String filename = filenamein + '.ped.LD.PNG'

	String hapleUrl = request.contextPath + '/chart/displayChart?filename=' + filename
        if (s1) {
	    '<td><img src="' + hapleUrl + '" border="0"></td>'
        }
        else {
	    '<td>Not enough data to generate haploview</td>'
        }
    }

    /**
     * Helper method to create the haploview for each subset
     *
     * @param sampleIdList - The list of samples in this subset.
     * @param genes - the list of genes
     * @param con - the database connection
     */
    private String createHaploViewSample(sampleIdList, String genes, Connection con) {

	List<String> ids = i2b2HelperService.getSubjectsAsListFromSample(sampleIdList)

	String fileroot = TEMP_DIR
        File tempFile = File.createTempFile('haplo', '.tmp', new File(fileroot))
	String filenamein = tempFile.name

        if (!fileroot.endsWith(File.separator)) {
            fileroot = fileroot + File.separator
        }

	String pathin = fileroot + filenamein
	String pathinped = fileroot + filenamein + '.ped'
	String pathininfo = fileroot + filenamein + '.info'

	boolean s1 = new PEDFormat().createPEDFile(genes, ids, pathin, con)
        if (s1) {
	    callHaploText(['-nogui', '-quiet', '-pedfile', pathinped, '-info', pathininfo, '-png'])

            String filename = filenamein + '.ped.LD.PNG'
	    String hapleUrl = request.contextPath + '/chart/displayChart?filename=' + filename

	    '<td><img src="' + hapleUrl + '" border="0"></td>'
        }
        else {
	    '<td>Not enough data to generate haploview</td>'
        }
    }

    def showGwasSelection(String result_instance_id1, String result_instance_id2) {

	int snpDatasetNum1 = 0
	int snpDatasetNum2 = 0
	if (result_instance_id1?.trim()) {
	    String subjectIds1 = i2b2HelperService.getSubjects(result_instance_id1)
            List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1)
	    if (idList) {
		snpDatasetNum1 = idList.size()
	    }
        }
	if (result_instance_id2?.trim()) {
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

	[chroms: chroms,
	 snpDatasetNum_1: snpDatasetNum1,
	 snpDatasetNum_2: snpDatasetNum2,
	 warningMsg: warningMsg,
	 chromDefault: 'ALL']
    }

    /**
     * Method that will run a survival analysis and is called asynchronously from the datasetexplorer
     */
    def runGwas(String result_instance_id1, String result_instance_id2, String querySummary1,
	        String querySummary2, String jobName, String chroms) {
	logParams()

	result_instance_id1 = nullCheck(result_instance_id1)
	result_instance_id2 = nullCheck(result_instance_id2)

	List<String> statusList = ['Validating Parameters', 'Obtaining Cohort Information',
		                   'Obtaining SNP data', 'Triggering PLINK job', 'Running PLINK']

        jobResultsService[jobName]['StatusList'] = statusList

	asyncJobService.updateStatus jobName, statusList[0]

	accessLogService.report "Survival Analysis, Job: $jobName",
	    eventMessage: "result_instance_id1: $result_instance_id1, result_instance_id2: $result_instance_id2"

	asyncJobService.updateStatus jobName, statusList[1]

	List<String> subjectIds1
	if (result_instance_id1) {
	    subjectIds1 = i2b2HelperService.getSubjectsAsList(result_instance_id1)
        }

	List<String> subjectIds2
	if (result_instance_id2) {
	    subjectIds2 = i2b2HelperService.getSubjectsAsList(result_instance_id2)
        }

	asyncJobService.updateStatus jobName, statusList[2]

        GwasFiles gwasFiles = new GwasFiles(getGenePatternFileDirName(),
					    createLink(controller: 'analysis', action: 'getGenePatternFile', absolute: true).toString())
        try {
            i2b2HelperService.getGwasDataByPatient(subjectIds1, subjectIds2, chroms, gwasFiles)
        }
	catch (e) {
	    handleException e
            return
        }

	logger.debug 'Checking to see if the user cancelled the job prior to running it'
        if (jobResultsService[jobName]['Status'] == 'Cancelled') {
	    logger.warn '{} has been cancelled', jobName
            return
        }

	asyncJobService.updateStatus jobName, statusList[3]

	String group = 'heatmaps'
	JobDetail jobDetail = new JobDetailImpl(jobName, group, GenePatternService)
	jobDetail.jobDataMap = new JobDataMap(
	    analysis: 'GWAS',
	    gwasFiles: gwasFiles,
	    querySum1: querySummary1,
	    querySum2: querySummary2,
	    userName: securityService.currentUsername())
	quartzScheduler.scheduleJob jobDetail, new SimpleTriggerImpl('triggerNow', group)

	render([jobName: jobName] as JSON)
    }

    protected String getGenePatternFileDirName() {
        String webRootName = servletContext.getRealPath('/')
	if (webRootName.endsWith(File.separator) == false) {
            webRootName += File.separator
	}
	return webRootName + genePatternFileDir
    }

    /**
     * Helper method to return null from Javascript calls
     *
     * @param s - the input arguments
     * @return null or the input argument if it is not null (or empty or undefined)
     */
    private String nullCheck(String s) {
	logger.debug 'Input argument to nullCheck: {}', s
	if (s && s != 'undefined' && s != 'null') {
	    s
	}
	else {
	    logger.debug 'Returning null in nullCheck'
        }
    }

    /**
     * Helper method to derive the pathway name
     *
     * @param analysis the type of analysis to run
     * @param pathwayName the pathway name in the request
     * @return the pathway name, null or the search result
     */
    private String derivePathwayName(String analysis, String pathwayName) {
	logger.info 'Derived pathway name as {}', pathwayName
        if (analysis != 'Select' && analysis != 'PCA') {
	    logger.debug 'Pathway name has been set to {}', pathwayName
	    if (!pathwayName || pathwayName == 'null') {
		logger.debug 'Resetting pathway name to null'
		pathwayName = null
	    }
	    boolean nativeSearch = genepathway == 'native'
	    logger.debug 'nativeSearch: {}', nativeSearch
	    if (!nativeSearch) {
		pathwayName = SearchKeyword.get(pathwayName).uniqueId
		logger.debug 'pathway_name has been set to a keyword ID: {}', pathwayName
            }
        }
	pathwayName
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // These are for the synchronous operation - soon to be replaced
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    def showWorkflowStatus() {
	WorkflowStatus wfstatus = sessionWorkflowstatus()
	if (!wfstatus) {
            wfstatus = new WorkflowStatus()
	    wfstatus.setCurrentJobStatus new JobStatus(name: 'initializing Workflow', status: 'R')
	    session.workflowstatus = wfstatus
        }

	render view: 'workflowStatus'
    }

    def checkWorkflowStatus() {
	WorkflowStatus wfstatus = sessionWorkflowstatus()

	JSONObject result = wfstatus.result ?: new JSONObject()

	String statusHtml = groovyPageRenderer.render(template: 'jobStatus', model: [wfstatus: wfstatus])
	result.put 'statusHTML', statusHtml

        if (wfstatus.isCompleted()) {
	    result.put 'wfstatus', 'completed'
            wfstatus.rpCount++
	    result.put 'rpCount', wfstatus.rpCount
        }
        else {
	    result.put 'wfstatus', 'running'
        }
        render result.toString()
    }

    def cancelJob() {
	WorkflowStatus wfstatus = sessionWorkflowstatus()
        wfstatus.setCancelled()
        render(wfstatus.jobStatusList as JSON)
    }

    private WorkflowStatus sessionWorkflowstatus() {
	session.workflowstatus
    }

    private void logParams() {
	if (logger.debugEnabled) {
	    for (String key in request.parameterMap.keySet()) {
		logger.debug '{} -> {}', key, request.getParameter(key)
	    }
	}
    }

    private void handleException(Exception e) {
	String error = e.message
	logger.error 'Exception: {}', error, e
	jobResultsService[jobName]['Status'] = 'Error'
	jobResultsService[jobName]['Exception'] = error
    }
}
