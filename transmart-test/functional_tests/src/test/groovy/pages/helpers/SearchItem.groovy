package pages.helpers


import geb.navigator.Navigator
import geb.Module

class SearchItem extends Module {

    @Delegate
    Navigator navigator

    String getType() {
//        println "in SearchItem.getType"
        find('span').text()[0..-2] //remove tailing >
    }

    String getSearchKeyword() {
        println "in SearchItem.getSearchKeyword"
        find('b').text()
    }

    List<String> getSynonyms() {
        /*
         * Next to the <b> element, there should something like
         * '  (NCRNA00096, P53TG1-D, P53TG1, TP53AP1)'
         * unless there are actually no synonyms, in which case only the leading
         * space should be there.
         */
//        println "in SearchItem.getSynonyms"
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
//        println "in SearchItem.toString"
        "SearchItem{" +
                "type=" + type + ', ' +
                "searchKeyword=" + searchKeyword + ', ' +
                "synonyms=" + synonyms +
                '}';
    }
}
