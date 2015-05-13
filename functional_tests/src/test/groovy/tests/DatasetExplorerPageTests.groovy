package tests
import geb.junit4.GebReportingTest

import org.junit.Test

import functions.Constants

import pages.LoginPage
import pages.DatasetExplorerPage

class DatasetExplorerPageTests extends CheckLoginPageAbstract {

	@Test
	void datasetExplorerTargetTest() {
        goToPageMaybeLogin(DatasetExplorerPage)
		assert at(DatasetExplorerPage)
	}

}
