package org.transmartproject.core.exceptions

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Thrown whenever some query that should return a non-empty set returns an empty set.
 */
@CompileStatic
@InheritConstructors
class EmptySetException extends RuntimeException { }
