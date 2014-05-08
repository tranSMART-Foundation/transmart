package tests
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import geb.junit4.GebReportingTest

import org.junit.Test

import pages.Constants
import pages.SearchPage
import pages.SearchResultsPage

class SearchPageTests extends GebReportingTest {

	//Note: for now, these tests assume auto-login
	//TODO: these test need to be made robust in the face of a possible login requirement 	

	@Test
	void clickOnDiseaseSelectsDisease(){
		// The first hit in the session always lands on the landingPage
		via (SearchPage)
		assert at(Constants.LANDING_PAGE.class)
		
		// after that, we can go to search page
		to (SearchPage)
		assert at(SearchPage)
		
		// click on a category
		String category = "disease"
		categoryBarItem(category).click()
		assert categorySelected.text() == category.toUpperCase() // selected item text is transformed to UC by CSS

		// submit a search term 
		//NOTE: this is searching under the category "ALL" - unexpected
		//TODO: fix this so that it is searching in the category "disease"
		String term = "Brain Diseases"
		searchInput.value(term)
		searchButton.click()
		
//		assert at(SearchResultsPage)
	}

}
