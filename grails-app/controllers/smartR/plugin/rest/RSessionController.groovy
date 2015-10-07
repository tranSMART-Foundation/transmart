package smartR.plugin.rest

import org.springframework.web.context.support.WebApplicationContextUtils
import smartR.plugin.RServeSessionService

/**
 * R session. Rserve binds a session to a connection tightly.
 * That's why you could see that both are used interchangeably here.
 */
class RSessionController {

    //TODO Return correct HTTP statuses.
    //TODO Add url mappings

    def RServeSessionService

    /**
     * Creates a new R session
     */
    def create() {
        def sessionId = RServeSessionService.manager.createNewSession()

        render(contentType: 'text/json') {
            [sessionId: sessionId]
        }
    }

}
