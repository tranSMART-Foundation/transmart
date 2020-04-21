import command.UserGroupCommand
import grails.converters.JSON
//import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Principal
import org.transmart.searchapp.SecureObjectAccess
import org.transmart.searchapp.UserGroup
import org.transmartproject.db.log.AccessLogService

@Slf4j('logger')
class UserGroupController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired private AccessLogService accessLogService
    @Autowired private UtilService utilService

    def list() {
        if (!params.max) {
            params.max = 10
        }
	[ugs: UserGroup.findAllByIdGreaterThanEquals(0, params),
	 ugCount: UserGroup.count()]
    }

    def membership() {}

    def show(UserGroup userGroup) {
	if (userGroup) {
	    [ug: userGroup, soas: SecureObjectAccess.findAllByPrincipal(userGroup, [sort: 'accessLevel'])]
        }
        else {
	    flash.message = 'UserGroup not found with id ' + params.id
	    redirect action: 'list'
        }
    }

//    @Transactional // TODO move tx work to service
    def delete(UserGroup userGroup) {
	if (userGroup) {
	    List<SecureObjectAccess> accessList = SecureObjectAccess.findAllByPrincipal(userGroup)
	    for (SecureObjectAccess soa in accessList) {
		soa.delete(flush: true)
	    }
	    userGroup.delete()
            flash.message = 'UserGroup ' + params.id + ' deleted'
        }
        else {
            flash.message = 'UserGroup not found with id ' + params.id
        }
	redirect action: 'list'
    }

    def edit(UserGroup userGroup) {
	if (userGroup) {
	    [ug: userGroup]
        }
        else {
	    flash.message = 'UserGroup not found with id ' + params.id
	    redirect action: 'list'
        }
    }

    def update(UserGroup userGroup) {
	if (userGroup) {
	    userGroup.properties = params
	    if (!userGroup.hasErrors() && userGroup.save()) {
                flash.message = 'UserGroup ' + params.id + ' updated'
		redirect action: 'show', id: userGroup.id
            }
            else {
		render view: 'edit', model: [ug: userGroup]
            }
        }
        else {
            flash.message = 'UserGroup not found with id ' + params.id
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	[ug: new UserGroup(params)]
    }

    def save() {
	UserGroup userGroup = new UserGroup(params)
	if (userGroup.save()) {
	    accessLogService.report 'Group created',
		'Group: ' + userGroup.name + ' created.'
	    redirect action: 'show', id: userGroup.id
        }
	else {
	    logger.error '{}', utilService.errorStrings(userGroup)
	    render view: 'create', model: [ug: userGroup]
        }
    }

    def ajaxGetUserSearchBoxData() {
	List<Map> userData = AuthUser.withCriteria {
            // TODO: searchText is not escaped for like special characters.
            // This is not trivial to do in a database agnostic way afaik, see:
            // http://git.io/H9y7gQ
            or {
		ilike 'name', '%' + params.query + '%'
		ilike 'username', '%' + params.query + '%'
            }
        }.collect { AuthUser user ->
            [name       : user.name, username: user.username, type: user.type,
             description: user.description, uid: user.id]
        }

	render text: params.callback + '(' + ([rows: userData] as JSON) + ')',
            contentType: 'application/javascript'
    }

    def searchUsersNotInGroup(UserGroup userGroup, Long id, String searchtext) {
	render template: 'addremove', model: [
	    ug: userGroup,
	    usersToAdd: searchForUsersNotInGroup(id, searchtext)]
    }

    def searchGroupsWithoutUser(AuthUser user, String searchtext) {
	render template: 'addremoveg', model: [
	    userInstance: user,
	    groupswithuser: getGroupsWithUser(user.id),
	    groupswithoutuser: getGroupsWithoutUser(user.id, searchtext)]
    }

    def addUserToGroups(UserGroupCommand fl, String searchtext) {
	AuthUser user = AuthUser.get(params.currentprincipalid)
	if (user) {
	    List<UserGroup> groupsToAdd = UserGroup.findAllByIdInList fl.groupstoadd.collect { it.toLong() }
	    for (UserGroup group in groupsToAdd) {
		group.addToMembers user
		group.save failOnError: true, flush: true
            }
        }

        render template: 'addremoveg',
	    model: [userInstance     : user,
		    groupswithuser   : getGroupsWithUser(user.id),
		    groupswithoutuser: getGroupsWithoutUser(user.id, searchtext)]
    }

    def removeUserFromGroups(UserGroupCommand fl, String searchtext) {
	AuthUser user = AuthUser.get(params.currentprincipalid)
	if (user) {
	    List<UserGroup> groupsToRemove = UserGroup.getAll(fl.groupstoremove.collect { it.toLong() }).findAll()
	    for (UserGroup group in groupsToRemove) {
		group.removeFromMembers user
		group.save failOnError: true, flush: true
            }
        }

        render template: 'addremoveg',
	    model: [userInstance     : user,
		    groupswithuser   : getGroupsWithUser(user.id),
		    groupswithoutuser: getGroupsWithoutUser(user.id, searchtext)]
    }

    def addUsersToUserGroup(UserGroupCommand fl, UserGroup userGroup, Long id) {

	List<AuthUser> usersToAdd = AuthUser.getAll(fl.userstoadd.collect { it.toLong() }).findAll()
	if (userGroup) {
	    if (params.version) {
		long version = params.long('version', 0)
		if (userGroup.version > version) {
		    userGroup.errors.rejectValue 'version',
			'userGroup.optimistic.locking.failure',
			'Another user has updated this UserGroup while you were editing.'
		    render template: 'addremove', model: [ug: userGroup]
		    return
                }
            }

	    for (r in usersToAdd) {
		userGroup.members << r
            }

	    if (!userGroup.hasErrors() && userGroup.save(flush: true)) {
                flash.message = 'UserGroup ' + params.id + ' updated'
            }
        }
        else {
            flash.message = 'UserGroup not found with id ' + params.id
        }

	render template: 'addremove', model: [
	    ug: userGroup,
	    usersToAdd: searchForUsersNotInGroup(id, fl.searchtext)]
    }

    def removeUsersFromUserGroup(UserGroupCommand fl, UserGroup userGroup, Long id) {
	logger.error '{}', utilService.errorStrings(fl)
	List<Long> removeIds = fl.userstoremove?.collect { it.toLong() }
	if (userGroup && removeIds) {
	    List<AuthUser> usersToRemove = AuthUser.getAll(removeIds).findAll()
	    if (params.version) {
		long version = params.long('version', 0)
		if (userGroup.version > version) {
		    userGroup.errors.rejectValue 'version',
			'userGroup.optimistic.locking.failure',
			'Another user has updated this userGroup while you were editing.'
		    render template: 'addremove', model: [ug: userGroup]
		    return
                }
            }

	    for (r in usersToRemove) {
		userGroup.members.remove r
	    }

	    if (!userGroup.hasErrors() && userGroup.save(flush: true)) {
		flash.message = 'UserGroup ' + id + ' updated'
            }
	}

	render template: 'addremove', model: [
	    ug: userGroup,
	    usersToAdd: searchForUsersNotInGroup(id, fl.searchtext)]
    }

    private List<AuthUser> searchForUsersNotInGroup(Long groupid, String searchText) {
	AuthUser.executeQuery('''
				from AuthUser us
				WHERE us NOT IN (
					select u.id
					from UserGroup g, IN (g.members) u
					where g.id=?
				)
				AND upper(us.name) LIKE ?
				ORDER BY us.userRealName''',
			      [groupid, '%' + searchText.toUpperCase() + '%']).sort { it.name }
    }

    def ajaxGetUsersAndGroupsSearchBoxData(String query, String callback) {
	List<Map> userdata = []
	List<Principal> users = Principal.executeQuery('''
				from Principal p
				where upper(p.name) like :query
				order by p.name''',
						       [query: '%' + query.toUpperCase() + '%'])
	for (Principal user in users) {
	    userdata << [name: user.name,
			 username: user.type == 'USER' ? user.username : 'No Login',
			 type: user.type,
			 description: user.description,
			 uid: user.id]
        }

	render contentType: 'text/javascript',
	    text: callback + '(' + ([rows: userdata] as JSON) + ')'
    }

    private List<UserGroup> getGroupsWithUser(long userid) {
	UserGroup.executeQuery('Select g FROM UserGroup g, IN (g.members) m WHERE m.id=?', userid)
    }

    private List<UserGroup> getGroupsWithoutUser(long userid, String searchText) {
	UserGroup.executeQuery '''
				from UserGroup g
				WHERE g.id<>-1
				AND g.id NOT IN (
					SELECT g2.id
					from UserGroup g2, IN (g2.members) m
					WHERE m.id=?
				)
				AND upper(g.name) like ?''',
	[userid, '%' + searchText.toUpperCase() + '%']
    }
}
