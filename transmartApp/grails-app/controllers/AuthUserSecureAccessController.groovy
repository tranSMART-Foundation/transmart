import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.AuthUserSecureAccess
import org.transmart.searchapp.SecureAccessLevel
import org.transmart.searchapp.SecureObject

class AuthUserSecureAccessController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Value('${com.recomdata.admin.paginate.max:0}')
    private int paginateMax

    def list() {
	int max = Math.min(params.int('max', paginateMax), 100)

	// NOTE: grails <g:sortableColumn> can't reference subobjects, which means
	// that we can just use .list(params) to query AuthUserSecureAccess.
	// Instead we need to build a hibernate query and map custom property
	// names to the subobjects. http://grails.org/GSP+Tag+-+sortableColumn
	int offset = params.int('offset', 0)
	String order = params.order ?: 'asc'
	String sort = params.sort ?: 'username'
	List<AuthUserSecureAccess> list = AuthUserSecureAccess.withCriteria {
	    maxResults max
	    firstResult offset
	    if (sort == 'username') {
                authUser {
		    order 'username', order
                }
            }
	    else if (sort == 'accessLevelName') {
                accessLevel {
		    order 'accessLevelName', order
                }
            }
	    else if (sort == 'displayName') {
                secureObject {
		    order 'displayName', order
                }
            }
            else {
		order sort, order
            }
        }
	[instances: list, totalCount: AuthUserSecureAccess.count()]
    }

    def show(AuthUserSecureAccess authUserSecureAccess) {
	if (authUserSecureAccess) {
	    [ausa: authUserSecureAccess]
        }
        else {
	    flash.message = "AuthUserSecureAccess not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def delete(AuthUserSecureAccess authUserSecureAccess) {
	if (authUserSecureAccess) {
            try {
		authUserSecureAccess.delete()
		flash.message = "AuthUserSecureAccess ${params.id} deleted"
		redirect action: 'list'
            }
	    catch (DataIntegrityViolationException ignored) {
		flash.message = "AuthUserSecureAccess ${params.id} could not be deleted"
		redirect action: 'show', id: params.id
            }
        }
        else {
	    flash.message = "AuthUserSecureAccess not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def edit(AuthUserSecureAccess authUserSecureAccess) {
	if (authUserSecureAccess) {
	    editModel ausa
        }
        else {
	    flash.message = "AuthUserSecureAccess not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def update(AuthUserSecureAccess authUserSecureAccess) {
	if (authUserSecureAccess) {
            if (params.version) {
		long version = params.long('version', 0)
		if (authUserSecureAccess.version > version) {
		    authUserSecureAccess.errors.rejectValue 'version',
			'authUserSecureAccess.optimistic.locking.failure',
			'Another user has updated this AuthUserSecureAccess while you were editing.'
		    render view: 'edit', model: editModel(authUserSecureAccess)
                    return
                }
            }
	    authUserSecureAccess.properties = params
	    if (!authUserSecureAccess.hasErrors() && authUserSecureAccess.save()) {
		flash.message = "AuthUserSecureAccess ${params.id} updated"
		redirect action: 'show', id: authUserSecureAccess.id
            }
            else {
		render view: 'edit', model: editModel(authUserSecureAccess)
            }
        }
        else {
	    flash.message = "AuthUserSecureAccess not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	createModel new AuthUserSecureAccess(params)
    }

    def save() {
	AuthUserSecureAccess authUserSecureAccess = new AuthUserSecureAccess(params)
	if (!authUserSecureAccess.hasErrors() && authUserSecureAccess.save()) {
	    flash.message = "AuthUserSecureAccess ${authUserSecureAccess.id} created"
	    redirect action: 'show', id: authUserSecureAccess.id
        }
        else {
	    render view: 'create', model: createModel(authUserSecureAccess)
        }
    }

    private boolean isAllowOwn(id) {
	AuthUser authUser = AuthUser.get(id)
        for (role in authUser.authorities) {
	    if (Roles.SPECTATOR.authority.equalsIgnoreCase(role.authority)) {
                return false
            }
        }
	true
    }

    private List<SecureAccessLevel> getAccessLevelList(id) {
	if (isAllowOwn(id)) {
	    SecureAccessLevel.listOrderByAccessLevelValue()
        }
        else {
	    SecureAccessLevel.executeQuery('''
					FROM SecureAccessLevel
					WHERE accessLevelName <> 'OWN'
					ORDER BY accessLevelValue''')
        }
    }

    def listAccessLevel() {
	render template: 'accessLevelList', model: [accessLevelList: getAccessLevelList(params.id)]
    }

    private Map createModel(AuthUserSecureAccess ausa) {
	[ausa: ausa,
	 authUsers: AuthUser.listOrderByUsername(),
	 secureAccessLevels: SecureAccessLevel.listOrderByAccessLevelValue(),
	 secureObjects: SecureObject.listOrderByDisplayName()]
    }

    private Map editModel(AuthUserSecureAccess ausa) {
	Map model = createModel(ausa)
	model.accessLevels = getAccessLevelList(ausa.authUserId)
	model
    }
}
