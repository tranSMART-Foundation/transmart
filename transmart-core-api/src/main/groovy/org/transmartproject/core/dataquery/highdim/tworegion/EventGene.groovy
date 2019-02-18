package org.transmartproject.core.dataquery.highdim.tworegion

/**
 * @author j.hudecek
 */
interface EventGene {
    /**
     * HUGO gene identifier
     */
    String getGeneId()

    /**
     * effect of the event on the gene: FUSION, CONTAINED, DISRUPTED, ...  (from CGA)
     */
    String getEffect()
}
