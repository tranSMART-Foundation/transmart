package transmartapp

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.custom.RequiresLevel
import org.transmart.plugin.custom.UserLevel

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Slf4j('logger')
class LoggingController {

	@Autowired private LoggingService loggingService

	@RequiresLevel(UserLevel.ADMIN)
	def index() {
		[allLevels: loggingService.LOG_LEVELS.keySet(),
		 files: loggingService.getFiles(),
		 loggers: loggingService.loggersAndLevels()]
	}

	@RequiresLevel(UserLevel.ADMIN)
	def setLogLevel(String logger, String level) {
		loggingService.setLogLevel logger, level
		redirect action: 'index'
	}

	@RequiresLevel(UserLevel.ADMIN)
	def downloadFile(String id) {
		if (!loggingService.downloadFile(id, response)) {
			redirect action: 'index'
		}
	}
}
