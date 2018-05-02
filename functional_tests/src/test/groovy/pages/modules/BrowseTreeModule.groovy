package pages.modules

import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement
import geb.waiting.WaitTimeoutException

import functions.Constants
import pages.modules.CommonHeaderModule
import pages.modules.ScrollingModule

class BrowseTreeModule extends Module {

    static url = 'RWG/index'

    static content = {
        browse(wait: true)           { $() }

        results(wait: true)          { $('div#results-div').children('div.search-results-table') }
        programs(wait: true)         { results.find('table.folderheader').has('span.program') }
        studies                      { program -> anyContent(program).find('table.folderheader').has('span.studywsubject,span.study') }
            
        openStudies(required: false) { programs.has('span.studywsubject,span.study') }
        assays                       { study -> anyContent(study).find('table.folderheader').has('span.assay') }
        analyses                     { study -> anyContent(study).find('table.folderheader').has('span.analysis') }
        folders                      { holder -> anyContent(holder).find('table.folderheader').has('span.folder') }
        
        anyContent                   { withTable -> withTable.next('div.detailexpand') }

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
        scrolling { module ScrollingModule }
        utility { module UtilityModule }
    }

    Navigator programFind(String programName) {

        Navigator ret = null

//        println "programFind '${programName}'"
        programs.eachWithIndex { pr, prIndex ->
            String prText = pr.text()
            if(prText == programName) {
//                println "Program found '${prText}'"
                if(pr.find('a[onclick^=toggleDetailDiv]')) {
//                    println "Onclick found for open/close"
                }
                if(pr.find('a[onclick^=showDetail]')) {
//                    println "Onclick found for edit"
                }
                def con = anyContent(pr)
                if(con) {
//                    println "Contents found within program"
                    if(con.isDisplayed()) {
//                        println "Content is expanded"
                    }
                }
                ret = pr
            }
        }

        if(!ret){
//            println "programFind '${programName}' not found"
        }

        ret
    }

    Boolean programView(Navigator pr) {

        Boolean ret = false

        String prText = pr.text()
//        println "programView '${prText}'"

        def show = pr.find('a[onclick^=showDetail]')
        if(show) {
//            println "clicking on showDetail"
            scrolling.scrollToBottom(show)
            show.click()
//            println "clicked on showDetail"
            ret = true
        }

        if(!ret){
//            println "programView '${prText}' not found or not viewable (not loaded, not admin)"
        }

        ret
    }

    Boolean programExpand(Navigator pr) {

        Boolean ret = false

        String prText = pr.text()
//        println "programExpand '${prText}'"

        def con = anyContent(pr)
        if(con) {
//            println "Contents found within program"
            if(con.isDisplayed()) {
//                println "Content is expanded"
            } else {
//                println "Content is currently collapsed"
                def togDD = pr.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            }
        }

        if(!ret){
//            println "programExpand '${prText}' program not expandable"
        }

        ret
    }

    Boolean programCollapse(Navigator pr) {

        Boolean ret = false

        String prText
        prText = pr.text()
//        println "programCollapse '${prText}'"

        def con = anyContent(pr)
        if(con) {
//            println "Contents found within program"
            if(con.isDisplayed()) {
//                println "Content is expanded"
                def togDD = pr.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            } else {
//                println "Content is already collapsed"
            }
        }

        if(!ret){
//            println "programCollapse '${prText}' not collapsible"
        }

        ret
    }

    Navigator studyFind(String studyName, Navigator pr) {

        Navigator ret = null
        String prText = pr.text()

//        println "studyFind program: '${prText}' study: '${studyName}'"
        programExpand(pr)

        studies(pr).eachWithIndex { st, stIndex ->
            String stText
            stText = st.text()
            if(stText == studyName) {
//                println "Study found '${stText}'"
                def con = anyContent(st)
                if(con) {
//                    println "Contents found within study"
                    if(con.isDisplayed()) {
//                        println "Content is expanded"
                    }
                }
                ret = st
            }
        }
        
        if(!ret){
//            println "studyFind '${studyName}' in program '${prText}' not found"
        }

        ret
    }

    Navigator studyFind(String studyName, String programName) {

        Navigator pr = programFind(programName)

        return studyFind(studyName, pr)
    }

    Boolean studyView(Navigator st) {
        Boolean ret = false

        String stText = st.text()
//        println "studyView '${stText}'"

        def show = st.find('a[onclick^=showDetail]')
        if(show) {
//            println "clicking on showDetail"
            scrolling.scrollToBottom(show)
            show.click()
//            println "clicked on showDetail"
            ret = true
        }

        if(!ret){
//            println "studyView '${stText}' not found or not viewable (not loaded, not admin)"
        }

        ret
    }

    Boolean studyExpand(Navigator st) {
        Boolean ret = false

        String stText = st.text()
//        println "studyExpand '${stText}'"

        def con = anyContent(st)
        if(con) {
//            println "Contents found within study"
            if(con.isDisplayed()) {
//                println "Content is expanded"
            } else {
//                println "Content is currently collapsed"
                def togDD = st.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            }
        }

        if(!ret){
//            println "studyExpand '${stText}' study not expandable"
        }

        ret
    }

    Boolean studyCollapse(Navigator st) {
        Boolean ret = false

        String stText = st.text()
//        println "studyCollapse '${stText}'"

        def con = anyContent(st)
        if(con) {
//            println "Contents found within study"
            if(con.isDisplayed()) {
//                println "Content is expanded"
                def togDD = st.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            } else {
//                println "Content is already collapsed"
            }
        }

        if(!ret){
//            println "studyCollapse '${stText}' study not collapsible"
        }

        ret
    }

    Navigator assayFind(String assayName, Navigator st) {
        Navigator ret = null
        String stText = st.text()
        String assText

//        println "assayFind program: '${stText}' assay: '${assayName}'"
        studyExpand(st)

        assays(st).eachWithIndex { ass, assIndex ->
            assText = ass.text()
            if(assText == assayName) {
//                println "Assay found '${assText}'"
                def con = anyContent(ass)
                if(con) {
//                    println "Contents found within assay"
                    if(con.isDisplayed()) {
//                        println "Content is expanded"
                    }
                }
                ret = ass
            }
        }
        
        if(!ret){
//            println "assayFind '${assayName}' in study '${stText}' not found"
        }

        ret
    }

    Navigator assayFind(String assayName, String studyName, String programName) {

        Navigator st = studyFind(studyName, programName)

        return assayFind(assayName, st)
    }

    Boolean assayView(Navigator ass) {
        Boolean ret = false

        String assText = ass.text()
//        println "assayView '${assText}'"

        def show = ass.find('a[onclick^=showDetail]')
        if(show) {
//            println "clicking on showDetail"
            scrolling.scrollToBottom(show)
            show.click()
//            println "clicked on showDetail"
            ret = true
        }

        if(!ret){
//            println "assayView '${assText}' not found or not viewable (not loaded, not admin)"
        }

        ret
    }

    Boolean assayExpand(Navigator ass) {
        Boolean ret = false

        String assText = ass.text()
//        println "assayExpand '${assText}'"

        def con = anyContent(ass)
        if(con) {
//            println "Contents found within study"
            if(con.isDisplayed()) {
//                println "Content is expanded"
            } else {
//                println "Content is currently collapsed"
                def togDD = ass.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            }
        }

        if(!ret){
//            println "assayExpand '${assText}' assay not expandable"
        }

        ret
    }

    Boolean assayCollapse(Navigator ass) {
        Boolean ret = false

        String assText = ass.text()
//        println "assayExpand '${assText}'"

        def con = anyContent(ass)
        if(con) {
//            println "Contents found within assay"
            if(con.isDisplayed()) {
//                println "Content is expanded"
                def togDD = ass.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            } else {
//                println "Content is already collapsed"
            }
        }

        if(!ret){
//            println "assayCollapse '${assText}' assay not collapsible"
        }

        ret
    }

    Navigator analysisFind(String analysisName, Navigator st) {
        Navigator ret = null
        String stText = st.text()
        String anaText

//        println "analysisFind study: '${stText}' analysis: '${analysisName}'"
        studyExpand(st)

        analyses(st).eachWithIndex { ana, anaIndex ->
            anaText = ana.text()
            if(anaText == analysisName) {
//                println "Analysis found '${anaText}'"
                def con = anyContent(ana)
                if(con) {
//                    println "Contents found within analysis"
                    if(con.isDisplayed()) {
//                        println "Content is expanded"
                    }
                }
                ret = ana
            }
        }
        
        if(!ret){
//            println "analysisFind '${analysisName}' in study '${stText}' not found"
        }

        ret
    }

    Navigator analysisFind(String analysisName, String studyName, String programName) {

        Navigator st = studyFind(studyName, programName)

        return analysisFind(analysisName, st)
    }

    Boolean analysisView(Navigator ana) {
        Boolean ret = false

        String anaText = ana.text()
//        println "analysisView '${anaText}'"

        def show = ana.find('a[onclick^=showDetail]')
        if(show) {
//            println "clicking on showDetail"
            scrolling.scrollToBottom(show)
            show.click()
//            println "clicked on showDetail"
            ret = true
        }

        if(!ret){
//            println "analysisView '${anaText}' not found or not viewable (not loaded, not admin)"
        }

        ret
    }

    Boolean analysisExpand(Navigator ana) {
        Boolean ret = false

        String anaText = ana.text()
//        println "analysisExpand '${anaText}'"

        def con = anyContent(ana)
        if(con) {
//            println "Contents found within study"
            if(con.isDisplayed()) {
//                println "Content is expanded"
            } else {
//                println "Content is currently collapsed"
                def togDD = ana.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            }
        }

        if(!ret){
//            println "analysisExpand '${anaText}' analysis not expandable"
        }

        ret
    }

    Boolean analysisCollapse(Navigator ana) {
        Boolean ret = false

        String anaText = ana.text()
//        println "analysisExpand '${anaText}'"

        def con = anyContent(ana)
        if(con) {
//            println "Contents found within analysis"
            if(con.isDisplayed()) {
//                println "Content is expanded"
                def togDD = ana.find('a[onclick^=toggleDetailDiv]')
                if(togDD) {
//                    println "Clicking on toggleDetailDiv"
                    scrolling.scrollToBottom(togDD)
                    togDD.click()
//                    println "Clicked on toggleDetailDiv"
                    ret = true
                }
            } else {
//                println "Content is already collapsed"
            }
        }

        if(!ret){
//            println "analysisCollapse '${anaText}' analysis not collapsible"
        }

        ret
    }

    Navigator folderFind(String folderName, Navigator parent) {

        Navigator ret = null
        String parText = parent.text()
        String folText

//        println "folderFind '${folderName}' parent '${parText}'"
        programExpand(parent)

        folders(parent).eachWithIndex { fol, folIndex ->
            folText = fol.text()
//            println "Testing folder '${folText}'"
            def (folName) = folText =~ /(?m)^(.+)$/
            def (folSize) = folText =~ /(?m)^Documents \((\d+)\)$/
//            println "matcher '${folName[1]}' '${folSize[1]}'"
            if(folName[1] == folderName) {
//                println "Folder found '${folName[1]}' with ${folSize[1]} documents"
                def con = anyContent(fol)
                if(con) {
//                    println "Contents found within folder"
                    if(con.isDisplayed()) {
//                        println "Content is expanded"
                    }
                }
                ret = fol
            }
        }
        
        if(!ret){
//            println "folderFind '${folderName}' in parent '${parText}' not found"
        }

        ret
        
    }

    Navigator folderFind(String folderName, String programName) {

        Boolean ret = false

//        println "folderExpand '${folderName}' study '${studyName}' program '${programName}'"

    }

    Navigator folderFind(String folderName, String parentName, String programName) {

        Boolean ret = false

//        println "folderFind '${folderName}' parent '${parentname}' program '${programName}'"

    }

    Boolean folderView(Navigator fol) {
        Boolean ret = false

        String folText = fol.text()
//        println "folderView '${folText}'"

        def show = fol.find('a[onclick^=showDetail]')
        if(show) {
//            println "clicking on showDetail"
            scrolling.scrollToBottom(show)
            show.click()
//            println "clicked on showDetail"
            ret = true
        }

        if(!ret){
//            println "folderView '${folText}' not found or not viewable (not loaded, not admin)"
        }

        ret
    }

    Boolean folderView(String folderName, String programName) {

        Boolean ret = false

//        println "folderView '${folderName}' program '${programName}'"

    }

    Boolean folderView(String folderName, String studyName, String programName) {

        Boolean ret = false

//        println "folderView '${folderName}' study '${studyName}' program '${programName}'"

    }

    Boolean folderExpand(Navigator fol) {

        Boolean ret = false

//        println "folderExpand '${folderName}' program '${programName}'"

    }

    Boolean fileView(String fileName, String folderName, String programName) {

        Boolean ret = false

//        println "fileView '${fileName}' folder '${folderName}' program '${programName}'"

    }

    Boolean fileView(String fileName, String folderName, String studyName, String programName) {

        Boolean ret = false

//        println "fileView '${fileName}' folder '${folderName}' study '${studyName}' program '${programName}'"

    }



}

abstract class BrowseTreeNavigator implements Navigator {
    String name = 'Program'

    String getName() {
        return name
    }

}

