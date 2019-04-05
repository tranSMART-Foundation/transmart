import grails.converters.JSON
import i2b2.Comparison
import org.springframework.beans.factory.annotation.Autowired

class ComparisonController {

    @Autowired private I2b2HelperService i2b2HelperService

    def index() {}

    def getQueryDefinition(String qid) {
	render i2b2HelperService.getQueryDefinitionXMLFromQID(qid)
    }

    def save() {
	Comparison s = new Comparison(
	    queryResultId1: params.int('result_instance_id1', -1),
	    queryResultId2: params.int('result_instance_id2', -1))
        boolean success = s.save()

	String link = '<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="mailto:?subject=Link to Saved comparison ID=' + s.id +
	    '&body=The following is a link to the saved comparison in tranSMART.  ' +
	    'Please, note that you need to be logged into tranSMART prior to using this link.%0A%0A' +
	    createLink(controller: 'datasetExplorer', id: s.id, absolute: true) +
	    '" target="_blank" class="tiny" style="text-decoration:underline;color:blue;font-size:11px;">Email this comparison</a><br /><br />'
	render([success: success, id: s.id, link: link] as JSON)
    }
}
