package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection
import grails.converters.JSON


class ScriptExecutorService {

    def interrupted = false
    def rServePid = -1
    def rServeConnection = null

    def run(parameterMap) {
        def c

        if (this.rServeConnection) {
            c = this.rServeConnection
        } else {
            c = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port)
            this.rServeConnection = c
            this.rServePid = c.eval("Sys.getpid()").asInteger()
            c.stringEncoding = 'utf8'
        }

        c.assign("data_cohort1", parameterMap['data_cohort1'])
        c.assign("data_cohort2", parameterMap['data_cohort2'])
        c.assign("settings", parameterMap['settings'])

        c.eval("""
            require(jsonlite)
            data.cohort1 <- fromJSON(data_cohort1)
            data.cohort2 <- fromJSON(data_cohort2)
            settings <- fromJSON(settings)
            output <- list()
        """)

        def scriptCommand = "source('${parameterMap['scriptDir'] + parameterMap['script']}')".replace("\\", "/")
        def r = c.parseAndEval("try(${scriptCommand}, silent=TRUE)")

        if (r.inherits("try-error")) {
            return [false, r.asString()]
        }

        def results = c.eval("toString(toJSON(output))").asString()
        return [true, results]
    }

    def interrupt() {
        this.interrupted = true // check against this in run() or the Rscript itself for natural termination
    }

    def forceKill() {
        def c = new RConnection()
        c.eval("tools::pskill(${this.rServePid})")
        c.eval("tools::pskill(${this.rServePid}, tools::SIGKILL)")
    }
}

