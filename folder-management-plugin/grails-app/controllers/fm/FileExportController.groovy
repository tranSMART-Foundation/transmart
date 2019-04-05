package fm

import annotation.AmTagAssociation
import annotation.AmTagItemService
import annotation.AmTagTemplate
import annotation.AmTagValue
import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.springframework.beans.factory.annotation.Value
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.BioData
import org.transmart.biomart.ConceptCode
import org.transmart.biomart.Experiment
import org.transmart.mongo.MongoUtils
import org.transmart.searchapp.SearchKeyword

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Slf4j('logger')
class FileExportController {

    FmFolderService fmFolderService
    AmTagItemService amTagItemService

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

    def add(String id) {
	List<String> exportList = sessionExportList()
	for (i in id.split(',')) {
	    if (i && !exportList.contains(i)) {
		exportList << i
            }
	}

	render exportList.size().toString()
    }

    def remove(String id) {
	List<String> exportList = sessionExportList()
	for (i in id.split(',')) {
	    if (i && exportList.contains(i)) {
		exportList.remove i
            }
        }

	render exportList.size().toString()
    }

    def view() {

	List<String> exportList = sessionExportList()
	List<Map> files = []
        for (id in exportList) {
            FmFile f = FmFile.get(id)
            if (f) {
		files << [id: f.id, fileType: f.fileType, displayName: f.displayName, folder: fmFolderService.getPath(f.folder)]
            }
        }

	files.sort { Map a, Map b ->
	    if (a.folder != b.folder) {
		a.folder.compareTo b.folder
            }
	    else {
		a.displayName.compareTo b.displayName
            }
	}

	render template: 'export', model: [files: files]
    }

    def export(String id) {

	List<String> errorResponse = []
	Set<String> metadataExported = []
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream()
	    ZipOutputStream zipStream = new ZipOutputStream(baos)

	    Map<String, List<FmFile>> manifestMap = [:]

	    //Final export list comes from selected checkboxes
	    for (f in id.split(',')) {
                FmFile fmFile = FmFile.get(f)
		File file = new File(filestoreDirectory, fmFile.filestoreLocation + '/' + fmFile.filestoreName)

                //Construct a file name out of the display name + suffix, if needed
		String exportName = fmFile.displayName
                if (!exportName.endsWith('.' + fmFile.fileType)) {
                    exportName += '.' + fmFile.fileType
                }

                String dirName = fmFolderService.getPath(fmFile.folder, true)
                if (dirName.startsWith('/') || dirName.startsWith('\\')) {
                    dirName = dirName.substring(1)
                }

                //Lose the first separator character, this would cause a blank folder name in the zip
		zipStream.putNextEntry new ZipEntry(dirName + '/' + fmFolderService.safeFileName(exportName))
		if (!enableMongo) {
                    if (file.exists()) {
                        file.withInputStream({ is -> zipStream << is })
                    }
                    else {
			String errorMessage = 'File not found for export: ' + file.path
                        logger.error errorMessage
			errorResponse << errorMessage
                    }
                }
                else{
		    if (useDriver) {
			MongoClient mongo = new MongoClient(mongoServer, mongoPort)
			GridFSDBFile gfsFile = new GridFS(mongo.getDB(mongoDbName)).findOne(fmFile.filestoreName)
			if (!gfsFile) {
			    String errorMessage = 'File not found for export: ' + file.path
                            logger.error errorMessage
			    errorResponse << errorMessage
                        }
                        else{
			    zipStream << gfsFile.inputStream
                        }
                        mongo.close()
                    }
                    else{
			new HTTPBuilder(mongoApiUrl + fmFile.filestoreName + '/fsfile').request(Method.GET, ContentType.BINARY) { req ->
			    headers.'apikey' = MongoUtils.hash(mongoApiKey)
                            response.success = { resp, binary ->
                                assert resp.statusLine.statusCode == 200
				InputStream inputStream = binary
                                byte[] dataBlock = new byte[1024]
                                int count = inputStream.read(dataBlock, 0, 1024)
                                while (count != -1) {
				    zipStream.write dataBlock, 0, count
                                    count = inputStream.read(dataBlock, 0, 1024)
                                }
                            }
                            response.failure = { resp ->
				logger.error 'Problem during connection to API: {}', resp.status
				render 'Error writing ZIP: File not found'
                            }
                        }
                    }
                }
                zipStream.closeEntry()

		List<FmFile> manifestList = manifestMap[dirName]
		if (manifestList == null) {
		    manifestList = []
		    manifestMap[dirName] = manifestList
                }
		manifestList << fmFile

                //for each folder of the hierarchy of the file path, add file with metadata
		String path = fmFile.folder.folderFullName
		if (metadataExported.add(path)) {
		    exportMetadata path, zipStream
		}
            }

            //Now for each item in the manifest map, create a manifest file and add it to the ZIP.
	    manifestMap.each { String key, List<FmFile> manifestList ->
		zipStream.putNextEntry new ZipEntry(key + '/manifest.txt')
		zipStream.write String.format('%60s%5s%15s\n', 'File Name', 'Type', 'Size').bytes
		zipStream.write '--------------------------------------------------------------------------------\n'.bytes
                for (fmFileIt in manifestList) {
		    zipStream.write String.format('%60s%5s%15d\n', fmFileIt.displayName, fmFileIt.fileType, fmFileIt.fileSize).bytes
                }
                zipStream.closeEntry()
            }
            zipStream.flush()
            zipStream.close()

	    header 'Content-disposition', 'attachment; filename=export.zip'
            response.contentType = 'application/zip'
            response.outputStream << baos.toByteArray()
            response.outputStream.flush()
        }
	catch (e) {
	    logger.error 'Error writing ZIP', e
	    render errorResponse.join('\n') + '\nError writing ZIP: ' + e.message
        }
        catch (OutOfMemoryError oe) {
	    logger.error 'Files too large to be exported: ' + id
	    render 'Error: Files too large to be exported.\n' +
		'Please click on the "Previous" button on your web browser to go back to tranSMART.'
        }
    }

    //add in a zip a file containing metadata for a given folder
    private void exportMetadata(String path, ZipOutputStream zipStream) {
        try {
            //create path for the metadata file
	    String dirName = ''
            for (folderFullId in path.split('\\\\', -1)) {
		if (folderFullId) {
		    if (dirName) {
			dirName += '/'
		    }
		    String folderId = folderFullId.split(':', 2)[1]
                    dirName += fmFolderService.safeFileName((FmFolder.get(folderId)).folderName)
                }
            }
            if (dirName.startsWith('/') || dirName.startsWith('\\')) {
                dirName = dirName.substring(1)
	    }

	    zipStream.putNextEntry new ZipEntry(dirName + '/metadata.txt')
            for (folderFullId in path.split('\\\\', -1)) {
		if (folderFullId) {
		    String folderId = folderFullId.split(':', 2)[1]
		    FmFolder folder = FmFolder.get(folderId)

		    def metaDataTagItems = amTagItemService.getDisplayItems(AmTagTemplate.findByTagTemplateType(folder.folderType).id)

                    zipStream.write((folder.folderType + ': ' + folder.folderName + '\r\n').getBytes())
                    zipStream.write(('Description: ' + (folder.description).replace('\n', ' ') + '\r\n').getBytes())

                    //get associated bioDataObject
                    def bioDataObject
                    def folderAssociation = FmFolderAssociation.findByFmFolder(folder)
                    if (folderAssociation) {
                        bioDataObject = folderAssociation.getBioObject()
                    }
                    if (!bioDataObject) {
                        bioDataObject = folder
                    }

                    for (amTagItem in metaDataTagItems) {
                        if (amTagItem.tagItemType == 'FIXED') {
                            if (amTagItem.tagItemAttr != null ? bioDataObject?.hasProperty(amTagItem.tagItemAttr) : false) {
				def values = ""
                                def value = fieldValue(bean: bioDataObject, field: amTagItem.tagItemAttr)
                                for (v in (value.split('\\|', -1))) {
                                    def bioData = BioData.findByUniqueId(v)
                                    if (bioData != null) {
                                        def concept = ConceptCode.findById(bioData.id)
                                        if (concept != null) {
					    if (values) {
						values += '; '
					    }
                                            values += concept.codeName
                                        }
                                    }
                                }
				if (!values && value) {
				    values = value
				}
                                zipStream.write((amTagItem.displayName + ': ' + values + '\r\n').getBytes())
                            }
                        }
                        else if (amTagItem.tagItemType == 'CUSTOM') {
                            if (amTagItem.tagItemSubtype == 'FREETEXT') {
				def value = ""
                                def tagAssoc = AmTagAssociation.find('from AmTagAssociation where subjectUid=? and tagItemId=?', ['FOL:' + folderId, amTagItem.id])
                                if (tagAssoc != null) {
                                    if ((tagAssoc.objectUid).split('TAG:', 2).size() > 0) {
                                        def tagValue = AmTagValue.findById((tagAssoc.objectUid).split('TAG:', 2)[1])
					if (tagValue != null) {
					    value = tagValue.value
					}
                                    }
                                }
                                zipStream.write((amTagItem.displayName + ': ' + value + '\r\n').getBytes())
                            }
                            else if (amTagItem.tagItemSubtype == 'PICKLIST') {
				def value = ""
                                def tagAssoc = AmTagAssociation.find('from AmTagAssociation where subjectUid=? and tagItemId=?', ['FOL:' + folderId, amTagItem.id])
                                if (tagAssoc != null) {
                                    def valueUId = tagAssoc.objectUid
                                    def bioData = BioData.findByUniqueId(valueUId)
                                    if (bioData != null) {
                                        def concept = ConceptCode.findById(bioData.id)
                                        if (concept != null) {
                                            value = concept.codeName
                                        }
                                    }
                                }
                                zipStream.write((amTagItem.displayName + ': ' + value + '\r\n').getBytes())
                            }
                            else if (amTagItem.tagItemSubtype == 'MULTIPICKLIST') {
				def values = ""
                                def tagAssocs = AmTagAssociation.findAll('from AmTagAssociation where subjectUid=? and tagItemId=?', ['FOL:' + folderId, amTagItem.id])
                                for (tagAssoc in tagAssocs) {
                                    def valueUId = tagAssoc.objectUid
                                    def bioData = BioData.findByUniqueId(valueUId)
                                    if (bioData != null) {
                                        def concept = ConceptCode.findById(bioData.id)
                                        if (concept != null) {
					    if (values) {
						values += '; '
					    }
                                            values += concept.codeName
                                        }
                                    }
                                }
                                zipStream.write((amTagItem.displayName + ': ' + values + '\r\n').getBytes())
                            }

                        }
                        else if (amTagItem.tagItemType == 'BIO_ASSAY_PLATFORM') {
			    def values = ""
                            def tagAssocs = AmTagAssociation.findAll('from AmTagAssociation where subjectUid=? and objectType=?', ['FOL:' + folderId, amTagItem.tagItemType])
                            for (tagAssoc in tagAssocs) {
                                def tagValue = (tagAssoc.objectUid).split(':', 2)[1]
                                def bap = BioAssayPlatform.findByAccession(tagValue)
                                if (bap != null) {
				    if (values) {
					values += '; '
				    }
                                    values += bap.platformType + '/' + bap.platformTechnology + '/' + bap.vendor + '/' + bap.name
                                }
                            }
                            zipStream.write((amTagItem.displayName + ': ' + values + '\r\n').getBytes())
			}
			else {//bio_disease, bio_coumpound...
			    def values = ""
                            def tagAssocs = AmTagAssociation.findAll('from AmTagAssociation where subjectUid=? and objectType=?', ['FOL:' + folderId, amTagItem.tagItemType])
                            for (tagAssoc in tagAssocs) {
                                def key = SearchKeyword.findByUniqueId(tagAssoc.objectUid)
                                if (key != null) {
				    if (values) {
					values += '; '
				    }
                                    values += key.keyword
                                }
                                else {
                                    def bioData = BioData.findByUniqueId(tagAssoc.objectUid)
                                    if (bioData != null) {
                                        def concept = ConceptCode.findById(bioData.id)
                                        if (concept != null) {
					    if (values) {
						values += '; '
					    }
                                            values += concept.codeName
                                        }
                                    }
                                }
                            }
                            zipStream.write((amTagItem.displayName + ': ' + values + '\r\n').getBytes())
                        }
                    }
                    zipStream.write(('\r\n').getBytes())
                }
            }
            zipStream.closeEntry()
        }
	catch (e) {
	    logger.error 'Error writing ZIP', e
        }
    }

    def exportStudyFiles() {
	List<Long> ids = []
	FmFolder folder = fmFolderService.getFolderByBioDataObject(Experiment.findByAccession(params.accession))

	for (FmFile file in folder.fmFiles) {
            if (file.activeInd) {
		ids << file.id
            }
        }
	redirect action: 'export', params: [id: ids.join(',')]
    }

    def exportFile(FmFile fmFile) {
	File file = new File(filestoreDirectory, fmFile.filestoreLocation + '/' + fmFile.filestoreName)
        if (file.exists()) {
	    String exportName = fmFile.displayName
            if (!exportName.endsWith('.' + fmFile.fileType)) {
                exportName += '.' + fmFile.fileType
            }

            if (!params.open) {
		header 'Content-disposition', 'attachment; filename=' + exportName
            }
	    header 'Content-Type', URLConnection.guessContentTypeFromName(file.name)
            file.withInputStream({ is -> response.outputStream << is })
            response.outputStream.flush()
        }
        else {
	    render status: 500, text: 'This file (' + file.path + ') was not found in the repository.'
	}
    }

    private List<String> sessionExportList() {
	List<String> exportList = session['foldermanagement.exportlist']
	if (exportList == null) {
	    exportList = []
	    session['foldermanagement.exportlist'] = exportList
        }
	exportList
    }
}
