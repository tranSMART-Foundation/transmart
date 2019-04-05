package org.transmartproject.export

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Enables exporting high dimensional data 
 */
interface HighDimExporter {
    /**
     * Determines whether a datatype is supported
     * @param dataType Name of the datatype
     * @return true if the datatype is supported by this exporter
     */
    boolean isDataTypeSupported(String dataType)

    /**
     * The projection name to be used for retrieving data from the database.
     * @return Projection name
     */
    String getProjection()

    /**
     * @return A short string describing the format that is produced by this exporter
     */
    String getFormat()

    /**
     * @return a longer human readable description for this exporter
     */
    String getDescription()

    /**
     * Exports the data in the TabularResult to the outputStream given
     * @param data Data to be exported
     * @param projection Projection that was used to retrieve the data
     * @param outputStream Stream to write the data to
     */
    void export(TabularResult data, Projection projection,
	        Closure<OutputStream> newOutputStream)

    /**
     * Exports the data in the TabularResult to the outputStream given, 
     * although the export can be cancelled. Cancelling can be caused
     * by the user or by some other process.
     * @param data Data to be exported
     * @param projection Projection that was used to retrieve the data
     * @param outputStream Stream to write the data to
     * @param isCancelled Closure that returns true iff the export is cancelled
     */
    void export(TabularResult data, Projection projection,
	        Closure<OutputStream> newOutputStream, Closure<Boolean> isCancelled)

}
