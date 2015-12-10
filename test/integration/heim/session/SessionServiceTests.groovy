package heim.session

import com.google.common.collect.ImmutableMap
import grails.test.mixin.TestMixin
import groovy.util.logging.Log4j
import heim.jobs.JobInstance
import heim.tasks.TaskResult
import heim.tasks.TaskState
import org.gmock.WithGMock
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.users.User
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin

import java.util.concurrent.atomic.AtomicBoolean

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.AllOf.allOf

/**
 * Created by glopes on 13-10-2015.
 */
@TestMixin(RuleBasedIntegrationTestMixin)
@WithGMock
@Log4j
class SessionServiceTests {

    public static final String TEST_JOB_TYPE = 'test'

    @Autowired
    private SessionService testee

    @Autowired
    private JobInstance jobInstance

    @Autowired
    ApplicationContext applicationContext

    /**
     * Test that a call to createSession() creates a session and the appropriate
     * Spring context.
     */
    @Test
    void testCreateSession() {
        User user = mock(User)
        String jobType = 'heatmap'

        play {
            def sessionUUID = testee.createSession(user, jobType)

            assertThat sessionUUID, isA(UUID)

            testee.doWithSession(sessionUUID) {
                assertThat jobInstance.workflow, is(jobType)
            }
        }
    }

    @Test
    void testRunTaskGetData() {
        User user = mock(User)

        AtomicBoolean b = new AtomicBoolean()
        def monitor = new Object()

        play {
            def sessionUUID = testee.createSession(user, TEST_JOB_TYPE)
            def taskUUID = testee.createTask(sessionUUID, TEST_JOB_TYPE, closure: {
                b.set(true)
                new TaskResult(
                        successful: true,
                        artifacts: ImmutableMap.of('foo', 'bar'),)
            }, monitor: monitor)

            // wait until the the task is closed
            synchronized (monitor) {
                monitor.wait(2000L) // technically we should loop on a condition
            }

            assertThat taskUUID, isA(UUID)
            assertThat b.get(), is(true)

            def taskData = testee.getTaskData(sessionUUID, taskUUID)
            assertThat taskData, allOf(
                    hasEntry(is('state'), is(TaskState.FINISHED)),
                    hasEntry(is('result'), allOf(
                            isA(TaskResult),
                            hasProperty('successful', is(true)),
                            hasProperty('exception', is(nullValue(Exception))),
                            hasProperty('artifacts', allOf(
                                    hasEntry(is('foo'), is('bar'))
                            )))))
        }
    }

    @Test
    void testRunTaskGetDataWithWaiting() {
        User user = mock(User)

        play {
            def sessionUUID = testee.createSession(user, TEST_JOB_TYPE)
            def taskUUID = testee.createTask(sessionUUID, TEST_JOB_TYPE, closure: {
                new TaskResult(
                        successful: true,
                        artifacts: ImmutableMap.of('foo', 'bar'),)
            }, monitor: new Object())

            assertThat taskUUID, isA(UUID)

            def taskData = testee.getTaskData(sessionUUID, taskUUID, true)
            assertThat taskData, allOf(
                    hasEntry(is('state'), is(TaskState.FINISHED)),
                    hasEntry(is('result'),
                            hasProperty('artifacts', allOf(
                                    hasEntry(is('foo'), is('bar'))
                            ))))
        }
    }


    @Test
    void testDestroySessionBasic() {
        User user = mock(User)

        play {
            def sessionUUID = testee.createSession(user, TEST_JOB_TYPE)
            testee.destroySession(sessionUUID)

            assertThat testee.isSessionActive(sessionUUID), is(false)
            try {
                testee.doWithSession(sessionUUID) {
                    fail('Should not be reached')
                }
                fail('Should not be reached (outside doWithSession)')
            } catch (NoSuchResourceException state) {
                // ok
            }
        }
    }

    @Test
    void testDestroySessionInterruptsTaskThread() {
        User user = mock(User)

        AtomicBoolean closed = new AtomicBoolean()
        AtomicBoolean interrupted = new AtomicBoolean()
        Object monitor = new Object()
        boolean taskStarted = false

        play {
            def sessionUUID = testee.createSession(user, TEST_JOB_TYPE)
            testee.createTask(sessionUUID, TEST_JOB_TYPE, closure: {
                synchronized (monitor) {
                    taskStarted = true
                    monitor.notify()
                }

                try {
                    log.debug('Task going to sleep now')
                    Thread.sleep(5000L)
                    throw new RuntimeException('Waiting time exceeded')
                } catch (InterruptedException ie) {
                    log.info('Got interrupted as expected')
                    interrupted.set(true)
                }
            }, monitor: monitor, closed: closed)

            /* first wait for the task to start before destroying the session */
            int i = 5
            while (!taskStarted && --i > 0) {
                synchronized (monitor) {
                    monitor.wait(1000)
                }
            }
            if (!taskStarted) {
                fail('Exceeded max time we\'re willing to' +
                        'wait for the task to start')
            }

            testee.destroySession(sessionUUID)

            /* wait for close() on the task to be called */
            synchronized (monitor) {
                monitor.wait(6000L)
            }

            assertThat closed.get(), is(true)
            assertThat interrupted.get(), is(true)
        }
    }

    @Test
    void testUnsuccessfulNotThrowingTaskIsMarkedFailed() {
        User user = mock(User)

        play {
            def sessionUUID = testee.createSession(user, TEST_JOB_TYPE)
            def taskUUID = testee.createTask(sessionUUID, TEST_JOB_TYPE, closure: {
                new TaskResult(
                        successful: false,
                        artifacts: ImmutableMap.of(),)
            }, monitor: new Object())

            assertThat taskUUID, isA(UUID)

            def taskData = testee.getTaskData(sessionUUID, taskUUID, true)
            assertThat taskData, allOf(
                    hasEntry(is('state'), is(TaskState.FAILED)))
        }
    }

    @Test
    void testGarbageCollectionBasic() {
        User user = mock(User)
        play {
            def sessionUUID = testee.createSession(user, TEST_JOB_TYPE)
            testee.doWithSession(sessionUUID) {
                SessionContext sc = SmartRSessionSpringScope.ACTIVE_SESSION.get()
                sc.@lastActive.set(new Date(new Date().time - 20 * 60 * 1000 /* 20 min */))
            }
            testee.garbageCollection()

            try {
                testee.doWithSession(sessionUUID) {}
                Assert.fail('Expected exception')
            } catch(NoSuchResourceException nre) {}
        }
    }
}
