import com.recomdata.tea.TEABaseResult
import com.recomdata.util.ExcelGenerator
import com.recomdata.util.ExcelSheet
import de.DeMrnaAnnotation
import groovy.util.logging.Slf4j
import org.transmart.AnalysisResult
import org.transmart.AssayAnalysisValue
import org.transmart.ExperimentAnalysisResult
import org.transmart.SearchResult
import org.transmart.TrialAnalysisResult
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Experiment

/**
 * @author mmcduffie */

@Slf4j('logger')
class AnalysisDataExportService {

    static transactional = false

    private static final List<String> headers1 = ['Analysis', 'ProbeSet', 'Fold Change Ratio', 'p-Value',
	                                          'adjusted p-value', 'TEA p-Value', 'Gene'].asImmutable()
    private static final List<String> headers2 = ['Analysis', 'Antigen', 'Fold Change Ratio', 'p-Value',
	                                          'adjusted p-Value', 'TEA p-Value', 'Gene'].asImmutable()
    private static final List<String> headers3 = ['Analysis', 'ProbeSet', 'r-Value', 'Gene'].asImmutable()
    private static final List<String> headers4 = ['Analysis', 'Antigen', 'r-Value', 'Gene'].asImmutable()
    private static final List<String> headers5 = ['Analysis', 'ProbeSet', 'rho-value', 'Gene'].asImmutable()
    private static final List<String> headers6 = ['Analysis', 'Antigen', 'rho-value'].asImmutable()
    private static final List<String> headers7 = ['Accession Number', 'Type', 'Title', 'Description', 'Design',
	                                          'Status', 'Overall Design', 'Start Date', 'Completion Date',
	                                          'Primary Investigator', 'Compounds', 'Diseases',
	                                          'Organisms'].asImmutable()
    private static final List<String> headers8 = ['Accession Number', 'TEA Score', 'Analysis Title',
	                                          'Analysis Description', 'p-Value Cut Off', 'Fold Change Cut Off',
	                                          'QA Criteria', 'Analysis Platform', 'Method', 'Data type',
	                                          'Compounds', 'Diseases', 'Bio Marker', 'Description',
	                                          'Organism', 'ProbeSet', 'Fold Change', 'RValue', 'p-Value',
	                                          'TEA p-Value', 'FDR p-Value', 'Rho-Value', 'Cut Value',
	                                          'Results Value', 'Numeric Value Code', 'Numeric Value'].asImmutable()
    private static final List<String> headers9 = ['Title', 'Trial Number', 'Owner', 'Description',
	                                          'Study Phase', 'Study Type', 'Study Design',
	                                          'Blinding procedure', 'Duration of study (weeks)',
	                                          'Completion date', 'Inclusion Criteria', 'Exclusion Criteria',
	                                          'Dosing Regimen', 'Type of Control', 'Gender restriction mfb',
	                                          'Group assignment', 'Primary endpoints', 'Secondary endpoints',
	                                          'Route of administration', 'Secondary ids', 'Subjects',
	                                          'Max age', 'Min age', 'Number of patients', 'Number of sites',
	                                          'Compounds', 'Diseases', 'Organisms'].asImmutable()
    private static final List<String> headers10 = ['Trial Number', 'TEA Score', 'Analysis Title',
	                                           'Analysis Description', 'p-Value Cut Off', 'Fold Change Cut Off',
	                                           'QA Criteria', 'Analysis Platform', 'Method', 'Data type',
	                                           'Compounds', 'Diseases', 'Bio Marker', 'Description',
	                                           'Organism', 'ProbeSet', 'Fold Change', 'RValue', 'p-Value',
	                                           'TEA p-Value', 'FDR p-Value', 'Rho-Value', 'Cut Value',
	                                           'Results Value', 'Numeric Value Code', 'Numeric Value'].asImmutable()
    private static final List<String> blank1 = ['']
    private static final List<String> blank9 = [''] * 9
    private static final List<String> blank12 = [''] * 12
    private static final List<String> blank14 = [''] * 14

    byte[] renderAnalysisInExcel(BioAssayAnalysis analysis) {
	ExcelSheet sheet
	switch (analysis.analysisMethodCode.toLowerCase()) {
	    case 'correlation': sheet = renderCorrelationAnalysisExcel(analysis); break
	    case 'spearman correlation': sheet = renderSpearmanAnalysisExcel(analysis); break
	    default: sheet = renderComparisonAnalysisExcel(analysis) // todo -- need to handle more methods....
	}

	new ExcelGenerator().generateExcel([sheet])
    }

    ExcelSheet renderComparisonAnalysisExcel(BioAssayAnalysis analysis) {

	List<BioAssayAnalysisData> allprobesameexpr = BioAssayAnalysisData.executeQuery('''
				SELECT distinct g
				FROM BioAssayAnalysisData g
				WHERE g.analysis =:analysis''',
		[analysis: analysis])
	List<String> headers
	String dataType = analysis.assayDataType
        if (isGeneExpression(dataType)) {
	    headers = headers1
        }
        else if (isRBM(dataType)) {
	    headers = headers2
	}
	else {
	    headers = []
        }

	List<Map> values = []
	for (BioAssayAnalysisData data in allprobesameexpr) {
	    List<String> rowGenes = DeMrnaAnnotation.executeQuery('''
					select a.geneSymbol
					from DeMrnaAnnotation as a
					where a.probesetId=?
					  and geneSymbol is not null''',
		[data.probesetId])

	    for (String marker in rowGenes) {
		values << [data.analysis.name, data.probeset, data.foldChangeRatio, data.rawPvalue,
			   data.adjustedPvalue, data.teaNormalizedPValue, marker]
            }
	}

	new ExcelSheet('sheet1', headers, values)
    }

    ExcelSheet renderCorrelationAnalysisExcel(BioAssayAnalysis analysis) {

	List<BioAssayAnalysisData> allprobesameexpr = BioAssayAnalysisData.executeQuery('''
				SELECT distinct g
				FROM BioAssayAnalysisData g
				JOIN g.featureGroup.markers markers
				WHERE markers.bioMarkerType='GENE'
				  AND g.analysis=:analysis''')
	String dataType = analysis.assayDataType
	List<String> headers
        if (isGeneExpression(dataType)) {
	    headers = headers3
        }
        else if (isRBM(dataType)) {
	    headers = headers4
	}
	else {
	    headers = []
        }

	List<Map> values = []
	for (BioAssayAnalysisData data in allprobesameexpr) {
	    List<String> rowGenes = (DeMrnaAnnotation.executeQuery('''
					select a.geneSymbol
					from DeMrnaAnnotation as a
					where a.probesetId=?
					and geneSymbol is not null''',
		[data.probesetId]))

	    for (String marker in rowGenes) {
		values << [data.analysis.name, data.probeset, data.rvalue, marker]
            }
        }

	new ExcelSheet('sheet1', headers, values)
    }

    ExcelSheet renderSpearmanAnalysisExcel(BioAssayAnalysis analysis) {

	List<BioAssayAnalysisData> allprobesameexpr = BioAssayAnalysisData.executeQuery('''
				SELECT distinct g
				FROM BioAssayAnalysisData g
				WHERE g.analysis=:analysis
				order by g.probesetId''',
		[analysis: analysis])
	List<String> headers = []
	String dataType = analysis.assayDataType
	List<List> values = []
        if (isGeneExpression(dataType)) {
	    headers = headers5

            // build excel data
	    for (BioAssayAnalysisData data in allprobesameexpr) {
		List<String> rowGenes = DeMrnaAnnotation.executeQuery('''
						select a.geneSymbol
						from DeMrnaAnnotation as a
						where a.probesetId=?
						and geneSymbol is not null''',
			[data.probesetId])

		for (String marker in rowGenes) {
		    values << [data.analysis.name, data.probeset, data.rhoValue, marker]
                }
            }
        }
        else if (isRBM(dataType)) {
            // don't grab associated markers due to data annotation
	    headers = headers6
	    for (BioAssayAnalysisData data in allprobesameexpr) {
		values << [data.analysis.name, data.probeset, data.rhoValue]
            }
	}

	new ExcelSheet('sheet1', headers, values)
    }

    boolean isGeneExpression(String dataType) {
	'Gene Expression'.equalsIgnoreCase dataType
    }

    boolean isRBM(String dataType) {
	'RBM'.equalsIgnoreCase dataType
    }

    /**
     * Create the Excel objects and pass the resulting byte array back to be fed to the
     * response output stream
     *
     * @param sResult the search result
     * @param expAnalysisMap map with experiment accession as key and analysis as value
     *
     * @return the Excel workbook
     */
    byte[] createExcelEAStudyView(SearchResult sResult, Map<Experiment, List<AnalysisResult>> expAnalysisMap) {

	Map<String, String> experimentOrganisms = [:]
        String orgString = ''
        String organism = ''

	logger.info 'Number of Experiments: {}', sResult.result.expAnalysisResults.size()

	List values2 = []
	expAnalysisMap.each { Experiment k, List<AnalysisResult> v ->
	    values2 << [k.accession] + blank9 + [k.compoundNames] + [k.diseaseNames]
            orgString = ''
	    for (AnalysisResult ar in v) {
		values2 << [] + blank1 + [ar.calcDisplayTEAScore()] + ar.analysis.values + blank14
                // First column is for accession number
		for (AssayAnalysisValue aav in ar.assayAnalysisValueList) {
		    values2 << [] + blank12 + aav.bioMarker.values + aav.analysisData.values
		    organism = aav.bioMarker.organism
		    if (!orgString.contains(aav.bioMarker.organism)) {
			if (orgString) {
                            orgString += ', '
                        }
			orgString += aav.bioMarker.organism
                    }
                }
            }
	    experimentOrganisms[k.accession] = orgString
        }

	List values1 = []
	for (ExperimentAnalysisResult ear in sResult.result.expAnalysisResults) {
	    values1 << ear.experiment.expValues + [experimentOrganisms[ear.experiment.accession]]
        }

	new ExcelGenerator().generateExcel([new ExcelSheet('Experiments', headers7, values1),
		                            new ExcelSheet('Analysis', headers8, values2)])
    }

    /**
     * Create the Excel objects and pass the resulting byte array back to be fed to the
     * response output stream
     *
     * @param sResult the search result
     *
     * @return a byte array containing the Excel workbook
     */
    byte[] createExcelEATEAView(SearchResult sResult) {

	List values1 = []
	List values2 = []

	Set<Long> expIds = []

	TEABaseResult tbr = sResult.result.expAnalysisResults[0]
	logger.info 'Number of Experiments: {}', tbr.expCount

	for (AnalysisResult ar in tbr.analysisResultList) {
	    Experiment experiment = Experiment.get(ar.experimentId)
	    values2 << [experiment.accession] + [ar.calcDisplayTEAScore()] + ar.analysis.values +
		[experiment.compoundNames] + [experiment.diseaseNames] + blank14
	    String orgString = ''
	    for (AssayAnalysisValue aav in ar.assayAnalysisValueList) {
		values2 << [] + [] + blank12 + aav.bioMarker.values + aav.analysisData.values
		if (!orgString.contains(aav.bioMarker.organism)) {
		    if (orgString) {
                        orgString += ', '
                    }
		    orgString += aav.bioMarker.organism
                }
            }

	    if (!expIds.contains(experiment.id)) {
		values1 << experiment.expValues + [orgString]
		expIds << experiment.id
            }
        }
	new ExcelGenerator().generateExcel([new ExcelSheet('Experiments', headers7, values1),
		                            new ExcelSheet('Analysis', headers8, values2)])
    }

    /**
     * Create the Excel objects and pass the resulting byte array back to be fed to the
     * response output stream
     *
     * @param sResult the search result
     *
     * @return a byte array containing the Excel workbook
     */
    byte[] createExcelTrialStudyView(SearchResult sResult, Map<ClinicalTrial, List<AnalysisResult>> trialMap) {

	List values1 = []
	List values2 = []

	Map<String, String> trialOrganisms = [:]
        String organism = ''

	logger.info 'Number of Trials: {}', sResult.result.expAnalysisResults.size()

	trialMap.each { ClinicalTrial k, List<AnalysisResult> v ->
	    values2 << [k.trialNumber] + blank9 + [k.compoundNames] + [k.diseaseNames]
	    String orgString = ''
	    logger.info 'Trial Number: {}', k.trialNumber
	    for (AnalysisResult ar in v) {
		values2 << [] + blank1 + [ar.calcDisplayTEAScore()] + ar.analysis.values + blank14
                // First column is for accession number
		for (AssayAnalysisValue aav in ar.assayAnalysisValueList) {
		    values2 << [] + blank12 + aav.bioMarker.values + aav.analysisData.values
		    organism = aav.bioMarker.organism
		    if (!orgString.contains(aav.bioMarker.organism)) {
			if (orgString) {
                            orgString += ', '
                        }
			orgString += aav.bioMarker.organism
                    }
                }
            }
	    trialOrganisms[k.trialNumber] = orgString
        }

	for (TrialAnalysisResult tar in sResult.result.expAnalysisResults) {
	    values1 << tar.trial.values + [trialOrganisms[tar.trial.trialNumber]]
        }

	new ExcelGenerator().generateExcel([new ExcelSheet('Trials', headers9, values1),
		                            new ExcelSheet('Analysis', headers10, values2)])
    }

    /**
     * Create the Excel objects and pass the resulting byte array back to be fed to the
     * response output stream
     *
     * @return the Excel workbook
     */
    byte[] createExcelTrialTEAView(SearchResult sResult) {

	List values1 = []
	List values2 = []

	Set<Long> trialIds = []
	logger.info 'Number of Trials: {}', sResult.result.expCount

	TEABaseResult tbr = sResult.result.expAnalysisResults[0]

	for (AnalysisResult ar in tbr.analysisResultList) {
	    Experiment experiment = Experiment.get(ar.experimentId)
	    def trialValues = ar.experiment.values
	    logger.info 'Trial Number: {}', trialValues[1]
	    values2 << [trialValues[1]] + [ar.calcDisplayTEAScore()] + ar.analysis.values +
		[experiment.compoundNames] + [experiment.diseaseNames] + blank14
	    String orgString = ''
	    for (AssayAnalysisValue aav in ar.assayAnalysisValueList) {
		values2 << [] + blank12 + aav.bioMarker.values + aav.analysisData.values
		if (!orgString.contains(aav.bioMarker.organism)) {
		    if (orgString) {
                        orgString += ', '
                    }
		    orgString += aav.bioMarker.organism
                }
            }

	    if (!trialIds.contains(experiment.expId)) {
		values1 << trialValues + [orgString]
		trialIds << experiment.expId
            }
        }

	new ExcelGenerator().generateExcel([new ExcelSheet('Trials', headers9, values1),
		                            new ExcelSheet('Analysis', headers8, values2)])
    }
}
