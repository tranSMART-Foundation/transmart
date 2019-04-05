package org.transmartproject.export

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
@InheritConstructors
class NoSuchExporterException extends RuntimeException {}
