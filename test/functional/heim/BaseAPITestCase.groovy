package heim

import com.grailsrocks.functionaltest.APITestCase

class BaseAPITestCase extends APITestCase {

    protected void setUp() {
        super.setUp()
        //To fix "org.apache.http.conn.HttpHostConnectException: Connection to http://localhost:8080 refused"
        baseURL = baseURL.replace('localhost', '127.0.0.1')
    }
}
