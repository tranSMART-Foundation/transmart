package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import functions.Constants

import pages.LoginPage
import pages.BrowsePage
import pages.BrowseProgram
import pages.BrowseResultsPage

import pages.modules.CommonHeaderModule

@Stepwise
class BrowsePageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(BrowsePage)
    }

    // Assume a standard set of browse tab metadata has been loaded
    // using transmart-data and supplied sql loading scripts

    def "start on the browse tab"() {

        when:
        at(BrowsePage)

        then:
        assert at(BrowsePage)

        when:
        def prPublic = browseTree.programFind('Public Studies')
        println "PS program found"

        def prTest = browseTree.programFind('Test Studies')
        println "TS program found"

        browseTree.programExpand(prTest)
        println "TS program expanded"
        browseTree.programExpand(prTest)
        println "TS program expanded again"

        browseTree.programCollapse(prTest)
        println "TS program collapsed"
        browseTree.programCollapse(prTest)
        println "TS program collapsed again"

        browseTree.programView(prTest)
        println "TS program opened"
        browseTree.programView(prTest)
        println "TS program opened again"

        then:
        at(BrowseProgram)

        when:
        def prAsthma = browseTree.programFind('Etriks Asthma')
        println "EA program found"
        browseTree.programExpand(prAsthma)
        println "EA program expanded"
        browseTree.programCollapse(prAsthma)
        println "EA program collapsed"
        browseTree.programView(prAsthma)
        println "EA program opened"

        def stRic = browseTree.studyFind('Test serial highdim Ricerca', 'Test Studies')
        browseTree.studyView(stRic)
        browseTree.studyExpand(stRic)
        browseTree.studyCollapse(stRic)
        browseTree.studyExpand(stRic)

        def assEndo = browseTree.assayFind('Endocrine system', stRic)
        browseTree.assayView(assEndo)
        browseTree.assayExpand(assEndo)
        browseTree.assayCollapse(assEndo)

        def anaConn = browseTree.analysisFind('Connective tissue', stRic)
        browseTree.analysisView(anaConn)
        browseTree.analysisExpand(anaConn)
        browseTree.analysisCollapse(anaConn)

        def stInc = browseTree.studyFind('Test GSE4382 incremental', 'Test Studies')
        def fol = browseTree.folderFind('Raw data', stInc)
        browseTree.folderView(fol)

        // Look for document count in folder
        // look for folder count in folder
        // look for Associated files
        // download/open file

        then:

        // some check on the studies now open
        println "Checks needed"
    }

    def cleanupSpec() {
    }

}
