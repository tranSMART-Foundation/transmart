package xnat.plugin

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value

@CompileStatic
class ScanService {

    static transactional = false

    @Value('${org.xnat.domain:}')
    private String domain

    @Value('${org.xnat.password:}')
    private String password

    @Value('${org.xnat.projectName:}')
    private String projectName

    @Value('${org.xnat.username:}')
    private String username

    String getDomain() {
	domain
    }

    String getUsername() {
	username
    }

    String getPassword() {
	password
    }

    String getProjectName() {
	projectName
    }

    String generateInfoUrl(String sessionId, String scanId) {
	'http://' + getDomain() + '/data/experiments/' + sessionId + '/scans/' + scanId
    }

    String generateThumbnailUrl(String fileName) {
	'http://' + getDomain() + fileName
    }

    String generateDownloadUrl(String sessionId, String scanId) {
	'http://' + getDomain() + '/data/experiments/' + sessionId + '/scans/' + scanId + '/files?format=zip'
    }
}
