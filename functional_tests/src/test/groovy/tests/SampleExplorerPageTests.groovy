import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.SampleExplorerPage

import functions.Utilities

import geb.spock.GebSpec
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class SampleExplorerPageSpec extends GebReportingSpec {

    // Assume a standard set of sample explorer tab metadata has been loaded
    // using transmart-data and supplied sql loading scripts

    def util = new Utilities()

    def "start on the sample explorer tab"() {

        when:
        util.goToPageMaybeLogin(SampleExplorerPage)

        then:
        assert at(SampleExplorerPage)

    }


}
