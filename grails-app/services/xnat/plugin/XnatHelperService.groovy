package xnat.plugin

import groovy.sql.Sql
import com.recomdata.export.*
import i2b2.*

//import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

class XnatHelperService {

    def i2b2HelperService
    def dataSource
    def subjectService      //xnat

    def serviceMethod() {

    }

    /**  plugin
     * Fills the main demographic data in an export table for the grid
     */
    def ExportTableNew addAllPatientDemographicDataForSubsetToTable(ExportTableNew tablein, String result_instance_id, String subset) {
        //checkQueryResultAccess result_instance_id

        log.trace("Getting sampleCD's for patient number")
        def mapOfSampleCdsByPatientNum = i2b2HelperService.buildMapOfSampleCdsByPatientNum(result_instance_id)

        log.trace("Adding patient demographic data to grid with result instance id:" +result_instance_id+" and subset: "+subset)
        Sql sql = new Sql(dataSource)
        String sqlt = '''
            SELECT
                I.*
            FROM (
                SELECT
                    p.*,
                    t.trial
                FROM
                    patient_dimension p
                INNER JOIN patient_trial t ON p.patient_num = t.patient_num
                WHERE
                    p.PATIENT_NUM IN (
                        SELECT
                            DISTINCT patient_num
                        FROM
                            qt_patient_set_collection
                        WHERE
                            result_instance_id = ? ) )
                I
            ORDER BY
                I.PATIENT_NUM''';

        log.debug "Initial grid query: $sqlt, riid: $result_instance_id"


        Sql sql1 = new Sql(dataSource)
        String sqlt1 = '''SELECT sessionid from xnat.session where subjectid IN (SELECT xnat_subjectid from xnat.subject where tsmart_subjectid = ?)''';

        def sessionid;
        def scanid;
        def subjectid = "OPT_045"
        sql1.eachRow(sqlt1, [subjectid], {row ->
//            System.out.println("sessionid id "+ row.sessionid);
            sessionid = row.sessionid;
        })

        Sql sql2 = new Sql(dataSource)
        String sqlt2 = '''SELECT scanid from xnat.scan where sessionid IN (SELECT sessionid from xnat.session where subjectid IN (SELECT xnat_subjectid from xnat.subject where tsmart_subjectid = ?))''';


        sql2.eachRow(sqlt2, [subjectid], {row ->
//            System.out.println("scan id "+ row.scanid);
            scanid = row.scanid;
        })


        Sql sql3 = new Sql(dataSource)
        String sqlt3 = '''select resourceid, filename from xnat.snapshot where scanid = ?''';


        def resourceid;
        def filename;
        sql3.eachRow(sqlt3, [scanid], {row ->

            resourceid = row.resourceid;
            filename = row.filename;
        })

        //if i have an empty table structure so far
        if (tablein.getColumns().size() == 0) {

            tablein.putColumn("study_id", new ExportColumn("study_id", "Study ID", "", "String"));
            tablein.putColumn("subject", new ExportColumn("subject", "Subject ID", "", "String"));
            tablein.putColumn("T2_lesions_download", new ExportColumn("T2_lesions_download", "XNAT", "", "String"));


        }


        int i = 0;
        Random random = new Random();
        sql.eachRow(sqlt, [result_instance_id], { row ->
            /*If I already have this subject mark it in the subset column as belonging to both subsets*/
            String subject = row.PATIENT_NUM;
            if (tablein.containsRow(subject)) {
                String s = tablein.getRow(subject).get("subset");
                s = s + "," + subset;
                tablein.getRow(subject).put("subset", s);
            } else
            /*fill the row*/ {

                def arr = row.SOURCESYSTEM_CD?.split(":")

                ExportRowNew newrow = new ExportRowNew();
                i++;
                newrow.put("study_id", row.TRIAL);

                newrow.put("subject", arr?.length == 2 ? arr[1] : "");

                if(subjectService.SubjectExists(arr?.length == 2 ? arr[1] : "")) {
                    String subjectID =  arr?.length == 2 ? arr[1] : "";

                    newrow.put("T2_lesions_download", "<a href='/"+ grails.util.Metadata.current.'app.name' +"/scan?subjectID=" + subjectID + "' target = '_blank' style='color:blue'>View Sessions</a>");
                } else {
                    newrow.put("T2_lesions_download", "");

                }

                tablein.putRow(subject, newrow);



            }
        })
        return tablein;
    }

}
