/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/

package com.recomdata.grails.plugin.gwas

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import com.recomdata.upload.DataUploadResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import org.transmart.biomart.AnalysisMetadata

import javax.sql.DataSource
import java.math.MathContext
import java.math.RoundingMode

@Slf4j('logger')
class DataUploadService {

    static transactional = false

    @Value('${com.recomdata.dataUpload.etl.dir:}')
    private String etlPath

    @Value('${com.recomdata.dataUpload.stageScript:}')
    private String stageScript

    @Autowired private DataSource dataSource

    private DataUploadResult verifyFields(String[] providedFields, uploadType) {

	List<String> missingFields = []
	List<String> requiredFields = RequiredUploadField.findAllByType(uploadType)*.field
	for (String field in requiredFields) {
	    boolean found = false
	    for (providedField in providedFields) {
		if (providedField.trim().toLowerCase() == field.trim().toLowerCase()) {
		    found = true
		    break
		}
		//Special case for p-value - if we have log p-value, count this as present as well
		if (providedField.trim().toLowerCase() == 'log_p_value' && field.trim().toLowerCase() == 'p_value') {
		    found = true
		    break
		}
	    }
	    if (!found) {
		missingFields << field
	    }
	}

	boolean success = !missingFields
	new DataUploadResult(success: success, requiredFields: requiredFields, providedFields: providedFields,
			     missingFields: missingFields, error: 'Required fields were missing from the uploaded file.')
    }

    private BigDecimal log10(BigDecimal b, int dp) {
	final int NUM_OF_DIGITS = dp + 2 // need to add one to get the right number of dp
	//  and then add one again to get the next number
	//  so I can round it correctly.

	MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN)

	//special conditions:
	// log(-x) -> exception
	// log(1) == 0 exactly
	// log of a number lessthan one = -log(1/x)
	if (b.signum() <= 0) {
	    throw new ArithmeticException('log of a negative number! (or zero)')
	}

	if (b.compareTo(BigDecimal.ONE) == 0) {
	    return BigDecimal.ZERO
	}

	if (b.compareTo(BigDecimal.ONE) < 0) {
	    return (log10((BigDecimal.ONE).divide(b,mc),dp)).negate()
	}

	StringBuilder sb = new StringBuilder()
	//number of digits on the left of the decimal point
	int leftDigits = b.precision() - b.scale()

	//so, the first digits of the log10 are:
	sb << (leftDigits - 1) << '.'

	//this is the algorithm outlined in the webpage
	int n = 0
	while(n < NUM_OF_DIGITS) {
	    b = (b.movePointLeft(leftDigits - 1)).pow(10, mc)
	    leftDigits = b.precision() - b.scale()
	    sb << leftDigits - 1
	    n++
	}

	BigDecimal ans = new BigDecimal(sb.toString())

	//Round the number to the correct number of decimal places.
	ans.round new MathContext(ans.precision() - ans.scale() + dp, RoundingMode.HALF_EVEN)
    }

    private Map<String, Integer> getColumnIndex(String uploadType) {
	if (uploadType != 'EQTL' && uploadType != 'Metabolic GWAS') {
	    uploadType = 'GWAS'
	}

	Map<String, Integer> columnIdx = [:]
	new Sql(dataSource).eachRow(getColumnIndexSql, [uploadType]) { row ->
	    columnIdx[row.field_name] = row.field_idx
	}
	columnIdx
    }

    DataUploadResult writeFile(String location, MultipartFile file, AnalysisMetadata upload) {
	CSVReader csvRead = null
	CSVWriter csv = null
	try {
	    csvRead = new CSVReader(new InputStreamReader(file.inputStream), '\t'.charAt(0), CSVWriter.NO_QUOTE_CHARACTER)

	    String[] header = csvRead.readNext()
	    //Verify fields and return immediately if we don't have a required one
	    DataUploadResult result = verifyFields(header, upload.dataType)
	    if (!result.success) {
		return result
	    }
		
	    List<String> headerList = header.toList()
	    Map<String, Integer> columnOrder = getColumnIndex(upload.dataType)

	    //Currently ignoring: HETISQ
	    //  HETPVAL
	    int pValueIndex = -1
	    int logpValueIndex = -1
	    int rsIdIndex = -1
	    //contains index from the file to the index defined in the database.
	    //based on header field build this map, keep the field index and header Index.
	    Map<Integer, Integer> fileColumnIdx = [:]
		
	    for (int i = 0; i < headerList.size(); i++) {
		String column = headerList[i]
		if (column.trim().toLowerCase() == 'p_value') {
		    pValueIndex = i
		}
		else if (column.trim().toLowerCase() == 'log_p_value') {
		    logpValueIndex = i
		}
		else if (column.trim().toLowerCase() == 'rs_id') {
		    rsIdIndex=i
		}
		else {
		    Integer idx = columnOrder.get(headerList[i].toUpperCase())
		    if (idx != null) {
			fileColumnIdx[idx] = i
		    }
		}
	    }
		
	    //If we don't have p-value or log p-value, add this column at the end
	    if (pValueIndex < 0) {
		pValueIndex = headerList.size()
		headerList[headerList.size()] = 'p_value'
	    }
	    else if (logpValueIndex < 0) {
		logpValueIndex = headerList.size()
		headerList[headerList.size()] = 'log_p_value'
	    }
	    logger.info 'RS at {} pvalue {} -logPvalue {}', rsIdIndex, pValueIndex, logpValueIndex
	    fileColumnIdx[0] = rsIdIndex
	    fileColumnIdx[columnOrder.size() + 1] = pValueIndex
	    fileColumnIdx[columnOrder.size() + 2] = logpValueIndex
		
	    // Columns are sorted - now start writing the file
		
	    csv = new CSVWriter(new FileWriter(new File(location)), '\t'.charAt(0), CSVWriter.NO_QUOTE_CHARACTER)
	    //How to specify character in Grails...?!
	    String[] headerRow = new String[fileColumnIdx.size()]
	    for (int rowIdx = 0; rowIdx < fileColumnIdx.size(); rowIdx++) {
		Integer index = fileColumnIdx.get(rowIdx)
		if (index == null) {
		    logger.info 'file column Index is null for {}-{}', rowIdx, fileColumnIdx
		}
		else if (headerList[index] == null) {
		    logger.info  'header list is null for {} - {}', index, headerList
		}
		else {
		    headerRow[rowIdx] = headerList[fileColumnIdx[rowIdx]]
		}
	    }
	    csv.writeNext headerRow

	    //For each line, check the value and p-value - if we have one but not the other, calculate and fill it
	    String[] nextLine
	    boolean pflag = false
	    String linenos=''
	    int lineno=1
	    while ((nextLine = csvRead.readNext()) != null) {
		String[] curRow = new String[fileColumnIdx.size()]
		List<String> columns = nextLine.toList()
		String currentpValue = columns[pValueIndex]
		String currentlogpValue = columns[logpValueIndex]
		lineno++
		int flag=1
		if (!currentpValue && !currentlogpValue) {
		    linenos += columns[rsIdIndex]+','
		    pflag = true
		    flag = 0
		}
		else if (!currentpValue) {
		    columns[pValueIndex] = -Math.pow(10, Double.parseDouble(currentlogpValue)) as String
		}
		else if (!currentlogpValue) {
		    double logp = Math.log10(Double.parseDouble(currentpValue))
		    if (logp == Double.POSITIVE_INFINITY ) {
			logp = -log10(new BigDecimal(currentpValue), 10)
		    }
		    columns[logpValueIndex] = -logp as String
		}

		//This row is now complete - write it!
		if(flag==1){
		    for (int rowIdx = 0; rowIdx < fileColumnIdx.size(); rowIdx++) {
			Integer index = fileColumnIdx.get(rowIdx)
			if (index == null) {
			    logger.info 'file column Index is null for {}-{}', rowIdx, fileColumnIdx
			}
			else {
			    curRow[rowIdx] = columns[index]
			}
		    }
		    csv.writeNext curRow
		}
	    }
	    if(pflag){
		throw new Exception ('No p_value or log_p_value was provided for SNPs '+linenos)
	    }
			
	    return result
	}
	finally {
	    csvRead?.close()
	    csv?.flush()
	    csv?.close()
	}
    }
		
    void runStaging(long etlId) {
	ProcessBuilder pb = new ProcessBuilder(etlPath + stageScript, String.valueOf(etlId))
	pb.directory new File(new File(etlPath).canonicalPath)
	pb.start()
    }

    private static final String getColumnIndexSql = 'SELECT field_name, field_idx FROM biomart.bio_asy_analysis_data_idx WHERE ext_type = ?'
}
