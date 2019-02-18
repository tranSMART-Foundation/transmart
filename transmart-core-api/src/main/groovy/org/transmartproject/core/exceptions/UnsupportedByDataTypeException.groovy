package org.transmartproject.core.exceptions

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Thrown whenever some requested feature is unsupported by the data type being used.
 */
@CompileStatic
@InheritConstructors
class UnsupportedByDataTypeException extends RuntimeException { }
