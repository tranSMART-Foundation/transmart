package fm

import grails.converters.JSON
import groovy.util.logging.Slf4j

@Slf4j('logger')
class UploadFilesController {
    
    UploadFilesService uploadFilesService

    def index = { }
    
    def displayUpload() {
	FmFolder parentFolder = FmFolder.get(params.folderId)
	logger.debug 'displayUpload template uploadFiles parentFolder "{}"', parentFolder
	render template: 'uploadFiles', model: [parentFolder: parentFolder]
    }

    def upload() {
	String msg = 'Loading failed'
	for (file in request.getFiles('qqfile')) {
            msg = uploadFilesService.upload(file, params.parentId)
	    logger.debug '{}: {}', file,  msg
	}
	logger.debug 'upload msg "{}"', msg

	Map result
	if (msg == 'File successfully loaded') {
	    result = [success: true, folderId: params.parentId, folderParentId: params.folderParentId]
	}
	else {
	    result = [success: false, error: msg]
        }
        render text: result as JSON, contentType: 'text/html'
    }
}
