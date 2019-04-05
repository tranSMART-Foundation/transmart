import groovy.util.logging.Slf4j
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartRenderingInfo
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.AxisLocation
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.entity.StandardEntityCollection
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.servlet.ServletUtilities
import org.jfree.data.statistics.BoxAndWhiskerItem
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.ExpressionProfileFilter
import org.transmart.ExpressionProfileResult
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayDataStatistics
import org.transmart.biomart.BioMarker
import org.transmart.biomart.Disease

import javax.servlet.ServletException
import java.awt.*
import java.util.List

/**
 * @author mmcduffie
 */
@Slf4j('logger')
class ExpressionProfileController {

    @Autowired private ExpressionProfileQueryService expressionProfileQueryService

    def datasourceResult() {

        // reset profile filter
	sessionSearchFilter().exprProfileFilter.reset()

        // refresh experiment count
        logger.info '>> Count query:'
	int profCount = expressionProfileQueryService.countExperiment(sessionSearchFilter())

        // initialize session with profile results
        ExpressionProfileResult epr = new ExpressionProfileResult()
	session.exprProfileResult = epr

        // load genes and cache
        logger.info '>> Gene Query:'
	List<BioMarker> genes = expressionProfileQueryService.listBioMarkers(sessionSearchFilter())
	if (genes) {
	    sessionSearchFilter().exprProfileFilter.bioMarkerId = genes[0].id
	}
	logger.info '... number genes: {}', genes.size()

        logger.info '>> Diseases Query:'
	List<Disease> diseases = expressionProfileQueryService.listDiseases(sessionSearchFilter())
	if (diseases) {
	    sessionSearchFilter().exprProfileFilter.bioDiseaseId = diseases[0].id
	}

	List<String> probesets = []
	if (genes && diseases) {
            logger.info '>> Probesets Query:'
            probesets = expressionProfileQueryService.getProbesetsByBioMarker(genes[0], diseases[0])
	    sessionSearchFilter().exprProfileFilter.probeSet = probesets[0]

            // build graph results, stores in session
            createGraph()
        }

        // cache results
        epr.genes = genes
        epr.diseases = diseases
        epr.probeSets = probesets
        epr.profCount = profCount

        renderProfileView()
    }

    def selectGene() {
	logger.info '>> selectGene:{}', params.bioMarkerId

        // get profile results
	ExpressionProfileResult epr = sessionExprProfileResult()

        // refresh filter selections
	sessionSearchFilter().exprProfileFilter.reset()

        // bind gene selection
	bindData sessionSearchFilter().exprProfileFilter, params
	BioMarker marker = BioMarker.get(sessionSearchFilter().exprProfileFilter.bioMarkerId)

        // refresh diseases
	List<Disease> diseases = expressionProfileQueryService.listDiseases(sessionSearchFilter())
	sessionSearchFilter().exprProfileFilter.bioDiseaseId = diseases[0].id
        epr.diseases = diseases

        // refresh probesets using first disease
	List<String> probesets = expressionProfileQueryService.getProbesetsByBioMarker(marker, diseases[0])
	sessionSearchFilter().exprProfileFilter.probeSet = probesets[0]
        epr.probeSets = probesets

        createGraph()

        renderProfileView()
    }

    def selectDisease() {
	logger.info 'select Disease:{}', params.bioDiseaseId
	bindData sessionSearchFilter().exprProfileFilter, params

        // get profile results
	ExpressionProfileResult epr = sessionExprProfileResult()

        // load selections
	BioMarker marker = BioMarker.get(sessionSearchFilter().exprProfileFilter.bioMarkerId)
	Disease disease = Disease.get(sessionSearchFilter().exprProfileFilter.bioDiseaseId)

        // refresh probesets using first disease
	List<String> probesets = expressionProfileQueryService.getProbesetsByBioMarker(marker, disease)
        epr.probeSets = probesets
	sessionSearchFilter().exprProfileFilter.probeSet = probesets[0]

        createGraph()

        renderProfileView()
    }

    def selectProbeset() {
	logger.info 'select Probeset:{}', params.probeSet
	bindData sessionSearchFilter().exprProfileFilter, params

        // only update graph
        createGraph()
	render template: 'graphView', model: [epr: sessionExprProfileResult()]
    }

    /**
     * render expression profile view for indicated model
     */
    def renderProfileView() {
	render view: 'expressionProfileView', model: [epr: sessionExprProfileResult()]
    }

    def createGraph() {
	ExpressionProfileFilter eFilter = sessionSearchFilter().exprProfileFilter
	ExpressionProfileResult epr = sessionExprProfileResult()

        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset()

	int height = 200
	int offset = 10
	String chartname = 'Box Plot'
        if (eFilter.filterBioMarker()) {
            chartname = BioMarker.get(eFilter.bioMarkerId).name
        }

        logger.info '>> Boxplot query:'
	List<Object[]> allData = expressionProfileQueryService.queryStatisticsDataExpField(sessionSearchFilter())
        // don't create graph if no data
	logger.info '... number boxplot filter records: {}', allData.size()
	if (!allData) {
            epr.graphURL = 'empty'
            epr.datasetItems = null
            return
        }

	List<BioAssayDataStatistics> dsItems = []
	Double chartMinVal = null
	Double chartMaxVal = null
	BioAssayDataStatistics statdata
        int i = 0

        for (drow in allData) {
            // BioAssayDataStats record
            statdata = drow[0]
	    dsItems << statdata

	    String seriesName = drow[1]
	    String itemName = statdata.dataset.name + '(' + statdata.sampleCount + ')'

            // min, max outlier settings
	    Double minOutlier
	    Double maxOutlier
            if (statdata.minValue != null) {
		if (chartMinVal == null) {
		    chartMinVal = statdata.minValue
		}
                minOutlier = Math.min(0, statdata.minValue - 2)
		if (statdata.minValue < chartMinVal) {
		    chartMinVal = statdata.minValue
		}
            }

            if (statdata.maxValue != null) {
		if (chartMaxVal == null) {
		    chartMaxVal = statdata.maxValue
		}
                maxOutlier = statdata.maxValue + 3
		if (statdata.maxValue > chartMaxVal) {
		    chartMaxVal = statdata.maxValue
		}
            }

	    BoxAndWhiskerItem boxitem = new BoxAndWhiskerItem(statdata.meanValue,
							      statdata.quartile2,
							      statdata.quartile1,
							      statdata.quartile3,
							      statdata.minValue,
							      statdata.maxValue,
							      minOutlier,
							      maxOutlier,
							      null)
	    dataset.add boxitem, seriesName, itemName
	    height += offset
        }

        //create the chart
	JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(chartname,
								 'Samples', 'Log(2) Expression', dataset, true)
	CategoryPlot plot = chart.plot
	chart.backgroundPaint = Color.white
	plot.backgroundPaint = new Color(245, 250, 250)
	plot.domainGridlinePaint = Color.lightGray
	plot.domainGridlinesVisible = true
	plot.rangeGridlinePaint = Color.lightGray
	plot.orientation = PlotOrientation.HORIZONTAL

	NumberAxis rangeAxis = plot.rangeAxis
	rangeAxis.standardTickUnits = NumberAxis.createIntegerTickUnits()
	rangeAxis.lowerBound = chartMinVal - 0.5
	rangeAxis.upperBound = chartMaxVal + 0.5
	logger.info 'INFO: calculated info ... lowest val: {}; highest val: {}', chartMinVal, chartMaxVal

	plot.rangeAxisLocation AxisLocation.BOTTOM_OR_RIGHT
	BoxAndWhiskerRenderer rend = plot.renderer

	rend.maximumBarWidth = 0.2
	rend.fillBox = true

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection())
        String filename = ServletUtilities.saveChartAsJPEG(chart, 800, height, info, session)
	String graphURL = request.contextPath + '/expressionProfile/displayChart?filename=' + filename
        logger.info graphURL

        // store results
        epr.graphURL = graphURL
        epr.datasetItems = dsItems
    }

    def displayChart(String filename) {
	if (!filename) return

        //  Replace '..' with ''
        //  This is to prevent access to the rest of the file system
        filename = ServletUtilities.searchReplace(filename, '..', '')

        //  Check the file exists
        File file = new File(System.getProperty('java.io.tmpdir'), filename)
        if (!file.exists()) {
	    throw new ServletException('File "' + file.absolutePath + '" does not exist')
        }

	ServletUtilities.sendTempFile file, response
    }

    def printChart() {
	render view: 'printView', model: [filename: params.filename]
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }

    private ExpressionProfileResult sessionExprProfileResult() {
	session.exprProfileResult
    }
}
