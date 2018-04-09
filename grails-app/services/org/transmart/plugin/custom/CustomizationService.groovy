package org.transmart.plugin.custom

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.GrantedAuthority
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUser

import java.lang.reflect.Method

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class CustomizationService implements InitializingBean  {

	private Map<String, Map<String, UserLevel>> levels = [:].withDefault { [:] }
	private Map<String, String> controllerDefaultActions = [:]

	@Autowired private GrailsApplication grailsApplication
	@Autowired private SecurityService securityService

	/**
	 * Get the user level for the current user.
	 *
	 * @return UNREGISTERED if not authenticated,
	 *         ADMIN if the user has ROLE_ADMIN,
	 *         TWO if the user has ROLE_DATASET_EXPLORER_ADMIN,
	 *         ONE if the user has ROLE_STUDY_OWNER,
	 *         ZERO otherwise
	 */
	UserLevel currentUserLevel() {
		if (securityService.loggedIn()) {
			userLevel securityService.principal().authorities.collect { GrantedAuthority a -> a.authority }
		}
		else {
			UserLevel.UNREGISTERED
		}
	}

	/**
	 * Get the user level for the specified user. Do not call this if the user is
	 * currently authenticated as it would result in unnecessary database calls;
	 * use currentUserLevel() instead.
	 *
	 * @return ADMIN if the user has ROLE_ADMIN,
	 *         TWO if the user has ROLE_DATASET_EXPLORER_ADMIN,
	 *         ONE if the user has ROLE_STUDY_OWNER,
	 *         ZERO otherwise
	 */
	UserLevel userLevel(AuthUser authUser) {
		if (authUser.username == securityService.currentUsername()) {
			currentUserLevel()
		}
		else {
			userLevel authUser.authorities*.authority
		}
	}

	/**
	 * Get the user level for the given role names.
	 *
	 * @return ADMIN if the user has ROLE_ADMIN,
	 *         TWO if the user has ROLE_DATASET_EXPLORER_ADMIN,
	 *         ONE if the user has ROLE_STUDY_OWNER,
	 *         ZERO otherwise
	 */
	UserLevel userLevel(Collection<String> roleNames) {
		if (roleNames.contains(Roles.ADMIN.authority)) {
			UserLevel.ADMIN
		}
		else if (roleNames.contains(Roles.DATASET_EXPLORER_ADMIN.authority)) {
			UserLevel.TWO
		}
		else if (roleNames.contains(Roles.STUDY_OWNER.authority)) {
			UserLevel.ONE
		}
		else {
			UserLevel.ZERO
		}
	}

	void checkUserLevelAccess(String controller, String action) {
		String username = securityService.currentUsername()
		logger.debug 'checkUserLevelAccess() controller "{}" action "{}" user "{}"', controller, action, username
		if (controller && levels.containsKey(controller)) {
			if (!action) {
				action = controllerDefaultActions[controller]
			}
			UserLevel minLevel = levels[controller][action]
			if (!minLevel) {
				logger.trace 'checkUserLevelAccess() no RequiresLevel annotation for /{}/{}', controller, action
				return
			}

			UserLevel userLevel = currentUserLevel()
			if (userLevel < minLevel) {
				logger.error 'checkUserLevelAccess() {} < {}, access denied for /{}/{} user {}',
						userLevel, minLevel, controller, action, username
				throw new AccessDeniedException('You are not authorized to perform this action')
			}

			logger.debug 'checkUserLevelAccess() {} >= {}, access allowed for /{}/{} user {}',
					userLevel, minLevel, controller, action, username
		}
	}

	/**
	 * Get a <code>Settings</code> instance for the currently authenticated user.
	 */
	Settings userSetting(String name, long userId = securityService.currentUserId()) {
		findSetting name, userId
	}

	/**
	 * Get a shared <code>Settings</code> instance.
	 */
	Settings setting(String name) {
		findSetting name, 0
	}

	void afterPropertiesSet() {
		findControllerUserLevels()
	}

	protected void findControllerUserLevels() {
		levels.clear()
		for (GrailsClass gc in grailsApplication.getArtefacts(ControllerArtefactHandler.TYPE)) {
			String controllerName = gc.logicalPropertyName
			controllerDefaultActions[controllerName] = ((GrailsControllerClass) gc).defaultAction

			for (Method method in gc.clazz.methods) {
				RequiresLevel annotation = method.getAnnotation(RequiresLevel)
				if (annotation) {
					levels[controllerName][method.name] = annotation.value()
				}
			}
		}
		logger.debug 'levels {}, controllerDefaultActions {}', levels, controllerDefaultActions
	}

	@CompileDynamic // TODO use GrailsCompileStatic
	private Settings findSetting(String name, long userId) {
		Settings.createCriteria().get {
			eq 'fieldname', name
			eq 'userid', userId
			cache true
		}
	}
}
