import com.recomdata.util.ExcelGenerator
import com.recomdata.util.ExcelSheet
import com.recomdata.util.ariadne.Batch
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import org.transmart.SearchResult
import org.transmart.biomart.LiteratureAlterationData
import org.transmart.biomart.LiteratureInhibitorData
import org.transmart.biomart.LiteratureInteractionData
import org.transmartproject.db.log.AccessLogService

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

/**
 * @author mmcduffie
 */
class LiteratureController {

    AccessLogService accessLogService
    JubilantResNetService jubilantResNetService
    LiteratureQueryService literatureQueryService
    SearchService searchService
    SpringSecurityService springSecurityService

    def index() {}

    def showJubFilter() {
	def searchFilter = session.searchFilter
	render template: 'litFilter', model: [
	    disease               : literatureQueryService.diseaseList(searchFilter),
	    diseaseSite           : literatureQueryService.diseaseSiteList(searchFilter),
	    component             : literatureQueryService.componentList(searchFilter),
	    mutationType          : literatureQueryService.mutationTypeList(searchFilter),
	    mutationSite          : literatureQueryService.mutationSiteList(searchFilter),
	    epigeneticType        : literatureQueryService.epigeneticTypeList(searchFilter),
	    epigeneticRegion      : literatureQueryService.epigeneticRegionList(searchFilter),
	    moleculeType          : literatureQueryService.moleculeTypeList(searchFilter),
	    ptmType               : literatureQueryService.ptmTypeList(searchFilter),
	    ptmRegion             : literatureQueryService.ptmRegionList(searchFilter),
	    source                : literatureQueryService.sourceList(searchFilter),
	    target                : literatureQueryService.targetList(searchFilter),
	    experimentalModel     : literatureQueryService.experimentalModelList(searchFilter),
	    mechanism             : literatureQueryService.mechanismList(searchFilter),
	    trialType             : literatureQueryService.trialTypeList(searchFilter),
	    trialPhase            : literatureQueryService.trialPhaseList(searchFilter),
	    inhibitorName         : literatureQueryService.inhibitorNameList(searchFilter),
	    trialExperimentalModel: literatureQueryService.trialExperimentalModelList(searchFilter)]
    }

    def getJubOncologyAlterationDetail(LiteratureAlterationData result) {
	render template: 'litDetail', model: [result: result ?: [:]]
    }

    def getJubOncologyInhibitorDetail(LiteratureInhibitorData result) {
	render template: 'litDetail', model: [result: result ?: [:]]
    }

    def getJubOncologyInteractionDetail(LiteratureInteractionData result) {
	render template: 'litDetail', model: [result: result ?: [:]]
    }

    def filterJubilant() {
        def searchFilter = session.searchFilter
        bindData(searchFilter.litFilter, params)
	searchFilter.litFilter.parseDiseaseSite(params.diseaseSite)
	searchFilter.litFilter.parseComponentList(params.componentList)
        for (alterationType in searchFilter.litFilter.alterationTypes.keySet()) {
	    searchFilter.litFilter.alterationTypes[alterationType] =
		'on' == params['alterationtype_' + alterationType.toLowerCase().replace(' ', '_')]
        }
        searchFilter.datasource = 'literature'

	SearchResult searchResult = new SearchResult()
	searchService.doResultCount(searchResult, searchFilter)
	render view: '/search/list', model: [searchresult: searchResult]
    }

    def datasourceJubilant(String datatype) {
        def searchFilter = session.searchFilter
	SearchResult searchResult = new SearchResult()
	searchResult.litJubOncAltCount = literatureQueryService.litJubOncAltCount(searchFilter)
	if (datatype == null && searchResult.litJubOncAltCount > 0) {
            datatype = 'JUBILANT_ONCOLOGY_ALTERATION'
        }
	searchResult.litJubOncInhCount = literatureQueryService.litJubOncInhCount(searchFilter)
	if (datatype == null && searchResult.litJubOncInhCount > 0) {
            datatype = 'JUBILANT_ONCOLOGY_INHIBITOR'
        }
	searchResult.litJubOncIntCount = literatureQueryService.litJubOncIntCount(searchFilter)
	if (datatype == null && searchResult.litJubOncIntCount > 0) {
            datatype = 'JUBILANT_ONCOLOGY_INTERACTION'
        }
	searchResult.litJubAsthmaAltCount = literatureQueryService.litJubAsthmaAltCount(searchFilter)
	if (datatype == null && searchResult.litJubAsthmaAltCount > 0) {
            datatype = 'JUBILANT_ASTHMA_ALTERATION'
        }
	searchResult.litJubAsthmaInhCount = literatureQueryService.litJubAsthmaInhCount(searchFilter)
	if (datatype == null && searchResult.litJubAsthmaInhCount > 0) {
            datatype = 'JUBILANT_ASTHMA_INHIBITOR'
        }
	searchResult.litJubAsthmaIntCount = literatureQueryService.litJubAsthmaIntCount(searchFilter)
	if (datatype == null && searchResult.litJubAsthmaIntCount > 0) {
            datatype = 'JUBILANT_ASTHMA_INTERACTION'
        }
	searchResult.litJubAsthmaPECount = literatureQueryService.litJubAsthmaPECount(searchFilter)
	if (datatype == null && searchResult.litJubAsthmaPECount > 0) {
            datatype = 'JUBILANT_ASTHMA_PROTEIN_EFFECT'
        }
	searchResult.resultType = datatype
        switch (datatype) {
            case 'JUBILANT_ONCOLOGY_ALTERATION':
		searchResult.resultCount = searchResult.litJubOncAltCount
		searchResult.result = literatureQueryService.litJubOncAltData(searchFilter, params)
                break
            case 'JUBILANT_ONCOLOGY_INHIBITOR':
		searchResult.resultCount = searchResult.litJubOncInhCount
		searchResult.result = literatureQueryService.litJubOncInhData(searchFilter, params)
                break
            case 'JUBILANT_ONCOLOGY_INTERACTION':
		searchResult.resultCount = searchResult.litJubOncIntCount
		searchResult.result = literatureQueryService.litJubOncIntData(searchFilter, params)
                break
            case 'JUBILANT_ASTHMA_ALTERATION':
		searchResult.resultCount = searchResult.litJubAsthmaAltCount
		searchResult.result = literatureQueryService.litJubAsthmaAltData(searchFilter, params)
                break
            case 'JUBILANT_ASTHMA_INHIBITOR':
		searchResult.resultCount = searchResult.litJubAsthmaInhCount
		searchResult.result = literatureQueryService.litJubAsthmaInhData(searchFilter, params)
                break
            case 'JUBILANT_ASTHMA_INTERACTION':
		searchResult.resultCount = searchResult.litJubAsthmaIntCount
		searchResult.result = literatureQueryService.litJubAsthmaIntData(searchFilter, params)
                break
            case 'JUBILANT_ASTHMA_PROTEIN_EFFECT':
		searchResult.resultCount = searchResult.litJubAsthmaPECount
		searchResult.result = literatureQueryService.litJubAsthmaPEData(searchFilter, params)
                break
        }
	render template: 'litResult', model: [searchresult: searchResult]
    }

    def createJubSummary() {
        def searchFilter = session.searchFilter
	List<Map> rows = []
	for (summary in literatureQueryService.litJubOncAltSumData(searchFilter, params)) {
	    rows << [dataType          : summary.dataType,
                     alterationType    : summary.alterationType,
                     totalFrequency    : summary.totalFrequency,
                     totalAffectedCases: summary.totalAffectedCases,
                     summary           : summary.summary,
                     target            : summary.target,
                     variant           : summary.variant,
                     diseaseSite       : summary.diseaseSite]
        }

	[count: literatureQueryService.litJubOncAltSumCount(searchFilter), rows: rows]
    }

    def jubSummaryJSON() {
	render(params.callback + '(' + (createJubSummary() as JSON) + ')')
    }

    private ExcelSheet createLitSheet(String sheetName, String tableName, List<String> tableCols, results) {

	Set<String> dataCols = [] // all the null column names from each result set.
	List<Map<String, String>> dataRows = []

	for (record in results) {
	    Map<String, String> row = [:]
	    def refRecord = record.reference
	    for (col in litRefDataColumns) {
		String value = refRecord[col]
		if (value) {
		    dataCols << 'LiteratureReferenceData.' + col
		    row['LiteratureReferenceData.' + col] = value
		}
	    }
	    for (col in tableCols) {
		String value = record[col]
		if (value) {
		    dataCols << tableName + '.' + col
		    row[tableName + '.' + col] = value
		}
	    }
	    if (tableName != 'LiteratureInhibitorData' && record.inVivoModel != null) {
		for (col in litModelDataColumns) {
		    String value = record.inVivoModel[col]
		    if (value) {
			dataCols << 'LiteratureModelData.InVivo.' + col
			row['LiteratureModelData.InVivo.' + col] = value
		    }
		}
	    }
	    if (tableName != 'LiteratureInhibitorData' && record.inVitroModel != null) {
		for (col in litModelDataColumns) {
		    String value = record.inVitroModel[col]
		    if (value) {
			dataCols << 'LiteratureModelData.InVitro.' + col
			row['LiteratureModelData.InVitro.' + col] = value
		    }
		}
	    }
	    if (tableName == 'LiteratureAlterationData' && record.assocMoleculeDetails) {
		for (amdRecord in record.assocMoleculeDetails) {
		    for (col in litAMDDataColumns) {
			String value = amdRecord[col]
			if (value) {
			    dataCols << 'LiteratureAssocMoleculeDetailsData.' + col
			    row['LiteratureAssocMoleculeDetailsData.' + col] = value
			}
		    }
		    dataRows << row
		}
	    }
	    else {
		dataRows << row
	    }
	}

	if (!dataRows) {
	    return null
	}

	// Only use non-null columns, but put them in order based on the static column lists defined above.
	List<String> cols = []
	List<String> orderedCols = []
	for (col in litRefDataColumns) {
	    String orderedCol = 'LiteratureReferenceData.' + col
	    if (dataCols.contains(orderedCol)) {
		cols << message(code: orderedCol, default: orderedCol)
		orderedCols << orderedCol
	    }
	}
	for (col in tableCols) {
	    String orderedCol = tableName + '.' + col
	    if (dataCols.contains(orderedCol)) {
		cols << message(code: orderedCol, default: orderedCol)
		orderedCols << orderedCol
	    }
	}
	if (tableName != 'LiteratureInhibitorData') {
	    for (col in litModelDataColumns) {
		String orderedCol = 'LiteratureModelData.InVivo.' + col
		if (dataCols.contains(orderedCol)) {
		    cols << 'In Vivo ' + message(code: 'LiteratureModelData.' + col, default: col)
		    orderedCols << orderedCol
		}
	    }
	    for (col in litModelDataColumns) {
		String orderedCol = 'LiteratureModelData.InVitro.' + col
		if (dataCols.contains(orderedCol)) {
		    cols << 'In Vitro ' + message(code: 'LiteratureModelData.' + col, default: col)
		    orderedCols << orderedCol
		}
	    }
	}
	if (tableName == 'LiteratureAlterationData') {
	    for (col in litAMDDataColumns) {
		String orderedCol = 'LiteratureAssocMoleculeDetailsData.' + col
		if (dataCols.contains(orderedCol)) {
		    cols << message(code: orderedCol, default: orderedCol)
		    orderedCols << orderedCol
		}
	    }
	}

	List<List<String>> rows = []
	for (Map<String, String> dataRow in dataRows) {
	    List<String> row = []
	    for (String col in orderedCols) {
		row << dataRow[col]
	    }
	    rows << row
	}

	new ExcelSheet(name: sheetName, headers: cols, values: rows)
    }

    def downloadJubData() {
	def searchFilter = session.searchFilter
	params.offset = 0

	List<ExcelSheet> sheets = []

	params.max = literatureQueryService.litJubOncAltCount(searchFilter)
	def results = literatureQueryService.litJubOncAltData(searchFilter, params)
	ExcelSheet sheet = createLitSheet('Jub Onc Alterations', 'LiteratureAlterationData', litAltDataColumns, results)
	if (sheet) {
	    sheets << sheet
	}

	params.max = literatureQueryService.litJubOncInhCount(searchFilter)
	results = literatureQueryService.litJubOncInhData(searchFilter, params)
	sheet = createLitSheet('Jub Onc Inhibitors', 'LiteratureInhibitorData', litInhDataColumns, results)
	if (sheet) {
	    sheets << sheet
	}

	params.max = literatureQueryService.litJubOncIntCount(searchFilter)
	results = literatureQueryService.litJubOncIntData(searchFilter, params)
	sheet = createLitSheet('Jub Onc Interactions', 'LiteratureInteractionData', litIntDataColumns, results)
	if (sheet) {
	    sheets << sheet
	}

	params.max = literatureQueryService.litJubAsthmaAltCount(searchFilter)
	results = literatureQueryService.litJubAsthmaAltData(searchFilter, params)
	sheet = createLitSheet('Jub Asthma Alterations', 'LiteratureAlterationData', litAltDataColumns, results)
	if (sheet) {
	    sheets << sheet
	}

	params.max = literatureQueryService.litJubAsthmaIntCount(searchFilter)
	results = literatureQueryService.litJubAsthmaIntData(searchFilter, params)
	sheet = createLitSheet('Jub Asthma Interactions', 'LiteratureInteractionData', litIntDataColumns, results)
	if (sheet) {
	    sheets << sheet
	}

	params.max = literatureQueryService.litJubAsthmaPECount(searchFilter)
	results = literatureQueryService.litJubAsthmaPEData(searchFilter, params)
	sheet = createLitSheet('Jub Asthma Protein Effects', 'LiteratureProteinEffectData', litPEDataColumns, results)
	if (sheet) {
	    sheets << sheet
	}

	if (sheets) {
	    response.setHeader('Content-Type', 'application/vnd.ms-excel; charset=utf-8')
	    response.setHeader('Content-Disposition', 'attachment; filename="literature.xls"')
	    response.setHeader('Cache-Control', 'must-revalidate, post-check=0, pre-check=0')
	    response.setHeader('Pragma', 'public')
	    response.setHeader('Expires', '0')
	    response.outputStream << new ExcelGenerator().generateExcel(sheets)
	}
	// TODO: Display error message if there is no data.
    }

    //	 Call the ResNetService to create the ResNet .rnef file
    def downloadresnet() {
	String misses = 'No results found'
	jubilantResNetService.searchFilter = session.searchFilter
	Marshaller m = JAXBContext.newInstance(Batch).createMarshaller()
	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
	Batch b = jubilantResNetService.createResNet()
	if (b) {
	    misses = 'Misses: ' + jubilantResNetService.misses
	    response.setHeader('Content-Disposition', 'attachment; filename="resnetexport.rnef"')
	    response.setHeader('Cache-Control', 'must-revalidate, post-check=0, pre-check=0')
	    response.setHeader('Pragma', 'public')
	    response.setHeader('Expires', '0')
	    m.marshal(b, response.writer)
	}
	else {
	    render template: 'noResult'
	}

	accessLogService.report 'Export ResNet', misses
    }

    private static final List<String> litRefDataColumns = [
        'component',
        'componentClass',
        'geneId',
        'moleculeType',
        'variant',
        'referenceType',
        'referenceId',
        'referenceTitle',
        'backReferences',
        'studyType',
        'disease',
        'diseaseIcd10',
        'diseaseMesh',
        'diseaseSite',
        'diseaseStage',
        'diseaseGrade',
        'diseaseTypes',
        'diseaseDescription',
        'physiology',
        'statClinical',
        'statClinicalCorrelation',
        'statTests',
        'statCoefficient',
        'statPValue',
	'statDescription'].asImmutable()

    private static final List<String> litAltDataColumns = [
        'alterationType',
        'control',
        'effect',
        'description',
        'techniques',
        'patientsPercent',
        'patientsNumber',
        'popNumber',
        'popInclusionCriteria',
        'popExclusionCriteria',
        'popDescription',
        'popType',
        'popValue',
        'popPhase',
        'popStatus',
        'popExperimentalModel',
        'popTissue',
        'popBodySubstance',
        'popLocalization',
        'popCellType',
        'clinSubmucosaMarkerType',
        'clinSubmucosaUnit',
        'clinSubmucosaValue',
        'clinAsmMarkerType',
        'clinAsmUnit',
        'clinAsmValue',
        'clinCellularSource',
        'clinCellularType',
        'clinCellularCount',
        'clinPriorMedPercent',
        'clinPriorMedDose',
        'clinPriorMedName',
        'clinBaselineVariable',
        'clinBaselinePercent',
        'clinBaselineValue',
        'clinSmoker',
        'clinAtopy',
        'controlExpPercent',
        'controlExpNumber',
        'controlExpValue',
        'controlExpSd',
        'controlExpUnit',
        'overExpPercent',
        'overExpNumber',
        'overExpValue',
        'overExpSd',
        'overExpUnit',
        'lossExpPercent',
        'lossExpNumber',
        'lossExpValue',
        'lossExpSd',
        'lossExpUnit',
        'totalExpPercent',
        'totalExpNumber',
        'totalExpValue',
        'totalExpSd',
        'totalExpUnit',
        'glcControlPercent',
        'glcMolecularChange',
        'glcType',
        'glcPercent',
        'glcNumber',
        'ptmRegion',
        'ptmType',
        'ptmChange',
        'lohLoci',
        'mutationType',
        'mutationChange',
        'mutationSites',
        'epigeneticRegion',
	'epigeneticType'].asImmutable()

    private static final List<String> litInhDataColumns = [
        'effectResponseRate',
        'effectDownstream',
        'effectBeneficial',
        'effectAdverse',
        'effectDescription',
        'effectPharmacos',
        'effectPotentials',
        'trialType',
        'trialPhase',
        'trialStatus',
        'trialExperimentalModel',
        'trialTissue',
        'trialBodySubstance',
        'trialDescription',
        'trialDesigns',
        'trialCellLine',
        'trialCellType',
        'trialPatientsNumber',
        'trialInclusionCriteria',
        'inhibitor',
        'inhibitorStandardName',
        'casid',
        'description',
        'concentration',
        'timeExposure',
        'administration',
        'treatment',
        'techniques',
        'effectMolecular',
        'effectPercent',
        'effectNumber',
        'effectValue',
        'effectSd',
	'effectUnit'].asImmutable()

    private static final List<String> litIntDataColumns = [
        'sourceComponent',
        'sourceGeneId',
        'targetComponent',
        'targetGeneId',
        'interactionMode',
        'regulation',
        'mechanism',
        'effect',
        'localization',
        'region',
	'techniques'].asImmutable()

    private static final List<String> litPEDataColumns = ['description'].asImmutable()

    private static final List<String> litModelDataColumns = [
        'description',
        'stimulation',
        'controlChallenge',
        'challenge',
        'sentization',
        'zygosity',
        'experimentalModel',
        'animalWildType',
        'tissue',
        'cellType',
        'cellLine',
        'bodySubstance',
        'component',
        'geneId'
    ]

    static List litAMDDataColumns = [
        'molecule',
        'moleculeType',
        'totalExpPercent',
        'totalExpNumber',
        'totalExpValue',
        'totalExpSd',
        'totalExpUnit',
        'overExpPercent',
        'overExpNumber',
        'overExpValue',
        'overExpSd',
        'overExpUnit',
        'coExpPercent',
        'coExpNumber',
        'coExpValue',
        'coExpSd',
        'coExpUnit',
        'mutationType',
        'mutationSites',
        'mutationChange',
        'mutationPercent',
        'mutationNumber',
        'targetExpPercent',
        'targetExpNumber',
        'targetExpValue',
        'targetExpSd',
        'targetExpUnit',
        'targetOverExpPercent',
        'targetOverExpNumber',
        'targetOverExpValue',
        'targetOverExpSd',
        'targetOverExpUnit',
        'techniques',
	'description'].asImmutable()
}
