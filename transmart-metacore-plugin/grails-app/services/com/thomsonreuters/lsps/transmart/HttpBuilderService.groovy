package com.thomsonreuters.lsps.transmart

import groovyx.net.http.*
src/main/groovy/org/transmartproject/pipeline/converter/CARDSFormatter.groovy
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import org.apache.http.auth.*

@Slf4j('logger')
class HttpBuilderService {
	boolean transactional = true
	
	def getInstance(uri) {
		def site = new HTTPBuilder(uri)
			
		if (System.properties.proxyHost && System.properties.proxyPort) {
			logger.info "Using proxy -> ${System.properties.proxyHost}:${System.properties.proxyPort}"
			if (System.properties.proxyUser) {
				logger.info "Authenticating with proxy as ${System.properties.proxyUser}"
				if (System.properties.proxyNTLMDomain) logger.info "NTLM domain: ${System.properties.proxyNTLMDomain}"
				site.client.getCredentialsProvider().setCredentials(
				    new AuthScope(System.properties.proxyHost, System.properties.proxyPort.toInteger()),
				    new NTCredentials(System.properties.proxyUser, System.properties.proxyPassword, 
						InetAddress.getLocalHost().getHostName(), System.properties.proxyNTLMDomain)
				)
			}
				
			site.setProxy(System.properties.proxyHost, System.properties.proxyPort.toInteger(), null)
		}
		
		return site
		
	}
	
}
