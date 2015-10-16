package heim

import org.springframework.http.HttpStatus

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.Assume.assumeThat

class RSessionControllerTests extends BaseAPITestCase {

    void testCreate() {
        post('/RSession/create') {
            body json: [
                workflow: 'heatmap'
            ]
        }
        assertStatus HttpStatus.CREATED.value()

        assertThat JSON, hasEntry(equalTo('sessionId'), isA(String))
    }

    void testDestroy() {
        post('/RSession/create') {
            body json: [
                    workflow: 'heatmap',
            ]
        }
        assumeThat client.responseStatus, is(201)
        def sessionId = JSON.sessionId

        post('/RSession/delete') {
            body json: [
                    sessionId: sessionId,
            ]
        }

        assertStatus HttpStatus.ACCEPTED.value()
    }
}
