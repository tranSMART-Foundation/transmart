package xnat.plugin
import grails.util.Holders
import grails.transaction.Transactional

@Transactional
class ScanService {


    def getDomain(){
        return Holders.config.org.xnat.domain;
    }

    def getUsername(){
        return Holders.config.org.xnat.username;
    }

    def getPassword(){
        return Holders.config.org.xnat.password;
    }

    def getProjectName(){
        return Holders.config.org.xnat.projectName;
    }



    def generateInfoURL (def sessionID, def scanID) {

        String URL = "http://" + getDomain() + "/data/experiments/"+sessionID.toString()+"/scans/"+scanID.toString();
        return URL;
    }

    def generateThumbnailURL (def sessionID, def scanID, def resourceID, def fileName) {

        String URL = "http://" + getDomain() + fileName.toString();
        return URL;
    }

    def generateDownloadURL(def sessionID, def scanID)
    {
        String URL = "http://" + getDomain() + "/data/experiments/"+sessionID.toString();
        URL += "/scans/"+scanID;
        URL += "/files?format=zip"
        return URL;
    }

}