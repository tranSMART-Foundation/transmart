package org.transmartproject.core.dataquery.assay

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class TissueType {

    /**
     * The tissue code used for querying purposes. Typically a numerical value.
     */
    String code

    /**
     * A label used for displaying purposes.
     */
    String label

}
