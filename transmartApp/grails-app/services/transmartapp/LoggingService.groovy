package transmartapp

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.apache.log4j.RollingFileAppender
import org.springframework.security.crypto.codec.Hex
import org.springframework.util.FileCopyUtils

import javax.servlet.http.HttpServletResponse
import java.nio.file.Files
import java.security.MessageDigest

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class LoggingService {

	static transactional = false

	private static final List<String> SYMLINKED_APPENDER_FILE_NAMES = ['app.log', 'sql.log', 'stacktrace.log'].asImmutable()

	public static final Map<String, Level> LOG_LEVELS = [
			ALL:   Level.ALL,
			TRACE: Level.TRACE,
			DEBUG: Level.DEBUG,
			INFO:  Level.INFO,
			WARN:  Level.WARN,
			ERROR: Level.ERROR,
			FATAL: Level.FATAL,
			OFF:   Level.OFF].asImmutable()

	/**
	 * Called at startup to create duplicate file appenders for files that are
	 * symlinked in Docker to stderr/stdout which makes the content unreachable
	 * from inside the container. Near-duplicate rolling file appenders are
	 * added to loggers that have file appenders for symlinked files to write
	 * all log entries to both places.
	 */
	void duplicateSymlinkedAppenders() {
		for (Map.Entry<FileAppender, Logger> entry in allFileAppenders()) {
			File file = new File(entry.key.file)
			if (SYMLINKED_APPENDER_FILE_NAMES.contains(file.name) && isSymLink(file)) {
				duplicateSymlinkedAppender entry.key, entry.value, file
			}
		}
	}

	void setLogLevel(String logger, String level) {
		LogManager.getLogger(logger).level = LOG_LEVELS[level]
	}

	boolean downloadFile(String id, HttpServletResponse response) {
		String path = files[id]
		if (!path) {
			return false
		}

		File file = new File(path)
		response.contentType = 'text/plain'
		response.setHeader 'Content-disposition', 'attachment; filename=' + file.name
		FileCopyUtils.copy new FileInputStream(file), response.outputStream
		true
	}

	Map<String, String> getFiles() {
		Set<String> paths = []
		for (FileAppender appender in allFileAppenders().keySet()) {
			File file = new File(appender.file)
			if (!isSymLink(file)) {
				paths << file.absolutePath
			}
		}

		for (String path in ([] + paths)) {
			File logFile = new File(path)
			for (File file in logFile.parentFile.listFiles()) {
				if (file.file && file.name ==~ /${logFile.name}\.\d+/) {
					paths << file.absolutePath
				}
			}
		}

		Map<String, String> filesByHash = [:]
		for (String path in paths.sort()) {
			filesByHash[md5(path)] = path
		}
		filesByHash
	}

	private Map<FileAppender, Logger> allFileAppenders() {
		Map<FileAppender, Logger> appenders = [:]
		for (Logger logger in allLoggers()) {
			for (appender in logger.allAppenders) {
				if (appender instanceof FileAppender) {
					appenders[(FileAppender) appender] = logger
				}
			}
		}
		appenders
	}

	Map<String, String> loggersAndLevels() {
		Map<String, String> map = [:]
		for (Logger logger in allLoggers()) {
			map[logger.name] = logger.effectiveLevel.toString()
		}
		map
	}

	private List<Logger> allLoggers() {
		List<Logger> loggers = [LogManager.rootLogger]
		loggers.addAll LogManager.currentLoggers.toList().sort { ((Logger) it).name }
		loggers
	}

	private boolean isSymLink(File file) {
		Files.isSymbolicLink file.toPath()
	}

	private void duplicateSymlinkedAppender(FileAppender appender, Logger logger, File file) {
		if (appender instanceof RollingFileAppender) {
			appender.maximumFileSize = (long) (Long.MAX_VALUE * 0.9) // disable rollover
		}
		RollingFileAppender newAppender = new RollingFileAppender(appender.layout, (file.path - '.log') + '_file.log')
		newAppender.maxBackupIndex = 20
		newAppender.maximumFileSize = 1024 * 1024 // 1 MB
		newAppender.activateOptions()
		logger.addAppender newAppender
	}

	private String md5(String s) {
		new String(Hex.encode(MessageDigest.getInstance('MD5').digest(s.bytes)))
	}
}
