package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection
import groovy.time.*


class ScriptExecutorService {

    def CONNECTION_LIFETIME = 120 * 1000 * 60 // milliseconds
    def MAX_CONNECTIONS = 10
    def CONNECTION_ATTEMPTS = 3

    def rServeConnections = [:]

    def testConnection(connection) {
        try {
            connection.assign("test1", "123")
            connection.eval("test2 <- test1")
            return true
        } catch (all) {
            return false
        }
    }

    def openConnection(cookieID) {
        def rServeHost = Holders.config.RModules.host
        def rServePort = Holders.config.RModules.port
        try {
            rServeConnections[cookieID] = [:]
            rServeConnections[cookieID].connection = new RConnection(rServeHost, rServePort)
            rServeConnections[cookieID].connection.stringEncoding = 'utf8'
            rServeConnections[cookieID].expiration = System.currentTimeMillis() + CONNECTION_LIFETIME
        } catch (all) {
            return false
        }
        return true
    }

    def getConnection(parameterMap) {
        def cookieID = parameterMap['cookieID']

        if (rServeConnections[cookieID]) {
            if (testConnection(rServeConnections[cookieID].connection)) {
                // renew lifetime
                rServeConnections[cookieID].expiration = System.currentTimeMillis() + CONNECTION_LIFETIME
                return rServeConnections[cookieID].connection
            }
            // if we have a connection but it failed the assign test then it must be broken
            closeConnection(cookieID)
        }

        def valid = false
        // attempt to establish a working connection
        for (def i = 0; i < CONNECTION_ATTEMPTS; i++) {
            openConnection(cookieID)
            if (testConnection(rServeConnections[cookieID].connection)) {
                valid = true
                break
            }
            closeConnection(cookieID)
            sleep(5000)
        }

        while (rServeConnections.size() > MAX_CONNECTIONS) {
            closeOldesConnection()
        }
        assert rServeConnections.size() <= MAX_CONNECTIONS

        if (valid) {
            return rServeConnections[cookieID].connection
        }
        return null
    }

    def clearSession(connection) {
        connection.voidEval("rm(list=ls(all=TRUE))")
    }

    def transferData(parameterMap, connection) {
        def GROOVY_CHUNK_SIZE = 10 * 1024 * 1024 / 2 // should be the size for a string of 10MB
        def dataString1 = parameterMap['data_cohort1'].toString()
        def dataString2 = parameterMap['data_cohort2'].toString()

        def dataPackages1 = dataString1.split("(?<=\\G.{${GROOVY_CHUNK_SIZE}})")
        def dataPackages2 = dataString2.split("(?<=\\G.{${GROOVY_CHUNK_SIZE}})")

        connection.assign("data_cohort1", '')
        connection.assign("data_cohort2", '')

        dataPackages1.each { chunk ->
            connection.assign("chunk", chunk)
            connection.voidEval("data_cohort1 <- paste(data_cohort1, chunk, sep='')")
        }

        dataPackages2.each { chunk ->
            connection.assign("chunk", chunk)
            connection.voidEval("data_cohort2 <- paste(data_cohort2, chunk, sep='')")
        }

        connection.voidEval("""
            require(jsonlite)
            data.cohort1 <- fromJSON(data_cohort1)
            data.cohort2 <- fromJSON(data_cohort2)
        """)
    }

    def buildSettings(parameterMap, connection) {
        connection.assign("settings", parameterMap['settings'].toString())
        connection.voidEval("""
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
        def R_CHUNK_SIZE = 10 * 1024 * 1024 // should be the size for a string of about 10MB
        connection.voidEval("""
            json <- toString(toJSON(output, digits=5))
            start <- 1
            stop <- ${R_CHUNK_SIZE}
        """)
        def json = ''
        def chunk = ''
        while(chunk = connection.eval("""
            chunk <- substr(json, start, stop)
            start <- stop + 1
            stop <- stop + ${R_CHUNK_SIZE}
            chunk
        """).asString()) {
            json += chunk
        }
        return json
    }

    def run(parameterMap) {
        // initialize Rserve connection
        def connection = getConnection(parameterMap)
        removeExpiredConnections()
        if (! connection) {
            return [false, 'Rserve refused the connection! Is it running?']
        }

        // this is a critical package as it is necessary to pipe errors back to the client
        def ret = connection.parseAndEval("""
            try(
                if (! suppressMessages(require(jsonlite))) {
                    stop('R Package jsonlite is missing. Please go to https://github.com/sherzinger/SmartR and read the installation instructions.')
                }
            , silent=TRUE)
        """)
        if (ret.inherits("try-error")) {
            return [false, ret.asString()]
        }

        // if first run start with a clear environment and send all data to our connection
        if (parameterMap['init']) {
            clearSession(connection)
            transferData(parameterMap, connection)
        }

        // send settings to our connection
        buildSettings(parameterMap, connection)

        // evaluate analysis script
        ret = evaluateScript(parameterMap, connection)
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
        killConnection.voidEval("tools::pskill(${pid})")
        killConnection.voidEval("tools::pskill(${pid}, tools::SIGKILL)")
        killConnection.close()
    }
}
