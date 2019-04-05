package org.transmart

import com.recomdata.security.AuthUserDetailsService
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import org.opensaml.xml.XMLObject
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.userdetails.SAMLUserDetailsService
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role
import org.transmartproject.core.exceptions.UnexpectedResultException

import javax.annotation.Resource

// not in grails-app/services to avoid being automatically instantiated
// we want to instantiate it only if SAML is on
@CompileStatic
@Slf4j('logger')
class FederatedUserDetailsService implements SAMLUserDetailsService, InitializingBean {

    @Resource
    GrailsApplication grailsApplication

    @Autowired
    SessionFactory sessionFactory

    @Autowired
    AuthUserDetailsService userDetailsService

    @Autowired
    UtilService utilService

    @Value('${org.transmart.security.saml.createInexistentUsers:false}')
    private boolean createInexistentUsers

    @Value('${org.transmart.security.saml.attribute.username:}')
    private String usernameAttribute

    @Value('${org.transmart.security.saml.attribute.federatedId:}')
    private String federatedIdAttribute

    @Value('${org.transmart.security.saml.attribute.firstName:}')
    private String firstNameAttribute

    @Value('${org.transmart.security.saml.attribute.lastName:}')
    private String lastNameAttribute

    @Value('${org.transmart.security.saml.attribute.email:}')
    private String emailAttribute

    private List<String> defaultRoleNames

    @Transactional
    def loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {

        String federatedId = fetchFederatedId(credential)
        try {
	    logger.debug 'Searching for user with federated id "{}"', federatedId
	    userDetailsService.loadUserByProperty 'federatedId', federatedId, true
        }
        catch (UsernameNotFoundException nf) {
	    logger.info 'No user found with federated id "{}"', federatedId
	    tryCreateUser credential, federatedId, nf
	    logger.info 'Trying to load user with federated id "{}" again', federatedId

	    userDetailsService.loadUserByProperty 'federatedId', federatedId, true
        }
    }

    String fetchFederatedId(SAMLCredential credential) {
	if (federatedIdAttribute) {
	    getAttr credential, federatedIdAttribute
        }
        else {
            credential.nameID.value //better be persistent
        }
    }

    static String getAttr(SAMLCredential credential, String it) {
        def values = credential.getAttribute(it)?.attributeValues
        if (!values) {
	    throw new UnexpectedResultException(
		'Could not find values for attribute ' + it + ' in SAML credential')
        }
        if (values.size() > 1) {
	    throw new UnexpectedResultException(
		'Found more than one value for attribute ' + it + ': ' + values)
        }

        XMLObject attrValue = values.getAt(0)
        if (attrValue.hasProperty('value')) {
	    attrValue['value']
        }
        else if (values.hasProperty('textContent')) {
	    attrValue['textContent']
        }
        else {
	    throw new UnexpectedResultException(
		'Unexpected value for attribute ' + it + ': ' + attrValue)
        }
    }

    private void tryCreateUser(SAMLCredential credential, String federatedId, UsernameNotFoundException nf) {
	if (!createInexistentUsers) {
	    logger.warn 'Will not try to create user with federated id "{}", such option is deactivated',
		federatedId
	    throw nf
	}

	String username = null
	if (usernameAttribute) {
	    username = getAttr(credential, usernameAttribute)
	}

	String realName = null
	if (firstNameAttribute && lastNameAttribute) {
	    String firstName = getAttr(credential, firstNameAttribute) ?: ''
	    String lastName = getAttr(credential, lastNameAttribute) ?: ''
	    realName = firstName && lastName ? firstName + ' ' + lastName : firstName + lastName
        }

	String email = null
	if (emailAttribute) {
	    email = getAttr(credential, emailAttribute)
        }

	AuthUser newUser = AuthUser.createFederatedUser(federatedId, username, realName, email)
	updateRoles newUser

	if (newUser.save(flush: true)) {
	    logger.info 'Created new user. {federatedId={}, username={}, realName={}, email={}}',
		federatedId, username, realName, email
        }
	else {
	    logger.error 'Failed creating new user with federatedId {}, errors: {}',
		federatedId, utilService.errorStrings(newUser)
	    throw nf
        }
    }

    @CompileDynamic
    private void updateRoles(AuthUser user) {
	if (defaultRoleNames) {
            // if new user authorities specified then replace default authorities
	    user.authorities.clear()
	    for (Role role in Role.findAllByAuthorityInList(defaultRoleNames)) {
		user.addToAuthorities role
            }
        }
    }

    @CompileDynamic
    void afterPropertiesSet() {
	defaultRoleNames = grailsApplication.config.org.transmart.security.saml.defaultRoles as List<String>
    }
}
