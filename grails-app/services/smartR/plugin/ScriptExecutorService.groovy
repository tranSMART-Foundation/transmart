package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection
import org.apache.log4j.Logger


class ScriptExecutorService {

    def log = Logger

    def rServeConnections = [:]

    def getConnection(parameterMap) {
        def cookieID = parameterMap['cookieID']
        try {
            def connection = this.rServeConnections[cookieID]
            this.rServeConnections[cookieID].assign("test", "123") // make sure that this is a _working_ connection
            return connection
        } catch (all) { 
            // if we actually established a connection but it failed the assign test then it must be broken
            if (this.rServeConnections[cookieID]) {
                closeConnection(cookieID)
            }
        }
        
        try {
            def rServeHost = Holders.config.RModules.host
            def rServePort = Holders.config.RModules.port
            def connection = new RConnection(rServeHost, rServePort)
            this.rServeConnections[cookieID] = connection
            connection.stringEncoding = 'utf8'
            return connection
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
        def results = connection.eval("toString(toJSON(output))").asString()
        return results
    }

    def run(parameterMap) {
        print '==========================='
        print rServeConnections
        print '==========================='
        // initialize Rserve connection
        def connection = getConnection(parameterMap)
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
        clearSession(this.rServeConnections[id])
        this.rServeConnections[id].close()
        this.rServeConnections.remove(id)
    }

    def closeAllConnection() {
        this.rServeConnections.each { id, connection ->
            closeConnection(id)
        }
        assert this.rServeConnections.size() == 0
    }

    def forceKill(connection) {
        def pid = connection.eval("Sys.getpid()").asInteger()
        def killConnection = new RConnection()
        killConnection.eval("tools::pskill(${pid})")
        killConnection.eval("tools::pskill(${pid}, tools::SIGKILL)")
        killConnection.close()
    }
}
