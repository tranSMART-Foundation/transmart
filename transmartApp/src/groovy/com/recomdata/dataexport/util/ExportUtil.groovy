package com.recomdata.dataexport.util

import groovy.transform.CompileStatic
import org.apache.commons.lang.StringUtils

@CompileStatic
class ExportUtil {

    static String getShortConceptPath(String conceptPath, List<String> removalArr) {
	String[] split = StringUtils.split(conceptPath, "\\")
	List<String> values = []
        //Remove upto Study-name and any string values specified in the removalArr
	if (split.length > 2) {
	    split.eachWithIndex { String value, int i ->
		boolean valShouldBeRemoved = false
		for (String removalVal in removalArr) {
		    if (StringUtils.equalsIgnoreCase(removalVal, value)) {
			valShouldBeRemoved = true
			break
                    }
		}

		if (i > 1 && !valShouldBeRemoved) {
		    values << value
		}
		else if (valShouldBeRemoved) {
		    split[i] = ''
		}
            }
        }

	String shortenedConceptPath = StringUtils.join(values, '\\')
	StringUtils.leftPad(shortenedConceptPath, shortenedConceptPath.length() + 1, '\\')
    }

    static String getSampleValue(String value, String sampleType, String timepoint, String tissueType) {
	if (StringUtils.equalsIgnoreCase(value, "E") || StringUtils.equalsIgnoreCase(value, "normal")) {
	    List<String> values = []
	    if (sampleType) {
		values << sampleType
	    }
	    if (timepoint) {
		values << timepoint
	    }
	    if (tissueType) {
		values << tissueType
	    }
	    StringUtils.join values, "/"
        }
        else {
	    value
        }
    }
}
