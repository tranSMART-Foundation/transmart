package fm

import annotation.AmData
import annotation.AmTagAssociation
import annotation.AmTagItem
import annotation.AmTagTemplate
import annotation.AmTagTemplateAssociation
import annotation.AmTagValue
import com.mongodb.MongoClient
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.recomdata.util.FolderType
import grails.transaction.Transactional
import grails.validation.ValidationException

import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.solr.util.SimplePostTool
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.biomart.BioData
import org.transmart.biomart.Experiment
import org.transmart.mongo.MongoUtils
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.SecureObject
import org.transmart.searchapp.SecureObjectAccess
import org.transmartproject.db.log.AccessLogService
import org.transmartproject.db.ontology.I2b2Secure

@Slf4j('logger')
class FmFolderService {

    private static final String DEFAULT_FILE_TYPES =
        'xml,json,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log'

    private static final String filenameBlacklist = '\\/:;*?"<>|'

    @Value('${transmartproject.mongoFiles.apiURL:}')
    private String apiUrl

    @Value('${transmartproject.mongoFiles.apiKey:}')
    private String apiKey

    @Value('${transmartproject.mongoFiles.enableMongo:false}')
    private boolean enableMongo

    @Value('${com.recomdata.FmFolderService.importDirectory:}')
    private String importDirectory

    @Value('${com.recomdata.FmFolderService.filestoreDirectory:}')
    private String filestoreDirectory

    @Value('${com.recomdata.FmFolderService.fileTypes:}')
    private String fileTypes

    @Value('${transmartproject.mongoFiles.dbServer:}')
    private String mongoServer

    @Value('${transmartproject.mongoFiles.dbPort:0}')
    private int mongoPort

    @Value('${transmartproject.mongoFiles.dbName:}')
    private String mongoDbName

    @Value('${com.recomdata.solr.baseURL:}')
    private String solrBaseUrl

    @Value('${transmartproject.mongoFiles.useDriver:false}')
    private boolean useDriver

    AccessLogService accessLogService
    def i2b2HelperService
    @Autowired private SecurityService securityService
    @Autowired private UtilService utilService

    private String getSolrUrl() {
        solrBaseUrl + '/update'
    }

    /**
     * Imports files processing them into filestore and indexing them with SOLR.
     */
    @Transactional
    void importFiles() {

        logger.info 'importFiles() called'

	logger.debug 'Importing files from {} into {}', importDirectory, filestoreDirectory

        if (!importDirectory || !filestoreDirectory || !solrUrl) {
            if (!importDirectory) {
		logger.error 'Unable to check for new files. com.recomdata.FmFolderService.importDirectory ' +
		    'setting has not been defined in the Config.groovy file'
            }
            if (!filestoreDirectory) {
		logger.error 'Unable to check for new files. com.recomdata.FmFolderService.filestoreDirectory ' +
                    'setting has not been defined in the Config.groovy file'
            }
            if (!solrUrl) {
		logger.error 'Unable to check for new files. com.recomdata.solr.baseURL ' +
                    'setting has not been defined in the Config.groovy file'
            }
            return
        }

        processDirectory new File(importDirectory)

        logger.debug 'Finished importing files'
    }

    /**
     * Re-index all files through SOLR.
     */
    void reindexFiles() {
	for (FmFile fmFile in FmFile.list()) {
	    indexFile fmFile
        }
    }

    /**
     * Process files and sub-directories in specified directory.
     */
    @Transactional
    def processDirectory(File directory) {
        def fmFolder = null
        /* null: uninitialized; false: not a folder */

        /* the lazy initialization is to avoid loading the fmFolder if there are
         * actually no files under the directory being processed */
        def getFmFolder = { ->
            if (fmFolder != null) {
                return fmFolder
            }

            long folderId
            try {
                folderId = Long.parseLong directory.name
            }
	    catch (NumberFormatException ignored) {
                fmFolder = false
                return fmFolder
            }

	    fmFolder = FmFolder.get(folderId)
	    if (!fmFolder) {
		logger.error 'Folder with id {} does not exist (reference in directory {}', folderId, directory
                fmFolder = false
            }

            fmFolder
        }

	logger.debug 'Looking for data in {}', directory

	for (File file in directory.listFiles()) {
            if (file.isDirectory()) {
		processDirectory file
            }
            else if (file.name != '.keep') {
                if (getFmFolder()) {
                    processFile getFmFolder(), file
                }
                else {
		    logger.warn 'Ignoring file {} because its parent directory {} could not be matched to a folder in tranSMART', file, directory
                }
            }
        }

	if (directory != new File(importDirectory) /* not import root */ && !directory.list().length) {
            if (!directory.delete()) {
		logger.warn 'Could not delete presumably empty directory {}', directory
            }
            else {
		logger.debug 'Deleted empty directory {}', directory
            }
        }
    }

    /**
     * Processes a file into the filestore associating it with a folder and indexes file using SOLR
     *
     * @param file file to be proceessed
     */
    @Transactional
    void processFile(FmFolder fmFolder, File file, String customName = null, String description = null) {
	logger.info 'Importing file {} into folder {}', file, fmFolder

        // Check if folder already contains file with same name.
	FmFile fmFile
        for (f in fmFolder.fmFiles) {
	    if (f.originalName == file.name) {
                fmFile = f
                break
            }
        }

	// If it does, then use existing file record and increment its version. Otherwise, create a new file.
	if (fmFile) {
            fmFile.fileVersion++
            fmFile.fileSize = file.length()
            fmFile.linkUrl = ''
	    logger.debug 'File = {} ({}) - Existing', file.name, fmFile.id
        }
        else {
            fmFile = new FmFile(
		displayName: file.name,
		originalName: file.name,
                fileType: getFileType(file),
                fileSize: file.length(),
                filestoreLocation: '',
                filestoreName: '',
                linkUrl: '',
                fileDescription: description
            )
	    if (!save(fmFile)) {
                return
            }

            fmFile.filestoreLocation = getFilestoreLocation(fmFolder)
	    fmFolder.addToFmFiles fmFile
	    if (!save(fmFolder)) {
                return
            }

	    logger.info 'File = {}  ({}) - New', file.name, fmFile.id
        }

        fmFile.filestoreName = fmFile.id + '-' + fmFile.fileVersion + '.' + fmFile.fileType
	if (!save(fmFile)) {
            return
        }

        // Use filestore directory based on file's parent study or common directory
        // for files in folders above studies. If directory does not exist, then create it.
        // PREREQUISITE: Service account running tomcat has ownership of filestore directory.
	File filestoreDir = new File(filestoreDirectory, fmFile.filestoreLocation)
        if (!filestoreDir.exists()) {
            if (!filestoreDir.mkdirs()) {
		logger.error 'unable to create filestore {}', filestoreDir.path
                return
            }
        }

        // Move file to appropriate filestore directory.
	File filestoreFile = new File(filestoreDirectory, fmFile.filestoreLocation + '/' + fmFile.filestoreName)
        try {
	    FileUtils.copyFile file, filestoreFile
            if (!file.delete()) {
		logger.error 'unable to delete file {}', file.path
            }
        }
	catch (IOException ignored) {
	    logger.error 'unable to copy file to {}', filestoreFile.path
            return
        }

	logger.info 'Moved file to {}', filestoreFile.path

	indexFile fmFile
    }

    /**
     * Gets type (extension) of specified file or an empty string if it cannot be determined
     */
    private String getFileType(File file) {
	FilenameUtils.getExtension file.name
    }

    /**
     * Gets filestore location for specified folder. Files are stored in directories
     * grouped by their parent study folder id. If the files are being loaded at
     * the program level, then a default folder, '0' will be used.
     */
    private String getFilestoreLocation(FmFolder fmFolder) {

        String filestoreLocation

        if (fmFolder.folderLevel == 0) {
            filestoreLocation = '0'
        }
        else if (fmFolder.folderLevel == 1) {
            filestoreLocation = fmFolder.id
        }
        else {
	    logger.debug 'folderFullName = {}', fmFolder.folderFullName
            int pos = fmFolder.folderFullName.indexOf('\\', 1)
            pos = fmFolder.folderFullName.indexOf('\\', pos + 1)
	    logger.debug 'find name = {}', fmFolder.folderFullName.substring(0, pos)
            FmFolder fmParentFolder = FmFolder.findByFolderFullName(fmFolder.folderFullName.substring(0, pos + 1))
	    if (!fmParentFolder) {
		logger.error 'Unable to find folder with folderFullName of {}', fmFolder.folderFullName.substring(0, pos + 1)
                filestoreLocation = '0'
            }
            else {
                filestoreLocation = fmParentFolder.id
            }
        }

	File.separator + filestoreLocation
    }

    /**
     * Indexes file using SOLR.
     * @param fmFile file to be indexed
     */
    private void indexFile(FmFile fmFile) {

        try {
	    // Create the file entry first - the POST will handle the content.
	    String xmlString = "<add><doc><field name='id'>" + fmFile.uniqueId +
		"</field><field name='folder'>" + fmFile.folder.uniqueId +
		"</field><field name='name'>" + fmFile.originalName + '</field></doc></add>'
	    URL updateUrl = new URL(solrUrl + '?stream.body=' + URLEncoder.encode(xmlString, 'UTF-8') + '&commit=true')
            HttpURLConnection urlc = (HttpURLConnection) updateUrl.openConnection()
	    if (HttpURLConnection.HTTP_OK != urlc.responseCode) {
		logger.warn 'The SOLR service returned an error #{} {} for url {}',
		    urlc.responseCode, urlc.responseMessage, updateUrl
            }
            else {
		logger.debug 'Pre-created record for {}', fmFile.uniqueId
            }

	    // POST the file - if it has readable content, the contents will be indexed.
	    StringBuilder url = new StringBuilder(solrUrl)
	    if (enableMongo) {
		url << '/extract'
	    }
            // Use the file's unique ID as the document ID in SOLR
	    url << '?literal.id=' << URLEncoder.encode(fmFile.uniqueId, 'UTF-8')

            // Use the file's parent folder's unique ID as the folder_uid in SOLR
	    if (fmFile.folder) {
		url << '&literal.folder=' << URLEncoder.encode(fmFile.folder.uniqueId, 'UTF-8')
            }

            // Use the file's name as document name is SOLR
	    url << '&literal.name=' << URLEncoder.encode(fmFile.originalName, 'UTF-8')

	    if (!enableMongo) {
                // Get path to actual file in filestore.
                String[] args = [filestoreDirectory + File.separator + fmFile.filestoreLocation + File.separator + fmFile.filestoreName] as String[]

		new SimplePostTool(SimplePostTool.DATA_MODE_FILES, new URL(url.toString()), true,
				   null, 0, 0, fileTypes ?: DEFAULT_FILE_TYPES,
				   System.out, true, true, args).execute()
            }
            else {
		if (useDriver) {
		    MongoClient mongo = new MongoClient(mongoServer, mongoPort)
		    GridFSDBFile gfsFile = new GridFS(mongo.getDB(mongoDbName)).findOne(fmFile.filestoreName)
		    new HTTPBuilder(url).request(Method.POST) { request ->
                        requestContentType: 'multipart/form-data'
                        MultipartEntity multiPartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
			multiPartContent.addPart(fmFile.filestoreName,
				new InputStreamBody(gfsFile.inputStream, 'application/octet-stream', fmFile.originalName))
                        request.setEntity(multiPartContent)
                        response.success = { resp ->
			    logger.info 'File successfully indexed: {}', fmFile.id
                        }
                        response.failure = { resp ->
			    logger.error 'Problem to index file {} : {}', fmFile.id, resp.status
                        }
                    }
                    mongo.close()
                }
                else {
		    url << '&commit=true'
		    new HTTPBuilder(apiUrl + fmFile.filestoreName + '/fsfile').request(Method.GET, ContentType.BINARY) { req ->
                        headers.'apikey' = MongoUtils.hash(apiKey)
                        response.success = { resp, binary ->
                            assert resp.statusLine.statusCode == 200
			    InputStream inputStream = binary

			    new HTTPBuilder(url).request(Method.POST) { request ->
                                requestContentType: 'multipart/form-data'
                                MultipartEntity multiPartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
				multiPartContent.addPart(fmFile.filestoreName,
					new InputStreamBody(inputStream, 'application/octet-stream', fmFile.originalName))
                                request.setEntity(multiPartContent)
                                response.success = { resp2 ->
				    logger.info 'File successfully indexed: {}', fmFile.id
                                }

                                response.failure = { resp2 ->
				    logger.error 'Problem to index file {}: {}', fmFile.id, resp.status
                                }
                            }
                        }
                        response.failure = { resp ->
			    logger.error 'Problem during connection to API: {}', resp.status
                        }
                    }
                }
            }
        }
	catch (e) {
	    logger.error 'Exception while indexing fmFile with id of {}', fmFile.id, e
        }
    }

    /**
     * Removes files/folders by UID from the SOLR index.
     * @param uid UID of file or folder to remove
     */
    void removeSolrEntry(String uid) {
        try {
	    String xmlString = URLEncoder.encode('<delete><query>id:"' + uid + '"</query></delete>', 'UTF-8')
	    URL updateUrl = new URL(solrUrl + '?stream.body=' + xmlString + '&commit=true')
            HttpURLConnection urlc = (HttpURLConnection) updateUrl.openConnection()
	    if (HttpURLConnection.HTTP_OK != urlc.responseCode) {
		logger.warn 'The SOLR service returned an error #{} {} for url {}',
		    updateUrl, urlc.responseCode, urlc.responseMessage
            }
        }
	catch (e) {
	    logger.error 'Exception while deleting entry with uid of {}', uid, e
        }
    }

    private void deleteFromAmApp(String uniqueId) {
        AmTagTemplateAssociation.executeUpdate('delete AmTagTemplateAssociation where objectUid = ?', [uniqueId])
        AmTagAssociation.executeUpdate('delete AmTagAssociation where subjectUid = ?', [uniqueId])
        AmData.executeUpdate('delete AmData where uniqueId not in (select ata.objectUid from AmTagAssociation ata)')
        AmTagValue.executeUpdate('delete AmTagValue where id not in (select ad.id from AmData ad)')
    }

    private void deleteFromFmApp(FmFolder fmFolder) {
        //Delete information
	FmFolderAssociation.executeUpdate('Delete FmFolderAssociation where fmFolder = ?', [fmFolder])
	FmFolder.executeUpdate('delete FmFolder where id = ? ', [fmFolder.id])
        FmData.executeUpdate('delete FmData fd where not exists (Select fm from FmFolder fm where fm.id = fd.id)')
    }

    @Transactional
    void deleteStudy(FmFolder study) {
	deleteFolder study
        //Delete child elements
	for (FmFolder child in study.children) {
	    deleteFolder child
	    deleteFromAmApp child.uniqueId
	    deleteFromFmApp child
        }

        //Check bio_experiment
	Experiment experiment = FmFolderAssociation.findByFmFolder(study)?.bioObject
        if (experiment) {
	    if (!I2b2Secure.findBySecureObjectToken(FmFolderAssociation.findByFmFolder(study).objectUid)) {
                Experiment.executeUpdate('DELETE Experiment where id = ?', [experiment.id])
                BioData.executeUpdate('Delete BioData where id = ?', [experiment.id])
		SecureObject secureObject = SecureObject.findByBioDataId(experiment.id)
                if (secureObject) {
                    SecureObjectAccess.executeUpdate('Delete SecureObjectAccess where secureObject = ?', [secureObject])
                    secureObject.delete(flush: true)
                }
            }
        }

        //Delete study
	deleteFromAmApp study.uniqueId
	deleteFromFmApp study
    }

    @Transactional
    void deleteProgram(FmFolder program) {
	deleteFolder program
	for (FmFolder child in program.children) {
	    if (child.folderType == 'STUDY') {
		deleteStudy child
            }
	    else if (child.folderType == 'FOLDER') {
		deleteFolder child
            }
        }

        //Delete program
	deleteFromAmApp program.uniqueId
	deleteFromFmApp program
    }

    @Transactional
    void deleteFolder(FmFolder folder) {
        //Delete all files within this folder.
	//Convert PersistentSets to list to avoid concurrent modification
	for (file in (folder.fmFiles as List)) {
	    deleteFile file
        }

	for (child in (folder.children as List)) {
	    deleteFolder child
	}

        folder.activeInd = false
	removeSolrEntry folder.uniqueId

        if (!folder.save(flush: true)) {
	    logger.error 'Unable to delete folder with uid of {}', folder.uniqueId
        }
        else {
	    accessLogService.report 'Browse-Delete object',
		folder.folderType + ': ' + folder.folderName + ' (' + folder.uniqueId + ')'
        }
    }

    File getFile(FmFile file) {
	new File(new File(filestoreDirectory, file.filestoreLocation), file.filestoreName)
    }

    @Transactional
    boolean deleteFile(FmFile file) {
	boolean deleted = false
        try {
	    if (enableMongo) {
		if (useDriver) {
		    MongoClient mongo = new MongoClient(mongoServer, mongoPort)
		    new GridFS(mongo.getDB(mongoDbName)).remove file.filestoreName
                    mongo.close()
                    deleted = true
                }
                else {
		    new HTTPBuilder(apiUrl + file.filestoreName + '/delete').request(Method.GET) { req ->
                        headers.'apikey' = MongoUtils.hash(apiKey)
			headers.'User-Agent' = 'Mozilla/5.0 Firefox/3.0.4'
                        response.success = { resp ->
                            if (resp.statusLine.statusCode == 200) {
				logger.info 'File deleted: {}', file.filestoreName
                                deleted = true
                            }
                            else {
				logger.error 'Error when deleting file: {}', file.filestoreName
                            }
                        }

                        response.failure = { resp ->
			    logger.error 'Error when deleting file: {}', resp.status
                        }
                    }
                }
            }
            else {
                File filestoreFile = getFile(file)
                if (filestoreFile.exists()) {
                    filestoreFile.delete()
                }
                deleted = true
            }

            if (deleted) {
		removeSolrEntry file.uniqueId
		FmData.get(file.id)?.delete(flush: true)
		file.folder.fmFiles.remove file
                file.folder.save(flush: true)
		accessLogService.report 'Browse-Delete file', file.displayName + ' (' + file.uniqueId + ')'
                return true
            }
        }
	catch (e) {
	    logger.error 'Exception while deleting file with uid of {}', file.uniqueId, e
        }
    }

    List<FmFolder> getFolderContents(id) {
	FmFolder parent
	long folderLevel = 0
        if (id) {
            parent = FmFolder.get(id)
            folderLevel = parent.folderLevel + 1
        }

        FmFolder.createCriteria().list {
	    if (parent) {
		eq 'parent', parent
            }
	    eq 'folderLevel', folderLevel
	    eq 'activeInd', true
	    order 'folderName', 'asc'
        }
    }

    Map<FmFolder, String> getAccessLevelInfoForFolders(Collection<FmFolder> fmFolders) {
	if (!fmFolders) {
	    return [:]
	}

	boolean isAdmin = securityService.principal().isAdminOrDseAdmin()

	Map<FmFolder, List<FmFolder>> foldersByStudy = fmFolders.groupBy { it.findParentStudyFolder() }

	Map<String, String> userAssignedTokens
	Map<FmFolder, String> studyFolderStudyIdMap
	Map<String, String> studyTokensMap
        if (!isAdmin) {
            studyFolderStudyIdMap = foldersByStudy.keySet().findAll().collectEntries {
		String studyId = FmFolderAssociation.findByFmFolder(it)?.bioObject?.accession
                [(it): studyId]
            }

            studyTokensMap = i2b2HelperService.getSecureTokensForStudies(studyFolderStudyIdMap.values().findAll())

	    userAssignedTokens = i2b2HelperService.getSecureTokensWithAccessForUser()
        }

	Map<FmFolder, String> results = [:]
        foldersByStudy.each { FmFolder studyFolder, List<FmFolder> folders ->
            if (studyFolder) {
                if (isAdmin) {
		    results.putAll folders.collectEntries { [(it): 'ADMIN'] }
                }
                else {
		    String studyId = studyFolderStudyIdMap[studyFolder]
		    String token = studyTokensMap[studyId]
		    String accessLevelInfo = userAssignedTokens[token] ?: 'LOCKED'
		    results.putAll folders.collectEntries { [(it): accessLevelInfo] }
                }
            }
            else {
		results.putAll folders.collectEntries { [(it): 'NA'] }
            }
        }

        results
    }

    Map<FmFolder, String> getFolderContentsWithAccessLevelInfo(folderId) {
	getAccessLevelInfoForFolders getFolderContents(folderId)
    }

    String getAssociatedAccession(FmFolder fmFolder) {
        //Walk up the tree to find the study accession for this folder
        if (!fmFolder) {
            return null
        }

	if (fmFolder.folderType == FolderType.PROGRAM.name()) {
            //Programs use their folderUID as accession
	    return fmFolder.uniqueId
        }

	if (fmFolder.folderType == FolderType.STUDY.name()) {
	    def experiment = FmFolderAssociation.findByFmFolder(fmFolder)?.bioObject
            if (!experiment) {
		logger.error 'No experiment associated with study folder: {}', fmFolder.folderFullName
            }
	    experiment?.accession
        }
        else {
	    getAssociatedAccession fmFolder.parent
        }
    }

    String getPath(FmFolder fmFolder, boolean safe = false) {
        //Get the full path of a folder by gathering folder names
	List<String> names = [fmFolder.folderName]
	while (fmFolder.parent) {
            fmFolder = fmFolder.parent
	    names.add 0, fmFolder.folderName
        }

        if (safe) {
            for (int i = 0; i < names.size(); i++) {
                names[i] = safeFileName(names[i])
            }
        }

	names.join '/'
    }

    String safeFileName(String name) {
        //Handle special cases - files should not be named like this!
	if (name == '.') {
            return 'dot'
        }

	if (name == '..') {
            return 'dotdot'
        }

        //Normal sanitation, should cover Windows/Unix
        for (chr in filenameBlacklist) {
            name = name.replace(chr, '_')
        }

	name
    }

    /**
     * Helper method to check whether a folder's parent program is included in the current search results.
     *
     * @param folderSearchString The current search string
     * @param folderFullName The folder's full path (from which the program UID can be extracted).
     */
    boolean searchMatchesParentProgram(String folderSearchString, String folderFullName) {
	String programUid = folderFullName.substring(1, folderFullName.indexOf('\\', 1))
	('\\' + programUid + '\\') in folderSearchString.split(',')
    }

    FmFolder getFolderByBioDataObject(bioDataObject) {
	def uniqueId = bioDataObject?.uniqueId?.uniqueId
	if (!uniqueId) {
	    logger.error 'No unique ID found for bio object {}', bioDataObject?.id
	    return null
	}

	FmFolderAssociation ffa = FmFolderAssociation.findByObjectUid(uniqueId)
	if (!ffa) {
	    logger.error 'No fmFolderAssociation found for unique ID {}', uniqueId
	}

	ffa?.fmFolder
    }

    /**
     * @return list of folders which are the children of the folder of which the identifier is passed as parameter
     */
    List<FmFolder> getChildrenFolder(String parentId) {
	FmFolder folder = FmFolder.get(parentId)
	FmFolder.executeQuery('''
			from FmFolder as fd
			where fd.activeInd = true
			  and fd.folderFullName like :fn escape '*'
			  and fd.folderLevel= :fl''',
			[fl: folder.folderLevel + 1, fn: folder.folderFullName + '%'])
    }

    /**
     * @return list of folders which are the children of the folder of which the identifier is passed as parameter by folder types
     */
    List<FmFolder> getChildrenFolderByType(Long parentId, String folderType) {
	FmFolder folder = FmFolder.get(parentId)
	FmFolder.executeQuery('''
		from FmFolder as fd
		where fd.activeInd = true
		  and fd.folderFullName like :fn escape '*'
		  and fd.folderLevel= :fl
		  and upper(fd.folderType) = upper(:ft)''',
		[fl: folder.folderLevel + 1, fn: folder.folderFullName + '%', ft: folderType])
    }

    /**
     * @return list of folders which are the children of the folder of which the identifier is passed as parameter
     */
    List<String> getChildrenFolderTypes(Long parentId) {
	FmFolder folder = FmFolder.get(parentId)
	FmFolder.executeQuery('''
		select distinct(fd.folderType)
		from FmFolder as fd
		where fd.activeInd = true
		  and fd.folderFullName like :fn escape '*'
		  and fd.folderLevel= :fl''',
		[fl: folder.folderLevel + 1, fn: folder.folderFullName + '%'])
    }

    /**
     * Validates and saves folder, associated business, and metadata fields.
     * @param object associated business object or folder, if there is none
     */
    @Transactional
    void saveFolder(FmFolder folder, object, GrailsParameterMap params) {

        AmTagTemplate template = AmTagTemplate.findByTagTemplateType(folder.folderType)

        // If this is new folder, then use viewInGrid items for validation, otherwise use editable items.
	String column
        if (folder.id == null) {
	    column = 'viewInGrid'
        }
        else {
	    column = 'editable'
        }

	String hql = 'from AmTagItem ati where ati.amTagTemplate.id = :templateId and ati.' + column + ' = 1 order by displayOrder'
	List<AmTagItem> items = AmTagItem.findAll(hql, [templateId: template.id])

	logger.info 'calling validateFolder'
	validateFolder folder, object, items, params

	logger.info 'calling doSaveFolder'
	doSaveFolder folder, object, template, items, params
    }

    /**
     * Validates required folder and meta data fields.
     */
    private void validateFolder(FmFolder folder, object, List<AmTagItem> items, GrailsParameterMap params) {

	if (object instanceof Experiment && object.id) {
	    List<FmFolder> existingFolders = FmFolder.executeQuery('''
			select ff
			from BioData bdu, FmFolderAssociation fla, FmFolder ff
			where fla.objectUid = bdu.uniqueId
			  and fla.fmFolder = ff
			  and bdu.id = ?
			  and ff.activeInd = true''',
			[object.id])

	    logger.info 'validating Experiment object id {} : {}', object.id, existingFolders
	    if (existingFolders && existingFolders[0] != folder) {
		folder.errors.rejectValue 'id', 'blank',
		    ['StudyId'] as String[], '{0} must be unique.'
            }
        }

        // Validate folder specific fields, if there is no business object
        if (folder == object) {
	    List<Map<String, String>> fields = [
		[displayName: 'Name', fieldName: 'folderName'],
		[displayName: 'Description', fieldName: 'description']]

	    logger.info 'validate object=folder fields {}', fields
	    for (Map<String, String> field in fields) {
		def value = params[field.fieldName]
		if (!value) {
		    folder.errors.rejectValue field.fieldName, 'blank',
			[field.displayName] as String[], '{0} field requires a value.'
                }
            }
        }

	logger.info 'validate items {}', items
	for (AmTagItem item in items) {
            if (item.required) {
                def value = null
		if (item.tagItemType == 'FIXED') {
		    value = params.list(item.tagItemAttr)
		    logger.info 'item FIXED param tagItemAttr {} value {}', item.tagItemAttr, value
                }
                else {
		    value = params.list('amTagItem_' + item.id)
		    logger.info 'item OTHER param amTagItem_{} value {}', item.id, value
                }
		if (!value || value[0] == null || value[0].length() == 0) {
		    folder.errors.rejectValue 'id', 'blank',
			[item.displayName] as String[], '{0} field requires a value.'
                }
                // TODO: Check for max values
            }
            else {
		logger.info '{} not required type {}', item.displayName, item.tagItemType
            }

            //check for unique study identifer
            if (item.codeTypeName == 'STUDY_IDENTIFIER') {
		Experiment experiment = Experiment.findByAccession(params.list(item.tagItemAttr));
		if (experiment && object == experiment && (!(object instanceof Experiment))) {
		    folder.errors.rejectValue 'id', 'blank',
			[item.displayName] as String[], '{0} must be unique.'
                }
            }
        }

        if (folder.hasErrors()) {
            throw new ValidationException('Validation errors occurred.', folder.errors)
        }
    }

    /**
     * Saves folder, associated business object, and metadata fields
     * @param folder folder to be saved
     * @param object associated business object (e.g. Experiment) or folder (if there is none)
     * @param template tag template associated with folder
     * @param items items associated with template
     * @param params field values to be saved
     * @throws ValidationException if there are any errors persisting data to the database
     */
    private void doSaveFolder(FmFolder folder, object, AmTagTemplate template, List<AmTagItem> items,
	                      GrailsParameterMap params) {

        // Save folder object
        folder.save(flush: true, failOnError: true)
	logger.info 'folder saved, process tagItems'

        // Using items associated with this folder's template, set business object property values or create tags.
        for (tagItem in items) {
	    logger.info 'tagItem {}/{} attr {}', tagItem.tagItemType, tagItem.tagItemSubtype, tagItem.tagItemAttr

	    // save by tagItemType
	    // FIXED: save in object
	    // CUSTOM: save in amTagAssociation (known value) amTagValue (user value)
	    // OTHER:
	    //        BIO_DISEASE save in amTagAssociation

	    def newValue
	    LocalDate localDateValue
	    LocalDateTime localDateTimeValue
	    if (tagItem.tagItemType == 'FIXED') {
		newValue = params[tagItem.tagItemAttr]
		logger.info 'FIXED params[{}] {}', tagItem.tagItemAttr, newValue
                if (newValue != null) {
		    String value = ''
		    if (tagItem.tagItemSubtype == 'MULTIPICKLIST') {
			newValue = params.list(tagItem.tagItemAttr)
			logger.info 'MULTIPICKLIST list {}', newValue 
			if (newValue) {
			    for (it in newValue) {
				if (value) {
                                    value += '|'
                                }
                                value += it
                            }
                        }
			logger.info 'list as string {}', value
			object[tagItem.tagItemAttr] = value
                    }
		    else if (tagItem.tagItemSubtype == 'DATE' || tagItem.tagItemSubtype == 'HIDDENDATE') {
			localDateValue = LocalDate.parse(newValue)
			object[tagItem.tagItemAttr] = java.sql.Date.valueOf(localDateValue)
		    }
		    else if (tagItem.tagItemSubtype == 'TIME' || tagItem.tagItemSubtype == 'HIDDENTIME') {
			localDateTimeValue = LocalDateTime.parse(newValue)
			object[tagItem.tagItemAttr] = java.sql.Timestamp.valueOf(localDateTimeValue)
		    }
		    else if (tagItem.tagItemSubtype == 'CURRENTTIME' || tagItem.tagItemSubtype == 'HIDDENCURRENTTIME') {
			localDateTimeValue = LocalDateTime.parse(newValue)
			object[tagItem.tagItemAttr] = java.sql.Timestamp.valueOf(localDateTimeValue)
		    }
                    else {
                        value = newValue
			object[tagItem.tagItemAttr] = value
                    }
                }
            }
	    else if (tagItem.tagItemType == 'CUSTOM') {
		newValue = params['amTagItem_' + tagItem.id]
		logger.info 'CUSTOM params[amTagItem_{}] {}', tagItem.id, newValue
		if (tagItem.tagItemSubtype == 'FREETEXT' || tagItem.tagItemSubtype == 'FREETEXTAREA') {
		    logger.info '{} delete any existing AM_TAG_VALUE value', tagItem.tagItemSubtype
		    AmTagAssociation.executeUpdate '''
				delete from AmTagAssociation as ata
				where ata.objectType=:objectType
				and ata.subjectUid=:subjectUid
				and ata.tagItemId=:tagItemId''',
				[objectType: 'AM_TAG_VALUE', subjectUid: folder.uniqueId, tagItemId: tagItem.id]
		    if (newValue) {
                        AmTagValue newTagValue = new AmTagValue(value: newValue)
			logger.debug 'save text value {}', newTagValue
                        newTagValue.save(flush: true, failOnError: true)
			logger.info 'save AmTagAssociation subject {} object {} tagItemId {}', folder.uniqueId, newTagValue.uniqueId, tagItem.id
			new AmTagAssociation(
			    objectType: 'AM_TAG_VALUE',
			    subjectUid: folder.uniqueId,
			    objectUid: newTagValue.uniqueId,
			    tagItemId: tagItem.id).save(flush: true, failOnError: true)
		    }
		}
		else if (tagItem.tagItemSubtype == 'PICKLIST') {
		    logger.info '{} delete any existing BIO_CONCEPT_CODE value', tagItem.tagItemSubtype
		    AmTagAssociation.executeUpdate '''
				delete from AmTagAssociation as ata
				where ata.objectType=:objectType
				and ata.subjectUid=:subjectUid
				and ata.tagItemId=:tagItemId''',
				[objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.uniqueId, tagItemId: tagItem.id]
		    if (newValue) {
			logger.info 'save AmTagAssociation subject {} objectvalue {} tagItemId {}', folder.uniqueId, newValue, tagItem.id
			new AmTagAssociation(
			    objectType: 'BIO_CONCEPT_CODE',
			    subjectUid: folder.uniqueId,
			    objectUid: newValue,
			    tagItemId: tagItem.id).save(flush: true, failOnError: true)
		    }
		}
		else if (tagItem.tagItemSubtype == 'MULTIPICKLIST') {
		    logger.info '{} delete any existing BIO_CONCEPT_CODE value', tagItem.tagItemSubtype
		    AmTagAssociation.executeUpdate '''
			delete from AmTagAssociation as ata
			where ata.objectType=:objectType
			  and ata.subjectUid=:subjectUid
			  and ata.tagItemId=:tagItemId''',
			[objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.uniqueId, tagItemId: tagItem.id]
		    for (it in params.list('amTagItem_' + tagItem.id)) {
                        if (it) {
			    logger.info 'save AmTagAssociation subject {} nextObject {} tagItemId {}', folder.uniqueId, it, tagItem.id
			    new AmTagAssociation(
				objectType: 'BIO_CONCEPT_CODE',
				subjectUid: folder.uniqueId,
				objectUid: it,
				tagItemId: tagItem.id).save(flush: true, failOnError: true)
                        }
                        else {
			    logger.error 'amTagItem_{} is null', tagItem.id
                        }
                    }
                }
	    }

	    else {
		logger.info 'Other tagItemType {} delete any existing value', tagItem.tagItemType
		AmTagAssociation.executeUpdate('''
			delete from AmTagAssociation as ata
			where ata.objectType=:objectType
			  and ata.subjectUid=:subjectUid
			  and ata.tagItemId=:tagItemId''',
			[objectType: tagItem.tagItemType,
			 subjectUid: folder.uniqueId,
			 tagItemId: tagItem.id])
		for (it in params.list('amTagItem_' + tagItem.id)) {
		    if (it) {
			logger.info 'save AmTagAssociation subject {} nextObject {} tagItemId {}', folder.uniqueId, it, tagItem.id
			new AmTagAssociation(
			    objectType: tagItem.tagItemType,
			    subjectUid: folder.uniqueId,
			    objectUid: it,
			    tagItemId: tagItem.id).save(flush: true, failOnError: true)
		    }
		    else {
			logger.error 'amTagItem_{} is null', tagItem.id
		    }
                }
	    }
        }

        // Create tag template association between folder and template, if it does not already exist
	AmTagTemplateAssociation templateAssoc = AmTagTemplateAssociation.findByObjectUid(folder.uniqueId)
        if (templateAssoc == null) {
	    new AmTagTemplateAssociation(tagTemplateId: template.id, objectUid: folder.uniqueId).save(flush: true, failOnError: true)
	    accessLogService.report 'Browse-Create object', folder.folderType + ': ' + folder.folderName + ' (' + folder.uniqueId + ')'
        }
        else {
	    accessLogService.report 'Browse-Modify object', folder.folderType + ': ' + folder.folderName + ' (' + folder.uniqueId + ')'
        }

        // If there is business object associated with folder, then save it and create association, if it does not exist.
        if (object != folder) {
	    object.save(flush: true, failOnError: true)
	    FmFolderAssociation folderAssoc = FmFolderAssociation.findByFmFolder(folder)
	    if (!folderAssoc) {
		new FmFolderAssociation(
		    objectUid: BioData.get(object.id).uniqueId,
		    objectType: object.getClass().name,
		    fmFolder: folder).save(flush: true, failOnError: true)
	    }
        }
    }

    private boolean save(o) {
	if (o.save(flush: true)) {
	    true
	}
	else {
	    logger.error '{}', utilService.errorStrings(o)
	    false
	}
    }
}
