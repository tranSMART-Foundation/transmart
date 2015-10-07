package heim

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import heim.RServeSessionExecutor
import heim.RServeThread
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class RServeSessionExecutorSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test RServeSessionExecutor"() {
        given:""
            RServeSessionExecutor executor = new RServeSessionExecutor()
            RServeThread exampleThread1 = new RServeThread("heatmap/init.r")
            RServeThread exampleThread2 = new RServeThread("heatmap/run.r")
            RServeThread exampleThread3 = new RServeThread("finalize/run.r")
        when:""
            executor.execute(exampleThread1)
            executor.execute(exampleThread2)
            executor.execute(exampleThread3)
        then:""
            true == true
    }
}
