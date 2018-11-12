/**
 *
 */
package com.recomdata.transmart.validate

import groovy.util.logging.Slf4j

/**
 * @author SMunikuntla
 *
 */
@Slf4j('logger')
abstract class RequestValidator {

    /**
     * Helper method to return null from Javascript calls
     *
     * @param inputArg - the input arguments
     * @return null or the input argument if it is not null (or empty or undefined)
     */
    public static String nullCheck(inputArg) {
        logger.debug('Input argument to nullCheck: ' + inputArg)
        if (inputArg == 'undefined' || inputArg == 'null' || inputArg == '') {
            logger.debug('Returning null in nullCheck')
            return null
        }
        return inputArg
    }

}
