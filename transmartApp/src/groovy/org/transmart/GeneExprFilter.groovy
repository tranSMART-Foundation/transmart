package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class GeneExprFilter {
    String cellline
    String disease

    boolean hasCellline() {
	cellline
    }

    boolean hasDisease() {
	disease
    }
}
