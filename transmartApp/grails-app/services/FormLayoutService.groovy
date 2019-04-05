import groovy.transform.CompileStatic

@CompileStatic
class FormLayoutService {

    static transactional = false

    List<FormLayout> getLayout(String key) {
	FormLayout.createCriteria().list() {
            eq('key', key)
            order('sequence', 'asc')
	} as List
    }

    List<FormLayout> getProgramLayout() {
	[new FormLayout(
            dataType: 'string',
            displayName: 'Program title',
	    column: 'Oncology_pan-PI3K inhibition'),
         new FormLayout(
                dataType: 'string',
                displayName: 'Program description',
		column: 'pan-PI3K inhibition is a strategy to target all four of the class one PI3K isoforms (alpha, beta, gamma, delta) since the activity of any class 1A PI3K isoform appears to sustain cell proliferation and survival.'),
         new FormLayout(
                dataType: 'string',
                displayName: 'Program target',
		column: 'pan-PI3K inhibition (PIK3CA, PIK3CB, PIK3CD, PIK3CG)'),
         new FormLayout(
                dataType: 'string',
                displayName: 'Therapeutic domain',
		column: 'Oncology')]
    }
}
