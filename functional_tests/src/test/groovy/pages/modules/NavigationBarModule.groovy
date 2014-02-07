package pages.modules

import geb.Module

/**
 * Created by glopes on 06-02-2014.
 */
class NavigationBarModule extends Module {

    static content = {
        currentNavTab(required: false) { $('div#navlist li.active')?.text() }
    }
}
