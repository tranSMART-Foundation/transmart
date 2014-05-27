package pages

import geb.Page
import pages.modules.NavigationBarModule

class BrowsePage extends Page {
    public static final String BROWSE_TAB_NAME = 'Browse'

    static url = 'RWG/index'

    static at = {
		navigationBarBrowse.currentNavTab == BROWSE_TAB_NAME
    }

    static content = {
        navigationBarBrowse { module NavigationBarModule }
    }
	
	
}
