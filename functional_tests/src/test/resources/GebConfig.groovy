import geb.Page
import org.openqa.selenium.Dimension
import pages.BrowsePage

/*
 * This is the Geb configuration file.
 *
 * See: http://www.gebish.org/manual/current/configuration.html
 */

// The tranSMART Foundation CI testing site
baseUrl = 'http://postgres-test.transmartfoundation.org/transmart/'

// for local testing set here or use -Pfirefoxlocal below
//baseUrl = 'http://localhost:8080/transmart/'

reports = 'build/geb-reports'

def instantiateDriver(String className) {
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

environments {
    chrome {
        driver = { instantiateDriver 'org.openqa.selenium.chrome.ChromeDriver' }
    }

    firefox {
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    firefoxlocal {
        baseUrl = 'http://localhost:8080/transmart/'
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    firefoxoracle {
        baseUrl = 'http://75.124.74.46:5080/transmart/'
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    firefoxpostgres {
        baseUrl = 'http://75.124.74.46:5880/transmart/'
        driver = { instantiateDriver 'org.openqa.selenium.firefox.FirefoxDriver' }
    }

    htmlunit {
        // See: http://code.google.com/p/selenium/wiki/HtmlUnitDriver
        driver = { Class.forName('org.openqa.selenium.htmlunit.HtmlUnitDriver').newInstance() }
    }
}

