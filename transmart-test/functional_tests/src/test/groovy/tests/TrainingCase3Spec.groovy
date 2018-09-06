package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.AnalyzeSummary
import pages.AnalyzeGridview
import pages.AnalyzeExport
import pages.AnalyzeSmartR
import pages.AnalyzeResultsPage
import pages.smartr.SmartRHeatmapFetch

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.ScrollingModule
import org.openqa.selenium.Keys

@Stepwise
class TrainingCase3Spec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(AnalyzeQuery)
    }

    def "start on Analyze page"() {

        when:
        isAt(AnalyzeQuery)

        then:
        assert at(AnalyzeQuery)
    }

    def "make query"() {
        when:
        println ""
        println "drag subset1"
        queryDragNodeToSubset "${Constants.GSE15258_KEY}Biomarker Data\\Gene Expression\\Affymetrix Human Genome U133 Plus 2.0 Array\\Blood\\", 1
        waitFor { queryHidome }
        
        println "HiDome '${queryHidome}'"
        println "HiDomeDiv size ${queryHidomeDiv.size()}"
        println "HiDomeDiv[0] '${queryHidomeDiv[0]}'"
        println "HiDome Type '${queryHidomeType}'"
        println "HiDome Type text '${queryHidomeType.text()}'"
        println "HiDome Platform '${queryHidomeField('Platform').text()}'"
        queryHidomeField('Search in').find('select option').eachWithIndex{ it, i ->
            println "Search in [${i}] '${it.attr('value')}' '${it.text()}'"
        }
        queryHidomeField('Query on').find('select option').eachWithIndex{ it, i ->
            println "Query on [${i}] '${it.attr('value')}' '${it.text()}'"
        }
        println "HiDome Button 'No filter' '${queryHidomeButton('No filter').value()}'"
        println "HiDome Button 'OK' '${queryHidomeButton('OK').value()}'"
        println "HiDome Button 'Cancel' '${queryHidomeButton('Cancel').value()}'"
        queryHidomeField('Search term').find('input').value 'PTPN'

        waitFor { queryHidomeGene('PTPN22') }
        queryHidomeGene('PTPN22').click()

        waitFor { queryHidomeSubjects.text() }
        println "Subjects '${queryHidomeSubjects}'"
        println "Subjects id '${queryHidomeSubjects.attr('id')}'"
        println "Subjects text '${queryHidomeSubjects.text()}'"

        def subjSelected = queryHidomeSubjects.text()

        // need to use sendKeys to clear and  values using #highdimension-amount-max
        //
        // using value() only appends - unable to clear existing value
        
        queryHidomeSliderMax.firstElement().sendKeys Keys.chord(Keys.CONTROL, "a")+Keys.BACK_SPACE
        queryHidomeSliderMax.firstElement().sendKeys '4.0\n'

        waitFor { queryHidomeSubjects.text() != subjSelected }
        println "New Subjects '${queryHidomeSubjects.text()}'"

        then:
        at(AnalyzeQuery)
    }

}
