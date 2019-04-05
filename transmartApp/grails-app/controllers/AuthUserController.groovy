import grails.plugin.springsecurity.SpringSecurityService
import groovy.util.logging.Slf4j
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.TransactionStatus
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.AuthUserSecureAccess
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.Role
import org.transmart.searchapp.SecureObjectAccess
import org.transmartproject.db.log.AccessLogService

import java.util.regex.Pattern

@Slf4j('logger')
class AuthUserController implements InitializingBean {

    private static final List<String> BINDABLE_NAMES = ['enabled', 'username', 'userRealName', 'email', 'description',
	                                                'emailShow', 'authorities', 'changePassword'].asImmutable()

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired private AccessLogService accessLogService
    @Autowired private SecurityService securityService
    @Autowired private SpringSecurityService springSecurityService

    @Value('${transmartproject.authUser.create.passwordRequired:false}')
    private boolean passwordRequired

    @Value('${com.recomdata.passwordstrength.description:}')
    private String passwordStrengthDescription

    private Pattern passwordStrengthPattern

    def list() {
        if (!params.max) {
            params.max = 999999
        }
	[personList: AuthUser.list(params), personCount: AuthUser.count()]
    }

    def show(AuthUser authUser) {
	if (authUser) {
	    [person: authUser,
	     roleNames: authUser.authorities*.authority.sort(),
	     soas: SecureObjectAccess.findAllByPrincipal(authUser, [sort: 'accessLevel']),
	     ausas: AuthUserSecureAccess.findAllByAuthUser(authUser, [sort: 'accessLevel'])]
	}
	else {
	    flash.message = "AuthUser not found with id $params.id"
            redirect action: 'list'
        }
    }

    def delete(AuthUser authUser) {
	if (authUser) {
	    if (authUser.username == securityService.currentUsername()) {
                flash.message = 'You can not delete yourself, please login as another admin and try again'
            }
            else {
		logger.info 'Deleting {} from the roles', authUser.username
		for (Role role in Role.list()) {
		    role.removeFromPeople authUser
		}
		logger.info 'Deleting {} from secure access list', authUser.username
		for (AuthUserSecureAccess ausa in AuthUserSecureAccess.findAllByAuthUser(authUser)) {
		    ausa.delete()
		}
		logger.info 'Deleting the gene signatures created by {}', authUser.username
                try {
		    for (GeneSignature gs in GeneSignature.findAllByCreatedByAuthUser(authUser)) {
			gs.delete()
                    }
                }
		catch (InvalidPropertyException ignored) {
		    logger.warn 'AuthUser properties in the GeneSignature domain need to be enabled'
		}
		logger.info 'Finally, deleting {}', authUser.username
		authUser.delete()
		String msg = authUser.userRealName + ' has been deleted.'
                flash.message = msg
		accessLogService.report 'User Deleted', msg
            }
        }
        else {
	    flash.message = "User not found with id $params.id"
        }
        redirect action: 'list'
    }

    def edit(AuthUser authUser) {
	if (authUser) {
	    buildPersonModel authUser
	}
	else {
	    flash.message = "AuthUser not found with id $params.id"
            redirect action: 'list'
        }
    }

    def update() {
        saveOrUpdate()
    }

    def create() {
        [person: new AuthUser(params), authorityList: Role.list()]
    }

    def save() {
        saveOrUpdate()
    }

    private saveOrUpdate() {

        boolean create = params.id == null
	AuthUser authUser = create ? new AuthUser() : AuthUser.get(params.id)

	bindData authUser, params, [include: BINDABLE_NAMES]

        // We have to make that check at the user creation since the RModules check over this.
        // It could mess up with the security at archive retrieval.
        // This is bad, but we have no choice at this point.
	if (!(authUser.username ==~ /^[0-9A-Za-z-]+$/)) {
	    flash.message = 'Username can only contain alphanumerical charaters and hyphens (Sorry)'
	    render view: create ? 'create' : 'edit', model: buildPersonModel(authUser)
	    return
        }

	String passwd = params.passwd
	if (passwd) {
	    if (passwordStrengthPattern && !passwordStrengthPattern.matcher(passwd).matches()) {
		flash.message = 'Password does not match complexity criteria. ' + passwordStrengthDescription
		render view: create ? 'create' : 'edit', model: buildPersonModel(authUser)
		return
            }

	    authUser.passwd = springSecurityService.encodePassword(passwd)
        }
        else if (create) {
	    if (passwordRequired) {
                flash.message = 'Password must be provided'
		render view: create ? 'create' : 'edit', model: buildPersonModel(authUser)
		return
            }

	    passwd = passwd ?: 'FilledByAuthUserController_' + RandomStringUtils.random(12, true, true)
	    authUser.passwd = springSecurityService.encodePassword(passwd)
        }

	authUser.name = authUser.userRealName

        /* the auditing should probably be done in the beforeUpdate() callback,
         * but that might cause problems in users created without a spring
         * security login (does this happen?) */
	String msg
        if (create) {
	    msg = "User: ${authUser.username} for ${authUser.userRealName} created"
        }
        else {
	    msg = "${authUser.username} has been updated. Changed fields include: "
	    msg += authUser.dirtyPropertyNames.collect { String field ->
		def newValue = authUser[field]
		def oldValue = authUser.getPersistentValue(field)
                if (newValue != oldValue) {
		    "$field ($oldValue -> $newValue)"
                }
	    }.findAll().join(', ')
        }

	AuthUser.withTransaction { TransactionStatus tx -> // TODO move to tx service
	    manageRoles authUser
	    if (authUser.save(flush: true)) {
		accessLogService.report "User ${create ? 'Created' : 'Updated'}", msg.toString()
		redirect action: 'show', id: authUser.id
            }
            else {
                tx.setRollbackOnly()
                flash.message = 'An error occured, cannot save user'
		render view: create ? 'create' : 'edit', model: [authorityList: Role.list(), person: authUser]
            }
        }
    }

    // the owning side of the many-to-many are the roles

    private void manageRoles(AuthUser authUser) {
	Collection<Role> oldRoles = unproxiedRoles(authUser)
	Set<Role> newRoles = params.keySet().findAll { String key ->
	    key.contains('ROLE') && params[key] == 'on'
	}.collect { String key -> Role.findByAuthority(key) }

	for (Role it in (newRoles - oldRoles)) {
	    it.addToPeople authUser
        }

	for (Role it in (oldRoles - newRoles)) {
	    it.removeFromPeople authUser
        }
    }

    private Collection<Role> unproxiedRoles(AuthUser authUser) {
	Collection<Role> unproxied = []
	for (Role role in (authUser.authorities ?: [])) {
	    unproxied << GrailsHibernateUtil.unwrapIfProxy(role)
        }
	unproxied
    }

    private Map buildPersonModel(AuthUser authUser) {
	Set<String> userRoleNames = authUser.authorities*.authority ?: []
	Map<Role, Boolean> roleMap = [:]
	List<Role> roles = Role.list().sort { Role role -> role.authority }
        for (role in roles) {
            roleMap[(role)] = userRoleNames.contains(role.authority)
        }
	[person: authUser, roleMap: roleMap, authorityList: roles]
    }

    void afterPropertiesSet() {
	// can't use @Value because Pattern isn't a supported conversion type
	passwordStrengthPattern = grailsApplication.config.com.recomdata.passwordstrength.pattern ?: null
    }
}
