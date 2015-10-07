package heim

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.rosuda.REngine.Rserve.RConnection
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(smartR.plugin.SmartRService)
class RinitSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test running init.r of Heatmap workflow"() {
        given: "SmartRService with mocked getWebAppFolder() and simple params object"
        def rServePort = 6311
        def rServeHost = "localhost"
        def connection = new RConnection(rServeHost, rServePort)
        def script = ScriptManagerService.readScript("heatmap", "init.r")


        when:
        def result = connection.eval(script)
        result = result.asString()
        then:
        result == '[{"variableName":"expression","variableType":"High-Dimension"},{"variableName":"patients","variableType":"Patient-Set"}]'
    }


}
