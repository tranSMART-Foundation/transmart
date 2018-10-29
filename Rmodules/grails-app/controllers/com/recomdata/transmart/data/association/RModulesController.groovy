/*************************************************************************   
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************/

package com.recomdata.transmart.data.association

import com.recomdata.asynchronous.JobResultsService
import grails.converters.JSON
import jobs.AcghFrequencyPlot
import jobs.AcghGroupTest
import jobs.AcghSurvivalAnalysis
import jobs.BoxPlot
import jobs.CorrelationAnalysis
import jobs.Geneprint
import jobs.Heatmap
import jobs.HierarchicalClustering
import jobs.KMeansClustering
import jobs.LineGraph
import jobs.LogisticRegression
import jobs.MarkerSelection
import jobs.PCA
import jobs.RNASeqGroupTest
import jobs.ScatterPlot
import jobs.SurvivalAnalysis
import jobs.TableWithFisher
import jobs.UserParameters
import jobs.Waterfall
import jobs.misc.AnalysisConstraints
import jobs.misc.AnalysisQuartzJobAdapter
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONElement
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.transmart.plugin.shared.SecurityService
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.users.User

import static jobs.misc.AnalysisQuartzJobAdapter.PARAM_ANALYSIS_CONSTRAINTS
import static jobs.misc.AnalysisQuartzJobAdapter.PARAM_GRAILS_APPLICATION
import static jobs.misc.AnalysisQuartzJobAdapter.PARAM_JOB_CLASS
import static jobs.misc.AnalysisQuartzJobAdapter.PARAM_USER_IN_CONTEXT
import static jobs.misc.AnalysisQuartzJobAdapter.PARAM_USER_PARAMETERS

class RModulesController {

    private static final Map<String, String> lookup = [
			'Gene Expression': 'mrna',
			MIRNA_QPCR       : 'mirnaqpcr',
			MIRNA_SEQ        : 'mirnaseq',
			RBM              : 'rbm',
			PROTEOMICS       : 'protein',
			RNASEQ           : 'rnaseq_cog',
			METABOLOMICS     : 'metabolite',
			Chromosomal      : 'acgh',
			acgh             : 'acgh',
			rnaseq           : 'rnaseq',
			RNASEQ_RCNT      : 'rnaseq',
			VCF              : 'vcf'
    ].asImmutable()

    def asyncJobService
    User currentUserBean
    GrailsApplication grailsApplication
    JobResultsService jobResultsService
    def quartzScheduler
    RModulesService RModulesService
    SecurityService securityService

    def canceljob(String jobName) {
	response.contentType = 'text/json'
	response.outputStream << asyncJobService.canceljob(jobName).toString()
    }

    /**
     * Create the new asynchronous job name
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob() {
     	response.contentType = 'text/json'
	response.outputStream << asyncJobService.createnewjob(params).toString()
    }

    def scheduleJob(String jobName, String analysis) {
        def json

        if (jobResultsService[jobName] == null) {
            throw new IllegalStateException('Cannot schedule job; it has not been created')
        }

        // has to come before and flush the new state, otherwise the
        // sessionFactory running on the quartz thread may get stale values
        asyncJobService.updateJobInputs(jobName, params)

        switch (analysis) {
 	    case 'heatmap': json = createJob(Heatmap); break
	    case 'kclust': json = createJob(KMeansClustering); break
	    case 'hclust': json = createJob(HierarchicalClustering); break
	    case 'markerSelection': json = createJob(MarkerSelection); break
	    case 'pca': json = createJob(PCA); break
	    case 'tableWithFisher': json = createJob(TableWithFisher, false); break
	    case 'boxPlot': json = createJob(BoxPlot, false); break
	    case 'scatterPlot': json = createJob(ScatterPlot, false); break
	    case 'survivalAnalysis': json = createJob(SurvivalAnalysis, false); break
	    case 'lineGraph': json = createJob(LineGraph, false); break
	    case 'correlationAnalysis': json = createJob(CorrelationAnalysis, false); break
	    case 'waterfall': json = createJob(Waterfall, false); break
	    case 'logisticRegression': json = createJob(LogisticRegression, false); break
	    case 'geneprint': json = createJob(Geneprint); break
	    case 'acghFrequencyPlot': json = createJob(AcghFrequencyPlot); break
	    case 'groupTestaCGH': json = createJob(AcghGroupTest); break
	    case 'aCGHSurvivalAnalysis': json = createJob(AcghSurvivalAnalysis); break
	    case 'groupTestRNASeq': json = createJob(RNASeqGroupTest); break
	    default: json = RModulesService.scheduleJob(securityService.currentUsername(), params)
        }

        response.contentType = 'text/json'
        response.outputStream << json.toString()
    }

    private Date createJob(Class clazz, boolean useAnalysisConstraints = true) {

        UserParameters userParams = new UserParameters(map: [:] + params)

        params[PARAM_GRAILS_APPLICATION] = grailsApplication
        params[PARAM_JOB_CLASS] = clazz
        if (useAnalysisConstraints) {
            params[PARAM_ANALYSIS_CONSTRAINTS] = createAnalysisConstraints(params)
        }

        params[PARAM_USER_PARAMETERS] = userParams
        params[PARAM_USER_IN_CONTEXT] =  currentUserBean.targetSource.target

	JobDetail jobDetail = new JobDetailImpl(params.jobName, params.jobType, AnalysisQuartzJobAdapter)
	jobDetail.jobDataMap = new JobDataMap(params)
	quartzScheduler.scheduleJob jobDetail,
			new SimpleTriggerImpl('triggerNow ' + System.currentTimeMillis(), 'RModules')
    }

    static AnalysisConstraints createAnalysisConstraints(Map params) {
        Map map = validateParamAnalysisConstraints(params) as Map
        map.data_type = lookup[map.data_type]
        new AnalysisConstraints(map: massageConstraints(map))
    }

    private static Map massageConstraints(Map analysisConstraints) {
        analysisConstraints.dataConstraints.each { String constraintType, value ->
            if (constraintType == 'search_keyword_ids') {
                analysisConstraints.dataConstraints[constraintType] = [ keyword_ids: value ]
            }

            if (constraintType == 'mirnas') {
                analysisConstraints.dataConstraints[constraintType] = [ names: value ]
            }
        }

        analysisConstraints
    }

    private static JSONElement validateParamAnalysisConstraints(Map params) {
        if (!params[PARAM_ANALYSIS_CONSTRAINTS]) {
            throw new InvalidArgumentsException("No parameter $PARAM_ANALYSIS_CONSTRAINTS")
        }

        JSONElement constraints
        try {
            constraints = JSON.parse(params[PARAM_ANALYSIS_CONSTRAINTS])
        }
	catch (ConverterException ignored) {
            throw new InvalidArgumentsException("Parameter $PARAM_ANALYSIS_CONSTRAINTS is not a valid JSON string")
        }

        if (!(constraints instanceof Map)) {
            throw new InvalidArgumentsException(
                    "Expected $PARAM_ANALYSIS_CONSTRAINTS to be a map (JSON object); got ${constraints.getClass()}")
        }

        // great naming consistency here!
        for (String it in ['data_type', 'assayConstraints', 'dataConstraints']) {
            if (!constraints[it]) {
                throw new InvalidArgumentsException("No sub-parameter '$it' for request parameter $PARAM_ANALYSIS_CONSTRAINTS")
            }
        }

        constraints
    }
}
