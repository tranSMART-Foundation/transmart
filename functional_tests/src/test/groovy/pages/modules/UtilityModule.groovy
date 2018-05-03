package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

/**
 * Created by peter.rice@transmartfoundation.org on 4-jul-2016
 */
class UtilityModule extends Module {

    static base = { $('div#utilitiesMenu').parent() } // menu and button are children of this level

    static content = {
        tableMenuUtilities               { $('#utilitiesMenuButton') }
        utilitiesMenu                    { $('ul#utilitiesMenuList') }
    }

    final static String UTILITIES_HELP = 'Help'
    final static String UTILITIES_CONTACT = 'Contact Us'
    final static String UTILITIES_ABOUT = 'About'
    final static String UTILITIES_PASSWORD = 'Change My Password'
    final static String UTILITIES_LOGOUT = 'Log Out'

    private List<String> ALL_UTILITIESMENU = []

    def UtilityModule() {
        ALL_UTILITIESMENU.add(UTILITIES_HELP)
        ALL_UTILITIESMENU.add(UTILITIES_CONTACT)
        ALL_UTILITIESMENU.add(UTILITIES_ABOUT)
        ALL_UTILITIESMENU.add(UTILITIES_PASSWORD)
        ALL_UTILITIESMENU.add(UTILITIES_LOGOUT)
    }
        
    private Boolean utilitiesAbout() {
        def util = utilitiesMenuFind('About')
        assert util : "'About' not found in utility menu"

        String utilText = withAlert(wait:true) {util.click()}
        assert utilText.startsWith('tranSMART v')
//      assert utilText.startsWith('tranSMART v1.2.rev2-eTI (PostgreSQL)')

// clicking has hidden the utilities menu
// although the alert has been faked
// we need to get it back to able to continue
        tableMenuUtilities.click()
    }
     
    private Boolean utilitiesContact() {
        def util = utilitiesMenuFind('Contact Us')
        assert util : "'Contact Us' not found in utility menu"

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('mailto:')
        true
    }
     
     
    private Boolean utilitiesPassword() {
        def util = utilitiesMenuFind('Change My Password')
        assert util  : "'Change My Password' not found in utility menu"

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('http://')
        true
    }
     
    private Boolean utilitiesHelp() {
        def util = utilitiesMenuFind('Help')
        assert util : "'Help' not found in utility menu"

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('http://')
        true
    }
     
    private Boolean utilitiesBug() {
        def util = utilitiesMenuFind('Report a Bug')
        assert util : "'Report a Bug' not found in utility menu"

        String utilHref = util.getAttribute('href')
        assert utilHref.startsWith('http://')
        true
    }
     
    private void utilitiesDoLogout() {
        def util = utilitiesMenuFind('Log Out')
        assert util  : "'Log Out' not found in utility menu"

        util.click()
    }
     
    private void utilitiesDoPassword() {
        def util = utilitiesMenuFind('Change My Password')
        assert util  : "'Change My password' not found in utility menu"

        util.click()
    }
     
    private Boolean utilitiesLogout() {
        def util = utilitiesMenuFind('Log Out')
        assert util  : "'Log Out' not found in utility menu"

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
        if(!utilitiesMenu.isDisplayed())
            tableMenuUtilities.click()
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
    
    List<String> utilitiesList() {
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

}
