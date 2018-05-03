package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import functions.Constants

/**
 * Created by peter.rice@transmartfoundation.org on 25-jul-2017
 */
class FilterModule extends Module {

    static base = { $('div#box-search') }

    static content = {
        // text 'Active Filters'
        filterTitle { $('div#title-search-div h2.title') }
        // 
        filterClearButton { $('div#clearbutton') }
        // 
        filterFilterButton { $('div#filterbutton') }
        // current filters reported
        filterSearch { $('div#active-search-div') }
    }

}
