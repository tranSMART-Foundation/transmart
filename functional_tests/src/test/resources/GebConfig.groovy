import geb.Page
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import geb.report.ScreenshotReporter
//import org.openqa.selenium.firefox.FirefoxDriverService
//import org.openqa.selenium.chrome.ChromeDriver
//import org.openqa.selenium.chrome.ChromeDriverService
//import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.os.ExecutableFinder

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC

import pages.BrowsePage

/*
 * This is the Geb configuration file.
 *
 * See: http://www.gebish.org/manual/current/configuration.html
 */

// The tranSMART Foundation CI testing site
//baseUrl = 'http://postgres-ci.transmartfoundation.org/transmart/'

// for local testing set here or use -Pfirefoxlocal below
baseUrl = 'http://localhost/transmart/'

// directory for copies of HTML and PNG image
// for each test
reportsDir = 'build/geb-reports'

// write HTML and PNG reports only on failure if this is set
reportOnTestFailureOnly = true

reporter = new ScreenshotReporter()

def instantiateDriver(String className) {
    def driverInstance = Class.forName(className).newInstance(profile)
    driverInstance.manage().window().size = new Dimension(1280, 1024)
    driverInstance
}

def instantiateDriverWebDriver(String className) {
    def profile = new org.openqa.selenium.firefox.FirefoxProfile()
    profile.setPreference("intl.accept_languages", "en-us")
    profile.setPreference("browser.download.dir", "/data/scratch/git-master/transmart-test/functional_tests/savefiles")
    profile.setPreference("browser.download.folderList", 2)
    profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "image/jpeg, image/jpg, image/png, image/gif,"+
                          " application/zip, application/x-compressed, application/x-zip-compressed,"+
                          " application/pdf, application/octet-stream,"+
                          " text/plain")
    profile.setPreference("pdfjs.disabled", true)
    def driverInstance = Class.forName(className).newInstance(profile)
    driverInstance.manage().window().size = new Dimension(1280, 1024)
    driverInstance
}

def instantiateDriverProfile(String className) {
    def profile = new org.openqa.selenium.firefox.FirefoxProfile()
    profile.setPreference("intl.accept_languages", "en-us")
    def driverInstance = Class.forName(className).newInstance(profile)
    driverInstance.manage().window().size = new Dimension(1280, 1024)
    driverInstance
}

/*
def instantiateDriverChrome(String className) {
    def profile = new org.openqa.selenium.chrome.ChromeOptions()
//    profile.setPreference("intl.accept_languages", "en-us")
    profile.addArguments("--start-maximized")
    def driverInstance = Class.forName(className).newInstance(profile)
    driverInstance.manage().window().size = new Dimension(1280, 1024)
    driverInstance
}
*/
environments {
    chrome {
        driver = { instantiateDriverChrome 'org.openqa.selenium.chrome.ChromeDriver' }
    }

    firefox {
        driver = { instantiateDriverWebDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    firefoxlocal {
        baseUrl = 'http://localhost/transmart/'
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    firefoxoracle {
        baseUrl = 'http://localhost/transmart/'
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    firefoxpostgres {
        baseUrl = 'http://localhost/transmart/'
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    htmlunit {
        // See: http://code.google.com/p/selenium/wiki/HtmlUnitDriver
        driver = { Class.forName('org.openqa.selenium.htmlunit.HtmlUnitDriver').newInstance() }
    }
}

HOST_SERVER = "undefined"

BAD_USERNAME = 'bad username'
BAD_PASSWORD = 'bad password'
GOOD_USERNAME = 'guest'
GOOD_PASSWORD = 'transmart2016'
ADMIN_USERNAME = 'admin'
String ADMIN_PASSWORD = 'admin'
AUTO_LOGIN_ENABLED = true // locally set to true
GALAXY_ENABLED = true
GWAS_PLINK_ENABLED = true
METACORE_ENABLED = true
SMARTR_ENABLED = true
XNAT_IMPORT_ENABLED = true
XNAT_VIEW_ENABLED = true

LANDING_PAGE = new BrowsePage() // local configuration

GSE8581_KEY = '\\\\Public Studies\\Public Studies\\GSE8581\\'

if(baseUrl == 'http://localhost/transmart/') {
    HOST_SERVER = 'localhost'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            GALAXY_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

} else if(baseUrl == 'http://postgres-ci.transmartfoundation.org/transmart/') {
    HOST_SERVER = 'TranSMART Oracle test server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//             AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

} else if(baseUrl == 'http://postgres-test.transmartfoundation.org/transmart/') {
    HOST_SERVER = 'TranSMART Postgres test server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

} else if(baseUrl == 'http://transmartci.etriks.org/') {
    HOST_SERVER = 'eTRIKS test server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

} else {                // default values
    HOST_SERVER = 'default server'

//            AUTO_LOGIN_ENABLED = true
//            LANDING_PAGE = new BrowsePage()

//            GOOD_USERNAME = 'guest'
//            GOOD_PASSWORD = 'transmart2016'
//            ADMIN_USERNAME = 'admin'
//            ADMIN_PASSWORD = 'admin'

//            AUTO_LOGIN_ENABLED = true
//            GWAS_PLINK_ENABLED = true
//            METACORE_ENABLED = true
//            SMARTR_ENABLED = true
//            XNAT_IMPORT_ENABLED = true
//            XNAT_VIEW_ENABLED = true

}


// force waiting for document to load before any at(Page)
// CommonHeader module base query was failing otherwise
// on many pages
atCheckWaiting = true

// set timeout for root element of a page
// as Firefox driver can timeout with default settings

baseNavigatorWaiting = true

// increase timeout for slow pages to 10 seconds
// default timeout is 5 seconds
waiting {
    timeout = 10
    retryInterval = 0.5
    includeCauseInMessage = true // Force Maven Surefire to include cause of exception
}

