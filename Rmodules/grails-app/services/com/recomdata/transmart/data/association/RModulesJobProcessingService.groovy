package com.recomdata.transmart.data.association

import groovy.util.logging.Slf4j
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.springframework.beans.factory.annotation.Value

/**
 * Methods for interacting with the R environment.
*/
@Slf4j('logger')
class RModulesJobProcessingService {

    static scope = 'request'
    static transactional = false
	
    @Value('${RModules.host:}')
    private String host

    @Value('${RModules.port:0}')
    private int port

    void runRScript(String workingDirectory, String scriptName, String commandToRun, String rScriptDirectory) {
	RConnection c = new RConnection(host, port)
	c.stringEncoding = 'utf8'

	String workingDirectoryCommand = "setwd('${workingDirectory}')".replace('\\', '\\\\')
	logger.debug 'Attempting following R Command : {}', workingDirectoryCommand
	REXP x = c.eval(workingDirectoryCommand)

	String sourceCommand = "source('${rScriptDirectory}/" + scriptName + "');".replace('\\','\\\\')
	logger.debug 'Attempting following R Command : {}', sourceCommand
	x = c.eval(sourceCommand)

	logger.debug 'Attempting following R Command : {}', commandToRun
	REXP r = c.parseAndEval('try('+commandToRun+',silent=TRUE)')

	if (r.inherits('try-error')) {
	    String rError = r.asString()

	    RserveException newError

	    //If it is a friendly error, use that, otherwise throw the default message.
	    if(rError ==~ /(?ms).*\|\|FRIENDLY\|\|.*/) {
		rError = rError.replaceFirst(/(?ms).*\|\|FRIENDLY\|\|/,'')
		newError = new RserveException(c,rError)
	    }
	    else {
	    logger.error 'RserveException thrown executing job: {}', rError
		newError = new RserveException(c,'There was an error running the R script for your job. Please contact an administrator.')
	    }

	    throw newError
	}
    }
}
