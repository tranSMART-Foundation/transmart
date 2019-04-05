package xnat.plugin

import grails.util.Metadata
import groovy.json.JsonSlurper
import org.springframework.beans.factory.InitializingBean

class ScanController implements InitializingBean {

    private String appName

    ScanService scanService
    SubjectService subjectService

    def index() {}

    def getAPIListData(String subjectID) {
	Subject subject = new Subject(
	    transmartSubjectId: subjectID,
	    xnatSubjectId: subjectService.getXnatID(subjectID),
	    xnatProject: subjectService.getXnatProject(subjectID))

	String sessionString = 'https://' + scanService.domain + '/data/projects/' + subject.xnatProject +
	    '/subjects/' + subject.xnatSubjectId + '/experiments?format=json'
	String sessionUrl = sessionString.toURL().text
	def sessionJSON = new JsonSlurper().parseText(sessionUrl)

        int numSessions = sessionJSON.ResultSet.totalRecords.toInteger()

        for (int i = 0; i < numSessions; i++) {
	    Session s = new Session(sessionID: sessionJSON.ResultSet.Result[i].ID)

	    String scanString = 'https://' + scanService.domain + '/data/experiments/' + s.sessionID + '/scans?format=json'
	    def scanJSON = new JsonSlurper().parseText(scanString.toURL().text)

            int numScans = scanJSON.ResultSet.totalRecords.toInteger()

            for (int j = 0; j < numScans; j++) {
		Scan scan = new Scan(
		    sessionID: s.sessionID,
		    scanID: scanJSON.ResultSet.Result[j].ID,
		    seriesDesc: scanJSON.ResultSet.Result[j].series_description)

		String resourceString = 'https://' + scanService.domain + scanJSON.ResultSet.Result[j].URI + '/resources?format=json'
		def resourceJSON = new JsonSlurper().parseText(resourceString.toURL().text)
                int numResources = resourceJSON.ResultSet.totalRecords.toInteger()

                for (int k = 0; k < numResources; k++) {
                    if (resourceJSON.ResultSet.Result[k].format == 'GIF') {
			Snapshot newSnap = new Snapshot(resourceID: resourceJSON.ResultSet.Result[k].xnat_abstractresource_id)
			String thumbnailString = 'https://' + scanService.domain + scanJSON.ResultSet.Result[j].URI +
			    '/resources/' + newSnap.resourceID + '/files?format=json'
			def thumbnailJSON = new JsonSlurper().parseText(thumbnailString.toURL().text)

                        if (thumbnailJSON.ResultSet != null) {
                            def numThumbnails = thumbnailJSON.ResultSet.Result[0].URI
                            if (numThumbnails!=null) {
                                newSnap.fileName = thumbnailJSON.ResultSet.Result[0].URI
				scan.snapshots << newSnap
                            }
                        }
                    }
                }

		s.scans << scan
            }
	    s.subjectID = subjectID
            s.sessionName = sessionJSON.ResultSet.Result[i].label
	    subject.sessions << s
        }

	StringBuilder jsonFile = new StringBuilder('[')
        Iterator newSessionIter = subject.sessions.iterator()

        while (newSessionIter.hasNext()) {
	    Session session = newSessionIter.next()
	    jsonFile << '{"name":"' << session.sessionName << '",'
	    jsonFile << '"scans":['

	    Iterator newScanIter = session.scans.iterator().iterator()
            while (newScanIter.hasNext()) {
		Scan scan = newScanIter.next()

		jsonFile << '{"id":"' << scan.scanID << '",'
		jsonFile << '"series":"' << scan.seriesDesc << '",'

		Iterator snapIter = scan.snapshots.iterator()
                while (snapIter.hasNext()) {
		    Snapshot snapshot = snapIter.next()
		    jsonFile << '"info":"' << "/" << appName << "/xnat/info?url=" <<
			scanService.generateInfoUrl(session.sessionID, scan.scanID) << '",'
		    jsonFile << '"thumbnail":"' << "/" << appName << "/xnat/image?url=" <<
			scanService.generateThumbnailUrl(snapshot.fileName) << '",'
		}
		jsonFile << '"download":"' << "/" << appName << "/xnat/download?url=" <<
		    scanService.generateDownloadUrl(session.sessionID, scan.scanID)
		jsonFile << '"}'
                if (newScanIter.hasNext()) {
		    jsonFile << ','
                }
            }

	    jsonFile << ']}'
            if (newSessionIter.hasNext()){
		jsonFile << ','
            }
        }
	jsonFile << ']'

	response.contentType = 'application/json'
	render jsonFile.toString()
    }

    void afterPropertiesSet() {
	appName = Metadata.current.'app.name'
    }
}
