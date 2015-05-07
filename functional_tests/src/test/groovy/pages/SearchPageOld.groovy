package pages

import geb.Page
import geb.waiting.WaitTimeoutException

class SearchPage extends Page {
    public static final String SEARCH_NAV_TAB_NAME = 'Search'

    static url = 'search/index'

    static at = {
		navigationBarSearch.currentNavTab == SEARCH_NAV_TAB_NAME
    }

    static content = {
        navigationBarSearch { module NavigationBarModule }
		categoryBarItem (wait:true) {String term ->
			$("div#categories-view span.x-view-item", text:term)
		}
		categorySelected (wait:true) {
			$("div#categories-view span.x-view-selected")
		}
		categoryPullDown (wait:true){
			$("div.x-combo-list-inner div.x-combo-list-inner div.search-item")
		}
		pullDownItemCategroyIs (wait:true) { String category ->
			$("div.x-combo-list-inner div.x-combo-list-inner div.search-item p span.category-${category}")
		}
		searchInput { $("input#search-combobox") }
		searchButton { $("button#search-button")}
    }
	
	
}
