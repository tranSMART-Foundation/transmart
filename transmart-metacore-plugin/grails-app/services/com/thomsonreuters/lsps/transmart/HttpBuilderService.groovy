package com.thomsonreuters.lsps.transmart

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials

@CompileStatic
@Slf4j('logger')
class HttpBuilderService {
	
    static transactional = false

    private static final String proxyHost = System.getProperty('proxyHost')
    private static final String proxyPassword = System.getProperty('proxyPassword')
    private static final String proxyPort = System.getProperty('proxyPort')
    private static final String proxyUser = System.getProperty('proxyUser')
    private static final String proxyNTLMDomain = System.getProperty('proxyNTLMDomain')

    HTTPBuilder getInstance(String uri) {
	HTTPBuilder site = new HTTPBuilder(uri)
			
	if (proxyHost && proxyPort) {
	    logger.info 'Using proxy -> {}:{}', proxyHost, proxyPort
	    if (proxyUser) {
		logger.info 'Authenticating with proxy as {}', proxyUser
		if (proxyNTLMDomain) {
		    logger.info 'NTLM domain: {}', proxyNTLMDomain
		}
		site.client.credentialsProvider.setCredentials(
		    new AuthScope(proxyHost, proxyPort.toInteger()),
		    new NTCredentials(proxyUser, proxyPassword,
				      InetAddress.localHost.hostName, proxyNTLMDomain)
		)
	    }
				
	    site.setProxy proxyHost, proxyPort.toInteger(), null
	}
	site
    }
}
