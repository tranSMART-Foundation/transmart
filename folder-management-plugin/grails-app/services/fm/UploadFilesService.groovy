package fm

import com.mongodb.MongoClient
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSInputFile
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.InputStreamBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.transmart.mongo.MongoUtils
import org.transmart.plugin.shared.UtilService

@Slf4j('logger')
class UploadFilesService {

    FmFolderService fmFolderService
    @Autowired private UtilService utilService

    @Value('${transmartproject.mongoFiles.enableMongo:false}')
    private boolean enableMongo

    @Value('${com.recomdata.FmFolderService.filestoreDirectory:}')
    private String filestoreDirectory

    @Value('${transmartproject.mongoFiles.apiKey:}')
    private String mongoApiKey

    @Value('${transmartproject.mongoFiles.apiURL:}')
    private String mongoApiUrl

    @Value('${transmartproject.mongoFiles.dbName:}')
    private String mongoDbName

    @Value('${transmartproject.mongoFiles.dbPort:0}')
    private int mongoPort

    @Value('${transmartproject.mongoFiles.dbServer:}')
    private String mongoServer

    @Value('${transmartproject.mongoFiles.useDriver:false}')
    private boolean useDriver

    @Transactional
    def upload(CommonsMultipartFile fileToUpload, String parentId){
	FmFile fmFile
        try{
	    String fileName = fileToUpload.originalFilename
	    String fileType = fileName.split('\\.', -1)[-1]
	    long fileSize = fileToUpload.size
            FmFolder fmFolder
            try {
                fmFolder = FmFolder.get(parentId)
		if (!fmFolder) {
		    logger.error 'Folder with id {} does not exist.', parentId
                    return 'Folder with id ' + parentId + ' does not exist.'
                }
            }
	    catch (NumberFormatException e) {
		logger.error 'Loading failed: {}', e.message
                return 'Loading failed'
            }

            // Check if folder already contains file with same name.
            fmFile = fmFolder.fmFiles.find { it.originalName == fileName }
            // If it does, then use existing file record and increment its version.
            // Otherwise, create a new file.
	    if (fmFile) {
                fmFile.fileVersion++
                fmFile.fileSize = fileSize
                fmFile.linkUrl = ''
		logger.debug 'File = {} ({}) - Existing', fileName, fmFile.id
            }
            else {
		fmFile = new FmFile(displayName: fileName, originalName: fileName, fileType: fileType,
				    fileSize: fileSize, filestoreLocation: '', filestoreName: '', linkUrl: '')
		if (!save(fmFile, 'File saving failed')) {
                    return 'Loading failed: fmfile saving'
                }
		fmFolder.addToFmFiles fmFile
		if (!save(fmFolder, 'Folder saving failed')) {
                    return 'Loading failed: fmfolder saving'
                }
            }
            fmFile.filestoreLocation = parentId
            fmFile.filestoreName = fmFile.id + '-' + fmFile.fileVersion + '.' + fmFile.fileType
	    if (!save(fmFile, 'File saving failed')) {
                return 'Loading failed: file saving'
            }
	    logger.debug 'File = {} ({}) - Stored', fmFile.filestoreName, fileName

	    if (!enableMongo) {
		File file = new File(filestoreDirectory, parentId + '/' + fmFile.filestoreName)
		logger.debug 'Writing to filestore file {}', file.path
		File filestoreDir = new File(filestoreDirectory, parentId)
                if (!filestoreDir.exists()) {
                    if (!filestoreDir.mkdirs()) {
			logger.error 'unable to create filestoredir {}', filestoreDir.path
                        return 'Loading failed: unable to create filestoredir'
                    }
                }
		OutputStream outputStream = new FileOutputStream(file)
		logger.debug 'Copying from fileToUpload {}', fileToUpload
		logger.debug 'Create outputStream {}', outputStream
		if (outputStream) {
		    byte[] fileBytes = new byte[1024]
                    int nread
                    InputStream istr = fileToUpload.getInputStream()
		    try {
			while ((nread = istr.read(fileBytes)) != -1) {
			    outputStream.write fileBytes, 0, nread
			}
                    } catch (Exception e) {
			throw e
                    } finally {
			istr.close()
			outputStream.close()
                    }
		    fmFolderService.indexFile fmFile
		    logger.debug 'File successfully loaded: {}', fmFile.id
                    return 'File successfully loaded'
                }
                else {
		    logger.error 'Unable to write to filestoreDirectory {}', filestoreDirectory
                    return 'Unable to write to filestoreDirectory'
                }
            }
            else {
		if (useDriver) {
		    MongoClient mongo = new MongoClient(mongoServer, mongoPort)
		    GridFSInputFile file = new GridFS(mongo.getDB(mongoDbName)).createFile(fileToUpload.inputStream, fmFile.filestoreName)
		    file.contentType = fileToUpload.contentType
                    file.save()
                    mongo.close()
		    fmFolderService.indexFile fmFile
		    logger.debug 'File successfully loaded: {}', fmFile.id
                    return 'File successfully loaded'
                }
                else {
		    new HTTPBuilder(mongoApiUrl + 'insert/' + fmFile.filestoreName).request(Method.POST) { request ->
			headers.'apikey' = MongoUtils.hash(mongoApiKey)
                        requestContentType: 'multipart/form-data'
                        MultipartEntity multiPartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
                        multiPartContent.addPart(fmFile.filestoreName,
                                 new InputStreamBody(fileToUpload.inputStream,
                                                     fileToUpload.contentType,
                                                     fileToUpload.originalFilename))

                        request.setEntity(multiPartContent)

                        response.success = { resp ->
                            if(resp.status < 400){
                                fmFolderService.indexFile(fmFile)
				logger.debug 'File successfully loaded: {}', fmFile.id
                                return 'File successfully loaded'
                            }
                        }

                        response.failure = { resp ->
			    logger.error 'Problem during connection to API: {}', resp.status
			    fmFile?.delete()
                            if(resp.status ==404){
				'Problem during connection to API'
                            }
			    else {
				'Loading failed'
                            }
			}
                    }
		}
            }
	}
	catch (e) {
	    logger.error 'transfer error: {}', e.message
	    fmFile?.delete()
        }
    }

    private boolean save(o, String message) {
	if (o.save(flush: true)) {
	    true
	}
	else {
	    logger.error '{}', utilService.errorStrings(o)
	    false
	}
    }
}
