package heim

import com.grailsrocks.functionaltest.APITestCase

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasEntry
import static org.hamcrest.Matchers.notNullValue

class RSessionControllerTests extends APITestCase {

    void testCreate() {
        post("${baseURL}RSession/create")
        assertStatus(200)
        assertThat JSON, hasEntry(equalTo('sessionId'), notNullValue())
    }
}
