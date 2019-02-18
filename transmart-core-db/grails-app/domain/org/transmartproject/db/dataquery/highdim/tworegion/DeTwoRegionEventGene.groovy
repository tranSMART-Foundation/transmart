package org.transmartproject.db.dataquery.highdim.tworegion

import groovy.transform.EqualsAndHashCode
import org.transmartproject.core.dataquery.highdim.tworegion.EventGene

/**
 * @author j.hudecek
 */
@EqualsAndHashCode()
class DeTwoRegionEventGene implements Serializable, EventGene {

    String effect
    DeTwoRegionEvent event
    String geneId

//    Long getEventId() {
//        return event.id
//    }

    static constraints = {
        effect nullable: true, maxSize: 500
        geneId nullable: true, maxSize: 50
    }

    static mapping = {
        table schema: 'deapp', name: 'de_two_region_event_gene'
        id column: 'two_region_event_gene_id'
        version false

//        effect column: 'effect'
        /* references */
//        event column: 'event_id'
//        geneId column: 'gene_id'
        event fetch: 'join'
    }
}

