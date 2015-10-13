package heim

import com.grailsrocks.functionaltest.APITestCase

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.equalToIgnoringCase
import static org.hamcrest.Matchers.hasEntry
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.notNullValue

/**
 * Created by piotrzakrzewski on 13/10/15.
 */
class ScriptExecutionControllerTests extends APITestCase{


    void testRun(){
        post("${baseURL}RSession/create")
        String sessionId = JSON['sessionId']
        String workflow = 'heatmap'
        post "${baseURL}ScriptExecution/run", {

            def runParams = [sessionId:sessionId,workflow:workflow]
            json(runParams)
        }
        assertStatus(200)
        assertThat JSON, hasEntry(equalTo('scriptExecutionId'), notNullValue())
    }

    void testFiles(){
        post("${baseURL}RSession/create")
        String sessionId = JSON['sessionId']
        String workflow = 'heatmap'
        post "${baseURL}ScriptExecution/run", {

            def runParams = [sessionId:sessionId,workflow:workflow]
            json(runParams)
        }
        String executionId = JSON['scriptExecutionId']
        sleep(5000)
        post "${baseURL}ScriptExecution/files", {

            def runParams = [sessionId:sessionId,executionId:executionId]
            json(runParams)
        }
        assertStatus(200)
        assertThat JSON, hasEntry(equalToIgnoringCase('files'),notNullValue())
    }
}
