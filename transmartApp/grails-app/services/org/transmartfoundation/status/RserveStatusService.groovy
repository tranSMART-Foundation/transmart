package org.transmartfoundation.status

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPMismatchException
import org.rosuda.REngine.REngineException
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.springframework.beans.factory.annotation.Value

@CompileStatic
@Slf4j('logger')
class RserveStatusService {

    static transactional = false

    private static final String SIMPLE_EXPRESSION = 'rnorm(10)'
    private static final String REQUIRED_PACKAGES_NAME = 'required.packages'
    private static final String[] REQUIRED_PACKAGES =
	['reshape2', 'ggplot2', 'data.table', 'Cairo',
	 'snowfall', 'gplots', 'Rserve', 'foreach', 'doParallel', 'visreg',
         'pROC', 'jsonlite', 'RUnit',
         'WGCNA', 'impute', 'multtest', 'CGHbase', 'CGHtest','CGHtestpar',
         'edgeR', 'snpStats', 'preprocessCore',
	 'GO.db', 'AnnotationDbi', 'QDNAseq']
    private static final String MISSING_PACKAGES_EXPRESSION =
	'required.packages[!(required.packages %in% installed.packages()[,"Package"])]'

    @Value('${RModules.host:}')
    private String rModulesHost

    @Value('${RModules.port:-1}')
    private int rModulesPort

    RserveStatus getStatus() {

	boolean canConnect = false
	boolean evalSimpleExpression = false
	boolean librariesOk = false

	String errorMessage = ''
        RConnection c
        try {
	    c = new RConnection(rModulesHost, rModulesPort)
	    ResultAndErrorMessage result = connectionExists(c)
	    canConnect = result.result
	    errorMessage = result.errorMessage
            if (canConnect) {
		evalSimpleExpression = willEvaluateSimpleExpression(c).result
		result = hasNecessaryDependencies(c)
		librariesOk = result.result
		errorMessage = result.errorMessage
            }
        }
	catch (e) {
	    errorMessage = 'Probe failed with Exception: ' + e.message
        }
        finally {
	    if (c) {
		closeConnection(c)
	    }
        }
		
	new RserveStatus(
	    url: rModulesHost + ':' + rModulesPort,
	    connected: canConnect,
	    simpleExpressionOK: evalSimpleExpression,
	    librariesOk: librariesOk,
	    lastErrorMessage: errorMessage,
	    lastProbe : new Date())
    }

    private ResultAndErrorMessage connectionExists(RConnection c) {
        if (c == null){
	    return new ResultAndErrorMessage(result: false,
					     errorMessage: 'Connection returned null')
	}

	if (c instanceof RConnection) {
	    new ResultAndErrorMessage(result: true)
        }
	else {
	    new ResultAndErrorMessage(result: false,
				      errorMessage: 'Connection returned unrecognized object')
        }
    }

    private ResultAndErrorMessage willEvaluateSimpleExpression(RConnection c) {

        REXP results = evaluate(c,SIMPLE_EXPRESSION)
        if (results == null) {
	    return new ResultAndErrorMessage(result: false, errorMessage: 'Probe = simple expression; returned null')
        }

	double[] d
        try {
            d = evaluate(c,SIMPLE_EXPRESSION).asDoubles()
        }
        catch (REXPMismatchException e) {
	    return new ResultAndErrorMessage(result: false,
					     errorMessage: 'Probe = simple expression; exception = ' + e.localizedMessage)
        }

        if (d.length == 10) {
	    new ResultAndErrorMessage(result: true)
	}
	else {
	    new ResultAndErrorMessage(result: false,
				      errorMessage: 'Probe = simple expression; wrong returned value.')
        }
    }

    private ResultAndErrorMessage hasNecessaryDependencies(RConnection c) {

	ResultAndErrorMessage result = determineMissingPackages(c)
	List<String> list = (List<String>) result.result
	String errorMessage = result.errorMessage
        if (list == null) {
	    logger.debug 'Return from hasNecessaryDependencies because missing packages array is null'
	    return new ResultAndErrorMessage(result: false, errorMessage: errorMessage)
        }

	boolean ok = !list
        if (!ok) {
	    logger.debug 'list of dependencies is not empty: {}', list
	    logger.debug 'lastErrorMessage from determineMissingPackages: {}', errorMessage
	    errorMessage = 'Packages not found: ' + list
        }

	new ResultAndErrorMessage(result: ok, errorMessage: errorMessage)
    }

    private ResultAndErrorMessage determineMissingPackages(RConnection c) {
        try {
	    c.assign REQUIRED_PACKAGES_NAME, REQUIRED_PACKAGES
        }
        catch (REngineException e) {
	    logger.debug 'Return from determineMissingPackages because assignment failed!'
	    logger.debug '  {}', e.localizedMessage
	    return new ResultAndErrorMessage(
		errorMessage: 'exception in assignment of required packages: ' + e.localizedMessage)
        }

	String[] array = null
        REXP results = evaluate(c,MISSING_PACKAGES_EXPRESSION)
        if (results != null) { // null may be returned when expression returns null - Character(0)
            try {
                array = results.asStrings()
            }
            catch (REXPMismatchException e) {
		logger.debug 'Return from determineMissingPackages because conversion of package array failed!'
		logger.debug '  {}', e.localizedMessage
		return new ResultAndErrorMessage(
		    errorMessage: 'Exception in converting results to an String array: ' + e.localizedMessage)
            }
        }

	new ResultAndErrorMessage(result: array as List<String>)
    }

    private void closeConnection(RConnection c) {
	c?.close()
    }

    private REXP evaluate(RConnection c,String expression) {
	if (c) {
            try {
		c.eval expression
            }
	    catch (RserveException ignored) {}
        }
    }

    @CompileStatic
    private static class ResultAndErrorMessage {
	def result
	String errorMessage = ''
   }
}
