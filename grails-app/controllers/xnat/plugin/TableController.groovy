package xnat.plugin

import com.recomdata.export.ExportTableNew
import grails.converters.JSON
import org.jfree.chart.servlet.ChartDeleter
import org.jfree.chart.servlet.ServletUtilities
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser
import org.transmartproject.core.users.User

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpSession

class TableController {

    def i2b2HelperService
    def springSecurityService
    def chartService
    def accessLogService
    User currentUserBean
    def xnatHelperService

    def index() {
    }

    def analysisGrid = {

        String concept_key = params.concept_key;
        def result_instance_id1 = params.result_instance_id1;
        def result_instance_id2 = params.result_instance_id2;

        /*which subsets are present? */
        boolean s1 = (result_instance_id1 == "" || result_instance_id1 == null) ? false : true;
        boolean s2 = (result_instance_id2 == "" || result_instance_id2 == null) ? false : true;

        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "DatasetExplorer-Grid Analysis Drag", eventmessage: "RID1:" + result_instance_id1 + " RID2:" + result_instance_id2 + " Concept:" + concept_key, accesstime: new java.util.Date())
        al.save()

        //XXX: session is a questionable place to store this because it breaks multi-window/tab nav
        ExportTableNew table = (ExportTableNew) request.getSession().getAttribute("gridtable");
        if (table == null) {

            table = new ExportTableNew();
            if (s1) xnatHelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id1, "subset1");
            if (s2) xnatHelperService.addAllPatientDemographicDataForSubsetToTable(table, result_instance_id2, "subset2");

            List<String> keys = i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2);
            Set<String> uniqueConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2);

            log.debug("Unique concepts: " + uniqueConcepts);
            log.debug("keys: " + keys)

            for (int i = 0; i < keys.size(); i++) {

                log.trace("adding concept data for " + keys.get(i));
                if (s1) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id1);
                if (s2) i2b2HelperService.addConceptDataToTable(table, keys.get(i), result_instance_id2);
            }
        }
        PrintWriter pw = new PrintWriter(response.getOutputStream());

        if (concept_key && !concept_key.isEmpty()) {

            String parentConcept = i2b2HelperService.lookupParentConcept(i2b2HelperService.keyToPath(concept_key));
            Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(parentConcept, result_instance_id1, result_instance_id2);

            def conceptKeys = [];
            def prefix = concept_key.substring(0, concept_key.indexOf("\\", 2));

            if (!cconcepts.isEmpty()) {
                for (cc in cconcepts) {
                    def ck = prefix + i2b2HelperService.getConceptPathFromCode(cc);
                    conceptKeys.add(ck);
                }
            } else
                conceptKeys.add(concept_key);

            for (ck in conceptKeys) {
                if (s1) i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id1);
                if (s2) i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id2);
            }
        }
        pw.write(table.toJSONObject().toString(5));
        pw.flush();

        request.getSession().setAttribute("gridtable", table);
    }
}
