/*************************************************************************   
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the 'License')
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an 'AS IS' BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

package com.recomdata.transmart.data.association

import com.recomdata.asynchronous.JobResultsService
import com.recomdata.transmart.data.association.asynchronous.RModulesJobService
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.json.JSONObject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.triggers.SimpleTriggerImpl

@Slf4j('logger')
class RModulesService {

    static transactional = false
    static scope = 'request'

    private static final List<String> STATUS_LIST = [
            'Started',
            'Validating Cohort Information',
            'Triggering Data-Export Job',
            'Gathering Data',
            'Running Conversions',
            'Running Analysis',
            'Rendering Output'].asImmutable()

	def asyncJobService
	def i2b2ExportHelperService
	def i2b2HelperService
	JobResultsService jobResultsService
	def pluginService
	def quartzScheduler
	
	private List<String> jobStatusList
	private JobDataMap jobDataMap = new JobDataMap()

	/**
	 * Use the moduleName and fetch its statusList config param from the <Plugin>Config.groovy
	 */
	private void setJobStatusList(Map params) {
        	jobStatusList = STATUS_LIST
		
		//Set the status list and update the first status.
		jobResultsService[params.jobName]['StatusList'] = jobStatusList
    	}
	
	private void initJobProcess(Map params) {
		setJobStatusList params
		
		//Update status to step 1 of jobStatusList : Started
		//Can add more initialization process before starting the Job
		asyncJobService.updateStatus params.jobName, jobStatusList[0]
	}
	
	private void validateJobProcess(Map params) {
	   //Update the status to say we are validating, No validation code yet though.
	   //Can add more validation process before starting the Job
	   asyncJobService.updateStatus params.jobName, jobStatusList[1]
	}

	private void beforeScheduleJob(Map params) {
	   initJobProcess params
	   
	   validateJobProcess params
	}
	
	/**
	 * Prepares the DataTypeMap to be embedded into the jobDataMap
	 */
	private Map<String, List> prepareDataTypeMap(Map moduleMap, Map params) {
		// Get the list of variables that dictate which data files to generate.
		// Check each of them against the HTML form and build the Files Map.
		Map pluginDataTypeVariables = moduleMap.dataFileInputMapping
		List subset1 = []
		List subset2 = []	
		//Loop over the items in the input map.
		for (currentPlugin in pluginDataTypeVariables) {
			//If true is in the key, we always include this data type.
			if (currentPlugin.value == 'TRUE') {
				subset1 << currentPlugin.key
				subset2 << currentPlugin.key
			}
			else {
				//We may have a list of inputs, for each one we check to see if the value is true. If it is, that type of file gets included.
				//Check each input.
				for (currentInput in currentPlugin.value.split('\\|')) {
					//If we have an input name, check to see if it's true, if it is then we can add the file type to the map.
					if(params[currentInput] == 'true'){
						subset1 << currentPlugin.key
						subset2 << currentPlugin.key
					}
				}
			}
		}
		
		[subset1: subset1, subset2: subset2]
	}
	
	private void prepareConceptCodes(Map params) {
		//Get the list of all the concepts that we are concerned with from the form.
		String variablesConceptPaths = params.variablesConceptPaths
		if (variablesConceptPaths) {
			List<String> conceptCodes = []
			for (conceptPath in variablesConceptPaths.split('\\|')) {
				conceptCodes << i2b2HelperService.getConceptCodeFromKey('\\\\' + conceptPath.trim())
			}
			jobDataMap.concept_cds = conceptCodes.toArray()
		}
	}
	
	/**
	 * Loads up the jobDataMap with all the variables from each R Module
	 */
	private void loadJobDataMap(String userName, Map params) {
		jobDataMap.analysis = params.analysis
		jobDataMap.userName = userName
		jobDataMap.jobName = params.jobName

		//Each subset needs a name and a RID. Put this info in a hash.
		Map<String, String> resultInstanceIds = [subset1: params.result_instance_id1, subset2: params.result_instance_id2]
		jobDataMap.result_instance_ids = resultInstanceIds
		jobDataMap.studyAccessions = i2b2ExportHelperService.findStudyAccessions(resultInstanceIds.values())
		
		Map moduleMap = null
		String moduleMapStr = pluginService.findPluginModuleByModuleName(params.analysis)?.paramsStr
		
		try {
			moduleMap = new JSONObject(moduleMapStr) as Map
		}
		catch (e) {
			logger.error('Module '+params.analysis+' params could not be loaded', e)
		}
		
		if (null != moduleMap) {
			jobDataMap.subsetSelectedFilesMap = prepareDataTypeMap(moduleMap, params)
			jobDataMap.conversionSteps = moduleMap.converter
			jobDataMap.analysisSteps = moduleMap.processor
			jobDataMap.renderSteps = moduleMap.renderer
			jobDataMap.variableMap = moduleMap.variableMapping
			jobDataMap.pivotData = moduleMap.pivotData
		}
		//Add each of the parameters from the html form to the job data map.
		for (currentParam in params) {
			jobDataMap.put currentParam.key,currentParam.value
		}
		
		//If concept codes exist put them in our jobDataMap.
		prepareConceptCodes params
	}
		
	/**
	 * Gather data from the passed in params collection and from the plugin
	 * descriptor stored in session to load up the jobs data map.
	 */
	Date scheduleJob(String userName, Map params) {
		beforeScheduleJob params
		loadJobDataMap userName, params

		if (jobResultsService[params.jobName]['Status'] == 'Cancelled') {
			return
		}

		if (asyncJobService.updateStatus(params.jobName, jobStatusList[2]))	{
			return
		}

		//com.recomdata.transmart.plugin.PluginJobExecutionService should be implemented by all Plugins
		JobDetail jobDetail = new JobDetailImpl(params.jobName, params.jobType, RModulesJobService)
		jobDetail.jobDataMap = jobDataMap

		quartzScheduler.scheduleJob jobDetail,
				new SimpleTriggerImpl('triggerNow' + System.currentTimeMillis(), 'RModules')
	}
	
	// method for non-R jobs, used in transmart-metacore-plugin
	JobDataMap prepareDataForExport(String userName, Map params) {
		loadJobDataMap userName, params
		jobDataMap
	}
}
