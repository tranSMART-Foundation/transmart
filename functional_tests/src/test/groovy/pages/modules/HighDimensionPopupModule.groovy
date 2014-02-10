package pages.modules

import geb.Module
import geb.navigator.AbstractNavigator
import geb.navigator.Navigator
import groovy.transform.ToString
import org.openqa.selenium.WebElement

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

class SearchItem implements Navigator {

    @Delegate
    AbstractNavigator navigator

    String getType() {
        find('span').text()[0..-2] //remove tailing >
    }

    String getSearchKeyword() {
        find('b').text()
    }

    List<String> getSynonyms() {
        /*
         * Next to the <b> element, there should something like
         * '  (NCRNA00096, P53TG1-D, P53TG1, TP53AP1)'
         * unless there are actually no synonyms, in which case only the leading
         * space should be there.
         */
        browser.js.exec(find('b').firstElement(), '''
            var textNode = arguments[0].nextSibling
            if (textNode === null) {
                throw 'Could not find expected text node'
            }
            // closing bracket may not be there; may end with ellipsis
            var match = /\\((.*)$/.exec(textNode.textContent)
            if (match == null) {
                return []
            } else {
                return match[1].split(', ')
            }''')
    }


    @Override
    public String toString() {
        "SearchItem{" +
                "type=" + type + ', ' +
                "searchKeyword=" + searchKeyword + ', ' +
                "synonyms=" + synonyms +
                '}';
    }
}
