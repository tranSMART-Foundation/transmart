import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role

class RoleController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired private SpringSecurityService springSecurityService

    @Value('${com.recomdata.search.paginate.max:0}')
    private int paginateMax

    def list(Integer max) {
	if (!max) {
	    params.max = paginateMax
        }
	[roles: Role.list(params), roleCount: Role.count()]
    }

    /**
     * Display the show authority page.
     */
    def show(Role authority, String sort, String order) {
        if (!authority) {
	    flash.message = "Role not found with id $params.id"
            redirect action: 'list'
            return
        }

	List<AuthUser> people = [] + authority.people
	if (sort) {
	    people.sort { AuthUser o1, AuthUser o2 ->
		peopleFieldSelector(sort, o1) <=> peopleFieldSelector(sort, o2)
            }
	    if (order != 'asc') {
		people.reverse true
            }
        }
        else {
	    people.sort { it.id }
        }

        [authority: authority, sortedPeople: people]
    }

    private peopleFieldSelector(String fieldName, AuthUser authUser) {
	switch (fieldName) {
	    case 'username':     return authUser.username
	    case 'userRealName': return authUser.userRealName
	    case 'enabled':      return authUser.enabled
	    case 'description':  return authUser.description
	    default:             return authUser.id
        }
    }

    def delete(Role authority) {
        if (!authority) {
	    flash.message = "Role not found with id $params.id"
            redirect action: 'list'
            return
        }

	springSecurityService.deleteRole authority

	flash.message = "Role $params.id deleted."
        redirect action: 'list'
    }

    def edit(Role authority) {
	if (authority) {
	    [authority: authority]
	}
	else {
	    flash.message = "Role not found with id $params.id"
            redirect action: 'list'
        }
    }

    def update(Role authority) {
        if (!authority) {
	    flash.message = "Role not found with id $params.id"
            redirect action: 'edit', id: params.id
            return
        }

	long version = params.long('version', 0)
        if (authority.version > version) {
            authority.errors.rejectValue 'version', 'authority.optimistic.locking.failure',
                'Another user has updated this Role while you were editing.'
            render view: 'edit', model: [authority: authority]
            return
        }

        if (springSecurityService.updateRole(authority, params)) {
            springSecurityService.clearCachedRequestmaps()
            redirect action: 'show', id: authority.id
        }
        else {
            render view: 'edit', model: [authority: authority]
        }
    }

    def create() {
        [authority: new Role()]
    }

    def save(String authority, String description) {
	Role role = new Role(params)

	if (!authority || !description) {
	    if (!authority) {
		flash.message = 'Please enter a role name'
            }
	    else {
		flash.message = 'Please enter a role description'
	    }
	    role.authority = authority
	    role.description = description
	    render view: 'create', model: [authority: role]
	    return
        }

        if (role.save()) {
            redirect action: 'show', id: role.id
        }
        else {
            render view: 'create', model: [authority: role]
        }
    }
}
