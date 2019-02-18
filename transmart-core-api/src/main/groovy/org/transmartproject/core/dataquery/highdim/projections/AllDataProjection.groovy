package org.transmartproject.core.dataquery.highdim.projections

/**
 * @author jan
 */
interface AllDataProjection extends Projection<Map<String, Object>>, MultiValueProjection {

    /**
     * The names and types of the datatype specific properties that
     * are available on the rows belonging to this datatype.
     */
    Map<String, Class> getRowProperties()
}
