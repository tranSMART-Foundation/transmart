package transmartapp

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.transmart.biomart.Disease
import org.transmart.biomart.Observation

@Slf4j('logger')
class DiseaseController {

    def diseaseService

    /**
     * Find the top 15 diseases with a case-insensitive LIKE
     */
    def extSearch() {
	String value = params.term.toUpperCase()

	List<Object[]> observations = null
        //eQTL requires just disease - GWAS types need diseases and observations
	List<Object[]> diseases = Disease.executeQuery('''
				SELECT meshCode, disease
				FROM Disease d
				WHERE upper(d.disease) LIKE '%' || :term || '%' ''',
						       [term: value], [max: 10])

	if (params.type != 'eqtl') {
	    observations = Observation.executeQuery('''
					SELECT code, name, codeSource
					FROM Observation o
					WHERE upper(o.name) LIKE '%' || :term || '%' ''',
						    [term: value], [max: 10])
	}

	List<Map> itemlist = []
	for (Object[] disease in diseases) {
	    itemlist << [id: disease[0],
			 keyword: disease[1],
			 sourceAndCode: 'MESH:' + disease[0],
			 category: 'DISEASE',
			 display: 'Disease']
	}

	for (Object[] observation in observations) {
	    itemlist << [id: observation[0],
			 keyword: observation[1],
			 sourceAndCode: observation[2] + ':' + observation[0],
			 category: 'OBSERVATION',
			 display: 'Observation']
        }

        render itemlist as JSON
    }

    def getMeshLineage(String code) {
        try {
	    Disease disease = Disease.findByMeshCode(code)
            def hierarchy = diseaseService.getMeshLineage(disease)

            //Return the list of disease names and codes, and a parsed lineage for convenience
	    List<Map> diseases = []
            for (dis in hierarchy) {
                if (dis != null) {
		    diseases << [code: dis.meshCode, name: dis.disease]
                }
            }
	    render([diseases: diseases] as JSON)
        }
	catch (e) {
	    logger.error e.message, e
	    render(status: 500, text: e.message)
        }
    }
}
