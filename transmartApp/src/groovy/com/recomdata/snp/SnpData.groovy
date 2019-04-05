package com.recomdata.snp

import com.recomdata.dataexport.util.BiomarkerDataRowProcessor
import com.recomdata.transmart.data.export.SnpDataObject
import groovy.transform.CompileStatic

/**
 * Represents a row of SNP data we are exporting to a file.
 * @author MMcDuffie
 */
@CompileStatic
class SnpData implements BiomarkerDataRowProcessor {

    private static final String lineSeparator = System.getProperty('line.separator')

    //The file format will be PATIENT_NUM,GENE,PROBE_ID,GENOTYPE,COPYNUMBER
    void processDataRow(SnpDataObject row, Writer snpOutputFile) {
	snpOutputFile.write row.patientNum +
	    '\t' + row.geneName +
	    '\t' + row.probeName +
	    '\t' + row.genotype +
	    '\t' + row.copyNumber +
	    //Add the filtering data.
	    '\t' + row.sample +
	    '\t' + row.timepoint +
	    '\t' + row.tissue +
	    '\t' + row.gplId +
	    (row.searchKeywordId != null ? '\t' + row.searchKeywordId : '') +
	    lineSeparator
    }
}
