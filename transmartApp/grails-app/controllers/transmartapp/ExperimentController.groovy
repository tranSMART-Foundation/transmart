package transmartapp

import com.recomdata.grails.plugin.gwas.GwasWebService
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.transmart.biomart.Experiment
import org.transmart.searchapp.SecureObject

@Slf4j('logger')
class ExperimentController {

    GwasWebService gwasWebService

    /**
     * Find the top 20 experiments with a case-insensitive LIKE
     */
    def extSearch(String term, String studyType) {
	String value = term.toUpperCase()
	studyType = studyType?.toUpperCase()

	List<String[]> rows = Experiment.executeQuery('''
				SELECT accession, title
				FROM Experiment e
				WHERE upper(e.title) LIKE '%' || :term || '%'
				  AND upper(e.type) = :studyType''',
						      [term: value, studyType: studyType], [max: 20])

	String category
	String categoryDisplay
	if (studyType == 'I2B2') {
            category = 'i2b2'
            categoryDisplay = 'i2b2'
        }
	else {
	    category = 'STUDY'
	    categoryDisplay = 'Study'
	}

	List<Map> itemlist = []
	for (String[] row in rows) {
	    itemlist << [id: row[0], keyword: row[1], category: category, display: categoryDisplay]
        }

	render(itemlist as JSON)
    }

    /**
     * This will display a list of the available studies in the system to the user. The user will only be able to select one item from the dropdown.
     */
    def browseExperimentsSingleSelect(String type) {

	List<Experiment> experiments
	if (type) {
	    experiments = getSortedList(Experiment.findAllByType(type))
        }
        else {
	    experiments = getSortedList(Experiment.list())
	}

	Map<String, Long> secObjs = getExperimentSecureStudyList()

	experiments = experiments.findAll { Experiment it ->
	    !secObjs.containsKey(it.accession) || !gwasWebService.getGWASAccess(it.accession) == 'Locked'
        }

	render template: 'browseSingle', model: [experiments: experiments]
    }

    /**
     * Get list of secured (i.e. private) experiment.
     */
    private Map<String, Long> getExperimentSecureStudyList() {

	Map<String, Long> map = [:]
        //return access levels for the children of this path that have them
	List<Object[]> rows = SecureObject.executeQuery('''
				SELECT so.bioDataUniqueId, so.bioDataId
				FROM SecureObject so
				WHERE so.dataType='Experiment' ''')
	for (Object[] row in rows) {
	    String token = row[0]
	    Long dataid = row[1]
	    map[token.replaceFirst('EXP:', '')] = dataid
        }
	map
    }
	
    /**
     * Render a UI where the user can pick an experiment from a list of all the experiments in the system.
     * Selection of multiple studies is allowed.
     */
    def browseExperimentsMultiSelect(String type) {

	List<Experiment> experiments
	if (type) {
	    experiments = getSortedList(Experiment.findAllByType(type))
        }
        else {
	    experiments = getSortedList(Experiment.list())
        }

	render template: 'browseMulti', model: [experiments: experiments]
    }

    private List<Experiment> getSortedList(List<Experiment> experiments) {
	experiments.sort { Experiment a, Experiment b ->
	    a.title.trim().compareToIgnoreCase b.title.trim()
	}
    }
}
