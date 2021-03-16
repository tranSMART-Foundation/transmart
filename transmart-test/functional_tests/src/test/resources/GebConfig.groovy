import geb.Page
import org.openqa.selenium.Point
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.firefox.ProfilesIni
import org.openqa.selenium.os.ExecutableFinder
import geb.report.ScreenshotReporter

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

// The tranSMART Foundation PostgreSQL demo server
//baseUrl = 'http://postgres-demo.transmartfoundation.org/transmart/'

// for local testing set here or use -Pfirefoxlocal below
baseUrl = 'http://localhost:8080/transmart/'

// directory for copies of HTML and PNG image
// for each test (for any driver unless overridden)
reportsDir = 'build/geb-reports'

// write HTML and PNG reports only on failure if this is set
reportOnTestFailureOnly = true

reporter = new ScreenshotReporter()

File findDriverExecutable(String driverName){
    def defaultExecutable = new ExecutableFinder().find(driverName)
    if(defaultExecutable) {
        new File(defaultExecutable)
    } else {
        new File("drivers").listFiles().findAll {
            it.name.contains(driverName) &&
            !it.name.endsWith(".version")
        }.find {
            if(IS_OS_LINUX) {
                it.name.contains("linux")
            }
            else if (IS_OS_MAC) {
                it.name.contains("mac")
            } else if (IS_OS_WINDOWS) {
                it.name.contains("windows")
            }
        }
    }
}

environments {
    chrome {
        driver = {
            File chromeFile = findDriverExecutable("chromedriver")

            System.setProperty("webdriver.chrome.driver", chromeFile.path)

            // chromeService to keep running for all tests
            ChromeDriverService chromeService = new ChromeDriverService.Builder()
            .usingAnyFreePort()
            .usingDriverExecutable(chromeFile)
            .build()
            chromeService.start()

            // chromeOpts for any options needed to ensure consistent results
            ChromeOptions chromeOpts = new ChromeOptions()
            chromeOpts.addArguments("--lang=en-us")
            chromeOpts.addArguments("--start-maximized")

            // chromeDriver
            WebDriver chromeDriver = new ChromeDriver(chromeService, chromeOpts)
            chromeDriver.manage().window().maximize()

            chromeDriver
        }
    }
    
    firefox {
        driver = {
            ProfilesIni iniprofile = new org.openqa.selenium.firefox.ProfilesIni()
            FirefoxProfile firefoxProfile = iniprofile.getProfile("tmtestProfile");

            firefoxProfile.setPreference("intl.accept_languages", "en-us")
            firefoxProfile.setPreference("browser.download.dir", "/data/scratch/git-master/transmart-test/functional_tests/savefiles")
            firefoxProfile.setPreference("browser.download.folderList", 2)
            firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "image/jpeg, image/jpg, image/png, image/gif, \
application/zip, application/x-compressed, application/x-zip-compressed, \
application/pdf, application/octet-stream, text/plain")
            firefoxProfile.setPreference("pdfjs.disabled", true)

            FirefoxOptions firefoxOptions = new FirefoxOptions()

            firefoxOptions.setProfile(firefoxProfile)

            File geckoFile = findDriverExecutable("geckodriver")
            System.setProperty("webdriver.gecko.driver", geckoFile.path)

            WebDriver firefoxDriver = new FirefoxDriver(firefoxOptions)
            firefoxDriver.manage().window().maximize()

            firefoxDriver
        }
    }
    
    firefoxlocal {
        baseUrl = 'http://localhost:8080/transmart/'
        driver = { new FirefoxDriver() }
    }

    firefoxoracle {
        baseUrl = 'http://localhost:8080/transmart/'
        driver = { new FirefoxDriver() }
    }

    firefoxpostgres {
        baseUrl = 'http://localhost:8080/transmart/'
        driver = { new FirefoxDriver() }
    }

}

HOST_SERVER = "undefined"

BAD_USERNAME = 'bad username'
BAD_PASSWORD = 'bad password'
GOOD_USERNAME = 'guest'
GOOD_PASSWORD = 'transmart2016'
ADMIN_USERNAME = 'admin'
ADMIN_PASSWORD = 'admin'
AUTO_LOGIN_ENABLED = true // locally set to true
GALAXY_ENABLED = true
GWAS_PLINK_ENABLED = true
METACORE_ENABLED = true
SMARTR_ENABLED = true
XNAT_IMPORT_ENABLED = true
XNAT_VIEW_ENABLED = true

LANDING_PAGE = new BrowsePage() // local configuration

GSE8581_KEY = '\\\\Public Studies\\Public Studies\\GSE8581\\'

if(baseUrl == 'http://localhost:8080/transmart/') {
    HOST_SERVER = 'localhost'
} else if(baseUrl == 'http://postgres-ci.transmartfoundation.org/transmart/') {
    HOST_SERVER = 'TranSMART Oracle test server'
} else if(baseUrl == 'http://postgres-test.transmartfoundation.org/transmart/') {
    HOST_SERVER = 'TranSMART Postgres test server'
} else {                // default values
    HOST_SERVER = 'default server'
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
