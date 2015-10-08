package smartR.plugin

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import heim.RScriptExecutionDefinition
import heim.RServeSession

import java.util.concurrent.TimeUnit

class RServeSessionService {

    def scriptManagerService

    private Cache<String, RServeSession> sessions = CacheBuilder.newBuilder()
            .concurrencyLevel(5)
            //.weakKeys()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build()

    String createNewSession() {
        def session = new RServeSession()
        sessions.put(session.sessionId, session)
        session.sessionId
    }

    def closeSession(String sessionId) {
        RServeSession executor = sessions.getIfPresent(sessionId)
        sessions.invalidate(sessionId)
        executor.closeSession()
    }

    def executeInitScript(String sessionId, String workflow) {
        executeScript(sessionId, workflow, 'init.r')
    }

    def executeRunScript(String sessionId, String workflow) {
        executeScript(sessionId, workflow, 'run.r')
    }

    def executeScript(String sessionId, String workflow, String script) {
        RServeSession executor = sessions.getIfPresent(sessionId)
        //TODO Clarify contract between current class and scriptManagerService
        def definition = new RScriptExecutionDefinition(
                code: scriptManagerService.readScript(workflow, script)
        )
        executor.execute(definition)
    }

    def getScriptExecutionStatus(String sessionId, String scriptExecutionId) {
        sessions.getIfPresent(sessionId)
            .getScriptExecutionStatus(scriptExecutionId)
    }

    def getScriptExecutionResult(String sessionId, String scriptExecutionId) {
        sessions.getIfPresent(sessionId)
            .getScriptExecutionResult(scriptExecutionId)
    }

}
