/**
 * @author mmcduffie
 */
class GeneExprAnalysisController {

    def findgenerifs(String id) {
	Map openrifs = sessionOpengenerifs()
        if (openrifs == null) {
            openrifs = [:]
	    session.opengenerifs = openrifs
        }

	boolean search = true
	if (openrifs[id] != null) {
	    openrifs.remove id
            search = false
        }
        else {
	    openrifs[id] = 'y'
	    sessionDetails()?.remove id
        }

        if (search) {
            def geneExprAnalysis = GeneExprAnalysis.get(params.id)
            def rifs = GeneRifs.findAllByGeneSymbolLike(geneExprAnalysis.geneSymbol)
	    render template: 'generifs', model: [generifs: rifs]
        }
        else {
	    render template: 'emptyTemplate'
        }
    }

    def detail(String id) {
	Map details = sessionDetails()
        if (details == null) {
            details = [:]
	    session.details = details
        }

	boolean search = true
	if (details[id] != null) {
	    details.remove id
            search = false
        }
        else {
	    details[id] = 'y'
	    sessionOpengenerifs()?.remove id
        }

        if (search) {
            def geneExprAnalysis = GeneExprAnalysis.get(params.id)

	    if (!geneExprAnalysis) {
		flash.message = "GeneExprAnalysis not found with id ${params.id}"
            }
	    render template: '/geneExprAnalysis/detail', model: [geneExprAnalysis: geneExprAnalysis]
        }
        else {
	    render template: 'emptyTemplate'
        }
    }

    def noResult() {
	render view: 'noresult'
    }

    private Map sessionDetails() {
	session.details
    }

    private Map sessionOpengenerifs() {
	session.opengenerifs
    }
}
