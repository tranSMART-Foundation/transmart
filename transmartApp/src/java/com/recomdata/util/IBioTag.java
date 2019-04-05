package com.recomdata.util;

/**
 * Implemented by domain objects that will be exported to Excel.
 *
 * @author mmcduffie
 */
public interface IBioTag {
    /**
     * Retrieves the object UID value
     *
     * @return a String of values
     */
    String getBioUID();
}
