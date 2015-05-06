package pages.modules

import geb.Module
import geb.navigator.AbstractNavigator
import geb.navigator.Navigator
import groovy.transform.ToString
import org.openqa.selenium.WebElement

import pages.helpers.SearchItem

class HighDimensionPopupModule extends Module {

    static content = {
        dialog      { $('div#compareStepPathwaySelectionWindow') }

        markerType  { $('input#platforms1').value() }
        gplPlatform { $('input#gpl1').value() }
        sample      { $('input#sample1').value() }
        tissue      { $('input#tissue1').value() }

        searchBox   { $('input#searchPathway') }

        applyButton { $('#dataAssociationApplyButton button') }

        searchKeywords(wait: true) {
            $('div.search-item').findAll {
                /* elements are not removed from the DOM after the search is closed */
                WebElement element = it.firstElement() // Navigator to WebElement
                element.location.x != 0 // hidden elements are here
            }.collect { new SearchItem(navigator: it) }
        }
    }

    void selectSearchItem(String keyword) {
        searchKeywords.find {
            it.searchKeyword == keyword
        }.click()
    }

}

