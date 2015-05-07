package pages

import geb.Page
import geb.navigator.Navigator
import pages.modules.CommonHeaderModule
import pages.modules.NavigationBarModule

// $ function is $(css-selector,index-or-range,attribute-or-text-matchers)

class BrowseOtherPage extends Page {
    public static final String BROWSE_NAV_TAB_NAME = 'tranSMART v1.2.rev2-eTI (PostgresSQL)'

    static url = 'RWG/index'

    static at = {
        String tsdText = $('div', id: 'title-search-div').text()

        if(tsdText) {
            tsdText.startsWith("Active Filters")
        } else {
            false
        }
        
        //   filterId.text() == "Active Filters"
    }

    static content = {
        //navigationBarSearch.currentNavTab == "Active Filters"
//        $("h2").text() != "Active Filters"
         
//        navigationBarSearch { module NavigationBarModule }
//		categoryBarItem (wait:true) {String term ->
//			$("div#program-explorer span.x-view-item", text:term)
//		}
//		categorySelected (wait:true) {
//			$("div#categories-view span.x-view-selected")
//		}
//		categoryPullDown (wait:true){
//			$("div.x-combo-list-inner div.x-combo-list-inner div.search-item")
//		}
//		pullDownItemCategroyIs (wait:true) { String category ->
//			$("div.x-combo-list-inner div.x-combo-list-inner div.search-item p span.category-${category}")
//		}
//		searchInput { $("input#search-combobox") }
//		searchButton { $("button#search-button")}
    }
	
	
}
