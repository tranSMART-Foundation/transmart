package heim.tasks

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import heim.SmartRExecutorService
import heim.session.SessionContext
import heim.session.SessionService
import heim.session.SmartRSessionScope
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.NoSuchResourceException

import java.lang.reflect.UndeclaredThrowableException
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

/**
 * Created by glopes on 09-10-2015.
 */
@Component
@SmartRSessionScope
@Slf4j('logger')
class JobTasksService implements DisposableBean {

    @Autowired
    private SmartRExecutorService smartRExecutorService

    @Autowired
    private SessionService sessionService

    @Value('#{sessionId}')
    private UUID sessionId

    private volatile boolean shuttingDown

    @ToString(includePackage = false, includes = ['state', 'taskResult'], includeNames = true)
    static final class TaskAndState {
        final Task task
        final TaskState state
        final TaskResult taskResult

        TaskAndState(Map<String, ? extends Object> args) {
            this.task = args.task
            this.state = args.state
            this.taskResult = args.taskResult
        }
    }

    private final Map<UUID, TaskAndState> tasks = new ConcurrentHashMap<>()
    private final Map<UUID, ListenableFuture<TaskResult>> futures =
            new ConcurrentHashMap<>() // future removed when task finishes/fails
    private final Map<UUID, SettableFuture<TaskResult>> publicFutures =
            new ConcurrentHashMap<>()

    boolean hasActiveTasks() {
        !futures.isEmpty()
    }

    void submitTask(Task task) {
        if (shuttingDown) {
            throw new IllegalStateException('Shutting down already')
        }

        def taskAndState = new TaskAndState(
                task: task,
                state: TaskState.QUEUED)

        tasks[task.uuid] = taskAndState

        logger.debug 'Task uuid {} will be submitted now', task.uuid
        ListenableFuture<TaskResult> future = smartRExecutorService.submit(
                new Callable() {
                    @Override
                    Object call() throws Exception {
                    logger.debug 'Task {} entered running state', task.uuid
                        tasks[task.uuid] = new TaskAndState(
                                task: task,
                                state: TaskState.RUNNING,
                        )

                        sessionService.doWithSession(sessionId) {
                            task.call()
                        }
                    }
                })

//	logger.info 'Task {} running', task.uuid
        futures[task.uuid] = future

        def publicFuture = new SettableFuture<TaskResult>()
        publicFutures[task.uuid] = publicFuture
        Futures.addCallback(future, new FutureCallback<TaskResult>() {
            void onSuccess(TaskResult taskResult1) {
                assert taskResult1 != null :
                        'Task must return TaskResult or throw'
                logger.debug 'Task {} terminated without throwing. Successful? {}', task.uuid, taskResult1.successful
                tasks[task.uuid] = new TaskAndState(
                        task: task,
                        state: taskResult1.successful ?
                                TaskState.FINISHED :
                                TaskState.FAILED,
                        taskResult: taskResult1)
                common(tasks[task.uuid])
            }
            void onFailure(Throwable thrown) {
                if (thrown instanceof UndeclaredThrowableException) {
                    thrown = thrown.undeclaredThrowable
                }
                if (thrown instanceof CancellationException) {
                    logger.debug 'Task {} was canceled', task
                }
                else {
                    logger.debug 'Task {} terminated by throwing {}', task, thrown
                }

                tasks[task.uuid] = new TaskAndState(
                        task: task,
                        state: TaskState.FAILED,
                        taskResult: new TaskResult(
                                successful: false,
                                exception: thrown,
                        ))
                common(tasks[task.uuid])
            }

            private void common(TaskAndState result) {
                futures.remove(task.uuid)
                try {
                    task.close()
                }
                catch (Exception e) {
                    logger.error('Failed calling close() on task ' + task, e)
                }
                publicFuture.set(result.taskResult)
                sessionService.touchSession(sessionId) // should not throw
                logger.info 'Task {} finished. Final result: {}', task.uuid, result
            }
        }, MoreExecutors.directExecutor()) // run on the same thread
    }

    ListenableFuture<TaskResult> getTaskResultFuture(UUID taskId) {
        publicFutures.get(taskId)
    }

    TaskState getTaskState(UUID uuid) {
        tasks.get(uuid)?.state
    }

    TaskResult getTaskResult(UUID uuid) {
        tasks.get(uuid)?.taskResult
    }

    @Override
    void destroy() throws Exception {
        // try to interrupt/cancel all the running tasks
        shuttingDown = true

        futures.values().each {
            it.cancel(true)
        }
    }
}
