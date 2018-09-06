package pages.modules

import geb.Module

import geb.navigator.Navigator
import org.openqa.selenium.WebElement

/**
 * Created by peter.rice@transmartfoundation.org on 14-aug-2017.
 */
class ScrollingModule extends Module {

    void scrollToBottom(Navigator hiddenNode) {
        WebElement element = hiddenNode.firstElement()
        browser.driver.executeScript("arguments[0].scrollIntoView(false);", element)
    }

    void scrollToTop(Navigator hiddenNode) {
        WebElement element = hiddenNode.firstElement()
        browser.driver.executeScript("arguments[0].scrollIntoView(true);", element)
    }

}
