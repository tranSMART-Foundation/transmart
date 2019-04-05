package org.transmartproject.security

import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.transmart.oauth2.Client

/**
 * Synchronize OAuth2 client configuration.
 */
@Slf4j('logger')
class OAuth2SyncService implements InitializingBean {

    static datasource = 'oauth2'

    @Autowired private GrailsApplication grailsApplication
    @Autowired private PasswordEncoder passwordEncoder

    private List<Map> clients

    @Transactional
    void syncOAuth2Clients() {

	if (!clients) {
	    logger.debug 'Clients list in config is false; will do no synchronization'
            return
        }

	for (Map m in clients) {
	    if (!m.clientId) {
		logger.error 'Client data without clientId: {}', m
		continue
            }

	    Client client = Client.findByClientId(m.clientId) ?: new Client()

	    boolean dirty = false
	    m.each { String prop, value ->
                if (Client.hasProperty(prop)) {
		    logger.error 'Invalid property {} in client definition {}', prop, m
                    return
                }

                // Convert GStrings to Strings, Lists to Sets
                if (!(value instanceof List)) {
                    value = value.toString()
                }
                else {
                    value = value*.toString() as Set
                }

		if (prop == 'clientSecret') {
		    if (passwordEncoder.isPasswordValid(client[prop], value, null)) {
                        return
                    }
                }
		else if (client[prop] == value) {
                    return
                }

		client[prop] = value
                dirty = true
            }

            if (dirty) {
		logger.info 'Updating client {}', m.clientId
                client.save(flush: true)
            }
        }

	List<String> allClientIds = clients.collect { Map m -> m.clientId }.findAll()

	int n = Client.where {
            ne 'clientId', '__BOGUS' // hack to avoid empty WHERE clause
            if (allClientIds) {
                not {
                    'in' 'clientId', allClientIds
                }
            }
        }.deleteAll()

	if (n) {
	    logger.warn 'Deleted {} OAuth2 clients', n
        }
    }

    void afterPropertiesSet() {
	clients = grailsApplication.config.grails.plugin.springsecurity.oauthProvider.clients ?: []
    }
}
