package heim.session

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.NoSuchResourceException

import java.nio.file.NoSuchFileException

/**
 * A holder for the files that were created during the session,
 * mapping the logical task/filename combination to a file path.
 */
@Component
@SmartRSessionScope
@Slf4j('logger')
class SessionFiles {

    private Table<UUID /* task */, String /* filename */, File> files =
            HashBasedTable.create()

    void add(UUID task, String filename, File file) {
        files.put(task, filename, file)
    }

    File get(UUID taskId, String filename) throws NoSuchResourceException {
        files.get(taskId, filename)
    }

    void removeAll() {
	logger.debug 'removeAll'
        try {
            for (File f: files.values()) {
		logger.debug 'delete file {}', f
                f.delete()
            }
        }
        catch(NoSuchFileException e) {
	    logger.debug 'Exception {}', e
	}
        finally {
	    logger.debug 'removeAll done, create empty HashBasedTable'
            files = HashBasedTable.create()
        }
    }
}
