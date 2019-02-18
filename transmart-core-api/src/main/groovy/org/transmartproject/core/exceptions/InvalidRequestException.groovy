package org.transmartproject.core.exceptions

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Designates the submission of invalid data to a resource method.
 */
@CompileStatic
@InheritConstructors
class InvalidRequestException extends RuntimeException { }
