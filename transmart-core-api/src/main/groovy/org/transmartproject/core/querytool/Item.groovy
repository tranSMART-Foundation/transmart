package org.transmartproject.core.querytool

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class Item {

    /**
     * A concept key mapping to a an ontology term.
     */
    String conceptKey

    /**
     * The constraint, or null.
     */
    ConstraintByValue constraint

    /**
     * The highdimension value constraint, or null.
     */
    ConstraintByOmicsValue constraintByOmicsValue
}
