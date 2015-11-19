package heim.session

import grails.util.Holders
import groovy.util.logging.Log4j
import heim.SmartRExecutorService
import heim.jobs.JobInstance
import heim.tasks.JobTasksService
import heim.tasks.TaskResult
import heim.tasks.TaskState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.users.User

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Public service with the controller is to communicate.
 */
@Component
@Log4j
class SessionService {

    @Autowired
    SmartRExecutorService smartRExecutorService

    @Autowired
    private JobTasksService jobTasksService // session scoped

    @Autowired
    private JobInstance jobInstance // session scoped

    @Autowired
    private SessionFiles sessionFiles

    private final ConcurrentMap<UUID, SessionContext> currentSessions =
            new ConcurrentHashMap<>()

    private final Set<UUID> sessionsShuttingDown = ([] as Set).asSynchronized()

    List<String> availableWorkflows() {
        File dir = Holders.config.smartR.pluginScriptDirectory
        dir.listFiles({ File f -> f.isDirectory() } as FileFilter)*.name
    }

    @Deprecated
    List<String> legacyWorkflows() {
        File dir = Holders.config.smartR.legacyPluginScriptDirectory
        dir.listFiles()*.name
    }

    UUID createSession(User user, String workflowType) {
        SessionContext newSession = new SessionContext(user, workflowType)
        log.debug("Created session with id ${newSession.sessionId} and " +
                "workflow type $workflowType")

        currentSessions[newSession.sessionId] = newSession

        newSession.sessionId
    }

    void destroySession(UUID sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId must be given")
        }
        SessionContext sessionContext = fetchOperationalSessionContext(sessionId)
        if (!sessionContext) {
            throw new InvalidArgumentsException(
                    "No such operational session: $sessionId")
        }

        sessionsShuttingDown << sessionId

        smartRExecutorService.submit({
            SmartRSessionSpringScope.withActiveSession(sessionContext) {
                log.debug(
                        "Running destruction callbacks for session $sessionId")
                sessionContext.destroy()
                log.debug("Finished running destruction " +
                        "callbacks for session $sessionId")
                currentSessions.remove(sessionId)
                sessionsShuttingDown.remove(sessionId)
            }
        } as Callable<Void>)

        log.debug("Submmitted session $sessionId for destruction")
    }

    UUID createTask(Map<String, Object> arguments,
                    UUID sessionId,
                    String taskType) {
        doWithSession(sessionId) {
            def task = jobInstance.createTask(taskType, arguments)
            jobTasksService.submitTask(task)
            task.uuid
        }
    }

    Map<String, Object> getTaskData(UUID sessionUUID,
                                    UUID taskUUID,
                                    boolean waitForCompletion = false) {
        doWithSession(sessionUUID) {
            def state = jobTasksService.getTaskState(taskUUID)
            if (!state) {
                throw new NoSuchResourceException(
                        "No task $taskUUID for session $sessionUUID")
            }

            TaskResult result
            if (waitForCompletion) {
                result = jobTasksService.getTaskResultFuture(taskUUID).get()
                if (state == TaskState.QUEUED || state == TaskState.RUNNING) {
                    state = jobTasksService.getTaskState(taskUUID)
                }
            } else {
                // not great code, this may be out of sync with the state we got before
                result = jobTasksService.getTaskResult(taskUUID)
            }

            [
                    state: state,
                    result: result, // null if the task has not finished
            ]
        }
    }

    public <T> T doWithSession(UUID sessionUUID, Closure<T> closure) {
        SessionContext context = fetchOperationalSessionContext(sessionUUID)
        if (context == null) {
            throw new NoSuchResourceException(
                    "No such operational session: $sessionUUID")
        }

        SmartRSessionSpringScope.withActiveSession(
                context, closure)
    }

    File getFile(UUID sessionUUID, UUID taskId, String filename) {
        def res = doWithSession(sessionUUID) {
            sessionFiles.get(taskId, filename)
        }
        if (res == null) {
            throw new NoSuchResourceException(
                    "No file '$filename' for sesison $sessionUUID and task $taskId")
        }
        res
    }

    public boolean isSessionActive(UUID sessionId) {
        currentSessions.containsKey(sessionId) &&
                !(sessionId in sessionsShuttingDown)
    }

    private SessionContext fetchOperationalSessionContext(UUID sessionId) {
        SessionContext sessionContext = fetchSessionContext(sessionId)
        if (sessionContext == null) {
            return null
        }
        if (sessionId in sessionsShuttingDown) {
            log.warn("Session is shutting down: $sessionId")
            return null
        }
        sessionContext
    }

    private SessionContext fetchSessionContext(UUID sessionId) {
        SessionContext sessionContext = currentSessions[sessionId]
        if (!sessionContext) {
            log.warn("No such session: $sessionId")
            return null
        }
        sessionContext
    }

}
