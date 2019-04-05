import org.springframework.beans.factory.annotation.Autowired
import org.transmart.biomart.BioMarker
import org.transmart.biomart.Compound
import org.transmart.searchapp.SearchKeyword

/**
 * Displays details data for genes, pathways, and compounds. For each of these there
 * is a corresponsing action and view which displays a window with tabs, e.g. 'gene' action and view. And then
 * if a summary is define a summary action and view, e.g. compoundSumary action and view.
 *
 * @author mmcduffie
 */
class DetailsController {

    @Autowired private DetailsService detailsService

    def gene(String id, String altId) {
	String geneSymbol = ''
	String geneId = ''
	if (!id && altId) {
            // TODO: Add type criteria
	    BioMarker result = BioMarker.findByPrimaryExternalId(altId)
	    if (result) {
		id = result.id
            }
        }

	if (id) {
	    BioMarker marker = BioMarker.get(id)
	    if (marker) {
                geneSymbol = marker.name
                geneId = marker.primaryExternalId
            }
        }

	[id: id, symbol: geneSymbol, geneId: geneId,
	 hydraGeneID: detailsService.getHydraGeneID(id)]
    }

    def pathway(Long id) {
	String pathwaySymbol = ''
	String pathwayType = ''
	SearchKeyword searchKeyword = SearchKeyword.findByBioDataId(Long.valueOf(id))
	if (searchKeyword) {
            pathwaySymbol = searchKeyword.keyword
	    String uniqueId = searchKeyword.uniqueId
            pathwayType = uniqueId.substring(uniqueId.indexOf(':') + 1, uniqueId.lastIndexOf(':'))
        }

	[id: id, symbol: pathwaySymbol, type: pathwayType]
    }

    def pathwaySummary(BioMarker pathway, Long id) {
	List<SearchKeyword> genes
	if (pathway) {
	    genes = SearchKeyword.executeQuery('''
					select k
					from org.transmart.searchapp.SearchKeyword k, org.transmart.biomart.BioDataCorrelation c
					where k.bioDataId=c.associatedBioDataId
					  and c.bioDataId=?''', [id])
	}
	[pathway: pathway, genes: genes]
    }

    def compound(Long id) {
	String compoundSymbol = ''
	SearchKeyword searchKeyword = SearchKeyword.findByBioDataId(id)
	if (searchKeyword) {
            compoundSymbol = searchKeyword.keyword
        }
	[id: id, symbol: compoundSymbol]
    }

    def compoundSummary(Compound compound) {
	[compound: compound]
    }
}
