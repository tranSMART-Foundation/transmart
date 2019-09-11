import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService

/**
 * Author: Denny Verbeeck (dverbeec@its.jnj.com)
 */
@Slf4j('logger')
class HighDimensionFilterController {

    I2b2HelperService i2b2HelperService
    HighDimensionResourceService highDimensionResourceService

    /**
     * Render the filter dialog.
     * @param gpl_id The GPL ID of the platform for this high dimensional dataset
     * @param filter Should be 'true' for filter dialog (cohort selection), 'false' for selection dialog (when dropping
     *        into summary statistics or grid view)
     * @param concept_key The concept key of the high dimensional concept
     */
    def filterDialog(String gpl_id) {
	String template = 'highDimensionFilterDialog'
	boolean filter = params.boolean('filter', true)
	String concept_key = params.concept_key ?: null

	if (!gpl_id) {
            render template: template, model: [error: 'No GPL ID provided.']
            return
        }

	if (!concept_key) {
            render template: template, model: [error: 'No concept key provided']
            return
        }

	DeGplInfo platform = DeGplInfo.get(gpl_id)
	if (!platform) {
            render template: template, model: [error: 'Unknown GPL ID provided.']
            return
        }

	HighDimensionDataTypeResource resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)

	render template: template, model: [
	    gpl_id               : platform.id,
            marker_type          : platform.markerType,
	    filter_type          : resource.highDimensionFilterType,
            searchable_properties: resource.getSearchableAnnotationProperties().collectEntries {
                [it, searchableAnnotationPropertiesDictionary.get(it, it)]
            },
            filter               : filter,
            projections          : resource.searchableProjections.collectEntries {
                [it, Projection.prettyNames.get(it, it)]
            }]
    }

    /**
     * Get general information on the high dimensional filter.
     * @param filter Should be 'true' for filter dialog (cohort selection), 'false' for selection dialog (when dropping
     *        into summary statistics or grid view)
     * @param concept_key The concept key of the high dimensional concept
     * @return JSON object with following properties: platform, auto_complete_source, filter_type,
     *         filter, concept_key, concept_code
     */
    def filterInfo() {
	String template = 'highDimensionFilterDialog'
	String concept_key = params.concept_key ?: null
	boolean filter = params.boolean('filter', true)

        if (concept_key == null) {
            render template: template, model: [error: 'No concept key provided.']
            return
        }

	String conceptCode = i2b2HelperService.getConceptCodeFromKey(concept_key)

	DeGplInfo platform = DeSubjectSampleMapping.findByConceptCode(conceptCode).platform

	HighDimensionDataTypeResource resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)

	Map result = [platform            : platform,
		      auto_complete_source: '/transmart/highDimensionFilter/searchAutoComplete',
		      filter_type         : resource.highDimensionFilterType,
                      filter              : filter,
                      concept_key         : concept_key,
		      concept_code        : conceptCode]

	if (result.filter_type == "") result['error'] = "Unrecognized marker type " + platform.markerType

	render (result as JSON)
    }

    def searchAutoComplete(String concept_key, String term, String search_property) {
	if (!concept_key || !term || !search_property) {
	    render([] as JSON)
	    return
        }
	HighDimensionDataTypeResource resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)
	String conceptCode = i2b2HelperService.getConceptCodeFromKey(concept_key)
	List<String> symbols = resource.searchAnnotation(conceptCode, term, search_property)
        symbols.collect {[label: it]}

        render symbols as JSON
    }

    /**
     * Map to convert from searchable annotation property names to a format suitable for user display
     */
    private static final Map<String, String> searchableAnnotationPropertiesDictionary =
	[geneSymbol     : 'Gene Symbol',
	 cytoband       : 'Cytoband',
	 name           : 'Region Name',
	 hmdbId         : 'HMDB ID',
	 biochemicalName: 'Biochemical Name',
	 probeId        : 'Probe ID',
	 mirnaId        : 'miRNA ID',
	 transcriptId   : 'transcript ID',
	 uniprotName    : 'Uniprot Name',
	 peptide        : 'Peptide',
	 antigenName    : 'Antigen Name',
	 annotationId   : 'Annotation ID',
	 chromosome     : 'Chromosome',
	 position       : 'Position',
	 rsId           : 'RSID',
	 referenceAllele: 'Reference Allele',
	 detector       : 'miRNA Symbol'].asImmutable()
}
