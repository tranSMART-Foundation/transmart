package heim

import heim.tasks.TaskState

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * Created by piotrzakrzewski on 13/10/15.
 */
class ScriptExecutionControllerTests extends BaseAPITestCase {


    private static final String SAMPLE_DATA_TO_PASS = 'sample data to pass'
    private static final String SAMPLE_DATA_TO_WRITE = 'sample data to write'

    private Map runSampleR() {
        String sessionId = createSession()
        post '/ScriptExecution/run', {
            body json: [
                    sessionId: sessionId,
                    taskType: 'sample',
                    arguments: [
                            data_to_pass: SAMPLE_DATA_TO_PASS,
                            data_to_write: SAMPLE_DATA_TO_WRITE,
                    ]
            ]
        }

        assertStatus 200
        assertThat JSON, hasEntry(
                equalTo('executionId'),
                isA(String))

        String taskId = JSON.executionId
        get '/ScriptExecution/status?' + buildQueryParameters(
                sessionId:         sessionId,
                executionId:       taskId,
                waitForCompletion: true,
        )

        [sessionId: sessionId, taskId: taskId]
    }

    void testRun() {
        runSampleR()

        assertStatus 200
        assertThat JSON, allOf(
                hasEntry(is('state'), is(TaskState.FINISHED.toString())),
                hasEntry(is('result'), allOf(
                        hasEntry(is('successful'), is(true)),
                        hasEntry(is('exception'), is(null)), // not is(nullValue()); it's a JSONObject.Null
                        hasEntry(is('artifacts'), isA(Map))
                )))

        Map artifacts = JSON.result.artifacts
        assertThat artifacts['files'], contains('test_file')
        assert artifacts['a'] == (1 as Double)
        assert artifacts['b'] == 'foobar'
        assert artifacts['passed'] == SAMPLE_DATA_TO_PASS
    }

    void testRetrieveFile() {
        Map m = runSampleR()

        get '/ScriptExecution/downloadFile?' + buildQueryParameters(
                sessionId:         m.sessionId,
                executionId:       m.taskId,
                filename:          JSON.result.artifacts.files[0],
        )

        assertThat content, containsString(SAMPLE_DATA_TO_WRITE)
    }
}
