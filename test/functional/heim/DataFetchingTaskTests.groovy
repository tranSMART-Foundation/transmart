package heim

import com.google.common.collect.Maps
import heim.tasks.DataFetchTaskFactory
import heim.tasks.TaskState
import org.transmartproject.db.dataquery.highdim.mrna.MrnaTestData

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * Tests tasks of type 'dataFetch'
 */
class DataFetchingTaskTests extends BaseAPITestCase {

    private static final String DATA_FETCH_TASK_TYPE = DataFetchTaskFactory.FETCH_DATA_TASK_NAME

    public static final LinkedHashMap<String, Serializable> TEST_ARGUMENTS = [
            conceptKeys      : [
                    test_label: '\\\\i2b2 main\\foo\\study1\\bar\\',
            ],
            dataType         : 'mrna',
            resultInstanceIds: [], //TODO: the test data has no result set for the mrna patients
            assayConstraints : [
                    trial_name: [name: MrnaTestData.TRIAL_NAME],
            ],
            dataConstraints  : [
                    genes: [names: ['BOGUSRQCD1']]
            ],
            projection       : 'zscore',
    ]

    private String getLabelSubset1() {
        TEST_ARGUMENTS.conceptKeys.keySet().first() + '_s1'
    }

    private String getLabelSubset2() {
        TEST_ARGUMENTS.conceptKeys.keySet().first() + '_s2'
    }

    void testMrnaDataFetching() {
        String sessionId = createSession('func_test')

        post '/ScriptExecution/run', {
            body json: [
                    sessionId: sessionId,
                    taskType: DATA_FETCH_TASK_TYPE,
                    arguments: TEST_ARGUMENTS,
            ]
        }

        assertStatus 200

        String taskId = JSON.executionId
        get '/ScriptExecution/status?' + buildQueryParameters(
                sessionId:         sessionId,
                executionId:       taskId,
                waitForCompletion: true,
        )

        assertStatus 200
        assertThat JSON, hasEntry(is('state'), is(TaskState.FINISHED.toString()))
        assertThat JSON.result.artifacts.currentLabels, contains(labelSubset1)
    }

    void testRefetchingReplacesDataset() {
        // instead of adding a new one
        String sessionId = createSession('func_test')

        2.times {
            post '/ScriptExecution/run', {
                body json: [
                        sessionId: sessionId,
                        taskType : DATA_FETCH_TASK_TYPE,
                        arguments: TEST_ARGUMENTS,
                ]
            }

            assertStatus 200

            String taskId = JSON.executionId
            get '/ScriptExecution/status?' + buildQueryParameters(
                    sessionId: sessionId,
                    executionId: taskId,
                    waitForCompletion: true,
            )

            assertStatus 200
            assertThat JSON, hasEntry(is('state'), is(TaskState.FINISHED.toString()))
            assertThat JSON.result.artifacts.currentLabels, contains(labelSubset1)
        }
    }

    void testFetchingMultipleSubsets() {
        String sessionId = createSession('func_test')

        get '/smartRTest/resultInstanceIds'
        assertStatus 200

        def args = Maps.newHashMap(TEST_ARGUMENTS)
        args.resultInstanceIds = JSON.values


        post '/ScriptExecution/run', {
            body json: [
                    sessionId: sessionId,
                    taskType : DATA_FETCH_TASK_TYPE,
                    arguments: args,
            ]
        }

        assertStatus 200

        String taskId = JSON.executionId
        get '/ScriptExecution/status?' + buildQueryParameters(
                sessionId: sessionId,
                executionId: taskId,
                waitForCompletion: true,
        )

        assertStatus 200
        assertThat JSON, hasEntry(is('state'), is(TaskState.FINISHED.toString()))
        assertThat JSON.result.artifacts.currentLabels, containsInAnyOrder(labelSubset1, labelSubset2)
    }

    void testOnlySubset2() {
        String sessionId = createSession('func_test')

        get '/smartRTest/resultInstanceIds'
        assertStatus 200

        def args = Maps.newHashMap(TEST_ARGUMENTS)
        args.resultInstanceIds = [null, JSON.values[1]]


        post '/ScriptExecution/run', {
            body json: [
                    sessionId: sessionId,
                    taskType : DATA_FETCH_TASK_TYPE,
                    arguments: args,
            ]
        }

        assertStatus 200

        String taskId = JSON.executionId
        get '/ScriptExecution/status?' + buildQueryParameters(
                sessionId: sessionId,
                executionId: taskId,
                waitForCompletion: true,
        )

        assertStatus 200
        assertThat JSON, hasEntry(is('state'), is(TaskState.FINISHED.toString()))
        assertThat JSON.result.artifacts.currentLabels, containsInAnyOrder(labelSubset2)
    }
}
