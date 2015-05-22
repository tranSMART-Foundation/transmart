import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.GenesigPage

import functions.Utilities

import geb.spock.GebSpec
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class GenesigPageSpec extends GebReportingSpec {

    // Assume a standard set of gene signature/list metadata has been loaded
    // using transmart-data and supplied loading scripts

    def util = new Utilities()

    def "start on the gene signatures/lists tab"() {

        when:
        util.goToPageMaybeLogin(GenesigPage)

        then:
        assert at(GenesigPage)

    }


}
