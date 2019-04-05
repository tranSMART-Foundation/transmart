package fm

class FmFileController {

    def formLayoutService

    def show(FmFile file) {
        if (!file) {
	    render 'File with ID ' + params.id + ' not found!'
            return
        }

	render template: 'show', model: [file: file, layout: formLayoutService.getLayout('file')]
    }
}
