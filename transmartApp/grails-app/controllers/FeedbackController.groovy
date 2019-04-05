import org.transmart.searchapp.Feedback

class FeedbackController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    def list() {
	if (!params.max) {
	    params.max = 10
	}
	[feedbackList: Feedback.list(params), feedbackCount: Feedback.count()]
    }

    def show(Feedback feedback) {
	if (feedback) {
	    [feedback: feedback]
        }
        else {
	    flash.message = "Feedback not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def delete(Feedback feedback) {
        if (feedback) {
            feedback.delete()
	    flash.message = "Feedback ${params.id} deleted"
        }
        else {
	    flash.message = "Feedback not found with id ${params.id}"
        }
	redirect action: 'list'
    }

    def edit(Feedback feedback) {
	if (feedback) {
	    [feedback: feedback]
        }
        else {
	    flash.message = "Feedback not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def update(Feedback feedback) {
        if (feedback) {
            feedback.properties = params
            if (!feedback.hasErrors() && feedback.save()) {
		flash.message = "Feedback ${params.id} updated"
		redirect action: 'show', id: feedback.id
            }
            else {
				render view: 'edit', model: [feedback: feedback]
            }
        }
        else {
	    flash.message = "Feedback not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	[feedback: new Feedback(params)]
    }

    def save() {
	Feedback feedback = new Feedback(params)
        if (!feedback.hasErrors() && feedback.save()) {
	    flash.message = "Feedback ${feedback.id} created"
	    redirect action: 'show', id: feedback.id
        }
        else {
	    render view: 'create', model: [feedback: feedback]
        }
    }

    def saveFeedback() {
	new Feedback(
	    searchUserId: 1,
	    createDate: new Date(),
	    appVersion: 'prototype',
	    feedbackText: params.feedbacktext).save()
	render template: 'emptyTemplate'
    }
}
