package heim.session

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Sets
import com.google.common.collect.Table
import com.google.common.collect.Tables
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.NoSuchResourceException

/**
 * A holder for the files that were created during the session,
 * mapping the logical task/filename combination to a file path.
 */
@Component
@SmartRSessionScope
class SessionFiles {

    private Table<UUID /* task */, String /* filename */, File> files =
            HashBasedTable.create()

    void add(UUID task, String filename, File file) {
        files.put(task, filename, file)
    }

    File get(UUID taskId, String filename) throws NoSuchResourceException {
        files.get(taskId, filename)
    }

}
