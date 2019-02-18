package org.transmartproject.db.dataquery.highdim.tworegion

import groovy.transform.EqualsAndHashCode
import org.transmartproject.core.dataquery.highdim.tworegion.JunctionEvent

/**
 * @author j.hudecek
 */
@EqualsAndHashCode()
class DeTwoRegionJunctionEvent implements Serializable, JunctionEvent {

    Double baseFreq
    DeTwoRegionEvent event
    DeTwoRegionJunction junction
    Integer pairsCounter
    Integer pairsEnd
    Integer pairsJunction
    Integer pairsSpan
    Integer readsJunction
    Integer readsSpan

    static constraints = {
        baseFreq nullable: true
        pairsCounter nullable: true
        pairsEnd nullable: true
        pairsJunction nullable: true
        pairsSpan nullable: true
        readsJunction nullable: true
        readsSpan nullable: true
    }

    static mapping = {
        table 'deapp.de_two_region_junction_event'
        id column: 'two_region_junction_event_id'
        version false

        baseFreq column: 'base_freq'
        event fetch: 'join'
        junction fetch: 'join'
        pairsCounter column: 'reads_counter'
        pairsEnd column: 'pairs_end'
        pairsJunction column: 'pairs_junction'
        pairsSpan column: 'pairs_span'
        readsJunction column: 'reads_junction'
        readsSpan column: 'reads_span'

        /* references */
//        event column: 'event_id'
    }
}

