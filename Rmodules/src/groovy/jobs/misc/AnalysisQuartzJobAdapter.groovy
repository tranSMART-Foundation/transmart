package jobs.misc

import com.recomdata.asynchronous.JobResultsService
import grails.util.Holders
import groovy.util.logging.Slf4j
import jobs.AbstractAnalysisJob
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.context.ApplicationContext
import org.springframework.core.NamedThreadLocal

@Slf4j('logger')
class AnalysisQuartzJobAdapter implements Job {

    public static final String PARAM_JOB_CLASS = 'jobClass'
    public static final String PARAM_GRAILS_APPLICATION = 'grailsApplication'
    public static final String PARAM_JOB_NAME = 'jobName'
    public static final String PARAM_USER_PARAMETERS = 'userParameters'
    public static final String PARAM_ANALYSIS_CONSTRAINTS = 'analysisConstraints'
    public static final String PARAM_USER_IN_CONTEXT = 'currentUser'

    public static final String BEAN_USER_PARAMETERS = PARAM_USER_PARAMETERS
    public static final String BEAN_ANALYSIS_CONSTRAINTS = PARAM_ANALYSIS_CONSTRAINTS
    public static final String BEAN_USER_IN_CONTEXT = 'currentUserJobScoped'

    JobDataMap jobDataMap

    private static ThreadLocal<Map<String, Object>> BEANS_MAP =
        new NamedThreadLocal<Map<String, Object>>('JobScope') {
        protected Map<String, Object> initialValue() { [:] }
    };

    private static ThreadLocal<String> BOUND_JOB_NAME = new ThreadLocal()

    static Map<String, Object> getBEANS_STORAGE() {
        BEANS_MAP.get()
    }

    static String getCURRENT_JOB_NAME() {
        BOUND_JOB_NAME.get()
    }

    void execute(JobExecutionContext context) throws JobExecutionException {
        jobDataMap = context.jobDetail.jobDataMap

        String jobName = jobDataMap[PARAM_JOB_NAME]
        BOUND_JOB_NAME.set jobName

//	logger.info 'execute jobName {} context {}', jobName, context

        setupDefaultScopeBeans()

        PersistenceContextInterceptor interceptor
        try {
            interceptor = Holders.applicationContext.persistenceInterceptor
            interceptor.init()

            AbstractAnalysisJob job
            try {
//		logger.info 'execute calling createAnalysisJob'
                job = createAnalysisJob()
            }
            catch (e) {
                logger.error 'Exception while creating the analysis job', e
                jobResultsService[jobName]['Exception'] = e.message
                asyncJobService.updateStatus jobName, 'Error'
                return
            }

            try {
//		logger.info 'execute calling job.run job: {}', job
                job.run()
            }
            catch (e) {
                logger.error 'Some exception occurred in the processing pipe', e
                jobResultsService[jobName]['Exception'] = e.message
                job.updateStatus 'Error'
            }
        }
        finally {
            cleanJobBeans()
            interceptor.flush()
            interceptor.destroy()
//	    logger.info 'execute COMPLETED'
        }
    }

    void setupDefaultScopeBeans() {
        BEANS_STORAGE['jobName'] = CURRENT_JOB_NAME
        BEANS_STORAGE[BEAN_USER_PARAMETERS] = jobDataMap[PARAM_USER_PARAMETERS]
        BEANS_STORAGE[BEAN_ANALYSIS_CONSTRAINTS] = jobDataMap[PARAM_ANALYSIS_CONSTRAINTS]
        BEANS_STORAGE[BEAN_USER_IN_CONTEXT] = jobDataMap[PARAM_USER_IN_CONTEXT]
    }

    static void cleanJobBeans() {
        //remove all the beans once the job has finished
        //Is this necessary? Does Quartz reuse threads across jobs?
        BEANS_STORAGE.clear()
    }

    AbstractAnalysisJob createAnalysisJob() {
        Class jobClass = jobDataMap.jobClass

        AbstractAnalysisJob job = jobDataMap[PARAM_GRAILS_APPLICATION].mainContext.getBean jobClass

        /* wire things up */
        job.updateStatus = { String status, String viewerUrl = null ->
//            job.logger.info "updateStatus called for status:$status, viewerUrl:$viewerUrl"
            asyncJobService.updateStatus job.name, status, viewerUrl
        }
        job.setStatusList = { List<String> statusList ->
            jobResultsService[job.name]['StatusList'] = statusList
        }

        job.topTemporaryDirectory = new File(Holders.config.RModules.tempFolderDirectory)
        job.scriptsDirectory = new File(Holders.config.RModules.pluginScriptDirectory)

        job.studyName = i2b2ExportHelperService.findStudyAccessions([jobDataMap.result_instance_id1])

        job
    }

    ApplicationContext getMainContext() {
        jobDataMap[PARAM_GRAILS_APPLICATION].mainContext
    }

    def getAsyncJobService() {
        mainContext.asyncJobService
    }

    JobResultsService getJobResultsService() {
        mainContext.jobResultsService
    }

    def getI2b2ExportHelperService() {
        mainContext.i2b2ExportHelperService
    }
}
