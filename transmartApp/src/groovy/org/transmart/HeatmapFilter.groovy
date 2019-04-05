package org.transmart

import groovy.transform.CompileStatic
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
@CompileStatic
class HeatmapFilter {

    String datatype
    String heatmapfiltertype
    SearchKeyword searchTerm

    void reset() {
        datatype = null
        heatmapfiltertype = null
        searchTerm = null
    }
}
