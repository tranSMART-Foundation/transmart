import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.transmart.searchapp.SecureObject
import org.transmart.searchapp.SecureObjectAccess

class SecureObjectController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Value('${com.recomdata.admin.paginate.max:0}')
    private int paginateMax

    def list() {
	params.max = Math.min(params.int('max', paginateMax), 100)
	[secureObjects: SecureObject.list(params), secureObjectCount: SecureObject.count()]
    }

    def show(SecureObject secureObject) {
	if (secureObject) {
	    [so: secureObject,
	     soas: SecureObjectAccess.findAllBySecureObject(secureObject, [sort: 'accessLevel'])]
        }
        else {
	    flash.message = "SecureObject not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def delete(SecureObject secureObject) {
	if (secureObject) {
            try {
		secureObject.delete()
		flash.message = "SecureObject ${params.id} deleted"
		redirect action: 'list'
            }
	    catch (DataIntegrityViolationException e) {
		flash.message = "SecureObject ${params.id} could not be deleted"
		redirect action: 'show', id: params.id
            }
        }
        else {
	    flash.message = "SecureObject not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def edit(SecureObject secureObject) {
	if (secureObject) {
	    [so: secureObject]
        }
        else {
	    flash.message = "SecureObject not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def update(SecureObject secureObject) {
	if (secureObject) {
            if (params.version) {
		long version = params.long('version', 0)
		if (secureObject.version > version) {
		    secureObject.errors.rejectValue 'version',
			'secureObject.optimistic.locking.failure',
			'Another user has updated this SecureObject while you were editing.'
		    render view: 'edit', model: [so: secureObject]
                    return
                }
            }

	    secureObject.properties = params
	    if (!secureObject.hasErrors() && secureObject.save()) {
		flash.message = "SecureObject ${params.id} updated"
		redirect action: 'show', id: secureObject.id
            }
            else {
		render view: 'edit', model: [so: secureObject]
            }
        }
        else {
	    flash.message = "SecureObject not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	[so: new SecureObject(params)]
    }

    def save() {
	SecureObject secureObject = new SecureObject(params)
	if (!secureObject.hasErrors() && secureObject.save()) {
	    flash.message = "SecureObject ${secureObject.id} created"
	    redirect action: 'show', id: secureObject.id
        }
        else {
	    render view: 'create', model: [so: secureObject]
        }
    }
}
