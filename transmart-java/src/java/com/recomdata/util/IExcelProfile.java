package com.recomdata.util;

import java.util.List;

/**
 * Implemented by domain objects that will be exported to Excel
 *
 * @author Florian
 */
public interface IExcelProfile {
    /**
     * The values that will be used to create an excel worksheet for the domain object
     *
     * @return a List (could be nested) of values
     */
    List getValues();
}
