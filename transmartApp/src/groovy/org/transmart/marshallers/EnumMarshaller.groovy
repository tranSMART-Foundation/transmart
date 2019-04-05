package org.transmart.marshallers

import groovy.transform.CompileStatic

@CompileStatic
class EnumMarshaller {
    static targetType = Enum

    String convert(Enum enumeration) {
        enumeration.name()
    }
}
