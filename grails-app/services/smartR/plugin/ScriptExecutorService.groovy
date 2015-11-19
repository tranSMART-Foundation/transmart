package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection
import groovy.time.*


class ScriptExecutorService {

    def SESSION_LIFETIME = 120 * 1000 * 60 // milliseconds
    def CONNECT_ATTEMPTS = 3
    def MAX_SESSIONS = 10
    def GROOVY_CHUNK_SIZE = 10 * 1024 * 1024 / 2 // should be the size for a string of 10MB
    def R_CHUNK_SIZE = 10 * 1024 * 1024 // should be the size for a string of about 10MB

    def sessions = [:]

    def testConnection(connection) {
        try {
            connection.assign("test1", "123")
            connection.voidEval("test2 <- test1")
            return connection.eval("test2").asString() == "123"
        } catch (all) {
            return false
        }
    }

    def createSession(id) {
        sessions[id] = [:]
        sessions[id].expiration = System.currentTimeMillis() + SESSION_LIFETIME
        sessions[id].success = false
        sessions[id].results = ''

        try {
            sessions[id].connection = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port)
            sessions[id].connection.stringEncoding = 'utf8'
        } catch (all) {
            return false
        }

        return true
    }

    def getConnection(parameterMap) {
        def id = parameterMap['cookieID']

        if (sessions[id]) {
            if (testConnection(sessions[id].connection)) {
                // renew lifetime
                sessions[id].expiration = System.currentTimeMillis() + SESSION_LIFETIME
                return sessions[id].connection
            }
            // if we have a session but it failed the test then it must be broken
            closeSession(id)
        }

        removeExpiredSessions()
        // the -1 is because we are going to create a new session after this
        while (sessions.size() > MAX_SESSIONS - 1) {
            closeOldestSession()
        }

        // attempt to establish a working connection
        for (def i = 0; i < CONNECT_ATTEMPTS; i++) {
            if (createSession(id) && testConnection(sessions[id].connection)) {
                assert sessions.size() <= MAX_SESSIONS
                return sessions[id].connection
            }
            sleep(5000)
        }

        return null
    }

    def transferData(parameterMap, connection) {
        def dataString1 = parameterMap['data_cohort1'].toString()
        def dataString2 = parameterMap['data_cohort2'].toString()

        def dataPackages1 = dataString1.split("(?<=\\G.{${GROOVY_CHUNK_SIZE}})")
        def dataPackages2 = dataString2.split("(?<=\\G.{${GROOVY_CHUNK_SIZE}})")

        connection.eval("data_cohort1 <- ''")
        connection.eval("data_cohort2 <- ''")

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
        def id = parameterMap['cookieID']
        def connection = getConnection(parameterMap)
        if (! connection) {
            sessions[id].success = false
            sessions[id].results = 'Rserve refused the connection! Is it running?'
            print sessions
            return
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
            sessions[id].success = false
            sessions[id].results = ret.asString()
            return
        }

        // if first run start with a clear environment and send all data to our connection
        if (parameterMap['init']) {
            clearSession(id)
            transferData(parameterMap, connection)
        }

        // send settings to our connection
        buildSettings(parameterMap, connection)

        // evaluate analysis script
        ret = evaluateScript(parameterMap, connection)
        if (ret.inherits("try-error")) {
            sessions[id].success = false
            sessions[id].results = ret.asString()
            return
        }

        // get the results of our connection
        sessions[id].success = true
        sessions[id].results = computeResults(connection)
    }

    def getResults(id) {
        if (sessions[id] && sessions[id].results) {
            return [sessions[id].success, sessions[id].results]
        }
        return [false, 'RUNNING']   
    }

    def clearSession(id) {
        if (sessions[id] && testConnection(sessions[id].connection)) {
            sessions[id].connection.voidEval("rm(list=ls(all=TRUE))")
            sessions[id].success = false
            sessions[id].results = ""
        }
    }

    def closeSession(id) {
        if (sessions[id] && testConnection(sessions[id].connection)) {
            clearSession(id)
            sessions[id].connection.close()
        }
        sessions.remove(id)
    }

    def closeOldestSession() {
        def oldestSessionID
        def oldestExpirationTime = Integer.MAX_VALUE
        sessions.each { id, session ->
            if (session.expiration < oldestExpirationTime) {
                oldestExpirationTime = session.expiration
                oldestSessionID = id
            }
        }
        closeSession(id)
    }

    def removeExpiredSessions() {
        def now = System.currentTimeMillis()
        sessions.each { id, session ->
            if (session.expiration < now) {
                closeSession(id)
            }
        }
    }

    // FIXME: UNTESTED
    def forceKill(connection) {
        def rServeHost = Holders.config.RModules.host
        def rServePort = Holders.config.RModules.port
        def killConnection = new RConnection(rServeHost, rServePort)
        def pid = connection.eval("Sys.getpid()").asInteger()
        killConnection.voidEval("tools::pskill(${pid})")
        killConnection.voidEval("tools::pskill(${pid}, tools::SIGKILL)")
        killConnection.close()
    }
}
