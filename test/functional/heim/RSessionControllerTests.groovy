package heim

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasEntry
import static org.hamcrest.Matchers.notNullValue

class RSessionControllerTests extends BaseAPITestCase {

    void testCreate() {
        post("${baseURL}RSession/create")
        assertStatus(200)
        assertThat JSON, hasEntry(equalTo('sessionId'), notNullValue())
    }
}
