package smartR.plugin.rest

import smartR.plugin.RServeSessionService

class ScriptExecutionController {

    def RServeSessionService

    /**
     *
     *{'sessionId': '',
     'workflow': '',}* Init a new script instance and adds it for execution
     */
    def init() {
        def json = request.JSON
        String scriptExecutionId = RServeSessionService.init(json)

        render(contentType: 'text/json') {
            [scriptExecutionId: scriptExecutionId]
        }
    }

}
