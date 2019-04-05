import org.transmartproject.db.log.AccessLogService

class SecureController {

    AccessLogService accessLogService

    def index() {
	accessLogService.report 'Access Dataset Explorer', null
	redirect controller: 'datasetExplorer'
    }
}
