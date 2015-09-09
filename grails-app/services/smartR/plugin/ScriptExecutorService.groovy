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
        }
        c.stringEncoding = 'utf8'
        c.assign("scriptPath", parameterMap['scriptDir'] + parameterMap['script'])
        c.assign("lowDimPath_cohort1", parameterMap['lowDimFile_cohort1'])
        c.assign("lowDimPath_cohort2", parameterMap['lowDimFile_cohort2'])
        c.assign("highDimPath_cohort1", parameterMap['highDimFile_cohort1'])
        c.assign("highDimPath_cohort2", parameterMap['highDimFile_cohort2'])
        c.assign("settings", parameterMap['settings'])

        def scriptCommand = "source('${parameterMap['scriptDir']}Wrapper.R')".replace("\\", "/")
        def r = c.parseAndEval("try(${scriptCommand}, silent=TRUE)");

        if (r.inherits("try-error")) {
            return [false, r.asString()]
        }

        return [true, JSON.parse(r.asList()[0].asString())]
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

