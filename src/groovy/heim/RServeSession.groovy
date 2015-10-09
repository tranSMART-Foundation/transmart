package heim

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import grails.util.Holders
import groovy.transform.ToString
import groovy.util.logging.Log4j
import org.rosuda.REngine.Rserve.RConnection

import java.util.concurrent.*

@Log4j
class RServeSession {

    String sessionId = UUID.randomUUID().toString()

    String rServeHost = Holders.config.RModules.host ?: '127.0.0.1'
    Integer rServePort = Holders.config.RModules.port ?: 6311
    String scriptFolder = Holders.config.SmartR.scriptFolder

    private ThreadPoolExecutor executor
    private RConnection rConnection
    private Cache<String, RServeScriptExecution> scriptsExecutionsCache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            //.weakKeys()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build()

    RServeSession() {
        executor = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>())
    }

    String execute(RScriptExecutionDefinition definition) {
        def thread = new RServeScriptExecution(definition: definition)

        executor.execute(thread)
        scriptsExecutionsCache.put(thread.scriptExecutionId, thread)

        thread.scriptExecutionId
    }

    def getScriptExecutionResult(String scriptExecutionId) {
        scriptsExecutionsCache.getIfPresent(scriptExecutionId).result
    }

    def getScriptExecutionStatus(String scriptExecutionId) {
        scriptsExecutionsCache.getIfPresent(scriptExecutionId).status
    }

    boolean closeSession() {
        executor.shutdown()
        if (rConnection) {
            rConnection.close()
        }
    }

    @Override
    protected void finalize() throws Throwable {
        closeSession()
    }

    synchronized private RConnection getRConnection() {
        if (!rConnection) {
            rConnection = new RConnection(rServeHost, rServePort)
        } else {
            if (!rConnection.isConnected()) {
                closeSession()
                //I'm not sure that we want just reconnect in this case.
                //As previous environment must be lost and further calculation might not make sense.
                throw new RuntimeException('RConnection went down')
            }
        }

        rConnection
    }

    @ToString
    @Log4j
    class RServeScriptExecution implements Runnable {

        String scriptExecutionId = UUID.randomUUID().toString()
        RScriptExecutionDefinition definition

        def status = 'Waiting'
        def result
        def outputFiles

        @Override
        void run() {
            try {
                status = 'Running'
                //TODO We might want to create subdirectory for each script execution
                //TODO Upload data to an environment?
                def connection = getRConnection()
                log.debug("Executing script with id: ${scriptExecutionId}")
                result = connection.eval(definition.code).asNativeJavaObject()
                def outputMngr = new RScriptOutputManager(connection,sessionId,scriptExecutionId)
                outputFiles = outputMngr.getScriptOutput()
                status = 'Done'
            } catch (Exception e) {
                log.error(e)
                status = 'Error'
            }
        }

    }

}
