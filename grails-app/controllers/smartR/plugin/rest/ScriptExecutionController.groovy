package smartR.plugin.rest

import smartR.plugin.RServeSessionService

class ScriptExecutionController {

    RServeSessionService rServeSessionService

    /**
     *
     *{'sessionId': '',
     'workflow': '',}* Init a new script instance and adds it for execution
     */
    def init() {
        def json = request.JSON
        String scriptExecutionId = rServeSessionService.init(json)

        render(contentType: 'text/json') {
            [scriptExecutionId: scriptExecutionId]
        }
    }

}
