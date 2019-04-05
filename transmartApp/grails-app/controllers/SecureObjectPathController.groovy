import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.transmart.searchapp.SecureObject
import org.transmart.searchapp.SecureObjectPath

class SecureObjectPathController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Value('${com.recomdata.admin.paginate.max:0}')
    private int paginateMax

    def list() {
	params.max = Math.min(params.int('max', paginateMax), 100)
	[sops: SecureObjectPath.list(params), sopCount: SecureObjectPath.count()]
    }

    def show(SecureObjectPath secureObjectPath) {
	if (secureObjectPath) {
	    [sop: secureObjectPath]
        }
        else {
	    flash.message = "SecureObjectPath not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def delete(SecureObjectPath secureObjectPath) {
	if (secureObjectPath) {
            try {
		secureObjectPath.delete()
		flash.message = "SecureObjectPath ${params.id} deleted"
		redirect action: 'list'
            }
	    catch (DataIntegrityViolationException e) {
		flash.message = "SecureObjectPath ${params.id} could not be deleted"
		redirect action: 'show', id: params.id
            }
        }
        else {
	    flash.message = "SecureObjectPath not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def edit(SecureObjectPath secureObjectPath) {
	if (secureObjectPath) {
	    createOrEditModel secureObjectPath
        }
        else {
	    flash.message = "SecureObjectPath not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def update(SecureObjectPath secureObjectPath) {
	if (secureObjectPath) {
            if (params.version) {
		long version = params.long('version', 0)
		if (secureObjectPath.version > version) {
		    secureObjectPath.errors.rejectValue 'version',
			'secureObjectPath.optimistic.locking.failure',
			'Another user has updated this SecureObjectPath while you were editing.'
		    render view: 'edit', createOrEditModel(secureObjectPath)
                    return
                }
            }

	    secureObjectPath.properties = params
	    if (!secureObjectPath.hasErrors() && secureObjectPath.save()) {
		flash.message = "SecureObjectPath ${params.id} updated"
		redirect action: 'show', id: secureObjectPath.id
            }
            else {
		render view: 'edit', model: createOrEditModel(secureObjectPath)
            }
        }
        else {
	    flash.message = "SecureObjectPath not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	createOrEditModel new SecureObjectPath(params)
    }

    def save() {
	SecureObjectPath secureObjectPath = new SecureObjectPath(params)
	if (!secureObjectPath.hasErrors() && secureObjectPath.save()) {
	    flash.message = "SecureObjectPath ${secureObjectPath.id} created"
	    redirect action: 'show', id: secureObjectPath.id
        }
        else {
	    render view: 'create', model: createOrEditModel(secureObjectPath)
        }
    }

    private Map createOrEditModel(SecureObjectPath sop) {
	[sop          : sop,
	 secureObjects: SecureObject.list()]
    }
}
