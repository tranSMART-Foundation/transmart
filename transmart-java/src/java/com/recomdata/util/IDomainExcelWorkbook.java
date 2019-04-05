package com.recomdata.util;

/**
 * @author jspencer
 */
public interface IDomainExcelWorkbook {

    /**
     * create an excel workbook for a domain object
     */
    byte[] createWorkbook();
}
