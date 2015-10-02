package heim

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import smartR.plugin.SmartRService
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(smartR.plugin.SmartRService)
class SmartRServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test getScriptsFolder method"() {
        given: "SmartRService with mocked getWebAppFolder()"
        def mock = [getWebAppFolder: {-> return "/web-app" }] as SmartRService
        when:
            def result = mock.getScriptsFolder()
        then:
            result == '/web-app/HeimScripts/'
    }
}
