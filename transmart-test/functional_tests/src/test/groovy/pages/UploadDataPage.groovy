package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class UploadDataPage extends Page {

    public static final String HEADER_TAB_NAME = 'Upload Data'

    static url = 'uploadData/index'

    static at = {
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_UPLOAD_DATA
        sideHeader.text() == "Upload target"
        sideTarget.size() == 3
    }

    static content = {
        uploadData(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }

        sidebar { $('div#uploadSidebar') }
        sideHeader { sidebar.find('h2.title') }
        sideTarget { sidebar.find('li.sidebarRadio') }
        targetGwas { sidebar.find('li#uploadAnalysisRadio') }
        targetFile { sidebar.find('li#uploadFileRadio') }
        targetAnalyze { sidebar.find('li#uploadFileDatasetExplorerRadio') }

        uploadPanel { $('form#dataUpload') }

        uploadStudy { uploadPanel.find('input#study-input') }
        uploadType { uploadPanel.find('select#dataType option') }
        uploadName { uploadPanel.find('input#analysisName') }
        uploadDesc { uploadPanel.find('textarea#description') }

        uploadData { uploadPanel.find('div#uploadFilePane') }
        uploadDataFile { uploadData.find('input#uploadFile[type=file]') }
        uploadDataName { uploadData.find('input#displayName[type=text]') }
    }
    
}

