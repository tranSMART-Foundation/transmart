package sanofi

import com.recomdata.transmart.data.export.SweepingService
import grails.util.Holders
import groovy.util.logging.Slf4j

@Slf4j('logger')
class FileSweepJob {

    SweepingService sweepingService

    static triggers = {
        def startDelay = Holders.config.com.recomdata.export.jobs.sweep.startDelay
        def repeatInterval = Holders.config.com.recomdata.export.jobs.sweep.repeatInterval
        if (startDelay instanceof String) {
            try {
                startDelay = Integer.parseInt(startDelay)
                repeatInterval = Integer.parseInt(repeatInterval)
            }
	    catch (NumberFormatException ignored) {}
        }

        if (startDelay instanceof Integer) {
            simple name: 'fileSweepTrigger', startDelay: startDelay, repeatInterval: repeatInterval
        }
    }

    void execute() {
        sweepingService.sweep()
    }
}
