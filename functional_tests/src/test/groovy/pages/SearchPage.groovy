package pages

import geb.Page
import pages.modules.NavigationBarModule

class SearchPage extends Page {
    public static final String NAV_TAB_NAME = 'Search'

    static url = 'search'

    static at = {
        navigationBar.currentNavTab == NAV_TAB_NAME
    }

    static content = {
        navigationBar { module NavigationBarModule }
    }
}
