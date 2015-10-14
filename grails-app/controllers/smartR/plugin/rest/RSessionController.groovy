package smartR.plugin.rest

class RSessionController {

    //TODO Return correct HTTP statuses.
    //TODO Add url mappings

    def RServeSessionService

    /**
     * Creates a new R session
     */
    def create() {
        def sessionId = RServeSessionService.createNewSession()

        render(contentType: 'text/json') {
            [sessionId: sessionId]
        }
    }

    /**
     * Deletes an R session
     */
    def delete() {
        def json = request.JSON
        RServeSessionService.closeSession(json.sessionId)

        render(contentType: 'text/json') {
            [status: 'OK']
        }
    }

}
