package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import functions.Constants

/**
 * Created by peter.rice@transmartfoundation.org on 25-jul-2017
 */
class AnalyzeModule extends Module {

    public static final String HEADER_TAB_NAME = 'Analyze'

    static base = { $('div#centerMainPanel') }

    static content = {
        commonHeader { module CommonHeaderModule }
        filter { module FilterModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
    }

}
