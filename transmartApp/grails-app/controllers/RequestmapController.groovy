import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.searchapp.Requestmap

class RequestmapController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired private SpringSecurityService springSecurityService

    @Value('${com.recomdata.admin.paginate.max:0}')
    private int paginateMax

    def list(Integer max) {
	if (!max) {
	    params.max = paginateMax
        }
	[requestmaps: Requestmap.list(params), requestmapCount: Requestmap.count()]
    }

    def show(Requestmap requestmap) {
	if (requestmap) {
	    [requestmap: requestmap]
	}
	else {
            flash.message = "Requestmap not found with id $params.id"
	    redirect action: 'list'
        }
    }

    def delete(Requestmap requestmap) {
        if (!requestmap) {
            flash.message = "Requestmap not found with id $params.id"
	    redirect action: 'list'
            return
        }

        requestmap.delete()

        springSecurityService.clearCachedRequestmaps()

        flash.message = "Requestmap $params.id deleted."
	redirect action: 'list'
    }

    def edit(Requestmap requestmap) {
	if (requestmap) {
	    [requestmap: requestmap]
	}
	else {
            flash.message = "Requestmap not found with id $params.id"
	    redirect action: 'list'
        }
    }

    def update(Requestmap requestmap) {
        if (!requestmap) {
            flash.message = "Requestmap not found with id $params.id"
	    redirect action: 'edit', id: params.id
            return
        }

	long version = params.long('version', 0)
        if (requestmap.version > version) {
	    requestmap.errors.rejectValue 'version', 'requestmap.optimistic.locking.failure',
		'Another user has updated this Requestmap while you were editing.'
            render view: 'edit', model: [requestmap: requestmap]
            return
        }

        requestmap.properties = params
        if (requestmap.save()) {
            springSecurityService.clearCachedRequestmaps()
	    redirect action: 'show', id: requestmap.id
	}
	else {
            render view: 'edit', model: [requestmap: requestmap]
        }
    }

    def create() {
        [requestmap: new Requestmap(params)]
    }

    def save() {
	Requestmap requestmap = new Requestmap(params)
        if (requestmap.save()) {
            springSecurityService.clearCachedRequestmaps()
	    redirect action: 'show', id: requestmap.id
	}
	else {
            render view: 'create', model: [requestmap: requestmap]
        }
    }
}
