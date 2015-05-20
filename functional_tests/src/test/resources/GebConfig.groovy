import geb.Page
import org.openqa.selenium.Dimension
import pages.BrowsePage

/*
 * This is the Geb configuration file.
 *
 * See: http://www.gebish.org/manual/current/configuration.html
 */

// The tranSMART Foundation CI testing site
//baseUrl = 'http://75.124.74.64/transmart/'

// for local testing
baseUrl = 'http://localhost:8080/transmart/'

reports = 'build/geb-reports'

def instantiateDriver(String className) {
    def driverInstance = Class.forName(className).newInstance()
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

    htmlunit {
        // See: http://code.google.com/p/selenium/wiki/HtmlUnitDriver
        driver = { Class.forName('org.openqa.selenium.htmlunit.HtmlUnitDriver').newInstance() }
    }
}

class Constants {

    public static final String BAD_USERNAME = 'bad username'
    public static final String BAD_PASSWORD = 'bad password'
    public static final String GOOD_USERNAME = 'admin'
    public static final String GOOD_PASSWORD = 'admin'
    public static final String ADMIN_USERNAME = 'admin'
    public static final String ADMIN_PASSWORD = 'admin'

    public static final boolean AUTO_LOGIN_ENABLED = true
    public static final Page LANDING_PAGE = new BrowsePage()

    public static final String GSE8581_KEY = '\\\\Public Studies\\Public Studies\\GSE8581\\'
}
