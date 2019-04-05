import com.recomdata.transmart.domain.searchapp.Report
import com.recomdata.transmart.domain.searchapp.Subset
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService

class WorkspaceController {

    @Autowired
    private SecurityService securityService

    def index() {}

    def listWorkspaceItems() {
	// Get all the subsets for this user and any public subsets.
	List<Subset> subsets = Subset.createCriteria().list {
            and {
                or {
		    eq 'creatingUser', securityService.currentUsername()
		    eq 'publicFlag', true
                }
		eq 'deletedFlag', false
            }
        }

	List<Report> reports = Report.findAllByCreatingUserOrPublicFlag(
	    securityService.currentUsername(), 'Y')

	// Pass in the reports, subsets and the username.
	// The username is used to determine whether or not to show the delete link.
	render template: '/workspace/list', model: [
	    reports         : reports,
            subsets         : subsets,
	    currentUser     : securityService.currentUsername(),
	    selectedSubsetId: params.int('selectedSubsetId', -1)]
    }
}
