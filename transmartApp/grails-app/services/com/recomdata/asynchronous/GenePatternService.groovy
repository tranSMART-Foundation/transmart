package com.recomdata.asynchronous

import com.recomdata.export.GwasFiles
import com.recomdata.export.IgvFiles
import com.recomdata.genepattern.JobStatus
import com.sun.pdfview.PDFFile
import com.sun.pdfview.PDFPage
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.genepattern.client.GPClient
import org.genepattern.webservice.JobResult
import org.genepattern.webservice.Parameter
import org.genepattern.webservice.WebServiceException
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.springframework.context.ApplicationContext
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder
import org.transmart.plugin.shared.SecurityService

import javax.imageio.ImageIO
import javax.servlet.http.HttpSession
import java.awt.*
import java.nio.channels.FileChannel
import java.util.List

/**
 * Manages the calls and jobs to the GenePattern server.
 * Implements Job as this will run asynchronously
 *
 * @author mmcduffie
 */
@Slf4j('logger')
class GenePatternService implements Job {

    static scope = 'session'
    static transactional = false

    GPClient gpClient

    // TODO: Figure out why dependency injection is not working as before.  Is the implements Job causing an issue?
    private ApplicationContext ctx = Holders.grailsApplication.mainContext
    private SecurityService securityService = ctx.securityService
    private i2b2HelperService = ctx.i2b2HelperService
    private JobResultsService jobResultsService = ctx.jobResultsService
    private String genePatternUrl = Holders.config.com.recomdata.datasetExplorer.genePatternURL
    private String genePatternRealUrlBehindProxy = Holders.config.com.recomdata.datasetExplorer.genePatternRealURLBehindProxy

    /**
     * Quartz job execute method
     */
    void execute(JobExecutionContext jobExecutionContext) {
	String gpUrl = genePatternUrl + '/gp/jobResults/'

	JobDetail jobDetail = jobExecutionContext.jobDetail
	String jobName = jobDetail.name
	logger.info '{} has been triggered to run', jobName

	JobDataMap jobDataMap = jobDetail.jobDataMap
	if (logger.debugEnabled) {
	    for (String key in jobDataMap.keys) {
		logger.debug '\t{} -> {}', key, jobDataMap[key]
	    }
	}

        if (jobResultsService[jobName]['Status'] == 'Cancelled') {
	    logger.warn '{} has been cancelled', jobName
            return
        }

	// Note: this is a superset of all parameters for all of the different analysis types.
	// Some will be present, others will not depending on the type of job
	String analysis = jobDataMap.analysis
	File gctFile = jobDataMap.gctFile
	File clsFile = jobDataMap.clsFile
	String resulttype = jobDataMap.resulttype
	String userName = jobDataMap.userName
	String nClusters = jobDataMap.nclusters
	String imgTmpDir = jobDataMap.imgTmpDir
	String imgTmpPath = jobDataMap.imgTmpPath
	String ctxtPath = jobDataMap.ctxtPath
	String querySum1 = jobDataMap.querySum1
	String querySum2 = jobDataMap.querySum2
	GwasFiles gwasFiles = jobDataMap.gwasFiles

        JobResult[] jresult
        String sResult
        try {
            if (analysis == 'Compare') {
                jresult = HMap(userName, jobName, gctFile, resulttype)
            }
            else if (analysis == 'Cluster') {
                jresult = HCluster(userName, jobName, gctFile, resulttype)
            }
            else if (analysis == 'KMeans') {
                jresult = kMeansCluster(userName, jobName, gctFile, resulttype, nClusters)
            }
            else if (analysis == 'Select') {
                jresult = CMAnalysis(userName, jobName, gctFile, clsFile, resulttype)
            }
            else if (analysis == 'PCA') {
                jresult = PCA(userName, jobName, gctFile, clsFile, resulttype)
            }
            else if (analysis == 'Survival') {
                sResult = survivalAnalysis(userName, jobName, gctFile, clsFile, imgTmpPath, imgTmpDir, ctxtPath, querySum1, querySum2)
            }
            else if (analysis == 'GWAS') {
                sResult = gwas(userName, jobName, gwasFiles, querySum1, querySum2)
            }
            else {
		logger.error 'Analysis not implemented yet!'
            }
        }
	catch (WebServiceException e) {
	    logger.error 'WebServiceException thrown executing job: {}', e.message, e
	    jobResultsService[jobName]['Exception'] = e.message
            return
        }

	if (analysis == 'Survival') {
	    jobResultsService[jobName]['Results'] =
		'<html><header><title>Survival Analysis</title></header><body>' +
		sResult + '</body></html>'
	}
	else if (analysis == 'GWAS') {
	    jobResultsService[jobName]['Results'] = sResult
        }
        else {
	    String viewerUrl = gpUrl + jresult[1].jobNumber + '?openVisualizers=true'
	    logger.debug 'URL for viewer: {}', viewerUrl
	    jobResultsService[jobName]['ViewerURL'] = viewerUrl

            if (analysis == 'Select') {
		String altviewerUrl = gpUrl + jresult[2].jobNumber + '?openVisualizers=true'
		logger.debug 'URL for second viewer: {}', altviewerUrl
		jobResultsService[jobName]['AltViewerURL'] = altviewerUrl
            }
        }
    }

    /**
     * Update the status of the job and log it.
     *
     * @param jobName - the unique job name
     * @param status - the new status
     */
    void updateStatus(String jobName, String status) {
        jobResultsService[jobName]['Status'] = status
	logger.debug status
    }

    private JobResult runJob(Parameter[] parameters, String analysisType) throws WebServiceException {

	startWorkflowJob analysisType

        GPClient gpClient = getGPClient()

	logger.debug 'sending {} job to {} as user {} with parameters {}',
	    analysisType, gpClient.server, gpClient.username, parameters

	JobResult result
        try {
            result = gpClient.runAnalysis(analysisType, parameters)
        }
	catch (WebServiceException ignored) {
            throw new WebServiceException('User needs to be registered on the Gene Pattern server')
        }

	logger.debug 'Response: {}', result
	logger.debug 'job number: {}', result.jobNumber
	logger.debug 'job was run on: {}', result.serverURL
	logger.debug 'Files:\n'
	for (String name in result.outputFileNames) {
	    logger.debug '\t{}\n', result.getURLForFileName(name)
	}
	logger.debug '\tdone listing files'
	logger.debug 'Parameters:'
	logger.debug '\tdone listing parameters'

        if (result.hasStandardError()) {
	    String stderrFile = result.getURLForFileName('stderr.txt').content
            throw new WebServiceException(analysisType + ' failed: ' + stderrFile)
        }
	completeWorkflowJob analysisType

	result
    }

    /**
     * Run job with no workflow (asynchronous)
     *
     * @param userName - The user requesting the job.
     * @param parameters - the job parameters
     * @param analysisType - the type of job to perform on the GenePattern server
     *
     * @return the job result from the GenePattern server
     */
    private JobResult runJobNoWF(String userName, Parameter[] parameters, String analysisType) throws WebServiceException {
        GPClient gpClient = getGPClient(userName)
	if (logger.debugEnabled) {
	    logger.debug 'Sending {} job to {}', analysisType, gpClient.server
	    logger.debug 'As user {} with parameters: ', gpClient.username
            for (parameter in parameters) {
		logger.debug '\t{}', parameter
            }
        }

        JobResult result = gpClient.runAnalysis(analysisType, parameters)
	if (logger.debugEnabled) {
	    logger.debug 'Response: {}', result
	    logger.debug 'Job Number: {}', result.jobNumber
	    logger.debug 'Run on server: {}', result.serverURL
	    logger.debug 'Files:'
	    for (String filename in result.outputFileNames) {
		logger.debug '\t{}', filename
		logger.debug '\t{}', result.getURLForFileName(filename)
            }
        }
        if (result.hasStandardError()) {
	    logger.error 'Result has standard error'
	    String stderrFile = result.getURLForFileName('stderr.txt').content
	    logger.error stderrFile
	    throw new WebServiceException(analysisType + ' failed: ' + stderrFile)
        }

	result
    }

    /**
     * Runs the clustering heatmap
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param resultType - applet or image
     *
     * @return the JobResult array from the GenePattern server
     */
    JobResult[] HCluster(String userName, String jobName, File gctFile, String resultType) throws WebServiceException {
	Parameter[] clusterParameters = [
	    new Parameter('input.filename', gctFile),
	    new Parameter('column.distance.measure', 2),
	    new Parameter('row.distance.measure', 2),
	    new Parameter('clustering.method', 'm')]

	updateStatus jobName, 'Performing Hierarchical Clustering'
        JobResult preProcessed = runJobNoWF(userName, clusterParameters, 'HierarchicalClustering')
        JobResult viewed

        if (resultType == 'applet') {
	    Parameter[] viewParameters = [
		new Parameter('cdt.file', preProcessed.getURL('cdt').toString()),
		new Parameter('gtr.file', preProcessed.getURL('gtr').toString()),
		new Parameter('atr.file', preProcessed.getURL('atr').toString())]
	    updateStatus jobName, 'Running Hierarchical Clustering Viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HierarchicalClusteringViewer')
        }
        else {
	    Parameter[] viewParameters = [
		new Parameter('cdt', preProcessed.getURL('cdt').toString()),
		new Parameter('gtr', preProcessed.getURL('gtr').toString()),
		new Parameter('atr', preProcessed.getURL('atr').toString()),
		new Parameter('column.size', 10),
		new Parameter('row.size', 10),
		new Parameter('show.row.descriptions', 'yes')]
	    logger.debug 'Run job to load the viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HierarchicalClusteringImage')
        }

        JobResult[] toReturn = [preProcessed, viewed]
	logger.debug 'Returning {} and {}', preProcessed, viewed

	toReturn
    }

    /**
     * Runs the KMeans clustering heatmap
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param nClusters - the number of clusters
     * @param resultType - applet or image
     */
    JobResult[] kMeansCluster(String userName, String jobName, File gctFile,
	                      String resultType, String nClusters) throws WebServiceException {
        Integer nC = 1
        try {
            nC = Integer.valueOf(nClusters)
        }
	catch (NumberFormatException ignored) {
	    logger.warn 'Cluster is not an integer {}, using 1', nClusters
        }
	Parameter[] clusterParameters = [
	    new Parameter('input.filename', gctFile),
	    new Parameter('number.of.clusters', nC),
	    new Parameter('cluster.by', 1), // 0 = rows, 1 = columns
	    new Parameter('distance.metric', 0)] // 0 = Euclidean

	updateStatus jobName, 'Performing KMeans Clustering'
        JobResult preProcessed = runJobNoWF(userName, clusterParameters, 'KMeansClustering')

        JobResult viewed

        if (resultType == 'applet') {
			Parameter[] viewParameters = [new Parameter('dataset',
			      preProcessed.getURL('KMcluster_output.gct').toString())]
	    updateStatus jobName, 'Running KMeans Clustering Viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HeatMapViewer')
        }
        else {
	    Parameter[] viewParameters = [
		new Parameter('input.dataset',
			      preProcessed.getURL('KMcluster_output.gct').toString()),
		new Parameter('column.size', 10),
		new Parameter('row.size', 10),
		new Parameter('show.row.descriptions', 'yes')]
	    logger.debug 'Run job to load the viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HeatMapImage')
        }

        JobResult[] toReturn = [preProcessed, viewed]
	logger.debug 'Returning {} and {}', preProcessed, viewed

	toReturn
    }

    /**
     * Runs the simple compare heatmap
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param resultType - applet or image
     *
     * @return the JobResult array from the GenePattern server
     */
    JobResult[] HMap(String userName, String jobName, File gctFile, String resultType) throws WebServiceException {
	Parameter[] preProcParameters = [new Parameter('input.filename', gctFile)]

	updateStatus jobName, 'Uploading file'
        JobResult preprocessed = runJobNoWF(userName, preProcParameters, 'ConvertLineEndings')

	String gctUrl = preprocessed.getURL('gct')

        JobResult viewed

        if (resultType == 'applet') {
	    Parameter[] viewParameters = [new Parameter('dataset', gctUrl)]
	    updateStatus jobName, 'Running Heatmap Viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HeatMapViewer')
        }
        else {
	    logger.debug 'resultType = {}', resultType
	    Parameter[] viewParameters = [new Parameter('input.dataset', gctUrl),
			                  new Parameter('column.size', 10),
			                  new Parameter('row.size', 10),
			                  new Parameter('show.row.descriptions', 'yes')]
	    logger.debug 'Run job to load the viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HeatMapImage')
        }

        JobResult[] toReturn = [preprocessed, viewed]
	logger.debug 'Returning {} and {}', preprocessed, viewed

	toReturn
    }

    /**
     * Runs the comparative marker analysis
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param clsFile - file with the heatmap data
     * @param resultType - applet or image
     */
    JobResult[] CMAnalysis(String userName, String jobName, File gctFile, File clsFile, String resultType) throws WebServiceException {
	Parameter[] impParameters = [
	    new Parameter('data.filename', gctFile),
	    new Parameter('k', 10),
	    new Parameter('rowmax', 0.5),
	    new Parameter('colmax', 0.8)]

	updateStatus jobName, 'Imputing Missing Value KNN'
        JobResult imputedMissing = runJobNoWF(userName, impParameters, 'ImputeMissingValuesKNN')

	Parameter[] cmsParameters = [
	    new Parameter('input.file',
			  getGenePatternRealURLBehindProxy(imputedMissing.getURL('gct').toString())),
	    new Parameter('cls.file', clsFile),
	    new Parameter('test.direction', 2),
	    new Parameter('test.statistic', 0),
	    new Parameter('number.of.permutations', 1000),
	    new Parameter('complete', 'false'),
	    new Parameter('balanced', 'false'),
	    new Parameter('random.seed', 779948241),
	    new Parameter('smooth.p.values', 'true')]

	updateStatus jobName, 'Performing Comparative Marker Selection'
        JobResult preProcessed = runJobNoWF(userName, cmsParameters, 'ComparativeMarkerSelection')

	Parameter[] ecmrParameters = [
	    new Parameter('comparative.marker.selection.filename',
			  getGenePatternRealURLBehindProxy(preProcessed.getURL('odf').toString())),
	    new Parameter('dataset.filename',
			  getGenePatternRealURLBehindProxy(imputedMissing.getURL('gct').toString())),
	    new Parameter('field', 'Rank'),
	    new Parameter('max', 100)]

	updateStatus jobName, 'Extracting Comparative Marker Results'
        JobResult extracted = runJobNoWF(userName, ecmrParameters, 'ExtractComparativeMarkerResults')

        JobResult viewed
        JobResult cmsv

        if (resultType == 'applet') {
	    Parameter[] viewParameters = [
		new Parameter('dataset', extracted.getURL('gct').toString())]
	    updateStatus jobName, 'Running Heatmap Viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HeatMapViewer')

	    Parameter[] cmsvParameters = [
		new Parameter('comparative.marker.selection.filename',
			      preProcessed.getURL('odf').toString()),
		new Parameter('dataset.filename',
			      imputedMissing.getURL('gct').toString())]
	    updateStatus jobName, 'Running Comparative Marker Selection Viewer'
	    cmsv = runJobNoWF(userName, cmsvParameters, 'ComparativeMarkerSelectionViewer')
        }
        else {
	    Parameter[] viewParameters = [
		new Parameter('input.dataset', extracted.getURL('gct').toString()),
		new Parameter('column.size', 10),
		new Parameter('row.size', 10),
		new Parameter('show.row.descriptions', 'yes')]
	    logger.debug 'Run job to load the viewer'
            viewed = runJobNoWF(userName, viewParameters, 'HeatMapImage')
        }

        JobResult[] toReturn = [extracted, viewed, cmsv]
	logger.debug 'Returning {}, {} and {}', extracted, viewed, cmsv

	toReturn
    }

    /**
     * Runs the PCA analysis
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param clsFile - file with the heatmap data
     * @param resultType - applet or image
     */
    JobResult[] PCA(String userName, String jobName, File gctFile, File clsFile,
	            String resultType) throws WebServiceException {
        /** Perhaps due to configuration issues of Gene Pattern server in transmartdev environment,
         * The input dataset file is imported into Gene Pattern correctly through web services interface, but is save into an
         * inaccessible location after use. The viewer applet needs to access the input data file, and will fail for these tasks.
         * The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
         * use the URL of this result file as input file to the later tasks and viewers. */
	Parameter[] preProcGctParameters = [new Parameter('input.filename', gctFile)]

	updateStatus jobName, 'Uploading file'
        JobResult preprocessedGct = runJobNoWF(userName, preProcGctParameters, 'ConvertLineEndings')
	String gctUrl = preprocessedGct.getURL('gct')

	Parameter[] preProcClsParameters = [new Parameter('input.filename', clsFile)]
        JobResult preprocessedCls = runJobNoWF(userName, preProcClsParameters, 'ConvertLineEndings')

	Parameter[] pcaProcParameters = [
	    new Parameter('input.filename', getGenePatternRealURLBehindProxy(gctUrl)),
	    new Parameter('cluster.by', '3'),
	    new Parameter('output.file', '<input.filename_basename>')]

	updateStatus jobName, 'Running PCA'
        JobResult pcaResult = runJobNoWF(userName, pcaProcParameters, 'PCA')

        JobResult viewed

        if (resultType == 'applet') {
	    String tFileName
	    String uFileName
	    String sFileName
	    for (String fileName in pcaResult.outputFileNames) {
		if (fileName.endsWith('_t.odf')) {
                    tFileName = fileName
		}
		else if (fileName.endsWith('_s.odf')) {
                    sFileName = fileName
		}
		else if (fileName.endsWith('_u.odf')) {
                    uFileName = fileName
		}
	    }

	    Parameter[] viewParameters = [
		new Parameter('dataset.file', gctUrl),
		new Parameter('s.matrix.file', pcaResult.getURLForFileName(sFileName).toString()),
		new Parameter('t.matrix.file', pcaResult.getURLForFileName(tFileName).toString()),
		new Parameter('u.matrix.file', pcaResult.getURLForFileName(uFileName).toString()),
		new Parameter('cls.or.sample.info.file', preprocessedCls.getURL('cls'))]
	    updateStatus jobName, 'Running PCA Viewer'
            viewed = runJobNoWF(userName, viewParameters, 'PCAViewer')
        }
        else {
	    Parameter[] viewParameters = [
		new Parameter('input.dataset', gctUrl),
		new Parameter('column.size', 10),
		new Parameter('row.size', 10),
		new Parameter('show.row.descriptions', 'yes')]
	    logger.debug 'Run job to load the viewer'
            viewed = runJobNoWF(userName, viewParameters, 'PCAViewer')
        }

        JobResult[] toReturn = [pcaResult, viewed]
	logger.debug 'Returning {} and {}', pcaResult, viewed

	toReturn
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // TEST - REMOVE AFTER DEBUGGING
    /**
     * Runs the comparative marker analysis
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param clsFile - file with the heatmap data
     * @param resultType - applet or image
     */
    JobResult[] CMAnalysis(File gctFile, File clsFile, String resultType) throws WebServiceException {
	Parameter[] impParameters = [
	    new Parameter('data.filename', gctFile),
	    new Parameter('k', 10),
	    new Parameter('rowmax', 0.5),
	    new Parameter('colmax', 0.8)]

        JobResult imputedMissing = runJob(impParameters, 'ImputeMissingValuesKNN')

	Parameter[] cmsParameters = [
	    new Parameter('input.file',
			  getGenePatternRealURLBehindProxy(imputedMissing.getURL('gct').toString())),
	    new Parameter('cls.file', clsFile),
	    new Parameter('test.direction', 2),
	    new Parameter('test.statistic', 0),
	    new Parameter('number.of.permutations', 1000),
	    new Parameter('complete', 'false'),
	    new Parameter('balanced', 'false'),
	    new Parameter('random.seed', 779948241),
	    new Parameter('smooth.p.values', 'true')]

        JobResult preProcessed = runJob(cmsParameters, 'ComparativeMarkerSelection')

	Parameter[] ecmrParameters = [
	    new Parameter('comparative.marker.selection.filename',
			  getGenePatternRealURLBehindProxy(preProcessed.getURL('odf').toString())),
	    new Parameter('dataset.filename',
			  getGenePatternRealURLBehindProxy(imputedMissing.getURL('gct').toString())),
	    new Parameter('field', 'Rank'),
	    new Parameter('max', 100)]

        JobResult extracted = runJob(ecmrParameters, 'ExtractComparativeMarkerResults')

        JobResult viewed
        JobResult cmsv

        if (resultType == 'applet') {
	    Parameter[] viewParameters = [new Parameter('dataset', extracted.getURL('gct').toString())]
            viewed = runJob(viewParameters, 'HeatMapViewer')

	    Parameter[] cmsvParameters = [
		new Parameter('comparative.marker.selection.filename',
			      preProcessed.getURL('odf').toString()),
		new Parameter('dataset.filename',
			      imputedMissing.getURL('gct').toString())]
            cmsv = runJob(cmsvParameters, 'ComparativeMarkerSelectionViewer')

        }
        else {
	    Parameter[] viewParameters = [
		new Parameter('input.dataset', extracted.getURL('gct').toString()),
		new Parameter('column.size', 10),
		new Parameter('row.size', 10),
		new Parameter('show.row.descriptions', 'yes')]
            viewed = runJob(viewParameters, 'HeatMapImage')
        }

        JobResult[] toReturn = [extracted, viewed, cmsv]
	logger.debug 'Returning {}, {} and {}', extracted, viewed, cmsv

	toReturn
    }

    /**
     * Runs the PCA analysis
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the heatmap data
     * @param clsFile - file with the heatmap data
     * @param resultType - applet or image
     */
    JobResult[] PCA(File gctFile, File clsFile, String resultType) throws WebServiceException {
        /** Perhaps due to configuration issues of Gene Pattern server in transmartdev environment,
         * The input dataset file is imported into Gene Pattern correctly through web services interface, but is save into an
         * inaccessible location after use. The viewer applet needs to access the input data file, and will fail for these tasks.
         * The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
         * use the URL of this result file as input file to the later tasks and viewers. */
	Parameter[] preProcGctParameters = [new Parameter('input.filename', gctFile)]

        JobResult preprocessedGct = runJob(preProcGctParameters, 'ConvertLineEndings')
	String gctUrl = preprocessedGct.getURL('gct')

	Parameter[] preProcClsParameters = [new Parameter('input.filename', clsFile)]
        JobResult preprocessedCls = runJob(preProcClsParameters, 'ConvertLineEndings')

	Parameter[] pcaProcParameters = [
	    new Parameter('input.filename', getGenePatternRealURLBehindProxy(gctUrl)),
	    new Parameter('cluster.by', '3'),
	    new Parameter('output.file', '<input.filename_basename>')]

        JobResult pcaResult = runJob(pcaProcParameters, 'PCA')

        JobResult viewed

        if (resultType == 'applet') {
	    String tFileName
	    String uFileName
	    String sFileName
	    for (String fileName in pcaResult.outputFileNames) {
		if (fileName.endsWith('_t.odf')) {
                    tFileName = fileName
		}
		else if (fileName.endsWith('_s.odf')) {
                    sFileName = fileName
		}
		else if (fileName.endsWith('_u.odf')) {
                    uFileName = fileName
		}
	    }

	    Parameter[] viewParameters = [
		new Parameter('dataset.file', gctUrl),
		new Parameter('s.matrix.file', pcaResult.getURLForFileName(sFileName).toString()),
		new Parameter('t.matrix.file', pcaResult.getURLForFileName(tFileName).toString()),
		new Parameter('u.matrix.file', pcaResult.getURLForFileName(uFileName).toString()),
		new Parameter('cls.or.sample.info.file', preprocessedCls.getURL('cls'))]

            viewed = runJob(viewParameters, 'PCAViewer')
        }
        else {
	    Parameter[] viewParameters = [
		new Parameter('input.dataset', gctUrl),
		new Parameter('column.size', 10),
		new Parameter('row.size', 10),
		new Parameter('show.row.descriptions', 'yes')]

            viewed = runJob(viewParameters, 'PCAViewer')
        }

        JobResult[] toReturn = [pcaResult, viewed]
	logger.debug 'Returning {} and {}', pcaResult, viewed

	toReturn
    }

    // TEST - REMOVE AFTER DEBUGGING
    //////////////////////////////////////////////////////////////////////////////////////////

    JobResult[] snpViewer(File dataFile, File sampleFile) {
        // The file submitted through web service interface is not accessible to Java Applet-based viewer.
        // The work-around is to use non-change tasks like ConvertLineEndings to put the input dataset file as a result file, and then
        // use the URL of this result file as input file to the later tasks and viewers.
	Parameter[] preProcDataParameters = [new Parameter('input.filename', dataFile)]
        JobResult preprocessedData = runJob(preProcDataParameters, 'ConvertLineEndings')

	Parameter[] preProcSampleParameters = [new Parameter('input.filename', sampleFile)]
        JobResult preprocessedSample = runJob(preProcSampleParameters, 'ConvertLineEndings')

	Parameter[] snpProcParameters = [
	    new Parameter('dataset.filename', preprocessedData.getURL('xcn')),
	    new Parameter('sample.info.filename', preprocessedSample.getURL('sample.cvt.txt'))]

        JobResult[] toReturn = new JobResult[2]
	toReturn[1] = runJob(snpProcParameters, 'SnpViewer')

	toReturn
    }

    JobResult[] igvViewer(IgvFiles igvFiles, String genomeVersion, String locus, String userName) {
        File sessionFile = igvFiles.getSessionFile()
	sessionFile << '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<Session genome="hg19" version="3">\n<Resources>\n'
	List<File> cnFileList = igvFiles.copyNumberFileList
	for (File cnFile in cnFileList) {
	    sessionFile << '<Resource path="' + igvFiles.getFileUrlWithSecurityToken(cnFile, userName) + '"/>\n'
        }
        sessionFile << '</Resources>\n</Session>'

	Parameter[] igvProcParameters = [new Parameter('input.file', getGPFileConvertUrl(sessionFile))]

        JobResult[] toReturn = new JobResult[2]
	toReturn[1] = runJob(igvProcParameters, 'IGV')

	toReturn
    }

    /**
     * Submits a file to GenePattern server, and get the URL to its GenePattern location
     */
    String getGPFileConvertUrl(File file) {
	String fileName = file.name
	String fileSuffix = fileName.substring(fileName.lastIndexOf('.') + 1)
	Parameter[] preProcDataParameters = [new Parameter('input.filename', file)]
        JobResult preprocessedData = runJob(preProcDataParameters, 'ConvertLineEndings')
	preprocessedData.getURL fileSuffix
    }

    /**
     * Runs the Survival Analysis
     *
     * @param userName - The user requesting the job
     * @param jobName - The name of the job given by the GP controller
     * @param gctFile - file with the survival analysis data
     * @param clsFile - file with the survival analysis data
     * @param imageTempPath - path to store the temporary images
     * @param imageTempDirName - the image temporary directory
     * @param contextPath - the servlet context path
     * @param querySummary1 - Results from the first subset
     * @param querySummary2 - Results from the second subset
     */
    String survivalAnalysis(String userName, String jobName, File dataFile, File clsFile,
	                    String imageTempPath, String imageTempDirName, String contextPath,
                            String querySummary1, String querySummary2) throws WebServiceException {

        if (dataFile == null) {
            throw new WebServiceException('The data file for survival analysis does not exist')
        }
        if (clsFile == null) {
            throw new WebServiceException('The cls file for survival analysis does not exist')
        }

	Parameter[] coxParameters = [
	    new Parameter('input.surv.data.filename', dataFile),
	    new Parameter('input.cls.filename', clsFile),
	    new Parameter('output.file', 'CoxRegression_result'),
	    new Parameter('time', 'time'),
	    new Parameter('status', 'censor'),
	    new Parameter('variable.continuous', 'cls'),
	    new Parameter('variable.category', 'NA'),
	    new Parameter('variable.interaction.terms', 'NA'),
	    new Parameter('strata', 'NA'),
	    new Parameter('input.subgroup', 'NA'),
	    new Parameter('variable.selection', 'none')]
	updateStatus jobName, 'Running Cox Regression'
        JobResult coxResult = runJobNoWF(userName, coxParameters, 'CoxRegression')

	String coxStr = parseCoxRegressionStr(getOutputFileText(
	    coxResult, 'CoxRegression_result', imageTempPath))

	Parameter[] curveParameters = [
	    new Parameter('input.surv.data.file', dataFile),
	    new Parameter('input.cls.file', clsFile),
	    new Parameter('time.field', 'time'),
	    new Parameter('censor.field', 'censor'),
	    new Parameter('print.fit.results', 'T'),
	    new Parameter('line.type.color.assign', 'automatic'),
	    new Parameter('line.width', '1'),
	    new Parameter('time.conversion', '1'),
	    new Parameter('surv.function.lower', '0'),
	    new Parameter('surv.function.higher', '1'),
	    new Parameter('curve.type', 'log'),
	    new Parameter('show.conf.interval', '0'),
	    new Parameter('add.legend', 'T'),
	    new Parameter('legend.position', 'left-bottom'),
	    new Parameter('output.filename', '<input.surv.data.file_basename>')]
	updateStatus jobName, 'Calculating Survival Curve'
        JobResult curveResult = runJobNoWF(userName, curveParameters, 'SurvivalCurve')

	String summaryStr = parseSurvivalCurveSummary(
	    getOutputFileText(curveResult, 'FitSummary', imageTempPath))

        String graphFileName = getOutputFileName(curveResult, 'SurvivalCurve')
        File graphFile = curveResult.downloadFile(graphFileName, imageTempPath)
	String imageFileName = graphFile.name
        try {
            imageFileName = convertPdfToPng(graphFile, imageTempPath)
        }
	catch (IOException e) {
	    logger.warn e.message
	    logger.warn 'GP server updated so PDF is no longer part of the results, just use png image file'
        }

	String imageUrlPath = contextPath + imageTempDirName + '/' + imageFileName

	'<h2>Survival Analysis</h2>' +
	    '<table border="1" width="100%"><tr><th>Subset 1 Query</th><th>Subset 2 Query</th></tr><tr><td>' +
	    querySummary1 + '</td><td>' + querySummary2 + '</td></tr></table>' +
	    '<p>Cox Regression Result:</p>' +
	    coxStr +
	    '<p>Survival Curve Fitting Summary:</p>' +
	    summaryStr +
	    '<img src="' + imageUrlPath + '" />'
    }

    String gwas(String userName, String jobName, GwasFiles gwasFiles,
                String querySummary1, String querySummary2) throws WebServiceException {

        if (gwasFiles == null) {
            throw new WebServiceException('The object gwasFiles does not exist')
        }

        try {
	    i2b2HelperService.runPlink gwasFiles
	    i2b2HelperService.reportGwas userName, gwasFiles, querySummary1, querySummary2
        }
	catch (WebServiceException e) {
	    throw e
        }
	catch (e) {
	    throw new WebServiceException('Failed to run PLINK on server or report the result: ' + e.message)
        }

	gwasFiles.reportFile.text
    }

    String convertPdfToPng(File graphFile, String imageTempPath) {
	Assert.notNull graphFile, 'The PDF file is empty'
	Assert.notNull imageTempPath, 'The temporary path for image folder is not defined'

	FileChannel channel = new RandomAccessFile(graphFile, 'r').channel
	PDFFile pdffile = new PDFFile(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()))

        // draw the first page to an image
        PDFPage page = pdffile.getPage(0)

        //get the width and height for the doc at the default zoom
	Rectangle rect = new Rectangle(0, 0, (int) page.BBox.width, (int) page.BBox.height)

        //generate the image
        Image img = page.getImage(
	    rect.width, rect.height, //width & height
            rect, // clip rect
            null, // null for the ImageObserver
            true, // fill background with white
	    true)  // block until drawing is done
	String imageFileName = graphFile.name.replace('.pdf', '.png')
	ImageIO.write img, 'png', new File(imageTempPath, imageFileName)
	imageFileName
    }

    String getOutputFileName(JobResult jobResult, String partialFileName) {
	if (!jobResult || !partialFileName) {
	    return null
	}

	String[] fileNames = jobResult.outputFileNames
	if (!fileNames) {
	    return null
	}

	for (String fileName in fileNames) {
	    if (fileName.contains(partialFileName)) {
                return fileName
            }
	}
    }

    String getOutputFileText(JobResult jobResult, String partialFileName, String downloadDirPath) {
	if (!jobResult || !partialFileName) {
	    return null
	}

	String[] fileNames = jobResult.outputFileNames
	if (!fileNames) {
            return null
	}

        String outFileName = null
	for (String fileName in fileNames) {
	    if (fileName.contains(partialFileName)) {
                outFileName = fileName
            }
	}
	if (outFileName == null) {
	    return null
	}

	jobResult.downloadFile(outFileName, downloadDirPath).text
    }

    GPClient getGPClient() {
	if (gpClient != null) {
            return gpClient
	}

	String userName = securityService.currentUsername()
	logger.debug 'starting genepattern client at {} as {}', genePatternUrl, userName

	gpClient = new GPClient(genePatternUrl, userName)
	logger.debug 'genepattern client initialized'
	gpClient
    }

    /**
     * Returns a new GenePattern Client object authenticated by the given userName
     *
     * @param userName - The user requesting the GenePattern work
     *
     * @return - the GenePattern Client object
     */
    GPClient getGPClient(String userName) throws WebServiceException {
	if (gpClient != null && userName.equalsIgnoreCase(gpClient.username)) {
	    logger.debug 'GPClient is already initialized for {}, returning existing client', userName
            return gpClient
	}

	logger.debug 'Starting GPClient at {} as {}', genePatternUrl, userName
	gpClient = new GPClient(genePatternUrl, userName)
	logger.debug 'GPClient has been initialized'
	gpClient
    }

    String parseCoxRegressionStr(String inStr) {
	StringBuilder sb = new StringBuilder()
	String numSubject
	String coef
	String hazardRatio
	String pVal
	String lower95
	String upper95
	boolean nextLineHazard = false
	boolean nextLine95 = false
	for (String it in inStr.readLines()) {
	    if (it.contains('n=')) {
                numSubject = it.substring(it.indexOf('n=') + 2).trim()
            }
	    else if (it.contains('se(coef)')) {
                nextLineHazard = true
            }
	    else if (it.contains('cls') && nextLineHazard) {
                nextLineHazard = false
                String[] resultArray = it.split()
                coef = resultArray[1]
                hazardRatio = resultArray[2]
                pVal = resultArray[5]
            }
	    else if (it.contains('lower')) {
                nextLine95 = true
            }
	    else if (it.contains('cls') && nextLine95) {
                nextLine95 = false
                String[] resultArray = it.split()
                lower95 = resultArray[3]
                upper95 = resultArray[4]
            }
        }
	sb << '<table border="1"  width="100%"><tr><th>Number of Subjects</th><td>' << numSubject << '</td></tr>'
	sb << '<tr><th>Hazard Ratio (95% CI)</th><td>' << hazardRatio << ' (' << lower95 << ' - ' << upper95 << ')</td></tr>'
	sb << '<tr><th>Relative Risk (p Value)</th><td>' << coef << ' (' << pVal << ')</td></tr>'
	sb << '</table>'
	sb
    }

    String parseSurvivalCurveSummary(String inStr) {
	StringBuilder sb = new StringBuilder()
	sb << '<table border="1" width="100%"><tr>' +
	    '<th>Subset</th><th>Number of Subjects</th><th>Number of Events</th>' +
	    '<th>Median Value</th><th>Lower Range of 95% Confidence Level</th>' +
	    '<th>Upper Range of 95% Confidence Level</th></tr>'
	for (String line in inStr.readLines()) {
	    if (line.contains('cls=')) {
		String[] strArray = line.split()
		if (strArray[0].contains('cls=1')) {
		    sb << '<tr><td>Subset 1</td>'
		}
		else if (strArray[0].contains('cls=2')) {
		    sb << '<tr><td>Subset 2</td>'
		}

                for (int i = 1; i < 6; i++) {
                    String value = strArray[i]
		    if (value.contains('Inf')) {
                        value = 'infinity'
                    }
		    sb << '<td>' << value << '</td>'
                }
		sb << '</tr>'
            }
        }
	sb << '</table>'
	sb
    }

    /**
     * Some GenePattern modules cannot access GenePattern stored files, if the GenePattern server is accessed through a proxy.
     * Separate GenePattern server is to have more resource for demanding tasks. Same URL for GenePattern as the Transmart server is to avoid
     * security issue of launching Java Applet from IE.
     * In the case of PCA, transmart code first submit input to GenePattern server. In PCA call, the URL of the GenePattern-stored input
     * file is submitted as input. These input URL is from the proxy, like https://transmartdev/gp/jobResults/2374/gp_df_8595641542636053092.cvt.cls.
     * Inside code of PCA module cannot programmatically access this input URL through proxy. We need to manually convert this URL to the real URL
     * http://xxx.xxx.xxx.xxx:xxxx/gp/jobResults/2374/gp_df_8595641542636053092.cvt.cls.
     *
     * Somehow, GenePattern server put the result file in the proxy'ed URL. In IE, the Java applet is launched to access this proxy'ed URL,
     * and will work correctly.
     * This function should NOT be used on input parameters to the Viewer modules.
     *
     * Note: the idea of proxy for the separately-hosted GenePattern is too complicated, and may break other things.
     */
    String getGenePatternRealURLBehindProxy(String gpUrlIn) {
	if (genePatternRealUrlBehindProxy && genePatternRealUrlBehindProxy != '{}') {
	    gpUrlIn.replace genePatternUrl, genePatternRealUrlBehindProxy
	}
	else {
	    gpUrlIn
	}
    }

    void startWorkflowJob(String jobName) throws WebServiceException {
	if (session?.getAttribute('workflowstatus')?.isCancelled()) {
            throw new WebServiceException('Workflow cancelled by user!')
        }
	session?.getAttribute('workflowstatus')?.currentJobStatus =
	    new JobStatus(name: jobName, status: 'R')
    }

    void completeWorkflowJob(String jobName) {
	session?.getAttribute('workflowstatus')?.currentJobStatus =
	    new JobStatus(name: jobName, status: 'C')
    }

    JobResult PLINK(File pedFile, File mapFile) throws WebServiceException {

	Parameter[] plinkParameters = [
	    new Parameter('ped.input', pedFile),
	    new Parameter('map.input', mapFile)]

	runJob plinkParameters, 'PLINK'
    }

    private HttpSession getSession() {
	RequestContextHolder.currentRequestAttributes().session
    }
}
