package pages

import geb.Page
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

class BrowsePage extends Page {

    public static final String HEADER_TAB_NAME = 'Browse'

    static url = 'RWG/index'

    static at = {
        commonHeader.currentMenuItem?.text() == HEADER_TAB_NAME
        programs.size() > 0
    }

    static content = {
        browse(wait: true)           { $() }

        results(wait: true)          { $('div#results-div').children('div.search-results-table') }
        programs                     { results.find('table.folderheader') }
        openStudies(required: false) { programs.has(class: "studywsubject") }
        
//        divColorBox              { $('div#colorbox') }
//        divSidebar               { $('div#sidebar') }
//        divMain                  { $('div#main') }
//        divHiddenItems           { $('div#hiddenitems') }
//        divExportOverlay         { $('div#exportOverlay') }
//        divEditMetadataOverlay   { $('div#editMetadataOverlay') }
//        divCreateAnalysisOverlay { $('div#createAnalysisOverlay') }
//        divCreateAssayOverlay    { $('div#createAssayOverlay') }
//        divCreateFolderOverlay   { $('div#createFolderOverlay') }
//        divCreateStudyOverlay    { $('div#createStudyOverlay') }
//        divCreateProgramOverlay  { $('div#createProgramOverlay') }
//        divBrowsePopups          { $('div#divBrowsePopups') }
//        divPlotOptions           { $('div#divPlotOptions') }
//        divSidebarToggle         { $('div#sidebartoggle') }
//        divFilterBrowser         { $('div#filter-browser') }

        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }
    }


    Navigator programFind(String prName) {

        Navigator ret = null

        programs.eachWithIndex { pr, prIndex ->
            String prText
            prText = pr.text()
            if(prText == prName) {
                if(pr.find(onclick: startsWith('toggleDetailDiv'))) {
                    println "Onclick found for open/close"
                }
                if(pr.find(onclick: startsWith('showDetail'))) {
                    println "Onclick found for edit"
                }

                ret = pr
            }
        }

        if(!ret){
            println "programFind '${prName}' not found"
        }

        ret
    }

    Boolean programOpen(String prName) {

        Boolean ret = false

        programs.eachWithIndex { pr, prIndex ->
            String prText
            prText = pr.text()
            if(prText == prName) {
                if(pr.find(onclick: startsWith('toggleDetailDiv'))) {
                    pr.find(onclick: startsWith('toggleDetailDiv')).click()
                    ret = true
                }
            }
        }

        if(!ret){
            println "programOpen '${prName}' not found or not expandable"
        }

        ret
    }

    Boolean programExpand(String programName) {

        Boolean ret = false

        programs.eachWithIndex { pr, prIndex ->
            String prText
            prText = pr.text()
            if(prText == prName) {
                if(pr.find(onclick: startsWith('showDetail'))) {
                    pr.find(onclick: startsWith('showDetail')).click()
                    ret = true
                }
            }
        }

        if(!ret){
            println "programExpand '${programName}' not found or not viewable (not loaded, not admin)"
        }

        ret
    }

    Boolean studyExpand(String studyName, String programName) {

        Boolean ret = false

        println "studyExpand '${analysisName}' study '${studyName}' program '${programName}'"

    }

    Boolean assayExpand(String assayName, String studyName, String programName) {

        Boolean ret = false

        println "assayExpand '${assayName}' study '${studyName}' program '${programName}'"

    }

    Boolean analysisExpand(String analysisName, String studyName, String programName) {

        Boolean ret = false

        println "analysisExpand '${analysisName}' study '${studyName}' program '${programName}'"

    }

    Boolean folderExpand(String folderName, String programName) {

        Boolean ret = false

        println "folderExpand '${folderName}' program '${programName}'"

    }

    Boolean folderExpand(String folderName, String studyName, String programName) {

        Boolean ret = false

        println "folderExpand '${folderName}' study '${studyName}' program '${programName}'"

    }


    Boolean programView(String programName) {

        Boolean ret = false

        println "programView '${programName}'"

    }

    Boolean studyView(String studyName, String programName) {

        Boolean ret = false

        println "studyView '${studyName}' program '${programName}'"

    }

    Boolean assayView(String assayName, String studyName, String programName) {

        Boolean ret = false

        println "assayView '${assayName}' study '${studyName}' program '${programName}'"

    }

    Boolean analysisView(String analysisName, String studyName, String programName) {

        Boolean ret = false

        println "analysisView '${analysisName}' study '${studyName}' program '${programName}'"

    }

    Boolean folderView(String folderName, String programName) {

        Boolean ret = false

        println "folderView '${folderName}' program '${programName}'"

    }

    Boolean folderView(String folderName, String studyName, String programName) {

        Boolean ret = false

        println "folderView '${folderName}' study '${studyName}' program '${programName}'"

    }

    Boolean fileView(String fileName, String folderName, String programName) {

        Boolean ret = false

        println "fileView '${fileName}' folder '${folderName}' program '${programName}'"

    }

    Boolean fileView(String fileName, String folderName, String studyName, String programName) {

        Boolean ret = false

        println "fileView '${fileName}' folder '${folderName}' study '${studyName}' program '${programName}'"

    }

}

abstract class BrowseProgramNavigator implements Navigator {
    String name = 'Program'

    String getName() {
        return name
    }
}

