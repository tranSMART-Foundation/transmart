package pages.helpers

import geb.navigator.AbstractNavigator
import geb.navigator.Navigator

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
