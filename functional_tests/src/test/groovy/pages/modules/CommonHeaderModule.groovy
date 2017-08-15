package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import functions.Constants

/**
 * Created by peter.rice@transmartfoundation.org on 18-nov-2014.
 */
class CommonHeaderModule extends Module {

    static base = { $('div#header-div') }

    static content = {
        // selected common header tab
        currentMenuItem (wait: true)     { $('th.menuVisited') }
        // table of all common header tabs
        tableMenu                        { $('table#menuLinks') }
    }


// test which tab is selected and return name

// check through menu table - check names, find selected name,
// check for items hidden by Config.groovy

    final static String TOPMENU_BROWSE = 'Browse'
    final static String TOPMENU_ANALYZE = 'Analyze'
    final static String TOPMENU_SAMPLE_EXPLORER = 'Sample Explorer'
    final static String TOPMENU_GENE_SIGNATURE_LISTS = 'Gene Signature/Lists'
    final static String TOPMENU_GWAS = 'GWAS'
    final static String TOPMENU_UPLOAD_DATA = 'Upload Data'
    final static String TOPMENU_ADMIN = 'Admin'
    final static String TOPMENU_UTILITIES = 'Utilities'

    private List<String> ALL_TOPMENU = []


    def CommonHeader() {
        ALL_TOPMENU.add(TOPMENU_BROWSE)
        ALL_TOPMENU.add(TOPMENU_ANALYZE)
        ALL_TOPMENU.add(TOPMENU_SAMPLE_EXPLORER)
        ALL_TOPMENU.add(TOPMENU_GENE_SIGNATURE_LISTS)
        ALL_TOPMENU.add(TOPMENU_GWAS)
        ALL_TOPMENU.add(TOPMENU_UPLOAD_DATA)
//        ALL_TOPMENU.add(TOPMENU_ADMIN)
        ALL_TOPMENU.add(TOPMENU_UTILITIES)
    }

    private List<String> headerList() {
        def res = ImmutableList.builder()

        tableMenu.find('tr').eachWithIndex { tr, trIndex ->
            if (trIndex == 0) {
                def expectedTopMenu = ALL_TOPMENU.iterator()
                //def rowHeader = tr.find('th').text()
                String nextTop
                String thText
                tr.children('th').each { th ->
                    thText = th.text()
                    // skip the empty width header
                    // skip test for Admin ... only present when logged in as administrator
                    if(thText.length() > 1 && thText != TOPMENU_ADMIN) {
                        nextTop = expectedTopMenu.next()
                        assert thText == nextTop : "Unexpected topmenu item"
                        res.add(thText)
                    }
                }
            } else {
                assert trIndex == 0 : "Multiple top menus"
            }
        }

        res.build()
    }

    private Navigator topMenuFind(String tabName) {

        Navigator ret = null

        tableMenu.find('tr').eachWithIndex { tr, trIndex ->
            if (trIndex == 0) {
                //def rowHeader = tr.find('th').text()
                String thText
                tr.children('th').each { th ->
                    thText = th.text()
                    if(thText == tabName) {
                        ret = th.find('a')
                    }
                }
            }
            else {
                println "Found tableMenu[${trIndex}] testing for '${tabName}'"
            }

        }

        if(!ret){
            println "topMenuFind '${tabName}' not found"
        }

        ret
    }

    Navigator listHeaderTab() { tableMenu().find('th') }
    Navigator findHeaderTab (String tabName) { listHeaderTab().find(tabName) }

}
