package pages.modules

import geb.Module

class NavigationBarModule extends Module {

    static content = {
		tabArea {$('table.menuDetail#menuLinks')}
        currentNavTab(required: false) { tabArea.find('th.menuVisited')?.text() }
    }
}
