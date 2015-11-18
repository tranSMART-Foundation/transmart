package heim

import com.grailsrocks.functionaltest.APITestCase

import static org.hamcrest.Matchers.is
import static org.junit.Assume.assumeThat

abstract class BaseAPITestCase extends APITestCase {

    protected void setUp() {
        super.setUp()
        //To fix "org.apache.http.conn.HttpHostConnectException: Connection to http://localhost:8080 refused"
        baseURL = baseURL.replace('localhost', '127.0.0.1')
    }

    protected static String buildQueryParameters(Map map) {
        // you were supposes to be able to do:
        // get url, { foo = 'bar' }
        // but it seems to be broken
        // this is a workaround
        map.collect { k, v ->
            "$k=${URLEncoder.encode(v.toString(), 'UTF-8')}"
        }.join('&')
    }

    protected String /* session id */ createSession(String workflowName) {
        post('/RSession/create') {
            body json: [
                    workflow: workflowName
            ]
        }
        assumeThat client.responseStatus, is(201)
        JSON.sessionId
    }
}
