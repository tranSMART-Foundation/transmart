package smartR.plugin

import heim.RServeSessionExecutor
import heim.RServeSessionsManager
import heim.RServeThread

class RServeSessionService {

    def manager = new RServeSessionsManager()

    def init(json) {
        RServeSessionExecutor executor = manager[json.sessionId]
        def initThread = new RServeThread(json.workflow, 'init.r')
        executor.execute(initThread)
        initThread.uuid
    }
}
