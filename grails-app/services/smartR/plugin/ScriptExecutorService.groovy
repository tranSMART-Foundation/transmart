package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection


class ScriptExecutorService {

    def interrupted = false
    def rServePid = -1
    def rServeConnection = null

    def initConnection() {
        try {
            // make sure that a _working_ connection is established
            this.rServeConnection.assign('test', 123)
            return true
        } catch (all) { }
        
        try {
            this.rServeConnection = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port)
            this.rServePid = this.rServeConnection.eval("Sys.getpid()").asInteger()
            this.rServeConnection.stringEncoding = 'utf8'
            return true
        } catch (all) { }

        return false
    }

    def clearSession() {
        this.rServeConnection.eval("rm(list=ls(all=TRUE))")
    }

    def transferData(parameterMap) {
        // This should be the size for a string of 10MB
        def STRING_PART_SIZE = 10 * 1024 * 1024 / 2

        def dataString1 = parameterMap['data_cohort1'].toString()
        def dataString2 = parameterMap['data_cohort2'].toString()

        def dataPackages1 = dataString1.split("(?<=\\G.{${STRING_PART_SIZE}})")
        def dataPackages2 = dataString2.split("(?<=\\G.{${STRING_PART_SIZE}})")

        this.rServeConnection.assign("data_cohort1", '')
        this.rServeConnection.assign("data_cohort2", '')

        dataPackages1.each { chunk ->
            this.rServeConnection.assign("chunk", chunk)
            this.rServeConnection.eval("data_cohort1 <- paste(data_cohort1, chunk, sep='')")
        }

        dataPackages2.each { chunk ->
            this.rServeConnection.assign("chunk", chunk)
            this.rServeConnection.eval("data_cohort2 <- paste(data_cohort2, chunk, sep='')")
        }

        this.rServeConnection.eval("""
            require(jsonlite)
            data.cohort1 <- fromJSON(data_cohort1)
            data.cohort2 <- fromJSON(data_cohort2)
        """)
    }

    def buildSettings(parameterMap) {
        this.rServeConnection.assign("settings", parameterMap['settings'])
        this.rServeConnection.eval("""
            require(jsonlite)
            settings <- fromJSON(settings)
            output <- list()
        """)
    }

    def evaluateScript(parameterMap) {
        def scriptCommand = "source('${parameterMap['scriptDir'] + parameterMap['script']}')".replace("\\", "/")
        def ret = this.rServeConnection.parseAndEval("try(${scriptCommand}, silent=TRUE)")
        return ret
    }

    def computeResults() {
        def results = this.rServeConnection.eval("toString(toJSON(output))").asString()
        return results
    }

    def run(parameterMap) {
        // initialize Rserve connection
        if (! initConnection()) {
            print '================'
            return [false, 'Rserve refused the connection! Is it running?']
        }

        // start with a clear environment and send all data to R
        if (parameterMap['init']) {
            clearSession()
            transferData(parameterMap)
        }
        
        // update settings
        buildSettings(parameterMap)

        // evaluate analysis script
        def ret = evaluateScript(parameterMap)
        if (ret.inherits("try-error")) {
            return [false, ret.asString()]
        }

        // get the results of the previous evaluation
        def results = computeResults()
        return [true, results]
    }

    def interrupt() {
        this.interrupted = true // check against this in run() or the Rscript itself for natural termination
    }

    def forceKill() {
        def killConnection = new RConnection()
        killConnection.eval("tools::pskill(${this.rServePid})")
        killConnection.eval("tools::pskill(${this.rServePid}, tools::SIGKILL)")
    }
}
