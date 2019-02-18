package org.transmartproject.core.exceptions

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Thrown whenever the user is denied access to some resource.
 */
@CompileStatic
@InheritConstructors
class AccessDeniedException extends RuntimeException { }
