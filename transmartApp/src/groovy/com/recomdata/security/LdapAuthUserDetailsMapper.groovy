package com.recomdata.security

import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper
import org.springframework.util.Assert
import org.transmart.plugin.shared.security.AuthUserDetails
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role
import org.transmartproject.db.log.AccessLogService
import org.transmartproject.security.BruteForceLoginLockService

/**
 * @author Florian Guitton
 */
@Slf4j('logger')
class LdapAuthUserDetailsMapper implements UserDetailsContextMapper {

    AccessLogService accessLogService
    BruteForceLoginLockService bruteForceLoginLockService
    SpringSecurityService springSecurityService

    String mappedUsernameProperty = 'username' // also 'federatedId' allowed
    boolean inheritPassword = false
    // Pattern for newly created username generation, ignored if mappedUsernameProperty == 'username'
    String newUsernamePattern
    // List of roles assigned to new user
    List<String> defaultAuthorities

    private String passwordAttributeName = 'userPassword'
    private String rolePrefix = 'ROLE_'
    private String[] roleAttributes = null
    private boolean convertToUpperCase = true

    @Value('${transmartproject.ldap.caseInsensitive:false}')
    private boolean caseInsensitive

    @Value('${transmartproject.ldap.doNotCreateUserIfNotExist:false}')
    private boolean doNotCreateUserIfNotExist

    protected Collection<? extends GrantedAuthority> collectAuthoritiesForRoleAttributes(DirContextOperations ctx) {
	Collection<? extends GrantedAuthority> result = []
        if (!roleAttributes) {
            return result
        }

	for (String roleAttribute in roleAttributes) {
	    String[] rolesForAttribute = ctx.getStringAttributes(roleAttribute)
            if (rolesForAttribute == null) {
		logger.debug '''Couldn't read role attribute '{}' for user {}''', roleAttribute, ctx.dn
                continue
            }

	    for (String role in rolesForAttribute) {
		GrantedAuthority authority = createAuthority(role)
		if (authority) {
		    result << authority
                }
            }
        }

	result
    }

    protected Collection<? extends GrantedAuthority> collectDatabaseAuthorities(AuthUser user) {
        user.authorities.collect { new SimpleGrantedAuthority(it.authority) }
    }

    protected AuthUser findOrSaveUser(DirContextOperations ctx, String username) {
        String fullName = ctx.getStringAttribute('cn')
        String email = ctx.getStringAttribute('mail')
        String password = mapPassword(ctx)

        AuthUser user
	if (caseInsensitive) {
            user = AuthUser.createCriteria().get { eq(mappedUsernameProperty, username, [ignoreCase: true])}
            if (user == null) {
                user = AuthUser.create()
                user.username = username
            }
        }
        else {
            user = AuthUser.findOrCreateWhere((mappedUsernameProperty): username)
        }
        user.name = fullName
        user.passwd = password
        user.userRealName = fullName
        user.email = email

        def created = !user.id
	boolean willGenerateUsername = false

	if (created && doNotCreateUserIfNotExist) {
	    logger.warn '''Can't create user '{}' because transmartproject.ldap.doNotCreateUserIfNotExist is set.''', username
	    throw new UsernameNotFoundException("User '$username' does not exist in transmart DB.")
        }

        if (created) {
            user.emailShow = true
            user.enabled = true
            if (mappedUsernameProperty != 'username') {
                // we will set username later
                if (!newUsernamePattern) {
                    user.username = username
                }
                else if (UsernameUtils.patternHasId(newUsernamePattern)) {
                    willGenerateUsername = true
                    user.username = UsernameUtils.randomName()
                }
                else {
                    user.username = UsernameUtils.evaluatePattern(user, newUsernamePattern)
                }
            }
        }
        user.save(flush: true)

        // generate user name after initial save, because it can use identifier
        if (!user.hasErrors() && willGenerateUsername) {
            user.username = UsernameUtils.evaluatePattern(user, newUsernamePattern)
            user.save(flush: true)
        }

        if (user.hasErrors()) {
	    logger.error '''Can't save User: {}''', username
	    user.errors.allErrors.each { logger.error '{}', it }
            return null
        }

        if (created) {
	    List<String> authorities = defaultAuthorities ?: [Roles.SPECTATOR.authority]
            Role.findAllByAuthorityInList(authorities).each { user.addToAuthorities(it) }
	    accessLogService.report 'LDAP', 'User Created', "User '$user.username' for $user.userRealName created"
        }

        if (!user.enabled) {
	    logger.error 'User is disabled: {}', username
            return null
        }

        return user
    }

    @Transactional(readOnly=true)
    UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        username = username.replaceAll(/[^0-9A-Za-z]*/, '').toLowerCase()
	logger.debug 'Mapping user details from context and database with username: {}', username

	AuthUser user = findOrSaveUser(ctx, username)
        if (!user) {
            return null
        }

	Collection<? extends GrantedAuthority> collectedAuthorities = new HashSet<>()
        collectedAuthorities.addAll(authorities)
        collectedAuthorities.addAll(collectAuthoritiesForRoleAttributes(ctx))
        collectedAuthorities.addAll(collectDatabaseAuthorities(user))

	new AuthUserDetails(
            user.username,
            user.passwd,
            user.enabled,
            true,
            true,
            !bruteForceLoginLockService.isLocked(user.username),
            collectedAuthorities ?: AuthUserDetailsService.NO_ROLES,
            user.id,
	    "LDAP '${user.userRealName}'",
	    user.email)
    }

    void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException('LdapAuthUserDetailsMapper only supports reading from a context. Please' +
						'use a subclass if mapUserToContext() is required.')
    }

    protected String mapPassword(DirContextOperations ctx) {
        if (!inheritPassword) {
            return 'NO_PASSWORD'
        }

        String password = ''
	def passwordValue = ctx.getObjectAttribute('userPasswordRaw')
        if (passwordValue != null) {
            if (!(passwordValue instanceof String)) {
                passwordValue = new String((byte[]) passwordValue)
            }
            password = (String) passwordValue
        }

	springSecurityService.encodePassword(password)
    }

    protected GrantedAuthority createAuthority(role) {
        if (role instanceof String) {
            if (convertToUpperCase) {
		role = role.toUpperCase()
            }
            return new SimpleGrantedAuthority(rolePrefix + role)
        }
    }

    void setConvertToUpperCase(boolean convert) {
	convertToUpperCase = convert
    }

    void setPasswordAttributeName(String name) {
	passwordAttributeName = name
    }

    void setRoleAttributes(String[] attributes) {
	Assert.notNull(attributes, 'roleAttributes array cannot be null')
	roleAttributes = attributes
    }

    void setRolePrefix(String prefix) {
	rolePrefix = prefix
    }
}
