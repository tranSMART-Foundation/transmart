package xnat.plugin

import com.recomdata.export.ExportTableNew
import org.transmartproject.db.log.AccessLogService

class TableController {

    AccessLogService accessLogService
    def i2b2HelperService
    XnatHelperService xnatHelperService

    def analysisGrid(String concept_key, String result_instance_id1, String result_instance_id2) {

	// which subsets are present?
	boolean s1 = result_instance_id1
	boolean s2 = result_instance_id2

	accessLogService.report 'DatasetExplorer-Grid Analysis Drag',
	    'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2 + ' Concept:' + concept_key

        //Copied from Grid view, but must not use the same table!
        //XXX: session is a questionable place to store this because it breaks multi-window/tab nav
	//ExportTableNew table = (ExportTableNew) session.gridtable
	ExportTableNew table = new ExportTableNew()
	if (s1) {
	    xnatHelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, 'subset1')
	}
	if (s2) {
	    xnatHelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, 'subset2')
	}

        List<String> keys = i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2)

	for (String key in keys) {
	    if (s1) {
		i2b2HelperService.addConceptDataToTable(table, key, result_instance_id1)
	    }
	    if (s2) {
		i2b2HelperService.addConceptDataToTable(table, key, result_instance_id2)
            }
        }

	PrintWriter pw = new PrintWriter(response.outputStream)

	if (concept_key) {

            String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key))
            Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2)

	    List<String> conceptKeys = []
	    String prefix = concept_key.substring(0, concept_key.indexOf('\\', 2))

	    if (cconcepts) {
		for (String cc in cconcepts) {
		    conceptKeys << prefix + i2b2HelperService.getConceptPathFromCode(cc)
		}
	    }
	    else {
		conceptKeys << concept_key
	    }

	    for (String ck in conceptKeys) {
		if (s1) {
		    i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id1)
		}
		if (s2) {
		    i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id2)
		}
            }
        }
        pw.write(table.toJSONObject().toString(5))
        pw.flush()
    }
}
