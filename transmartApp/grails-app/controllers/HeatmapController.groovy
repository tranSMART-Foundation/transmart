import com.recomdata.util.ExcelGenerator
import com.recomdata.util.ExcelSheet
import grails.converters.JSON
import org.transmart.KeywordSet
import org.transmart.SearchFilter
import org.transmart.SearchKeywordService
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
class HeatmapController {

    HeatmapService heatmapService
    SearchKeywordService searchKeywordService
    TrialQueryService trialQueryService
    UtilService utilService

    def initheatmap() {
	sessionSearchFilter().heatmapFilter.reset()
    }

    def filterheatmap(SearchKeyword searchKeyword, String heatmapfiltertype) {
	sessionSearchFilter().heatmapFilter.heatmapfiltertype = heatmapfiltertype

	if (searchKeyword) {
	    sessionSearchFilter().heatmapFilter.searchTerm = searchKeyword
        }

	render view: 'initheatmap'
    }

    def showheatmap() {

	Map dataResult = generateHeatmaps()

	int hmapcount = 1

	def comtable = null
	if (dataResult.comresult.datatable) {
	    comtable = dataResult.comresult.datatable as JSON
        }

	def cortable = null
	if (dataResult.corresult.datatable) {
            hmapcount++
	    cortable = dataResult.corresult.datatable as JSON
        }

	def rbmtable = null
	if (dataResult.rbmresult.datatable) {
            hmapcount++
	    rbmtable = dataResult.rbmresult.datatable as JSON
        }

	def rhotable = null
	if (dataResult.rhoresult.datatable) {
            hmapcount++
	    rhotable = dataResult.rhoresult.datatable as JSON
        }

	List<BioAssayAnalysis> allanalysis = []
	allanalysis.addAll dataResult.comresult.analysislist
	allanalysis.addAll dataResult.corresult.analysislist
	allanalysis.addAll dataResult.rbmresult.analysislist
	allanalysis.addAll dataResult.rhoresult.analysislist

	allanalysis.sort { BioAssayAnalysis a, BioAssayAnalysis b ->
	    a.shortDescription.toLowerCase() <=> b.shortDescription.toLowerCase()
	}

	[comtable: comtable,
	 cortable: cortable,
	 rbmtable: rbmtable,
	 rhotable: rhotable,
	 hmapwidth: 100 / hmapcount,
	 contentlist: allanalysis]
    }

    def downloadheatmapexcel() {

	Map dataResult = generateHeatmaps()

	List<ExcelSheet> sheets = []

	if (dataResult.comresult.datatable) {
	    sheets << createExcelSheet(dataResult.comresult.datatable.table, 'Gene Expression Comparison')
        }

	if (dataResult.corresult.datatable) {
	    sheets << createExcelSheet(dataResult.corresult.datatable.table, 'Gene Expression Correlation')
        }

	if (dataResult.rbmresult.datatable) {
	    sheets << createExcelSheet(dataResult.rbmresult.datatable.table, 'RBM')
        }

	if (dataResult.rhoresult.datatable) {
	    sheets << createExcelSheet(dataResult.rhoresult.datatable.table, 'RBM Spearman Correlation')
	}

	utilService.sendDownload response, 'application/vnd.ms-excel; charset=utf-8','heatmap.xls',
	    new ExcelGenerator().generateExcel(sheets)
    }

    private Map generateHeatmaps() {

        // need to decide which algorithm to run
        // we have 3 algorithms
        //1) search top gene
        //2) search pathway from heatmap filter
        //3) search genes from global filters

        boolean searchTopGene = false
        boolean searchHeatmapFilter = false

        // for genes to be displayed in the heatmap - this is used for searchHeatmapFilter and search global filter
	Set<SearchKeyword> orderedGenes = []
	List<Long> searchGeneIds = []
	List<Long> searchAnalysisIds = BioAssayAnalysisData.executeQuery(
	    trialQueryService.createAnalysisIDSelectQuery(sessionSearchFilter()), [max: 100])


	if ('topgene'.equalsIgnoreCase(sessionSearchFilter().heatmapFilter.heatmapfiltertype)) {
            searchTopGene = true
        }
        else {
	    SearchKeyword keyword = sessionSearchFilter().heatmapFilter.searchTerm
	    if (keyword) {
                searchHeatmapFilter = true

                if (keyword.dataCategory.equalsIgnoreCase('PATHWAY') ||
                    keyword.dataCategory.equalsIgnoreCase('GENESIG') ||
		    keyword.dataCategory.equalsIgnoreCase('GENELIST')) {
		    List<SearchKeyword> allGenes = searchKeywordService.expandAllListToGenes(keyword.bioDataId, 200)

		    for (SearchKeyword k in allGenes) {
			searchGeneIds << k.bioDataId
                    }
		    orderedGenes.addAll allGenes

		}
		else { // gene
		    orderedGenes << keyword
		    searchGeneIds << keyword.bioDataId
                }
            }
        }

        // if not by top gene nor by heatmap filer then use global filters
        if (!searchTopGene && !searchHeatmapFilter) {
            // otherwise use global filters

	    List<SearchKeyword> allPathwayGenes = []
	    if (sessionSearchFilter().globalFilter.hasAnyListFilters()) {
		allPathwayGenes = searchKeywordService.expandAllListToGenes(
		    sessionSearchFilter().globalFilter.allListFilters.keywordDataIdString, 200)
	    }

	    KeywordSet genes = sessionSearchFilter().globalFilter.geneFilters
	    orderedGenes.addAll genes
	    orderedGenes.addAll allPathwayGenes
	    for (SearchKeyword g in genes) {
		searchGeneIds << g.bioDataId
            }
	    for (SearchKeyword pg in allPathwayGenes) {
		searchGeneIds << pg.bioDataId
            }
        }

        // now it's time to get the data back

	Map dataList = [:]
	int maxshortdescr = 39

        // comparison
	dataList.comresult = createHeatmapData('comparison', 'Gene Expression', searchTopGene,
			       searchAnalysisIds, searchGeneIds, orderedGenes, maxshortdescr)

        // correlation
	dataList.corresult = createHeatmapData('correlation', 'Gene Expression', searchTopGene,
			       searchAnalysisIds, searchGeneIds, orderedGenes, maxshortdescr + 2)

        // rbm comparison
	dataList.rbmresult = createHeatmapData('comparison', 'RBM', searchTopGene,
			       searchAnalysisIds, searchGeneIds, orderedGenes, maxshortdescr)

        // rbm spearman
	dataList.rhoresult = createHeatmapData('spearman correlation', 'RBM', searchTopGene,
			       searchAnalysisIds, searchGeneIds, orderedGenes, maxshortdescr)

	dataList
    }

    /*
     * create heatmap result data
     */
    private Map createHeatmapData(String method, String dataType, boolean searchTopGene, List<Long> searchAnalysisIds,
	                          List<Long> searchGeneIds, Set<SearchKeyword> orderedGenes, int maxcolLength) {

	List dataList = heatmapService.createHeatMapData(sessionSearchFilter(), method, dataType,
				searchTopGene, searchGeneIds, searchAnalysisIds)

	double cutoff = 4.5

	if (dataList) {
            // create column name list and map using analysis name
            // get all analysis out of the bioDataFact object and format their names

	    List<Map> columnList = [[type: 'n', label: 'Gene Name', pattern: '', id: 0]]
	    int ccount = 0
	    Map columnPosMap = [:]
	    Map analysisNameMap = [:]
	    List<BioAssayAnalysis> assayAnalysisList = []

	    for (data in dataList) {
		String analysisName = analysisNameMap[data.assayAnalysisId]
                // reformat & shorten analysis name
                if (analysisName == null) {
		    BioAssayAnalysis analysis = BioAssayAnalysis.get(data.assayAnalysisId)
                    analysisName = analysis.shortDescription
                    if (analysisName == null) {
                        analysisName = analysis.name
                    }

                    analysisName = analysisName.replaceAll('\\s+', '_')
		    analysisName = analysisName.replaceAll("'", '*')

                    if (analysisName.length() > maxcolLength) {
                        analysisName = analysisName.substring(0, maxcolLength - 3) + '...'
                    }
                    else {
			int paddingnum = maxcolLength - analysisName.length()
			StringBuilder sb = new StringBuilder(maxcolLength).append(analysisName)
                        for (pi in 0..paddingnum - 1) {
			    sb << ' '
                        }
			analysisName = sb.toString()
                    }
		    analysisNameMap[data.assayAnalysisId] = analysisName
		    assayAnalysisList << analysis

                    // add into column list
		    columnList << [type: 'n', label: analysisName.toUpperCase(), pattern: '', id: ccount]
		    columnPosMap[data.assayAnalysisId] = ccount
                    ccount++
                }
            }

            // rows
	    Map<String, Object[]> rowmap = new TreeMap()
	    int totalcols = columnList.size() - 1

            for (keyword in orderedGenes) {
		rowmap[keyword.keyword] = new Object[totalcols]
            }

            // data list later
            for (data in dataList) {
		Object[] rowArray = rowmap[data.bioMarkerName]
                if (rowArray == null) {
                    rowArray = new Object[totalcols]
		    rowmap[data.bioMarkerName] = rowArray
                }

                // find column index by analysis id
		int columnIndex = columnPosMap[data.assayAnalysisId]

                if (rowArray != null) {
		    def datavalue
		    if ('correlation' == method) {
                        datavalue = data.rvalue
                    }
		    else if ('spearman correlation' == method) {
                        datavalue = data.rhoValue
                    }
                    else {
                        datavalue = data.foldChangeRatio
                    }
                    rowArray[columnIndex] = datavalue
                }
            }

	    List rowlist = []
	    for (Map.Entry entry in rowmap.entrySet()) {

		List row = []
                // this is the gene name
		String rowname = entry.key

                if (rowname == null) {
                    rowname = ''
                }
                else {
		    rowname = rowname.replaceAll("'", '*')
                }

                // this is an array
		def rvalues = entry.value
                // handle null value rows
		boolean hasRowValue = false
                // if not RBM
                if ('RBM'.equalsIgnoreCase(dataType)) {
                    for (value in rvalues) {
                        if (value != null) {
                            hasRowValue = true
                            break
                        }
                    }
                }
                else {
                    hasRowValue = true
                }

                if (hasRowValue) {
		    row << [v: rowname, f: rowname]
                }
                else {
		    row << [v: 'N/A', f: 'N/A']
                }

                for (value in rvalues) {
                    def vvalue = value
                    if (vvalue != null) {
			if (vvalue > cutoff) {
                            vvalue = cutoff
			}
			if (vvalue < -cutoff) {
                            vvalue = -cutoff
			}
                    }
		    row << [v: vvalue, f: value]
		}

		rowlist << row
	    }

	    [datatable: [table: [cols: columnList, rows: rowlist]], analysislist: assayAnalysisList]
        }
        else {
	    [datatable: [], analysislist: []]
        }
	
    }

    private ExcelSheet createExcelSheet(table, String name) {
	List cols = []
        for (col in table.cols) {
	    cols << col.label
        }

	List rows = []
        for (row in table.rows) {
            if (row[0].f != 'N/A') {
		List newrow = []
                for (value in row) {
		    newrow << value.f
                }
		rows << newrow
            }
        }

	new ExcelSheet(name: name, headers: cols, values: rows)
    }

    def noResult() {
	render view: 'noresult'
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }
}
