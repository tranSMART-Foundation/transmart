package smartR.plugin

import heim.RServeSessionExecutor
import heim.RServeSessionsManager
import heim.RServeThread

class RServeSessionService {

    def scriptManagerService
    def manager = new RServeSessionsManager()

    def init(json) {
        _executeRScript(json,'init.r')
    }

    def run(json){
        _executeRScript(json,'run.r')
    }


    def _executeRScript(json,script){
        RServeSessionExecutor executor = manager[json.sessionId]
        def initThread = new RServeThread(json.workflow, script ,scriptManagerService)
        executor.execute(initThread)
        initThread.uuid
    }
}
