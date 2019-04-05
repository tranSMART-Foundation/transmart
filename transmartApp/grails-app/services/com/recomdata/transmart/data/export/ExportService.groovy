package com.recomdata.transmart.data.export

import com.recomdata.asynchronous.GenericJobExecutor
import com.recomdata.asynchronous.JobResultsService
import com.recomdata.transmart.asynchronous.job.AsyncJobService
import com.recomdata.transmart.domain.i2b2.AsyncJob
import com.recomdata.transmart.validate.RequestValidator
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.json.JSONObject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.beans.factory.annotation.Value
import org.transmart.plugin.shared.SecurityService
import org.transmartproject.core.users.User
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService

@Slf4j('logger')
class ExportService {

    private static final List<String> statusList = ['Started', 'Validating Cohort Information',
	                                            'Triggering Data-Export Job', 'Gathering Data',
	                                            'Running Conversions', 'Running Analysis', 'Rendering Output'].asImmutable()

    AsyncJobService asyncJobService
    User currentUserBean
    HighDimensionResourceService highDimensionResourceService
    I2b2ExportHelperService i2b2ExportHelperService
    JobResultsService jobResultsService
    Scheduler quartzScheduler
    SecurityService securityService

    @Value('${com.recomdata.plugins.tempFolderDirectory:}')
    private String tempFolderDirectory

    @Transactional
    JSONObject createExportDataAsyncJob(String analysis, String querySummary1, String querySummary2) {
	String jobStatus = 'Started'

	AsyncJob newJob = new AsyncJob(lastRunOn: new Date(), jobType: analysis, jobStatus: jobStatus)
        newJob.save()

	String jobName = securityService.currentUsername() + '-' + analysis + '-' + newJob.id
        newJob.jobName = jobName
        newJob.altViewerURL = 'Test'
        newJob.save()

        jobResultsService[jobName] = [:]
	String querySummary = 'Subset 1:' + querySummary1 + (querySummary2 ? ' <br/> Subset 2:' + querySummary2 : '')
        asyncJobService.updateStatus(jobName, jobStatus, null, querySummary, null)

	logger.debug 'Sending {} back to the client', newJob.jobName

	new JSONObject(jobName: jobName, jobStatus: jobStatus)
    }

    /**
     * Fixes up the data export request data.
     * Format of individual values of selectedSubsetDataTypeFiles:
     *{*   subset: subset<1|2>,
     *   dataTypeId: <data type>,
     *   fileType: <EXTENSION>
     *}*
     * This method than builds a pointlessly deeply nested map for you!
     *
     * @param selectedCheckboxList List with selected checkboxes
     */
    protected Map getHighDimDataTypesAndFormats(selectedCheckboxList) {
        Map formats = [:]

	for (Map checkbox in selectedCheckboxList.collect { JSON.parse(it.toString()) }) {
            def fileType = checkbox.fileType
	    
            if (!formats.containsKey(checkbox.subset)) {
                formats[checkbox.subset] = [:]
            }

            if (!formats[checkbox.subset].containsKey(checkbox.dataTypeId)) {
                formats[checkbox.subset][checkbox.dataTypeId] = [:]
            }

            if (!formats[checkbox.subset][checkbox.dataTypeId].containsKey(fileType)) {
                formats[checkbox.subset][checkbox.dataTypeId][fileType] = []
            }
        }

        formats
    }

    private Map<String, List<String>> getSubsetSelectedFilesMap(List selectedCheckboxJsonList) {
	Map<String, List<String>> subsetSelectedFilesMap = [:]

	for (checkboxItem in selectedCheckboxJsonList?.unique()) {
            String currentSubset = null
            if (checkboxItem.subset) {
                //The first item is the subset name.
                currentSubset = checkboxItem.subset.trim().replace(' ', '')
		if (null == subsetSelectedFilesMap[currentSubset]) {
		    subsetSelectedFilesMap[currentSubset] = []
                }
            }

            if (checkboxItem.dataTypeId) {
                //Second item is the data type.
                String selectedFile = checkboxItem.dataTypeId.trim()
                if (!(checkboxItem.dataTypeId == 'CLINICAL'
                      || checkboxItem.dataTypeId in highDimensionResourceService.knownTypes)) {
                    selectedFile += '.' + checkboxItem.fileType
                }
		subsetSelectedFilesMap[currentSubset] << selectedFile
            }
        }

        subsetSelectedFilesMap
    }

    Map<String, Map> getsubsetSelectedPlatformsByFiles(checkboxList) {
	Map<String, Map> subsetSelectedPlatformsByFiles = [:]
        //Split the list on commas first, each box is seperated by ','.
		for (checkboxItem in checkboxList) {
            //Split the item by '_' to get the different attributes.
            // Attributes are: <subset_id>_<datatype>_<exportformat>_<platform>
            // e.g. subset1_mrna_TSV_GPL570
            String[] checkboxItemArray = StringUtils.split(checkboxItem, '_')

            //The first item is the subset name.
	    String currentSubset = checkboxItemArray[0].trim().replace(' ', '')

            //Fourth item is the selected (gpl) platform
            if (checkboxItemArray.size() > 3) {
		String fileName = checkboxItemArray[1].trim() + checkboxItemArray[2].trim()
		String platform = checkboxItemArray[3].trim()
                if (subsetSelectedPlatformsByFiles.containsKey(currentSubset)) {
                    if (subsetSelectedPlatformsByFiles.get(currentSubset).containsKey(fileName)) {
			subsetSelectedPlatformsByFiles.get(currentSubset).get(fileName) << platform
                    }
                    else {
                        subsetSelectedPlatformsByFiles.get(currentSubset).put(fileName, [platform])
                    }
                }
                else {
		    subsetSelectedPlatformsByFiles[currentSubset] = [(fileName): [platform]]
                }
            }
        }

        subsetSelectedPlatformsByFiles
    }

    private Date createExportDataJob(params) {

        //We need a sub hash for each subset.
	Map resultInstanceIds = [subset1: params.result_instance_id1, subset2: params.result_instance_id2]

        //Loop through the values for each selected checkbox.
        def checkboxList = params.selectedSubsetDataTypeFiles

        if (checkboxList instanceof String) {
            def tempArray = []
	    if (checkboxList?.trim()) {
		tempArray << checkboxList
	    }
            checkboxList = tempArray
        }

        def checkedFileTypes = [params.selectedSubsetDataTypeFiles].flatten().collect { JSON.parse(it) }

	JobDetail jobDetail = new JobDetailImpl(params.jobName, params.analysis, GenericJobExecutor.class)
	jobDetail.jobDataMap = new JobDataMap(
 	    analysis: params.analysis, userName: securityService.currentUsername(), jobName: params.jobName,
	    result_instance_ids: resultInstanceIds, selection: params.selection,
	    highDimDataTypes: getHighDimDataTypesAndFormats(checkboxList),
	    subsetSelectedPlatformsByFiles: getsubsetSelectedPlatformsByFiles(checkboxList),
	    checkboxList: checkboxList, subsetSelectedFilesMap: getSubsetSelectedFilesMap(checkedFileTypes),
	    resulttype: 'DataExport', studyAccessions: i2b2ExportHelperService.findStudyAccessions(resultInstanceIds.values().toArray()),
            //Add the pivot flag to the jobs map.
	    pivotData: true,
            //TODO: This should be a part of something else, config files eventually.
            //This is hardcoded for now but it adds the step of bundling the files to a workflow.
	    analysisSteps: ['bundle': ''],
            //This adds a step to the job to create a file link as the plugin output.
	    renderSteps: ['FILELINK': ''],
	    userInContext: currentUserBean.targetSource.target)

        if (asyncJobService.updateStatus(params.jobName, statusList[2])) {
            return
        }

        quartzScheduler.scheduleJob jobDetail, new SimpleTriggerImpl('triggerNow' + Math.random(), params.analysis)
    }

    Date exportData(Map params) {
        jobResultsService[params.jobName]['StatusList'] = statusList
        asyncJobService.updateStatus(params.jobName, statusList[0])

        //TODO get the required input parameters for the job and validate them
        def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
        def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
	logger.debug 'rID1 :: {} :: rID2 :: {}', rID1, rID2
	asyncJobService.updateStatus params.jobName, statusList[1]

	logger.debug 'Checking to see if the user cancelled the job prior to running it'
        if (jobResultsService[params.jobName]['Status'] == 'Cancelled') {
	    logger.warn '{} has been cancelled', params.jobName
            return
        }
	createExportDataJob params
    }

    InputStream downloadFile(String jobName) {
	new ExportDataProcessor(tempFolderDirectory).getExportJobFileStream AsyncJob.findByJobName(jobName).viewerURL
    }
}
