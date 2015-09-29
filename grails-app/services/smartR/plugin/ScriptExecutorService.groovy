package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection
import groovy.time.*


class ScriptExecutorService {

    def CONNECTION_LIFETIME = 120 * 1000 * 60 // milliseconds
    def MAX_CONNECTIONS = 10

    def rServeConnections = [:]

    def getConnection(parameterMap) {
        def cookieID = parameterMap['cookieID']
        try {
            // make sure that this is a _working_ connection
            rServeConnections[cookieID].connection.assign("test1", "123")
            rServeConnections[cookieID].connection.eval("test2 <- test1")
            // renew lifetime
            rServeConnections[cookieID].expiration = System.currentTimeMillis() + CONNECTION_LIFETIME
            return rServeConnections[cookieID].connection
        } catch (all) { 
            // if we have a connection but it failed the assign test then it must be broken
            if (rServeConnections[cookieID]) {
                closeConnection(cookieID)
            }
        }
        
        try {
            if (rServeConnections.size() == MAX_CONNECTIONS) {
                closeOldesConnection()
                assert rServeConnections.size() < MAX_CONNECTIONS
            }
            def rServeHost = Holders.config.RModules.host
            def rServePort = Holders.config.RModules.port
            rServeConnections[cookieID] = [:]
            rServeConnections[cookieID].connection = new RConnection(rServeHost, rServePort)
            rServeConnections[cookieID].connection.stringEncoding = 'utf8'
            rServeConnections[cookieID].expiration = System.currentTimeMillis() + CONNECTION_LIFETIME
            return rServeConnections[cookieID].connection
        } catch (all) { }

        return null
    }

    def clearSession(connection) {
        connection.eval("rm(list=ls(all=TRUE))")
    }

    def transferData(parameterMap, connection) {
        // This should be the size for a string of 10MB
        def STRING_PART_SIZE = 10 * 1024 * 1024 / 2

        def dataString1 = parameterMap['data_cohort1'].toString()
        def dataString2 = parameterMap['data_cohort2'].toString()

        def dataPackages1 = dataString1.split("(?<=\\G.{${STRING_PART_SIZE}})")
        def dataPackages2 = dataString2.split("(?<=\\G.{${STRING_PART_SIZE}})")

        connection.assign("data_cohort1", '')
        connection.assign("data_cohort2", '')

        dataPackages1.each { chunk ->
            connection.assign("chunk", chunk)
            connection.eval("data_cohort1 <- paste(data_cohort1, chunk, sep='')")
        }

        dataPackages2.each { chunk ->
            connection.assign("chunk", chunk)
            connection.eval("data_cohort2 <- paste(data_cohort2, chunk, sep='')")
        }

        connection.eval("""
            require(jsonlite)
            data.cohort1 <- fromJSON(data_cohort1)
            data.cohort2 <- fromJSON(data_cohort2)
        """)
    }

    def buildSettings(parameterMap, connection) {
        connection.assign("settings", parameterMap['settings'].toString())
        connection.eval("""
            require(jsonlite)
            settings <- fromJSON(settings)
            output <- list()
        """)
    }

    def evaluateScript(parameterMap, connection) {
        def scriptCommand = "source('${parameterMap['scriptDir'] + parameterMap['script']}')".replace("\\", "/")
        def ret = connection.parseAndEval("try(${scriptCommand}, silent=TRUE)")
        return ret
    }

    def computeResults(connection) {
        def results = connection.eval("toString(toJSON(output, digits=5))").asString()
        return results
    }

    def run(parameterMap) {
        // initialize Rserve connection
        def connection = getConnection(parameterMap)
        removeExpiredConnections()
        if (! connection) {
            return [false, 'Rserve refused the connection! Is it running?']
        }

        // if first run start with a clear environment and send all data to our connection
        if (parameterMap['init']) {
            clearSession(connection)
            transferData(parameterMap, connection)
        }
        
        // send settings to our connection
        buildSettings(parameterMap, connection)

        // evaluate analysis script
        def ret = evaluateScript(parameterMap, connection)
        if (ret.inherits("try-error")) {
            return [false, ret.asString()]
        }

        // get the results of our connection
        def results = computeResults(connection)
        return [true, results]
    }

    def closeConnection(id) {
        if (rServeConnections[id]) {
            clearSession(rServeConnections[id].connection)
            rServeConnections[id].connection.close()
            rServeConnections.remove(id)    
        }
    }

    def closeAllConnection() {
        rServeConnections.each { id, connection ->
            closeConnection(id)
        }
        assert rServeConnections.size() == 0
    }

    def closeOldestConnection() {
        def oldestConnectionID
        def oldestExpirationTime = Integer.MAX_VALUE
        rServeConnections.each { id, connection ->
            if (connection.expiration < oldestExpirationTime) {
                oldestExpirationTime = connection.expiration 
                oldestConnectionID = id
            }
        }
        closeConnection(id)
    }

    def removeExpiredConnections() {
        def now = System.currentTimeMillis()
        rServeConnections.each { id, connection ->
            if (connection.expiration < now) {
                closeConnection(id)
            }
        }
    }

    def forceKill(connection) {
        def rServeHost = Holders.config.RModules.host
        def rServePort = Holders.config.RModules.port
        if (parameterMap['DEBUG']) {
            // Rserve has a different behaviour when used with MS Windows. This is for dev. only
            rServePort.toInteger() + rServeConnections.size()
        }
        def killConnection = new RConnection(rServeHost, rServePort)
        def pid = connection.eval("Sys.getpid()").asInteger()
        killConnection.eval("tools::pskill(${pid})")
        killConnection.eval("tools::pskill(${pid}, tools::SIGKILL)")
        killConnection.close()
    }
}
