import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.GwasPage

import functions.Utilities

import geb.spock.GebSpec
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class GwasPageSpec extends GebReportingSpec {

    // Assume a standard set of GWAS tab metadata has been loaded
    // using transmart-data and supplied loading scripts

    def util = new Utilities()

    def "start on the gwas tab"() {

        when:
        util.goToPageMaybeLogin(GwasPage)

        then:
        assert at(GwasPage)

    }


}
