/*
	This is the Geb configuration file.

	See: http://www.gebish.org/manual/current/configuration.html
*/

baseUrl = 'http://ts-master-ci.thehyve.net/transmart/'

environments {

	// run as “mvn -Dgeb.env=chrome test”
	// See: http://code.google.com/p/selenium/wiki/ChromeDriver
	chrome {
		driver = { Class.forName('org.openqa.selenium.chrome.ChromeDriver').newInstance() }
	}

    firefox {
        driver = { Class.forName('org.openqa.selenium.firefox.FirefoxDriver').newInstance() }
    }

    htmlunit {
        // See: http://code.google.com/p/selenium/wiki/HtmlUnitDriver
        driver = { Class.forName('org.openqa.selenium.htmlunit.HtmlUnitDriver').newInstance() }
    }

}
