package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class GenesigPage extends Page {

    public static final String HEADER_TAB_NAME = 'Gene Signature/Lists'

    static url = 'geneSignature/list'

    static at = {
        commonHeader.currentMenuItem?.text() == HEADER_TAB_NAME
        if(!mySigsOpen.isDisplayed())
            mySigs.find('tr').size() > 0
        if(!pubSigsOpen.isDisplayed())
            pubSigs.find('tr').size() > 0
        if(!myListsOpen.isDisplayed())
            myLists.find('tr').size() > 0
        if(!pubListsOpen.isDisplayed())
            pubLists.find('tr').size() > 0
    }

    static content = {
        genesig(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }
        sigLists  { $('h1', text: 'Gene Signature Lists') }
        mySigsOpen   { $('a#my_signatures_fopen')  }
        pubSigsOpen   { $('a#pub_signatures_fopen')  }
        myListsOpen   { $('a#my_lists_fopen')  }
        pubListsOpen   { $('a#pub_lists_fopen')  }
        mySigs  { $('table#mySignatures') }
        pubSigs  { $('table#publicSignatures') }
        myLists  { $('table#myLists') }
        pubLists  { $('table#publicLists') }
    }
    
}

