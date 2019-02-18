package org.transmartproject.core.querytool

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Represents a definition used to query the data marts. Has
 * several items (concept keys); the data it represents is the union of these.
 *
 * Note that this panel definition is significantly more limited than i2b2's.
 * For instance, no constraints can be added and only dimension path keys are
 * supported as item_keys. Items cannot include patient sets,
 * encounter sets or other queries.
 */
@CompileStatic
@Immutable
class Panel {

    /**
     * Whether to invert this panel.
     */
    boolean invert

    /**
     * The items to be OR-ed together.
     */
    List<Item> items
}
