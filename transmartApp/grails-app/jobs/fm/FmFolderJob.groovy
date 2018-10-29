package fm

import grails.util.Holders
import groovy.util.logging.Slf4j

@Slf4j('logger')
class FmFolderJob {

    def fmFolderService

    static triggers = {
        def startDelay = Holders.config.com.recomdata.FmFolderJob.startDelayMs
        def cronExpression = Holders.config.com.recomdata.FmFolderJob.cronExpression

        if (startDelay instanceof String) {
            try {
                startDelay = Integer.parseInt(startDelay)
            } catch (NumberFormatException nfe) {
                logger.error("Folder job not initialized. Configuration not readable")
            }
        } else {
            startDelay = null
        }
        cron name: 'FmFolderJobTrigger',
                cronExpression: (cronExpression instanceof String) ? cronExpression : '0 0/5 * * * ?',
                startDelay: startDelay != null ? startDelay : 60000


    }

    def execute() {
        fmFolderService.importFiles();
    }
}
