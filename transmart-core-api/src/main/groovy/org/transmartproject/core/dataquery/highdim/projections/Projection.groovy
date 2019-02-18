package org.transmartproject.core.dataquery.highdim.projections

/**
 * A projection determines that which data will show up in the result set's cells.
 *
 * @param < CELL > The type of the cells in the
 * {@link org.transmartproject.core.dataquery.TabularResult}.
 */
interface Projection<CELL> {

    /**
     * The data type default projection for getting real data series from the
     * API. If several real values are available, the most representative or
     * or least processed value should be returned.
     *
     * Data type implementations are strongly encouraged to support this
     * projection, if possible.
     */
    String DEFAULT_REAL_PROJECTION = 'default_real_projection'

    /**
     * Projection for getting the log transformation of original data points.
     *
     */
    String LOG_INTENSITY_PROJECTION = 'log_intensity'

    /**
     * Projection for getting transformed data points representing the standard
     * score of each original data point.
     *
     * It's implementation-defined whether this score is calculated on the
     * filtered data set (with rows or assays excluded) or on the complete
     * data set.
     */
    String ZSCORE_PROJECTION = 'zscore'

    /**
     * This generic projection returns a map with the values of all fields of
     * the datatype it is applied to.
     *
     * The object returned from HighDimensionResource.createProjection is of
     * type AllDataProjection. Its rowProperties property describes the
     * properties available on the rows and the dataProperties likewise
     * for the cell map values.
     */
    String ALL_DATA_PROJECTION = 'all_data'

    String NORMALIZED_READ_COUNT_PROJECTION = 'normalized_readcount'
    String LOG_NORMALIZED_READ_COUNT_PROJECTION = 'log_normalized_readcount'

    // ACGH types
    String CHIP_COPYNUMBER_VALUE = 'chip_copy_number_value'
    String FLAG = 'flag'
    String PROB_AMP = 'probability_of_amplification'
    String PROB_LOSS = 'probability_of_loss'
    String PROB_GAIN = 'probability_of_gain'
    String PROB_NORM = 'probability_of_normal'
    String SEGMENT_COPY_NUMBER_VALUE = 'segment_copy_number_value'

    // VCF Projections
    String VAR_CLASS = 'variant'
    String REF_ALT = 'reference'
    String VAR_TYPE = 'variant_type'

    Map<String, String> prettyNames = [
            (LOG_INTENSITY_PROJECTION): 'Log Intensity',
            (DEFAULT_REAL_PROJECTION) : 'Raw Intensity',
            (ZSCORE_PROJECTION)       : 'Z-Score',
            (ALL_DATA_PROJECTION)     : 'All data',
            (NORMALIZED_READ_COUNT_PROJECTION) : 'Normalized Readcount',
            (LOG_NORMALIZED_READ_COUNT_PROJECTION): 'Log Normalized Readcount',
            (REF_ALT)                 : 'Reference',
            (VAR_CLASS)               : 'Variant',
            (VAR_TYPE)                : 'Variant Type',
            (PROB_LOSS)               : 'Probability of Loss',
            (PROB_AMP)                : 'Probability of Amplification',
            (CHIP_COPYNUMBER_VALUE)   : 'Chip Copy Number Value',
            (FLAG)                    : 'Flag',
            (SEGMENT_COPY_NUMBER_VALUE): 'Segment Copy Number Value',
            (PROB_NORM)               : 'Probability of Normal',
            (PROB_GAIN)               : 'Probablity of Gain'
    ]

    /**
     * Takes an implementation-defined object and returns the final cell value.
     * @param object an implementation-defined object
     * @return the final cell value
     */
    CELL doWithResult(object)
}
