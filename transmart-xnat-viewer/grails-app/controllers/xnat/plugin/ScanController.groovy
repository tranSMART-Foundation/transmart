package xnat.plugin
import groovy.json.*

class ScanController {

    def ScanService
    def SubjectService

    def index = {
    }

    def getAPIListData =
        {
            Subject subject = new Subject();
            subject.tranSMART_subjectID = params.subjectID;
            subject.xnat_subjectID = SubjectService.getXnatID(subject.tranSMART_subjectID);
            subject.xnat_project = SubjectService.getXnatProject(subject.tranSMART_subjectID);
//            println(subject.xnat_subjectID);

            String sessionString = "https://"+ ScanService.getDomain() + "/data/projects/" + subject.xnat_project + "/subjects/"+subject.xnat_subjectID+"/experiments?format=json";
            def sessionURL = sessionString.toURL().text;
            def sessionJSON = new JsonSlurper().parseText(sessionURL);

            int numSessions = sessionJSON.ResultSet.totalRecords.toInteger();

            for (int i = 0; i < numSessions; i++) {
                Session s = new Session();
                s.sessionID = sessionJSON.ResultSet.Result[i].ID;

                String scanString = "https://"+ ScanService.getDomain() + "/data/experiments/"+s.sessionID+"/scans?format=json";
                def scanURL = scanString.toURL().text;
                def scanJSON = new JsonSlurper().parseText(scanURL);

                int numScans = scanJSON.ResultSet.totalRecords.toInteger();

                for (int j = 0; j < numScans; j++) {
                    Scan aScan = new Scan();
                    aScan.sessionID = s.sessionID;
                    aScan.scanID = scanJSON.ResultSet.Result[j].ID;
                    aScan.seriesDesc = scanJSON.ResultSet.Result[j].series_description;

                    String resourceString = "https://"+ ScanService.getDomain() + scanJSON.ResultSet.Result[j].URI+"/resources?format=json";
                    def resourceURL = resourceString.toURL().text;
                    def resourceJSON = new JsonSlurper().parseText(resourceURL);
                    //println(resourceJSON);
                    int numResources = resourceJSON.ResultSet.totalRecords.toInteger();

                    for (int k = 0; k < numResources; k++) {
                        if (resourceJSON.ResultSet.Result[k].format == 'GIF') {
                            //println(resourceJSON.ResultSet.Result[k].xnat_abstractresource_id);
                            Snapshot newSnap = new Snapshot();
                            newSnap.resourceID = resourceJSON.ResultSet.Result[k].xnat_abstractresource_id;
                            //println(scanJSON.ResultSet.Result[j].URI);
                            String thumbnailString = "https://"+ ScanService.getDomain() + scanJSON.ResultSet.Result[j].URI+"/resources/"+newSnap.resourceID+"/files?format=json";
                            def thumbnailURL = thumbnailString.toURL().text;
                            def thumbnailJSON = new JsonSlurper().parseText(thumbnailURL);
//                            println("thumbnail:"+thumbnailURL);

                            if (thumbnailJSON.ResultSet != null) {
                                def numThumbnails = thumbnailJSON.ResultSet.Result[0].URI;
                                if (numThumbnails!=null) {
                                    newSnap.fileName = thumbnailJSON.ResultSet.Result[0].URI;
                                    //println(thumbnailJSON.ResultSet.Result[0].URI);
//                                    println("Nap:"+newSnap.fileName);
                                    aScan.snapshots.add(newSnap);
                                }
                            }
                        }
                    }

                    s.scans.add(aScan);

                }
                s.subjectID = params.subjectID;
                s.sessionName = sessionJSON.ResultSet.Result[i].label;
                subject.sessions.add(s);
            }

            String jsonFile = '[';
            Iterator newSessionIter = subject.sessions.iterator();

            while (newSessionIter.hasNext()) {
                Session aSession = newSessionIter.next();
                jsonFile += '{"name":"'+aSession.sessionName+'",';
                Iterator newScanIter = aSession.scans.iterator().iterator();
                jsonFile += '"scans":[';
                while (newScanIter.hasNext()) {
                    Scan aScan = newScanIter.next();

                    jsonFile += '{"id":"'+aScan.scanID+'",';
                    jsonFile += '"series":"'+aScan.seriesDesc+'",';
                    Iterator snapIter = aScan.snapshots.iterator();
                    while (snapIter.hasNext())
                    {
                        Snapshot aSnapshot = snapIter.next();
                        jsonFile += '"info":"'+ "/"+ grails.util.Metadata.current.'app.name' + "/xnat/info?url=" +  ScanService.generateInfoURL(aSession.sessionID, aScan.scanID) +'",';
                        jsonFile += '"thumbnail":"'+"/"+ grails.util.Metadata.current.'app.name' + "/xnat/image?url="+ ScanService.generateThumbnailURL(aSession.sessionID, aScan.scanID, aSnapshot.resourceID, aSnapshot.fileName) +'",';
                    }
                    jsonFile += '"download":"'+"/"+ grails.util.Metadata.current.'app.name' + "/xnat/download?url=" + ScanService.generateDownloadURL(aSession.sessionID, aScan.scanID);
                    jsonFile +='"}'
                    if (newScanIter.hasNext()) {
                        jsonFile +=','
                    }

                }
                jsonFile +=']}';
                if (newSessionIter.hasNext()){
                    jsonFile +=',';
                }
            }
            jsonFile +=']';

//            System.out.print(jsonFile);
            response.setContentType("application/json");
            render jsonFile;
        }

}