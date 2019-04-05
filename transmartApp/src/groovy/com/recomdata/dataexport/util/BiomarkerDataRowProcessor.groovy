package com.recomdata.dataexport.util

import com.recomdata.transmart.data.export.SnpDataObject

/**
 * Provides a method to write a data object to a file.
 * @author MMcDuffie
 */
interface BiomarkerDataRowProcessor {
    void processDataRow(SnpDataObject row, Writer resultingObject)
}
