/**
 * @author smunikuntla
 */
class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?" {}
	
        "/transmart/dataExport/getJobs"(controller: 'dataExport', action: 'getJobs')

        "/"(controller: 'userLanding')

        "500"(view: '/error')
    }
}
