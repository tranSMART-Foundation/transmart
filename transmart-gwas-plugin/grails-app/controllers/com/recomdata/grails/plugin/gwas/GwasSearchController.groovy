package com.recomdata.grails.plugin.gwas

import au.com.bytecode.opencsv.CSVWriter
import com.recomdata.transmart.domain.searchapp.FormLayout
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisDataIdx
import org.transmart.biomart.Experiment
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.GeneSignatureItem
import org.transmart.searchapp.SearchKeyword

import java.lang.reflect.UndeclaredThrowableException
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Slf4j('logger')
class GwasSearchController {

    @Autowired private GwasSearchService gwasSearchService
    @Autowired private GwasWebService gwasWebService
    @Autowired private RegionSearchService regionSearchService
    def RModulesJobProcessingService
    @Autowired private SecurityService securityService

    @Value('${grails.mail.attachments.dir:}')
    private String mailAttachmentsDir

    @Value('${com.recomdata.rwg.qqplots.cacheImages:}')
    private String cachedImageDir

    @Value('${com.recomdata.rwg.webstart.codebase:}')
    private String webstartCodebase

    @Value('${com.recomdata.rwg.webstart.jar:}')
    private String webstartJar

    @Value('${com.recomdata.rwg.webstart.gwavaInstance:}')
    private String gwavaInstance

    @Value('${com.recomdata.rwg.webstart.transmart.url:}')
    private String webstartUrl

    @Value('${RModules.pluginScriptDirectory:}')
    private String pluginScriptDirectory

    private static final List<Map> DEFAULT_COLUMNS = [
	[sTitle: 'Analysis', sortField: 'baa.analysis_name'],
	[sTitle: 'Probe ID', sortField: 'data.rs_id'],
	[sTitle: 'p-value', sortField: 'data.p_value'],
	[sTitle: '-log 10 p-value', sortField: 'data.log_p_value'],
	[sTitle: 'RS Gene', sortField: 'gmap.gene_name'],
	[sTitle: 'Chromosome', sortField: 'info.chrom'],
	[sTitle: 'Position', sortField: 'info.pos'],
	[sTitle: 'Exon/Intron', sortField: 'info.exon_intron'],
	[sTitle: 'Recombination Rate', sortField: 'info.recombination_rate'],
	[sTitle: 'Regulome Score', sortField: 'info.regulome_score']].asImmutable()

    /**
     * Renders a UI for selecting regions by gene/RSID or chromosome.
     */
    def getRegionFilter() {
	render template: '/GWAS/regionFilter', model: [ranges: [both: '+/-', plus: '+', minus: '-']]
    }

    def getEqtlTranscriptGeneFilter() {
	render template: '/GWAS/eqtlTranscriptGeneFilter'
    }

    def webStartPlotter(String analysisIds, String snpSource, String pvalueCutoff) {
	List<String> regionStrings = []
	for (List region in getWebserviceCriteria()) {
	    regionStrings << region[0] + ',' + region[1]
	}
	String regions = regionStrings.join(';') ?: '0,0'

	if (!pvalueCutoff) {
	    pvalueCutoff = 0
	}

	String responseText = """\
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="$webstartCodebase">
  <information>
    <title>GWAVA Gene Wide Association Visual Analyzer with search set</title>
    <vendor>Pfizer Inc</vendor>
    <homepage href="./index.html"/>
    <description>Tool for Manhattan plot visualization of GWAS data.</description>
    <description kind="short">GWAVA gene wide association visual analysis</description>
    <shortcut>
      <desktop/>
      <menu submenu="GWAVA Transmart"/>
    </shortcut>
    <icon href="./images/guava_16.jpg"/>
    <icon href="./images/guava_24.jpg"/>
    <icon href="./images/guava_48.jpg"/>
    <icon kind="splash" href="./images/gwava_splash2.jpg"/>
    <offline-allowed/>
  </information>
  <security>
      <all-permissions/>
  </security>
  <update check="always" policy="always"/>
  <resources>
    <j2se version="1.6+" java-vm-args="-Xmx800m"/>

    <jar href="./lib/commons-beanutils-1.8.3.jar"/>
    <jar href="./lib/commons-beanutils-bean-collections-1.8.3.jar"/>
    <jar href="./lib/commons-beanutils-core-1.8.3.jar"/>
    <jar href="./lib/commons-codec-1.6.jar"/>
    <jar href="./lib/commons-digester3-3.2.jar"/>
    <jar href="./lib/commons-lang3-3.1.jar"/>
    <jar href="./lib/commons-logging-1.1.1.jar"/>
    <jar href="./lib/httpclient-4.0.jar"/>
    <jar href="./lib/httpcore-4.2.1.jar"/>
    <jar href="./lib/jersey-client-1.4.jar"/>
    <jar href="./lib/jersey-core-1.4.jar"/>
    <jar href="./lib/jgoodies-common-1.3.1.jar"/>
    <jar href="./lib/jgoodies-looks-2.5.1.jar"/>
    <jar href="./lib/log4j-1.2.17.jar"/>
    <jar href="$webstartJar"/>

    <property name="jsessionid" value="$session.id" />
    <property name="serviceHost" value="$request.serverName" />
    <property name="sun.java2d.noddraw" value="true"/>
  </resources>
  <application-desc main-class="com.pfizer.mrbt.genomics.Driver">
	<argument>-services=$gwavaInstance</argument>
	<argument>$analysisIds</argument>
	<argument>$regions</argument>
	<argument>0</argument>
	<argument>$snpSource</argument>
	<argument>$pvalueCutoff</argument>
	<argument>$webstartUrl</argument>
   <argument>$session.id</argument>
  </application-desc>
</jnlp>
		"""
	render text: responseText, contentType: 'application/x-java-jnlp-file'
    }

    private List<List> getWebserviceCriteria() {
	List<List> genes = []

	for (String s in sessionSolrSearchFilter()) {
            if (s.startsWith('REGION')) {
                //Cut off REGION:, split by pipe and interpret chromosomes and genes
		for (String r in s.substring(7).split('\\|')) {
                    //Chromosome
                    if (r.startsWith('CHROMOSOME')) {
                        //Do nothing for now
                    }
                    else {
			//Gene
			String[] region = r.split('\\^')
			long geneId = region[1] as long
			long range = region[3] as long
			SearchKeyword searchKeyword = SearchKeyword.get(geneId)
			if (searchKeyword.dataCategory == 'GENE') {
			    genes << [searchKeyword.keyword, range]
                        }
			else if (searchKeyword.dataCategory == 'SNP') {
                            //Get the genes associated with this SNP
			    for (String snpGene in regionSearchService.getGenesForSnp(searchKeyword.keyword)) {
				genes << [snpGene, range]
                            }
                        }
                    }
                }
            }
            else if (s.startsWith('GENESIG')) {
                //Expand regions to genes and get their limits
		for (sigId in s.substring(8).split('\\|')) {
		    List<GeneSignatureItem> sigItems = GeneSignatureItem.createCriteria().list() {
			eq('geneSignature', GeneSignature.get(SearchKeyword.get(sigId).bioDataId))
                        like('bioDataUniqueId', 'GENE%')
                    }
		    for (GeneSignatureItem sigItem in sigItems) {
			genes << [SearchKeyword.findByUniqueId(sigItem.bioDataUniqueId).keyword, 0]
                    }
                }
            }
            else if (s.startsWith('GENE')) {
		for (geneString in s.substring(5).split('\\|')) {
		    genes << [SearchKeyword.get(geneString).keyword, 0]
                }
            }
            else if (s.startsWith('SNP')) {
                //If plain SNPs, as above (default to HG19)
		for (rsId in s.substring(4).split('\\|')) {
                    //Get the genes associated with this SNP
		    for (String snpGene in regionSearchService.getGenesForSnp(SearchKeyword.get(rsId).keyword)) {
			genes << [snpGene, 0]
                    }
                }
            }
        }

	genes
    }

    private Map getRegionSearchResults(Long max, Long offset, Double cutoff, String sortField, String order, String search, analysisIds) {

        //Get list of REGION restrictions from session and translate to regions
	List<Map> regions = getSearchRegions()
	List<String> geneNames = getGeneNames()
	Double searchCutoff = getSearchCutoff()
	if (searchCutoff) {
	    cutoff = searchCutoff
        }
	List<String> transcriptGeneNames = getTranscriptGeneNames()
        //Find out if we're querying for EQTL, GWAS, or both
	boolean hasGwas = BioAssayAnalysis.createCriteria().count() {
            or {
                eq('assayDataType', 'GWAS')
                eq('assayDataType', 'Metabolic GWAS')
                eq('assayDataType','GWAS Fail')
            }
            'in'('id', analysisIds)
        }

	boolean hasEqtl = BioAssayAnalysis.createCriteria().count() {
            eq('assayDataType', 'EQTL')
            'in'('id', analysisIds)
        }

        def gwasResult
        def eqtlResult

        if (hasGwas) {
            gwasResult = runRegionQuery(analysisIds, regions, max, offset, cutoff, sortField, order, search, 'gwas', geneNames, transcriptGeneNames)
        }
        if (hasEqtl) {
            eqtlResult = runRegionQuery(analysisIds, regions, max, offset, cutoff, sortField, order, search, 'eqtl', geneNames, transcriptGeneNames)
        }

	[gwasResults: gwasResult, eqtlResults: eqtlResult]
    }

    private Map runRegionQuery(List<Long> analysisIds, List<Map> regions, Long max, Long offset, Double cutoff, String sortField,
	                       String order, String search, String type, List<String> geneNames, List<String> transcriptGeneNames){

	// the index lookups for deciphering the large text meta-data field.
	Map<Integer, Integer> indexMap = [:]

	// the list was filtered by region
	boolean wasRegionFiltered = regions

        def queryResult
	List analysisData = []
	int totalCount

	List<Map> columnNames = [] + DEFAULT_COLUMNS

	boolean wasShortcut = false
	if (!regions && !geneNames && !transcriptGeneNames && analysisIds.size() == 1 && sortField == 'null' && !cutoff && !search && max > 0) {
            wasShortcut = true
            //If displaying no regions and only one analysis, run the alternative query and pull back the rows for the limits
	    BioAssayAnalysis analysis = BioAssayAnalysis.get(analysisIds[0])
            def quickAnalysisData = regionSearchService.getQuickAnalysisDataByName(analysis.name, type)
            for (int i = offset; i < (max+offset); i++) {
		analysisData << quickAnalysisData.results[i]
            }
            totalCount = analysis.dataCount
        }
        else {
            //Otherwise, run the query and recache the returned data
	    if (sortField == 'null') {
		sortField = 'data.log_p_value'; order = 'desc'
	    }
	    queryResult = regionSearchService.getAnalysisData(analysisIds, regions, max, offset, cutoff, sortField, order,
		search, type, geneNames, transcriptGeneNames, true)
            analysisData = queryResult.results
            totalCount = queryResult.total
        }

	List<BioAssayAnalysisDataIdx> analysisIndexData
	if (type == 'eqtl') {
            analysisIndexData = gwasSearchService.getEqtlIndexData()
        }
        else {
            analysisIndexData = gwasSearchService.getGwasIndexData()
        }

	List returnedAnalysisData = []

	if (type == 'eqtl') {
	    columnNames << [sTitle: 'Transcript Gene', sortField: 'data.gene']
        }

	for (BioAssayAnalysisDataIdx b in analysisIndexData) {
            //Put the index information into a map so we can look it up later.
	    indexMap[b.field_idx] = b.display_idx

            //We need to take the data from the index table and extract the list of column names.
	    columnNames << [sTitle: b.field_name]
        }

        //The returned data needs to have the large text field broken out by delimiter.
	for (data in analysisData) {
	    if (data != null) {
                //This temporary list is used so that we return a list of lists.
                def temporaryList = []

                //The third element is our large text field. Split it into an array, leaving trailing empties.
		def largeTextField = data[3]?.split(';', -1)

                //This will be the array that is reordered according to the meta-data index table.
                //String[] newLargeTextField = new String[largeTextField.size()]
                String[] newLargeTextField = new String[indexMap.size()]
		int counter = 0
                //Loop over the elements in the index map.
		indexMap.each { entry ->
                    //Reorder the array based on the index table.
                    //if (it.key-1<newLargeTextField.size())
		    if (entry.key - 1 < largeTextField?.size()) {
			newLargeTextField[entry.value - 1] = largeTextField[entry.key - 1]
                        counter++
                    }
                    else {
                        newLargeTextField[counter]=''
                        counter++
                    }
                }

                //Swap around the data types for easy array addition.
		List<String> finalFields = newLargeTextField as List

                //Add the non-dynamic meta data fields to the returned data.
		temporaryList << data[4]
		temporaryList << data[0]
		temporaryList << data[1]
		temporaryList << data[2]
		temporaryList << data[5]
		temporaryList << data[6]
		temporaryList << data[7]
		temporaryList << data[8]
		temporaryList << data[9]
		temporaryList << data[10]
		if (type == 'eqtl') {
		    temporaryList << data[11]
                }

                //Add the dynamic fields to the returned data.
		temporaryList.addAll finalFields

		returnedAnalysisData << temporaryList
            }
        }

	[analysisData: returnedAnalysisData,
	 columnNames: columnNames,
	 max: max,
	 offset: offset,
	 cutoff: cutoff,
	 totalCount: totalCount,
	 wasRegionFiltered: wasRegionFiltered,
	 wasShortcut: wasShortcut]
    }

    private File cachedImagePathFor(Long analysisId) {
	new File(new File(cachedImageDir, analysisId as String), 'QQPlot.png')
    }

    private String imageUrlFor(Long analysisId) {
	link(controller: 'gwasSearch', action: 'downloadQQPlotImage', absolute: true, params: [analysisId: analysisId])
    }

    def downloadQQPlotImage(Long analysisId) {
        // Should probably check access

        if (!analysisId) {
            render status: 404
            return
        }

	File targetFile = cachedImagePathFor(analysisId)
        if (!targetFile.isFile()) {
	    logger.warn 'Request for {}, but such file does not exist', targetFile
            render status: 404
            return
        }

        sendFileService.sendFile servletContext, request, response, targetFile
    }

    def getQQPlotImage(Long analysisId, Double pvalueCutoff, String search) {

        try {
            //We need to determine the data type of this analysis so we know where to pull the data from.
	    BioAssayAnalysis currentAnalysis = BioAssayAnalysis.get(analysisId)

	    File cachedQqPlotFile = cachedImagePathFor(analysisId)
            // use QQPlots cached images if they are available. QQPlots takes >10 minutes to run and only needs to be generated once per analysis.
            if (cachedQqPlotFile.exists()) {
		render([imageURL: imageUrlFor(analysisId)] as JSON)
                return
            }

	    if (!pvalueCutoff) {
		pvalueCutoff = 0
	    }
	    if (!search) {
		search = ''
	    }

            //Throw an error if we don't find the analysis for some reason.
            if(!currentAnalysis) {
                throw new Exception('Analysis not found.')
            }

	    //Get list of REGION restrictions from session and translate to regions
	    List<Map> regions = getSearchRegions()
	    List<String> geneNames = getGeneNames()
	    List<String> transcriptGeneNames = getTranscriptGeneNames()
	    List<Long> analysisIds = [currentAnalysis.id]

	    // the GWAS Data. Call a different class based on the data type.
            def analysisData
	    // the data from the index table for GWAS.
            def analysisIndexData
            switch(currentAnalysis.assayDataType) {
                case 'GWAS' :
                case 'GWAS Fail' :
                case 'Metabolic GWAS' :
		    analysisData = regionSearchService.getAnalysisData(analysisIds, regions, 0, 0, pvalueCutoff,
				'null', 'asc', search, 'gwas', geneNames, transcriptGeneNames, false).results
                    analysisIndexData = gwasSearchService.getGwasIndexData()
                    break
                case 'EQTL' :
		    analysisData = regionSearchService.getAnalysisData(analysisIds, regions, 0, 0, pvalueCutoff,
				'null', 'asc', search, 'eqtl', geneNames, transcriptGeneNames, false).results
                    analysisIndexData = gwasSearchService.getEqtlIndexData()
                    break
                    default :
			throw new Exception('No applicable data type found.')
            }

	    // the index lookups for deciphering the large text meta-data field.
	    def indexMap = [:]
	    for (it in analysisIndexData) {
                //Put the index information into a map so we can look it up later. Only add the GOOD_CLUSTERING column.
                if(it.field_name == 'GOOD_CLUSTERING') {
                    indexMap[it.field_idx] = it.display_idx
                }
            }

	    List<List> returnedAnalysisData = []
            //Create an entry that represents the headers to print to the file.
	    returnedAnalysisData << ['PROBEID', 'pvalue', 'good_clustering']

            //The returned data needs to have the large text field broken out by delimiter.
	    for (data in analysisData) {
                //This temporary list is used so that we return a list of lists.
                def temporaryList = []

                //This will be used to fill in the data array.
		int indexCount = 0

                //The third element is our large text field. Split it into an array.
		def largeTextField = data[3]?.split(';', -1)

                //This will be the array that is reordered according to the meta-data index table.
                String[] newLargeTextField = new String[indexMap.size()]

                //Loop over the elements in the index map.
		for (it in indexMap) {
                    //Reorder the array based on the index table.
                    newLargeTextField[indexCount] = largeTextField[it.key-1]

                    indexCount++
                }

                //Swap around the data types for easy array addition.
		List<String> finalFields = newLargeTextField as List

                //Add the non-dynamic meta data fields to the returned data.
		temporaryList << data[0]
		temporaryList << data[1]

                //Add the dynamic fields to the returned data.
		temporaryList.addAll finalFields

		returnedAnalysisData << temporaryList
            }

	    String currentWorkingDirectory = gwasWebService.createTemporaryDirectory('QQPlot-' + UUID.randomUUID()) + '/workingDirectory/'

            //Write the data file for generating the image.
	    gwasWebService.writeDataFile(currentWorkingDirectory, returnedAnalysisData, 'QQPlot.txt')

            //Run the R script to generate the image file.
	    RModulesJobProcessingService.runRScript(currentWorkingDirectory, '/QQ/QQPlot.R', "create.qq.plot('QQPlot.txt')", pluginScriptDirectory)

            //Verify the image file exists.
	    FileUtils.copyFile(new File(currentWorkingDirectory, 'QQPlot.png'), cachedQqPlotFile)
	    render([imageURL: imageUrlFor(currentAnalysis.id)] as JSON)
        }
	catch (e) {
            response.status = 500
	    renderException e
	}
    }

    //Retrieve the results for the search filter. This is used to populate the result grids on the search page.
    def getAnalysisResults(Long max, Long offset, Double cutoff, String sortField, String order, String search,
	                   Long analysisId, Boolean export) {

	//TODO Combine this and the table method, they're now near-identical
	if (!cutoff) {
	    cutoff = 0
	} //Special case - cutoff is 0 if blank string

	Map filter = session['filterAnalysis' + analysisId] ?: [:]
	if (max != null) {
	    filter.max = max
        }
	if (!filter.max || filter.max < 10) {
	    filter.max = 10
        }

	if (offset != null) {
	    filter.offset = offset
        }
	if (!filter.offset || filter.offset < 0) {
	    filter.offset = 0
        }

	if (cutoff != null) {
	    filter.cutoff = cutoff
        }

	if (sortField != null) {
	    filter.sortField = sortField
        }
	if (!filter.sortField) {
	    filter.sortField = 'null'
        }

	if (order != null) {
	    filter.order = order
        }
	if (!filter.order) {
	    filter.order = 'asc'
        }

	if (search != null) {
	    filter.search = search
        }

	List<Long> analysisIds = [analysisId]

        session['filterAnalysis' + analysisId] = filter

        //Override max and offset if we're exporting
	long maxToUse = filter.max
	long offsetToUse = filter.offset
        if (export) {
            maxToUse = 0
            offsetToUse = 0
        }

        def regionSearchResults
        try {
	    regionSearchResults = getRegionSearchResults(
		maxToUse, offsetToUse, filter.cutoff, filter.sortField,
		filter.order, filter.search, analysisIds)
        }
	catch (e) {
	    renderException e
            return
        }

        try {
            //regionSearchResults will either contain GWAS or EQTL data. Overwrite the base object with the one that's populated
            if (regionSearchResults.gwasResults) {
                regionSearchResults = regionSearchResults.gwasResults
            }
            else {
                regionSearchResults = regionSearchResults.eqtlResults
            }

            if (!regionSearchResults) {
                render(text: '<p>No data could be found for this analysis type.</p>')
                return
            }

	    //Return the data as a Grails template or CSV
            if (export) {
                exportResults(regionSearchResults.columnNames, regionSearchResults.analysisData, 'analysis' + analysisId + '.csv')
            }
            else {
		render template: '/GWAS/analysisResults',
		    model: [analysisData     : regionSearchResults.analysisData,
			    columnNames      : regionSearchResults.columnNames,
			    max              : regionSearchResults.max,
			    offset           : regionSearchResults.offset,
			    cutoff           : filter.cutoff,
			    sortField        : filter.sortField,
			    order            : filter.order,
			    search           : filter.search,
			    totalCount       : regionSearchResults.totalCount,
			    wasRegionFiltered: regionSearchResults.wasRegionFiltered,
			    wasShortcut      : regionSearchResults.wasShortcut,
			    analysisId       : analysisId]
            }
        }
	catch (e) {
	    render status: 500, text: e.message
        }
    }

    //Retrieve the results for all analyses currently examined.
    def getTableResults(Long max, Long offset, Double cutoff, String sortField, String order, String search,
	                Boolean export) {

	List<Long> analysisIds = session.solrAnalysisIds
	if (analysisIds[0] == -1) {
	    // in the case that no filter is selected - where we get no a 'not a set' indicator from the session
	    // which results in an empty set after the intersection with 'allowed ids' below
	    render '<p>To use the table view, please select one of more filters from the filter browser in the left pane.</p>'
	    return
        }

	if (!cutoff) {
	    cutoff = 0
	} //Special case - cutoff is 0 if blank string

	Map filter = session['filterTableView'] ?: [:]

	if (max != null) {
	    filter.max = max
	}
	if (!filter.max || filter.max < 10) {
	    filter.max = 10
	}

	if (offset != null) {
	    filter.offset = offset
	}
	if (!filter.offset || filter.offset < 0) {
	    filter.offset = 0
	}

	if (cutoff != null) {
	    filter.cutoff = cutoff
	}

	if (sortField != null) {
	    filter.sortField = sortField
	}
	if (!filter.sortField) {
	    filter.sortField = 'null'
	}

	if (order != null) {
	    filter.order = order
	}
	if (!filter.order) {
	    filter.order = 'asc'
	}

	if (search != null) {
	    filter.search = search
        }

        // following code will limit analysis ids to ones that the user is allowed to access
	Map<String, Long> secObjs = gwasWebService.getExperimentSecureStudyList()
	List<Object[]> analyses = BioAssayAnalysis.executeQuery('''
		select b.id, b.name, b.etlId
		from BioAssayAnalysis b
		order by b.name''')
	analyses = analyses.findAll { Object[] row ->
	    String etlId = row[2]
	    !secObjs.containsKey(etlId) || gwasWebService.getGWASAccess(etlId) != 'Locked'
	}
	analyses = analyses.findAll { Object[] row ->
	    analysisIds.contains row[0]
	} // get intersection of all analyses id and allowed ids

	analysisIds = analyses.collect { it[0] }
	if (!analysisIds) {
	    render '<p>No analyses were found for the current filter!</p>'
            return
        }

        //Override max and offset if we're exporting
	long maxToUse = filter.max
	long offsetToUse = filter.offset
        if (export) {
            maxToUse = 0
            offsetToUse = 0
        }

	Map regionSearchResults
        try {
            regionSearchResults = getRegionSearchResults(maxToUse, offsetToUse, filter.cutoff, filter.sortField, filter.order, filter.search, analysisIds)
        }
	catch (e) {
	    renderException e
            return
        }

        //Return the data as a GRAILS template or CSV
        if (export) {
	    if (params.type == 'GWAS') {
                exportResults(regionSearchResults.gwasResults.columnNames, regionSearchResults.gwasResults.analysisData, 'results.csv')
            }
            else {
                exportResults(regionSearchResults.eqtlResults.columnNames, regionSearchResults.eqtlResults.analysisData, 'results.csv')
            }
        }
        else {
	    render template: '/GWAS/gwasAndEqtlResults', model: [
		results: regionSearchResults,
		cutoff: filter.cutoff,
		sortField: filter.sortField,
		order: filter.order,
		search: filter.search]
        }
    }

    private Double getSearchCutoff() {
	String cutoff
	for (s in sessionSolrSearchFilter()) {
            if (s.startsWith('PVALUE') && s.length() > 6) {
		String[] pvalue = s.substring(7).split('\\^')
		if (pvalue.length > 1) {
                    cutoff = pvalue[1]
                }
            }
        }
        if (cutoff) {
            return cutoff.toDouble()
        }
    }

    private List<Map> getSearchRegions() {
	List<Map> regions = []

	for (String s in sessionSolrSearchFilter()) {
            if (s.startsWith('REGION')) {
                //Cut off REGION:, split by pipe and interpret chromosomes and genes
		for (r in s.substring(7).split('\\|')) {
                    //Chromosome
                    if (r.startsWith('CHROMOSOME')) {
			String[] region = r.split('\\^')
			String chrom = region[1]
			long position = region[3] as long
			String direction = region[4]
			long range = region[5] as long
			String ver = region[6]
			String low = position
			String high = position
			if (direction == 'plus') {
                            high = position + range
                        }
			else if (direction == 'minus') {
                            low = position - range
                        }
                        else {
                            high = position + range
                            low = position - range
                        }

			regions << [gene: null, chromosome: chrom, low: low, high: high, ver: ver]
                    }
                    //Gene
                    else {
			String[] region = r.split('\\^')
			long geneId = region[1] as long
			String direction = region[2]
			long range = region[3] as long
			String ver = region[4]
			SearchKeyword searchKeyword = SearchKeyword.get(geneId)
			Map limits
			if (searchKeyword.dataCategory == 'GENE') {
                            limits = regionSearchService.getGeneLimits(geneId, ver, 0L)
                        }
			else if (searchKeyword.dataCategory == 'SNP') {
                            limits = regionSearchService.getSnpLimits(geneId, ver, 0L)
                        }
                        if (limits) {
			    long low = limits.low
			    long high = limits.high
			    if (direction == 'plus') {
                                high = high + range
                            }
			    else if (direction == 'minus') {
                                low = low - range
                            }
                            else {
                                high = high + range
                                low = low - range
                            }
			    regions << [gene: geneId, chromosome: limits.chrom, low: low, high: high, ver: ver]
                        }
                        else {
                            logger.error('regionSearchService, called from GwasSearchController.getSearchRegions, returned ' +
					 'a null value for limit; most likely this is from a filter request that will fail ' +
					 'as a consequence of this error.')
                        }
                    }
                }
            }
            else if (s.startsWith('GENESIG') || s.startsWith('GENELIST')  ) {

                while (s.startsWith('GENELIST')) {
                    s = s.substring(9)
                }

                while (s.startsWith('GENESIG')) {
                    s = s.substring(8)
                }

		for (sigId in s.split('\\|')) {

		    GeneSignature sig = GeneSignature.get(sigId)
		    List<GeneSignatureItem> sigItems = GeneSignatureItem.createCriteria().list() {
                        eq('geneSignature', sig)
                        or {
                            like('bioDataUniqueId', 'GENE%')
                            like('bioDataUniqueId', 'SNP%')
                        }
                    }
		    for (GeneSignatureItem sigItem in sigItems) {
			SearchKeyword searchItem = SearchKeyword.findByUniqueId(sigItem.bioDataUniqueId)

			if (searchItem?.dataCategory == 'SNP' ) {
			    long rsId = searchItem.id as long
			    if (!rsId) {
				continue
			    }
			    Map limits = regionSearchService.getSnpLimits(rsId, '19', sig.flankingRegion)
			    regions << [gene: rsId, chromosome: limits.get('chrom'), low: limits.get('low'), high: limits.get('high'), ver: '19']
			}
			else if (searchItem?.dataCategory == 'GENE') {
			    def geneId = searchItem?.id
			    def limits = regionSearchService.getGeneLimits(geneId, '19', sig.flankingRegion)
			    if (limits!=null) {
				regions << [gene: geneId, chromosome: limits.get('chrom'), low: limits.get('low'), high: limits.get('high'), ver: '19']
			    }
			    else {
				logger.debug 'Gene not found deapp:{}', geneId
			    }
			}
                    }
                }
            }
            else if (s.startsWith('GENE')) {
                //If just plain genes, get the limits and default to HG19 as the version
		for (geneString in s.substring(5).split('\\|')) {
		    long geneId = SearchKeyword.findByUniqueId(geneString).id
		    Map limits = regionSearchService.getGeneLimits(geneId, '19', 0L)
		    regions << [gene: geneId, chromosome: limits.chrom, low: limits.low, high: limits.high, ver: '19']
                }
            }
            else if (s.startsWith('SNP')) {
                //If plain SNPs, as above (default to HG19)
		for (rsId in s.substring(4).split('\\|')) {
		    Map limits = regionSearchService.getSnpLimits(rsId as long, '19', 0L)
		    regions << [gene: rsId, chromosome: limits.chrom, low: limits.low, high: limits.high, ver: '19']
                }
            }
        }

	regions
    }

    private List<String> getGeneNames() {
	List<String> genes = []

	for (String s in sessionSolrSearchFilter()) {
            if (s.startsWith('GENESIG')|| s.startsWith('GENELIST'))  {

                while (s.startsWith('GENELIST')) {
                    s = s.substring(9)
                }

                while (s.startsWith('GENESIG')) {
                    s = s.substring(8)
                }

                //Expand regions to genes and get their names
		for (sigId in s.split('\\|')) {
		    List<GeneSignatureItem> sigItems = GeneSignatureItem.createCriteria().list() {
                        eq('geneSignature', GeneSignature.get(sigId))  //sigSearchKeyword.bioDataId))
                        like('bioDataUniqueId', 'GENE%')
                    }
		    for (GeneSignatureItem sigItem in sigItems) {
			Long geneId = SearchKeyword.findByUniqueId(sigItem.bioDataUniqueId)?.id
			if (!geneId) {
			    continue
			} //Signature may contain SNPs or probes
			genes << SearchKeyword.get(geneId).keyword
                    }
                }
            }
            else if (s.startsWith('GENE')) {
                //If just plain genes, get the names
		for (geneString in s.substring(5).split('\\|')) {
		    genes << SearchKeyword.findByUniqueId(geneString).keyword
                }
            }
        }

	genes
    }

    private List<String> getTranscriptGeneNames() {
	List<String> genes = []

	for (String s in sessionSolrSearchFilter()) {
            if (s.startsWith('TRANSCRIPTGENE')) {
                //If just plain genes, get the names
		for (geneString in s.substring(15).split('\\|')) {
		    genes << geneString
                }
            }
        }

	genes
    }

    private renderException(Exception e) {
	logger.error e.message, e

        if (e instanceof UndeclaredThrowableException) {
	    e = e.undeclaredThrowable
        }

	while (e.cause && e.cause != e) {
	    e = e.cause
        }

	StackTraceElement[] stackTrace = e.stackTrace

	StringBuilder text = new StringBuilder()
	text << "<div class='errorbox'>tranSMART encountered an error while running this query (" << e.class.getName() << ' ' << e.message
	text << '). Please contact an administrator with your search criteria and the information below.</div>'
	text << "<pre class='errorstacktrace'>"
	text << '<b>Error while retrieving data: ' << e.class.name << '.</b> Message: ' << e.message << '\n'

        for (el in stackTrace) {
	    text << '\t' << el.className << '.' << el.methodName << ', line ' << el.lineNumber << ' \n'
        }
	text << '</pre>'

	render text.toString()
    }

    private void exportResults(columns, rows, String filename) {

        response.setHeader('Content-disposition', 'attachment; filename=' + filename)
        response.contentType = 'text/plain'

        CSVWriter csv = new CSVWriter(response.writer)

	csv.writeNext(columns*.sTitle as String[])

        for (row in rows) {
	    csv.writeNext(row as String[])
        }

        csv.close()
    }

    //Common Method to export analysis data as link or attachment
    private void exportAnalysisData(long analysisId, Writer dataWriter, cutoff, regions, geneNames, transcriptGeneNames, max) {
	BioAssayAnalysis analysis = BioAssayAnalysis.get(analysisId)
	List<Long> analysisArr = [analysisId]
	Map analysisData
        if (analysis.assayDataType == 'GWAS' || analysis.assayDataType == 'Metabolic GWAS' || analysis.assayDataType == 'GWAS Fail') {
	    analysisData = regionSearchService.getAnalysisData(analysisArr, regions, max, 0, cutoff, 'data.log_p_value',
							       'desc', null, 'gwas', geneNames, transcriptGeneNames, false)
        }
        else {
	    analysisData = regionSearchService.getAnalysisData(analysisArr, regions, max, 0, cutoff, 'data.log_p_value',
							       'desc', null, 'eqtl', geneNames, transcriptGeneNames, false)
        }

        dataWriter.write 'Probe ID\tp-value\t-log10 p-value\tRS Gene\tChromosome\tPosition\tInteronExon\tRecombination Rate\tRegulome Score\n'

	List<List> dataset = analysisData.results
	for (List row in dataset) {
	    for (int i = 0; i < row.size(); i++) {
                if ((i < 3) || (i > 4)) {
		    if (row[i] != null) {
			dataWriter.write row[i] + '\t'
                    }
                    else {
                        dataWriter.write '\t'
                    }
                }
            }
            dataWriter.write '\n'
        }

        dataWriter.close()
    }
		
    def exportAnalysis(Double cutoff, String isLink) {

	Double searchCutoff = getSearchCutoff()
	if (searchCutoff) {
	    cutoff = searchCutoff
	}
	List<Map> regions = getSearchRegions()
	List<String> geneNames = getGeneNames()
	List<String> transcriptGeneNames = getTranscriptGeneNames()
	List<String> queryparameter = sessionSolrSearchFilter()

	Map<String, Long> secObjs = gwasWebService.getExperimentSecureStudyList()
		
        if (isLink == 'true') {
	    Long analysisId = params.long('analysisId')
	    response.setHeader('Content-disposition', 'attachment; filename=' + analysisId + '_ANALYSIS_DATA.txt')
            response.contentType = 'text/plain'
	    exportAnalysisData(analysisId, new PrintWriter(response.writer), cutoff, regions, geneNames, transcriptGeneNames, 0)
        }
        else if (isLink == 'false') {
	    String mailId = params.toMailId
	    StringBuilder link = new StringBuilder()
	    if (queryparameter) {
		link << 'Query Criteria at time of export: ' << queryparameter << '\n'
	    }
	    link << createLink(controller: 'gwasSearch', action: 'exportAnalysis', absolute: true)
	    String links = ''
	    for (analysisId in params.analysisIds.split(',')) {
		BioAssayAnalysis analysis = BioAssayAnalysis.get(analysisId)
		String access = gwasWebService.getGWASAccess(analysis.etlId)
		if (!secObjs.containsKey(analysis.etlId) || (access != 'Locked' && access != 'VIEW')) {
		    links += link + '?analysisId=' + analysisId + '&regions=' +
			regions.toString().replace(' ', '') + '&cutoff=' + cutoff +
			'&geneNames=' + geneNames.toString().replace(' ', '') +
			'&isLink=true\n'
		}
		else{
		    links += 'Analysis ' + analysis.name + ' is a restricted study, you do not have permission to export.\n'
		}
            }

            sendMail {
                to mailId
		subject 'Export Analysis Results'
                text links
            }
        }
        else {
	    String[] analysisIds = params.analysisIds.split(',')
	    String mailId = params.toMailId
	    String restrictedMsg = ''
	    String timestamp = new Date().format('yyyyMMddhhmmss')
	    String rootFolder = 'Export_' + timestamp
	    String rootDir = mailAttachmentsDir + File.separator + rootFolder
	    def analysisAIds=[]
	    for(analysisId in analysisIds){
		BioAssayAnalysis analysis = BioAssayAnalysis.get(analysisId)
		String access = gwasWebService.getGWASAccess(analysis.etlId)
		if (!secObjs.containsKey(analysis.etlId) || (access != 'Locked' && access != 'VIEW')) {
		    analysisAIds << analysisId.toLong()
		}
		else{
		    restrictedMsg += 'Analysis ' + analysis.name + 'is a restricted study, you do not have permission to export.\n'
		}
	    }

	    if (analysisIds) {
                for (analysisId in analysisAIds) {
		    BioAssayAnalysis analysis = BioAssayAnalysis.get(analysisId)
		    String accession = analysis.etlId
		    String analysisName = analysis.name
		    Matcher match = Pattern.compile('[^a-zA-Z0-9 ]').matcher(analysisName)
                    while(match.find()){
                        String s= match.group()
                        analysisName=analysisName.replaceAll('\\'+s, '')
                    }

		    String dirStudy = rootDir + File.separator + accession + File.separator
		    String dirAnalysis = dirStudy + analysisName
		    new File(dirAnalysis).mkdirs()

                    //Creating Analysis Data file
		    File file = new File(dirAnalysis, analysisId.toString() + '_ANALYSIS_DATA.txt')
                    BufferedWriter dataWriter = new BufferedWriter(new FileWriter(file))
		    exportAnalysisData(analysisId.toLong(), dataWriter, cutoff, regions, geneNames, transcriptGeneNames, 200)

                    //This is to generate a file with Study Metadata
		    BufferedWriter dataWriterStudy = new BufferedWriter(new FileWriter(new File(dirStudy, accession + '_STUDY_METADATA.txt')))

		    Experiment exp = Experiment.findByAccession(accession, [max: 1])

		    List<FormLayout> formLayouts = FormLayout.createCriteria().list() {
                        eq('key', 'study')
                        order('sequence', 'asc')
                    }

		    for (formLayout in formLayouts) {
			String dispName = formLayout.displayName
			dataWriterStudy.write dispName + ':'
			if (formLayout.column == 'accession') {
			    dataWriterStudy.write exp.accession + '\n'
                        }
			if (formLayout.column == 'title') {
                            if (exp.title) {
				dataWriterStudy.write exp.title
                            }
                            dataWriterStudy.write '\n'
                        }
			if (formLayout.column == 'description') {
                            if (exp.description) {
				dataWriterStudy.write exp.description
                            }
                            dataWriterStudy.write '\n'
                        }
			if (formLayout.column == 'institution') {
                            if (exp.institution) {
				dataWriterStudy.write exp.institution
                            }
                            dataWriterStudy.write '\n'
                        }
			if (formLayout.column == 'primaryInvestigator') {
                            if (exp.primaryInvestigator) {
				dataWriterStudy.write exp.primaryInvestigator
                            }
                            dataWriterStudy.write '\n'
                        }
			if (formLayout.column == 'adHocPropertyMap.Study Short Name') {
                            def add_col = exp.getAdHocPropertyMap().get('Study Short Name')
			    if (add_col) {
				dataWriterStudy.write(add_col as String)
                            }
                            dataWriterStudy.write '\n'
                        }
			if (formLayout.column == 'adHocPropertyMap.Data Availability') {
                            def add_col = exp.getAdHocPropertyMap().get('Data Availability')
			    if (add_col) {
				dataWriterStudy.write(add_col as String)
                            }
                            dataWriterStudy.write '\n'
                        }
                    }
                    dataWriterStudy.close()

		    File fileMeta = new File(dirAnalysis, analysisId.toString() + '_ANALYSIS_METADATA.txt')
                    BufferedWriter dataWriterMeta = new BufferedWriter(new FileWriter(fileMeta))
		    List<FormLayout> layouts = FormLayout.createCriteria().list() {
                        eq('key', 'analysis')
                        order('sequence', 'asc')
                    }
		    for (FormLayout layout in layouts) {
			String dispName = layout.displayName
			if (analysis.assayDataType == 'EQTL' && layout.column == 'phenotypes') {
                            dataWriterMeta.write '\nDiseases:'
                        }
			else if ((analysis.assayDataType == 'EQTL' || analysis.assayDataType == 'GWAS' ||
				  analysis.assayDataType == 'GWAS Fail' || analysis.assayDataType == 'Metabolic GWAS') &&
				 (layout.column == 'pValueCutoff' || layout.column == 'foldChangeCutoff')) {
                            //do nothing
                        }
                        else {
                            dataWriterMeta.write '\n' + dispName + ':'
                        }
			if (layout.column == 'study') {
                            if (exp.title) {
				dataWriterMeta.write exp.title
                            }
                        }
			else if (layout.column == 'phenotypes') {
			    for (disease in analysis.diseases.disease) {
				dataWriterMeta.write disease + ';'
                            }

                            if (!(analysis.assayDataType == 'EQTL')) {
				for (name in analysis.observations.name) {
				    dataWriterMeta.write name + ';'
                                }
                            }
                        }
			else if (layout.column == 'platforms') {
                            analysis.platforms.each() {
                                def add_col = it.vendor + ':' + it.name
                                dataWriterMeta.write add_col + ';'
                            }
                        }
			else if (layout.column == 'name') {
                            if (analysis.name) {
				dataWriterMeta.write analysis.name
                            }
                        }
			else if (layout.column == 'assayDataType') {
                            if (analysis.assayDataType) {
				dataWriterMeta.write analysis.assayDataType
                            }
                        }
			else if (layout.column == 'shortDescription') {
                            if (analysis.shortDescription) {
				dataWriterMeta.write analysis.shortDescription
                            }
                        }
			else if (layout.column == 'longDescription') {
                            if (analysis.longDescription) {
				dataWriterMeta.write analysis.longDescription
                            }
                        }
			else if (layout.column == 'pValueCutoff') {
                            if (analysis.pValueCutoff) {
				dataWriterMeta.write analysis.pValueCutoff as String
                            }
                        }
			else if (layout.column == 'foldChangeCutoff') {
                            if (analysis.foldChangeCutoff) {
				dataWriterMeta.write analysis.foldChangeCutoff as String
                            }
                        }
			else if (layout.column == 'qaCriteria') {
                            if (analysis.qaCriteria) {
				dataWriterMeta.write analysis.qaCriteria
                            }
                        }
			else if (layout.column == 'analysisMethodCode') {
                            if (analysis.analysisMethodCode) {
				dataWriterMeta.write analysis.analysisMethodCode
                            }
                        }
                        else if (analysis.ext != null) {
			    if (layout.column == 'ext.population') {
                                if (analysis.ext.population) {
				    dataWriterMeta.write analysis.ext.population
                                }
                            }
			    else if (layout.column == 'ext.sampleSize') {
                                if (analysis.ext.sampleSize) {
				    dataWriterMeta.write analysis.ext.sampleSize
                                }
                            }
			    else if (layout.column == 'ext.tissue') {
                                if (analysis.ext.tissue) {
				    dataWriterMeta.write analysis.ext.tissue
                                }
                            }
			    else if (layout.column == 'ext.cellType') {
                                if (analysis.ext.cellType) {
				    dataWriterMeta.write analysis.ext.cellType
                                }
                            }
			    else if (layout.column == 'ext.genomeVersion') {
                                if (analysis.ext.genomeVersion) {
				    dataWriterMeta.write analysis.ext.genomeVersion
                                }
                            }
			    else if (layout.column == 'ext.researchUnit') {
                                if (analysis.ext.researchUnit) {
				    dataWriterMeta.write analysis.ext.researchUnit
                                }
                            }
			    else if (layout.column == 'ext.modelName') {
                                if (analysis.ext.modelName) {
				    dataWriterMeta.write analysis.ext.modelName
                                }
                            }
			    else if (layout.column == 'ext.modelDescription') {
                                if (analysis.ext.modelDescription) {
				    dataWriterMeta.write analysis.ext.modelDescription
                                }
                            }
                        }
                    }
                    dataWriterMeta.close()
                }
            }

            File topDir = new File(rootDir)

	    File zipFile = new File(mailAttachmentsDir, rootFolder + '.zip')
            ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipFile))

            int topDirLength = topDir.absolutePath.length()
			
            topDir.eachFileRecurse { file ->
		String relative = file.absolutePath.substring(topDirLength).replace('\\', '/')
                if (file.isDirectory() && !relative.endsWith('/')) {
                    relative += '/'
                }

                ZipEntry entry = new ZipEntry(relative)
                entry.time = file.lastModified()
                zipOutput.putNextEntry(entry)
                if (file.isFile()) {
                    zipOutput << new FileInputStream(file)
                }
            }

            zipOutput.close()

            //the path of the file e.g. : 'c:/Users/nikos7/Desktop/myFile.txt'
            String messageBody = 'Attached is the list of Analyses\n'+restrictedMsg
	    String file = zipFile.path
	    if (queryparameter) {
		messageBody += 'Query Criteria at time of export: ' + queryparameter + '\n'
	    }
            file.substring(file.lastIndexOf('/')+1)
            sendMail {
                multipart true
                to mailId
		subject 'Export of Analysis as attachment'
                text messageBody
		attach file.substring(file.lastIndexOf('/') + 1), 'application/zip', new File(file)
            }
        }

	render([status: 'success'] as JSON)
    }

    private List<String> sessionSolrSearchFilter() {
	session.solrSearchFilter
    }
}
