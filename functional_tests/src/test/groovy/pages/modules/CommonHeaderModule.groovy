package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

/**
 * Created by price on 18-11-2014.
 */
class CommonHeaderModule extends Module {

// test which tab is selected and return name

// check through menu table - check names, find selected name,
// search for any passed in option e.g. to check for metacore plugin.

    final static String TOPMENU_BROWSE = 'Browse'
    final static String TOPMENU_ANALYZE = 'Analyze'
    final static String TOPMENU_SAMPLE_EXPLORER = 'Sample Explorer'
    final static String TOPMENU_GENE_SIGNATURE_LISTS = 'Gene Signature/Lists'
    final static String TOPMENU_GWAS = 'GWAS'
    final static String TOPMENU_UPLOAD_DATA = 'Upload Data'
    final static String TOPMENU_ADMIN = 'Admin'
    final static String TOPMENU_UTILITIES = 'Utilities'

    final static List<String> ALL_TOPMENU =
        ImmutableList.of(TOPMENU_BROWSE, TOPMENU_ANALYZE,
                         TOPMENU_SAMPLE_EXPLORER, TOPMENU_GENE_SIGNATURE_LISTS,
                         TOPMENU_GWAS, TOPMENU_UPLOAD_DATA,
//                         TOPMENU_ADMIN,
                         TOPMENU_UTILITIES)

    final static String UTILITIES_HELP = 'Help'
    final static String UTILITIES_CONTACT = 'Contact Us'
    final static String UTILITIES_ABOUT = 'About'
    final static String UTILITIES_PASSWORD = 'Change My Password'
    final static String UTILITIES_LOGOUT = 'Log Out'

    final static List<String> ALL_UTILITIESMENU =
        ImmutableList.of(UTILITIES_HELP, UTILITIES_CONTACT, UTILITIES_ABOUT, UTILITIES_PASSWORD, UTILITIES_LOGOUT)

 
    private List<String> headerList() {
        def res = ImmutableList.builder()

        tableMenu.find('tr').eachWithIndex { tr, trIndex ->
            if (trIndex == 0) {
                def expectedTopMenu = ALL_TOPMENU.iterator()
                def rowHeader = tr.find('th').text()
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
                assert trIndex == 0
            }
        }

        res.build()
    }

    private utilitiesAbout() {
        def util = utilitiesMenuFind('About')
        assert util

        String utilText = withAlert(wait:true) {util.click()}
        assert utilText.startsWith('tranSMART v1.2.rev2-eTI (PostgreSQL)')
// clicking has hidden the utilities menu
// although the alert has been faked
// we need to get it back to able to continue
        tableMenuUtilities.click()
    }
     
    private Boolean utilitiesContact() {
        def util = utilitiesMenuFind('Contact Us')
        assert util

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('mailto:')
        true
    }
     
     
    private Boolean utilitiesPassword() {
        def util = utilitiesMenuFind('Change My Password')
        assert util

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('http://')
        true
    }
     
    private Boolean utilitiesHelp() {
        def util = utilitiesMenuFind('Help')
        assert util

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('http://')
        true
    }
     
    private void utilitiesDoLogout() {
        def util = utilitiesMenuFind('Log Out')
        assert util

        util.click()
    }
     
    private Boolean utilitiesLogout() {
        def util = utilitiesMenuFind('Log Out')
        assert util

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('http://')
        assert utilHref.endsWith('/transmart/login/forceAuth')
        true
    }
     
    private int utilitiesMenuSize() {

        int ret = 0

        utilitiesMenu.find('li a').each { li ->
            ret++;
        }

        ret
    }
    
    private Navigator utilitiesMenuFind(String util) {

        Navigator ret = null

        utilitiesMenu.find('li a').each { li ->
            if(li.text() == util) {
                ret = li
            }
        }

        if(!ret){
            println "utilitiesMenuFind '${util}' not found"
        }
        
        ret
    }
    
    private List<String> utilitiesList() {
        def res = ImmutableList.builder()

        def expectedUtilitiesMenu = ALL_UTILITIESMENU.iterator()
        String nextUtility
        String liText
        utilitiesMenu.find('li a').eachWithIndex { li, liIndex ->
            nextUtility = expectedUtilitiesMenu.next()
            liText = li.text()
            assert liText == nextUtility : "Unexpected utilitiesmenu item"
            res.add(liText)
        }

        res.build()
    }

    private Navigator topMenuFind(String tabName) {

        Navigator ret = null

        tableMenu.find('tr').eachWithIndex { tr, trIndex ->
            if (trIndex == 0) {
                def rowHeader = tr.find('th').text()
                String thText
                tr.children('th').each { th ->
                    thText = th.text()
                    if(thText == tabName) {
                        ret = th.find('a')
                    }
                }
            }
        }

        if(!ret){
            println "topMenuFind '${tabName}' not found"
        }

        ret
    }

    static content = {
        commonheader(wait: true)         { $() }
        
        currentMenuItem(required: false) { $('th', class: 'menuVisited') }

        divHeader                        { $('div#header-div') }
        tableMenu                        { $('table#menuLinks') }
        tableMenuUtilities               { $('#utilitiesMenuButton') }
        utilitiesMenu                    { $('ul#utilitiesMenuList') }

/*
        listCommonheaderTab() {
            $('th', class: 'menuDetail').findAll{
                WebElement element = it
                true
            }
        }.collect { new SearchItem(navigator: it) }
*/

    }

    Navigator headerTab () { tableMenu().find('th', class: 'menuVisited') }
    Navigator listHeaderTab() { tablemenu().find('th') }
    Navigator findHeaderTab (String tabName) { listHeaderTab().find(tabName) }
}
