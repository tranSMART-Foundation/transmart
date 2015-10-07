package smartR.plugin

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import heim.RServeSessionExecutor
import heim.RServeSessionsManager
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class RServeSessionsManagerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test allocate new session"() {
        given:
            def rServeSessionManager = new RServeSessionsManager()
        when:
            String id = rServeSessionManager.createNewSession()
        then:
            rServeSessionManager[id] != null
            rServeSessionManager[id] instanceof RServeSessionExecutor
    }
}
