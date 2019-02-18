package org.transmartproject.db.dataquery.highdim.tworegion

import groovy.transform.EqualsAndHashCode
import org.transmartproject.core.dataquery.highdim.tworegion.Event

/**
 * @author j.hudecek
 */
@EqualsAndHashCode()
class DeTwoRegionEvent implements Serializable, Event {

    String cgaType
    String soapClass

    static hasMany = [eventGenes: DeTwoRegionEventGene]

    static constraints = {
        cgaType nullable: true, maxSize: 500
        eventGenes nullable: true
        soapClass nullable: true, maxSize: 500
    }

    static mapping = {
        table schema: 'deapp', name: 'de_two_region_event'
        id column: 'two_region_event_id'
        version false

        eventGenes fetch: 'join'

//        cgaType column: 'cga_type'
//        soapClass column: 'soap_class'

    }
}
