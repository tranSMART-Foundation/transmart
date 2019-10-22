package fm

import annotation.AmTagDisplayValue
import annotation.AmTagItem
import annotation.AmTagItemService
import annotation.AmTagTemplate
import annotation.AmTagTemplateService
import com.mongodb.MongoClient
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.FolderType
import de.DeMrnaAnnotation
import grails.converters.JSON
import grails.converters.XML
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.BioDataExternalCode
import org.transmart.biomart.ConceptCode
import org.transmart.biomart.Experiment
import org.transmart.mongo.MongoUtils
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.SearchKeyword

import javax.activation.MimetypesFileTypeMap
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

@Slf4j('logger')
class FmFolderController {

    //FIXME Quick hack to make title properties act as hyperlinks.
    //These name properties should be indicated in the database, and the sort value should be specified
    //(needs a rewrite of our ExportTable)
    private static final List<String> nameProperties = ['assay name', 'analysis name', 'study title', 'program title', 'folder name']

    static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST']
    static defaultAction = 'list'

    AmTagItemService amTagItemService
    AmTagTemplateService amTagTemplateService
    FmFolderService fmFolderService
    def ontologyService
    def solrFacetService
    @Autowired private SecurityService securityService

    @Value('${transmartproject.mongoFiles.enableMongo:false}')
    private boolean enableMongo

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

    @Lazy
    static MimetypesFileTypeMap MIME_TYPES_FILES_MAP = {
        File mimeTypesFile = [
            new File(System.getenv('HOME'), '.mime.types'),
            new File(System.getenv('JAVA_HOME'), 'lib/mime.types'),
            new File('/etc/mime.types')
        ].findResult null, { File file ->
            if (file.exists()) {
                return file
            }
        }

        if (!mimeTypesFile) {
	    FmFolderController.logger.warn 'Could not find a mime.types file'
            return new MimetypesFileTypeMap()
        }

	FmFolderController.logger.debug 'Loading mime.types file on {}', mimeTypesFile
        mimeTypesFile.withInputStream {
            new MimetypesFileTypeMap(it)
        }
    }()

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        [fmFolderInstanceList: FmFolder.list(params), fmFolderInstanceTotal: FmFolder.count()]
    }

    def create() {
	[fmFolderInstance: new FmFolder(params)]
    }

    def createAnalysis() {
	FmFolder parentFolder = FmFolder.get(params.folderId)
	FmFolder folder = new FmFolder(folderType: FolderType.ANALYSIS.name(), parent: parentFolder)

	AmTagTemplate amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.ANALYSIS.name())
	List<String> measurements = BioAssayPlatform.executeQuery('SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType')
	List<String> vendors = BioAssayPlatform.executeQuery('SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor')
	List<String> technologies = BioAssayPlatform.executeQuery('SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology')
	List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery('FROM BioAssayPlatform as p ORDER BY p.name')

	render template: 'createAnalysis', model: [
	    bioDataObject   : new BioAssayAnalysis(),
	    measurements    : measurements,
	    technologies    : technologies,
	    vendors         : vendors,
	    platforms       : platforms,
	    folder          : folder,
	    amTagTemplate   : amTagTemplate,
	    metaDataTagItems: amTagItemService.getDisplayItems(amTagTemplate.id)]
    }

    def createAssay() {
	FmFolder parentFolder = FmFolder.get(params.folderId)
	FmFolder folder = new FmFolder(folderType: FolderType.ASSAY.name(), parent: parentFolder)
	AmTagTemplate amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.ASSAY.name())
	List<String> measurements = BioAssayPlatform.executeQuery('SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType')
	List<String> vendors = BioAssayPlatform.executeQuery('SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor')
	List<String> technologies = BioAssayPlatform.executeQuery('SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology')
	List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery('FROM BioAssayPlatform as p ORDER BY p.name')

	render template: 'createAssay', model: [
	    bioDataObject   : folder,
	    measurements    : measurements,
	    technologies    : technologies,
	    vendors         : vendors,
	    platforms       : platforms,
	    folder          : folder,
	    amTagTemplate   : amTagTemplate,
	    metaDataTagItems: amTagItemService.getDisplayItems(amTagTemplate.id)]
    }

    def createFolder() {
	FmFolder parentFolder = FmFolder.get(params.folderId)
	FmFolder folder = new FmFolder(folderType: FolderType.FOLDER.name(), parent: parentFolder)

	AmTagTemplate amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.FOLDER.name())
	render template: 'createFolder', model: [
	    bioDataObject   : folder,
	    folder          : folder,
	    amTagTemplate   : amTagTemplate,
	    metaDataTagItems: amTagItemService.getDisplayItems(amTagTemplate.id)]
    }

    def createStudy() {
	FmFolder parentFolder = FmFolder.get(params.folderId)
	FmFolder folder = new FmFolder(folderType: FolderType.STUDY.name(), parent: parentFolder)
	AmTagTemplate amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.STUDY.name())
	Experiment experiment = new Experiment()
	
	render template: 'createStudy', model: [
	    bioDataObject   : experiment,
	    folder          : folder,
	    amTagTemplate   : amTagTemplate,
	    metaDataTagItems: amTagItemService.getDisplayItems(amTagTemplate.id)]
        }

    def createProgram() {
	FmFolder folder = new FmFolder(folderType: FolderType.PROGRAM.name())
	AmTagTemplate amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.PROGRAM.name())
	render template: 'createProgram', model: [
	    bioDataObject   : folder,
	    folder          : folder,
	    amTagTemplate   : amTagTemplate,
	    metaDataTagItems: amTagItemService.getDisplayItems(amTagTemplate.id)]
    }

    def save() {
	FmFolder fmFolder = new FmFolder(params)
	if (fmFolder.save(flush: true)) {
	    redirect action: 'show', id: fmFolder.id
        }
        else {
	    render view: 'create', model: [fmFolderInstance: fmFolder]
        }
    }

    def saveProgram() {
	FmFolder folder = new FmFolder(params)
        folder.folderLevel = 0
        folder.folderType = FolderType.PROGRAM.name()

        try {
	    fmFolderService.saveFolder folder, folder, params
	    render([id: folder.id] as JSON)
	    solrFacetService.reindexFolder folder.uniqueId
        }
	catch (ValidationException e) {
	    String errors = renderErrors(bean: e.errors)
	    logger.error 'Unable to save program: {}', errors
	    render([errors: errors] as JSON)
        }
	catch (e) {
	    renderUnexpectedError e
        }
    }

    def saveStudy() {
	FmFolder parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            logger.error 'Parent folder is null'
	    render([errors: '<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>'])
            return
        }

	FmFolder folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.STUDY.name()
        folder.parent = parentFolder
        params.accession = params.accession.toUpperCase()

	Experiment experiment = Experiment.findOrCreateByAccession(params.accession)
        experiment.title = folder.folderName
        experiment.description = folder.description
        experiment.type = 'Experiment'

        try {
	    fmFolderService.saveFolder folder, experiment, params
	    render([id: folder.id, parentId: folder.parentId] as JSON)
	    solrFacetService.reindexFolder folder.uniqueId
        }
	catch (ValidationException e) {
	    String errors = renderErrors(bean: e.errors)
	    logger.error 'Unable to save study: {}', errors
	    render([errors: errors] as JSON)
        }
	catch (e) {
	    logger.error 'Exception in FmFolderController.saveStudy', e
	    renderUnexpectedError e
        }
    }

    def saveAssay() {
	FmFolder parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            logger.error 'Parent folder is null'
	    render([errors: '<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>'])
            return
        }

	FmFolder folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.ASSAY.name()
        folder.parent = parentFolder

        try {
	    fmFolderService.saveFolder folder, folder, params
	    render([id: folder.id, parentId: folder.parentId] as JSON)
	    solrFacetService.reindexFolder folder.uniqueId
        }
	catch (ValidationException e) {
	    String errors = renderErrors(bean: e.errors)
	    logger.error 'Unable to save assay: {}', errors
	    render([errors: errors] as JSON)
        }
	catch (e) {
	    logger.error 'Exception in FmFolderController.saveAssay', e
	    renderUnexpectedError e
        }
    }

    def saveAnalysis() {
	FmFolder parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            logger.error 'Parent folder is null'
	    render([errors: '<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>'])
            return
        }

	FmFolder folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.ANALYSIS.name()
        folder.parent = parentFolder

	FmFolder parentFolderStudy = folder.findParentStudyFolder()

	BioAssayAnalysis analysis = new BioAssayAnalysis(name: folder.folderName, shortDescription: folder.description,
		longDescription: folder.description, analysisMethodCode: 'TBD', assayDataType: 'Browse analysis',
		dataCount: -1, teaDataCount: -1)
	FmFolderAssociation assocStudy = FmFolderAssociation.findByFmFolder(parentFolderStudy)
	if (assocStudy) {
	    def study = assocStudy.bioObject
            if (study instanceof Experiment) {
		analysis.etlId = study.accession
            }
        }

        try {
	    fmFolderService.saveFolder folder, analysis, params
	    render([id: folder.id, parentId: folder.parentId] as JSON)
	    solrFacetService.reindexFolder folder.uniqueId
        }
	catch (ValidationException e) {
	    String errors = renderErrors(bean: e.errors)
	    logger.error 'Unable to save study: {}', errors
	    render([errors: errors] as JSON)
        }
	catch (e) {
	    logger.error 'Exception in FmFolderController.saveAnalysis', e
	    renderUnexpectedError e
        }
    }

    def saveFolder() {
	FmFolder parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            logger.error 'Parent folder is null'
	    render([errors: '<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>'])
            return
        }

	FmFolder folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.FOLDER.name()
        folder.parent = parentFolder

        try {
	    fmFolderService.saveFolder folder, folder, params
	    render([id: folder.id, parentId: folder.parentId] as JSON)
	    solrFacetService.reindexFolder folder.uniqueId
	}
	catch (ValidationException e) {
	    String errors = renderErrors(bean: e.errors)
	    logger.error 'Unable to save folder: {}', errors
	    render([errors: errors] as JSON)
        }
	catch (e) {
	    logger.error 'Exception in FmFolderController.saveFolder', e
	    renderUnexpectedError e
        }
    }

    def edit(FmFolder fmFolder) {
	if (!fmFolder) {
	    redirect action: 'list'
        }
        else {
	    [fmFolderInstance: fmFolder]
        }
    }

    def update(FmFolder fmFolder) {
	if (fmFolder) {
	    fmFolder.properties = params

	    if (!fmFolder.hasErrors() && fmFolder.save(flush: true)) {
		redirect action: 'show', id: fmFolder.id
            }
            else {
		render view: 'edit', model: [fmFolderInstance: fmFolder]
            }
        }
        else {
	    redirect action: 'list'
        }
    }

    def delete(FmFolder fmFolder) {
	if (fmFolder) {
            try {
		fmFolder.delete(flush: true)
		redirect action: 'list'
            }
	    catch (DataIntegrityViolationException e) {
		redirect action: 'show', id: params.id
            }
        }
        else {
	    redirect action: 'list'
        }
    }

    def getPrograms() {
	render(getFolder(FolderType.PROGRAM.name(), null) as XML)
    }

    def getStudies() {
	render(getFolder(FolderType.STUDY.name(), params.parentPath) as XML)
    }

    def getFolders() {
	render(getFolder(FolderType.FOLDER.name(), params.parentPath) as XML)
    }

    def getAnalyses() {
	render(getFolder(FolderType.ANALYSIS.name(), params.parentPath) as XML)
    }

    def getAssays() {
	render(getFolder(FolderType.ASSAY.name(), params.parentPath) as XML)
    }

    //service to call to get all the children of a folder, regardless of their type
    //need a parameter parentId corresponding to the parent identifier
    def getAllChildren() {
	render(fmFolderService.getChildrenFolder(params.parentId) as XML)
    }

    //service to call to get all experiment objects that are associated with a folder in fm_folder_association table
    def getExperiments() {
        render(contentType: 'text/xml') {
            experiments {
		for (assoc in FmFolderAssociation.findAllByObjectType('org.transmart.biomart.Experiment')) {
		    def exp = assoc.bioObject
		    if (exp) {
                        experiment(id: exp.id) {
			    accession exp.accession
			    title exp.title?.encodeAsHTML()
			    folderId assoc.fmFolder.id
			    folderUid assoc.fmFolder.uniqueId
                        }
                    }
                }
            }
        }
    }

    //service to get analyses details, mainly analysis unique id and title
    def getAnalysesDetails() {
        render(contentType: 'text/xml') {
            analyses {
		for (assoc in FmFolderAssociation.findAllByObjectType('org.transmart.biomart.BioAssayAnalysis')) {
		    FmFolder folder = assoc.fmFolder
		    if (folder) {
                        analysis(id: assoc.objectUid) {
			    title assoc.fmFolder.folderName
			    folderId assoc.fmFolder.id
			    parentId assoc.fmFolder.parent.id
                        }
                    }
                }
            }
        }
    }

    def addProgram() {
	doAddFolder FolderType.PROGRAM.name(), new FmFolder(params.fmFolder), null
    }

    def addStudy() {
	doAddFolder FolderType.STUDY.name(), new FmFolder(params.fmFolder), params.parentId
    }

    def addFolder() {
	doAddFolder FolderType.STUDY.name(), new FmFolder(params.fmFolder), params.parentId
    }

    def addAnalysis() {
	doAddFolder FolderType.ANALYSIS.name(), new FmFolder(params.fmFolder), params.parentId
    }

    def addFile() {
	FmFolder p = FmFolder.get(params.folderId)
	FmFile f = new FmFile(params)
        if (f.save(flush: true)) {
	    p.addToFmFiles f
            if (p.save(flush: true)) {
		render(p as XML)
            }
            else {
                render p.errors
            }
        }
        else {
            render f.errors
        }

	doAddFolder FolderType.STUDY.name(), p, params.parentId
    }

    def getFolderContents() {
        def id = params.id
        if (!id) {
            id = FmFolder.findByUniqueId(params.uid).id
        }

	Boolean auto = params.boolean('auto')
        //Flag for whether folder was automatically opened - if not, then it shouldn't respect the folder mask
	Map<FmFolder, String> folderContentsAccessLevelMap = fmFolderService.getFolderContentsWithAccessLevelInfo(id)
	List<FmFolder> folderContents = folderContentsAccessLevelMap.keySet() as List
	def folderSearchLists = session.folderSearchList
        if (!folderSearchLists) {
            folderSearchLists = [[], []]
        }
	String folderSearchString = folderSearchLists[0] ? folderSearchLists[0].join(',') + ',' : ''
        //Extra , - used to identify leaves
	String uniqueLeavesString = folderSearchLists[1] ? folderSearchLists[1].join(',') + ',' : ''
	def nodesToExpand = session.rwgOpenedNodes
        //check that all folders from folderContents are in the search path, or children of nodes in the search path
	if (folderSearchLists[0]) {
	    for (folder in folderContents) {
                boolean found = false
		for (String path in folderSearchLists[0]) {
		    if (path.contains(folder.folderFullName) || folder.folderFullName.contains(path)) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    folderContents -= folder
                }
            }
        }

	String displayMetadata = ''
        //if there is an accession in filters, add the study node (there is just one) in the array for nodes to expand
	def filters = session.rwgSearchFilter
	for (filter in filters) {
	    if (filter?.contains('|ACCESSION;')) {
		for (folder in folderContents) {
                    if (folder.folderType == 'STUDY') {
                        if (!nodesToExpand.grep(folder.uniqueId)) {
			    nodesToExpand << folder.uniqueId
                            displayMetadata = folder.uniqueId
                        }
                    }
                }
            }
        }

	render template: 'folders', model: [
	    folders                     : folderContents,
	    folderContentsAccessLevelMap: folderContentsAccessLevelMap,
	    folderSearchString          : folderSearchString,
	    uniqueLeavesString          : uniqueLeavesString,
	    auto                        : auto,
	    nodesToExpand               : nodesToExpand,
	    displayMetadata             : displayMetadata]
    }

    private void doAddFolder(String folderType, FmFolder folder, long parentId) {
        folder.folderType = folderType

        if (FolderType.PROGRAM.name() == folderType) {
            folder.folderLevel = 0
        }
        else {
	    FmFolder parentFolder = FmFolder.get(parentId)
            folder.folderLevel = parentFolder.folderLevel + 1
            folder.parent = parentFolder
        }

        if (folder.save(flush: true)) {
            // Set folder's tag value based on a radix-36 conversion of its ID.
            folder.tag = Long.toString(folder.id, 36).toUpperCase()
            folder.save(flush: true)
	    solrFacetService.reindexFolder folder.uniqueId

	    render(folder as XML)
        }
        else {
            render folder.errors
        }
    }

    private void moveFolder(long folderId, String newFolderFullName, String newFolderLevel) {
	FmFolder folder = FmFolder.get(folderId)
	long oldLevel = folder.folderLevel
        folder.folderFullName = newFolderFullName
	folder.folderLevel = newFolderLevel as long

        if (folder.save()) {
	    List<FmFolder> subFolderList = FmFolder.executeQuery("from FmFolder as fd where fd.folderFullName like :fn escape '*'",
			[fn: folder.folderFullName + '%'])

	    for (FmFolder fmFolder in subFolderList) {
		moveFolder(fmFolder.id, newFolderFullName + fmFolder.folderName + '\\', newFolderLevel + (fmFolder.folderLevel - oldLevel))
            }

	    render(folder as XML)
        }
        else {
            render folder.errors
        }
    }

    private void removeFolder(long folderId) {
	FmFolder folder = FmFolder.get(folderId)
        folder.activeInd = false

        if (folder.save()) {
	    List<FmFolder> subFolderList = FmFolder.executeQuery('''
			from FmFolder as fd
			where fd.folderFullName like :fn escape '*'
			and fd.folderLevel = :fl''',
			[fn: folder.folderFullName + '%', fl: (folder.folderLevel + 1)])

	    for (FmFolder fmFolder in subFolderList) {
		removeFolder fmFolder.id
            }

	    render(folder as XML)

        }
        else {
            render folder.errors
        }
    }

    private List<FmFolder> getFolder(String folderType, String parentPath) {
	if (!parentPath) {
	    FmFolder.executeQuery('''
			from FmFolder as fd
			where fd.activeInd = true
			  and upper(fd.folderType) = upper(:fl)''',
			[fl: folderType])
        }
        else {
	    FmFolder.executeQuery('''
			from FmFolder as fd
			where fd.activeInd = true
			and upper(fd.folderType) = upper(:fl)
			and fd.folderFullName like :fn escape '*' ''',
                    [fl: folderType, fn: parentPath + '%'])
        }
    }
    private String createDataTable(Map<FmFolder, String> subFoldersAccessLevelMap, String folderType) {

        if (!subFoldersAccessLevelMap) {
            return '{}'
        }

        Set<FmFolder> folders = subFoldersAccessLevelMap.keySet()
        ExportTableNew table = new ExportTableNew()

        def dataObject
        def childMetaDataTagItems

	for (FmFolder folder in folders) {
            dataObject = getBioDataObject(folder)
            childMetaDataTagItems = getChildMetaDataItems(folder)
            if (dataObject && childMetaDataTagItems) {
                break
            }
        }

	childMetaDataTagItems.eachWithIndex { obj, i ->
            AmTagItem amTagItem = obj
            if (amTagItem.viewInChildGrid) {
                if (amTagItem.tagItemType == 'FIXED') {
                    if (dataObject.hasProperty(amTagItem.tagItemAttr)) {
                        table.putColumn(amTagItem.id.toString(),
                                        new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, '', 'String'))
                    }
                    else {
			logger.error 'CREATEDATATABLE::TAG ITEM ID = {} COLUMN {} is not a propery of {}',
			    amTagItem.id, amTagItem.tagItemAttr, dataObject                            }

                }
                else if (amTagItem.tagItemType == 'CUSTOM') {
                    table.putColumn(amTagItem.id.toString(),
                                    new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, '', 'String'))
                }
                else {
                    table.putColumn(amTagItem.id.toString(),
                                    new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, '', 'String'))
                }
            }
            else {
		logger.debug 'COLUMN {} is not to display in grid', amTagItem.displayName
            }
        }

	for (folderObject in folders) {
            def bioDataObject = getBioDataObject(folderObject)
            ExportRowNew newrow = new ExportRowNew()
            childMetaDataTagItems.eachWithIndex() { obj, i ->
                AmTagItem amTagItem = obj
                if (amTagItem.viewInChildGrid) {
                    if (amTagItem.tagItemType == 'FIXED' && bioDataObject.hasProperty(amTagItem.tagItemAttr)) {
			String bioDataDisplayValue = null
                        def bioDataPropertyValue = bioDataObject[amTagItem.tagItemAttr]
                        if (amTagItem.tagItemSubtype == 'PICKLIST' || amTagItem.tagItemSubtype == 'MULTIPICKLIST') {
                            if (bioDataPropertyValue) {
				ConceptCode cc = ConceptCode.findByUniqueId(bioDataPropertyValue)
                                if (cc) {
                                    bioDataDisplayValue = cc.codeName
                                }
                                else {
                                    bioDataDisplayValue = ''
                                }
                            }
                            else {
                                bioDataDisplayValue = ''
                            }
                        }
                        else if (amTagItem.tagItemSubtype == 'FREETEXT') {
			    bioDataDisplayValue = createTitleString(amTagItem, bioDataPropertyValue, folderObject,
				subFoldersAccessLevelMap[folderObject] != 'LOCKED')
                        }
                        else if (amTagItem.tagItemSubtype == 'FREETEXTAREA') {
                            bioDataDisplayValue = amTagItem.displayName
                        }
                        else {
                            logger.error 'FIXED ATTRIBUTE ERROR::Unknown tagItemSubType'
                        }

			newrow.put(amTagItem.id.toString(), bioDataDisplayValue ?: '')
                    }
                    else if (amTagItem.tagItemType == 'CUSTOM') {
			Collection<AmTagDisplayValue> tagValues = AmTagDisplayValue.findAllDisplayValue(folderObject.uniqueId, amTagItem.id)
                        newrow.put(amTagItem.id.toString(), createDisplayString(tagValues))
                    }
                    else {
                        if (amTagItem.displayName == 'Folder Name') {
                            newrow.put(amTagItem.id.toString(), createTitleString(amTagItem, folderObject.folderName, folderObject, true))
                        }
                        else if (amTagItem.displayName == 'Assay Name') {
                            newrow.put(amTagItem.id.toString(), createTitleString(amTagItem, folderObject.folderName, folderObject, true))
                        }
                        else if (amTagItem.displayName == 'Analysis Name') {
                            newrow.put(amTagItem.id.toString(), createTitleString(amTagItem, folderObject.folderName, folderObject, true))
                        }
                        else {
			    Collection<AmTagDisplayValue> tagValues = AmTagDisplayValue.findAllDisplayValue(folderObject.uniqueId, amTagItem.id)
                            newrow.put(amTagItem.id.toString(), createDisplayString(tagValues))
                        }
                    }
                }

                table.putRow(folderObject.uniqueId, newrow)
            }
	}

	table.toJSON_DataTables('', folderType).toString(5)
    }

    private String createTitleString(AmTagItem amTagItem, String name, FmFolder folderObject, boolean isLink = true) {
	String tagName = amTagItem.displayName
        if (nameProperties.contains(tagName.toLowerCase())) {
	    String titleHtml = isLink ?
		"<a href='#' onclick='openFolderAndShowChild(${folderObject.parent?.id}, ${folderObject.id})'>$name</a>" :
		name
	    //Comment with name at the start to preserve the sort order - precaution against "--" being included in a name
	    "<!-- ${name.replace('--', '_')} -->$titleHtml"
	}
	else {
	    name
        }
    }

    private String createDisplayString(tagValues) {
        if (tagValues) {
	    tagValues*.displayValue.join(', ')
	}
	else {
	    ''
        }
    }

    def folderDetail() {
        def folderId = params.id

	FmFolder folder
        def bioDataObject
	List<AmTagItem> metaDataTagItems
	List<String> jsonForGrids = []
	boolean subjectLevelDataAvailable = false
	List<String> measurements
	List<String> technologies
	List<String> vendors
	List<BioAssayPlatform> platforms
        Map searchHighlight
	String folderTypeName = "UNKNOWN"

        if (folderId) {
            folder = FmFolder.get(folderId)

            if (folder) {
                if (!folder.activeInd) {
		    render template: 'deletedFolder'
                    return
                }

		if(folder.folderType.equalsIgnoreCase(FolderType.PROGRAM.name())) {
		    folderTypeName = 'PROGRAM'
		}
		else if(folder.folderType.equalsIgnoreCase(FolderType.STUDY.name())){
		    folderTypeName = 'STUDY'
		}
		else if(folder.folderType.equalsIgnoreCase(FolderType.FOLDER.name())){
		    folderTypeName = 'FOLDER'
		}
		else if(folder.folderType.equalsIgnoreCase(FolderType.ANALYSIS.name())){
		    folderTypeName = 'ANALYSIS'
		}
		else if(folder.folderType.equalsIgnoreCase(FolderType.ASSAY.name())){
		    folderTypeName = 'ASSAY'
		}
		bioDataObject = getBioDataObject(folder)
                metaDataTagItems = getMetaDataItems(folder, false)

                //If the folder is a study, check for subject-level data being available
		if (folder.folderType.equalsIgnoreCase(FolderType.STUDY.name()) && bioDataObject?.hasProperty('accession')) {
                    subjectLevelDataAvailable = ontologyService.checkSubjectLevelData(bioDataObject.accession)
                }

                if (folder.folderType.equalsIgnoreCase(FolderType.ASSAY.name()) || folder.folderType.equalsIgnoreCase(FolderType.ANALYSIS.name())) {
                    measurements = BioAssayPlatform.executeQuery('SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType')
                    vendors = BioAssayPlatform.executeQuery('SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor')
                    technologies = BioAssayPlatform.executeQuery('SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology')
                    platforms = BioAssayPlatform.executeQuery('FROM BioAssayPlatform as p ORDER BY p.name')
                }

		for (String type in fmFolderService.getChildrenFolderTypes(folder.id)) {
		    List<FmFolder> subFolders = fmFolderService.getChildrenFolderByType(folder.id, type)
		    if (subFolders) {
			Map<FmFolder, String> subFoldersAccessLevelMap = fmFolderService.getAccessLevelInfoForFolders(subFolders)
                        String gridTitle = 'Associated ' + StringUtils.capitalize(subFolders[0].pluralFolderTypeName.toLowerCase())
			jsonForGrids << createDataTable(subFoldersAccessLevelMap, gridTitle)
                    }
                }

                // Highlight search terms (if specified by RWGController)
		searchHighlight = solrFacetService.getSearchHighlight(folder, session.rwgCategorizedSearchTerms)
            }
        }

	render template: '/fmFolder/folderDetail', model: [
            folder                   : folder,
            bioDataObject            : bioDataObject,
            measurements             : measurements,
            technologies             : technologies,
            vendors                  : vendors,
            platforms                : platforms,
            metaDataTagItems         : metaDataTagItems,
	    jSONForGrids             : jsonForGrids,
            subjectLevelDataAvailable: subjectLevelDataAvailable,
            searchHighlight          : searchHighlight,
	    useMongo                 : enableMongo,
	    folderTypeName           : folderTypeName
	]
    }

    def analysisTable() {
        try {
            def analysisId = params.id
	    List<String> genes = []
	    def geneFilter = session.geneFilter
            //Convert gene filter to a straight list
            if (geneFilter) {
                geneFilter = geneFilter.substring(5).split('::')[0].replace('|', '/').split('/')
            }

            //For each gene (ignore pathways), add the gene name and any synonyms to the list to match against
	    for (String item in geneFilter) {
                if (item.startsWith('GENE')) {
		    SearchKeyword sk = SearchKeyword.findByUniqueId(item)
		    genes << sk.keyword
		    for (s in BioDataExternalCode.findAllByBioDataIdAndCodeType(k.bioDataId, 'SYNONYM')) {
			genes << s.code
                    }
                }
            }

	    Map criteriaParams = [:]
            if (!params.boolean('full')) {
		criteriaParams.max = 1000
            }

	    FmFolderAssociation fmm = FmFolderAssociation.findByFmFolder(FmFolder.load(analysisId as Long))
            String ouid = fmm.objectUid
	    List<BioAssayAnalysisData> rows = BioAssayAnalysisData.createCriteria().list(criteriaParams) {
		eq 'analysis', BioAssayAnalysis.load(ouid.substring(ouid.indexOf(':') + 1) as Long)
		order 'rawPvalue', 'asc'
            }

            ExportTableNew table = new ExportTableNew()
            table.putColumn('probe', new ExportColumn('probe', 'Probe', '', 'String'))
            table.putColumn('gene', new ExportColumn('gene', 'Gene', '', 'String'))
            table.putColumn('pvalue', new ExportColumn('pvalue', 'p-value', '', 'Number'))
            table.putColumn('apvalue', new ExportColumn('apvalue', 'Adjusted p-value', '', 'Number'))
            table.putColumn('teapvalue', new ExportColumn('teapvalue', 'TEA-adjusted p-value', '', 'Number'))
            table.putColumn('foldchangeratio', new ExportColumn('foldchangeratio', 'Fold Change Ratio', '', 'Number'))

	    for (BioAssayAnalysisData baad in rows) {
		List<String> rowGenes = DeMrnaAnnotation.executeQuery('''
				select a.geneSymbol
				from DeMrnaAnnotation as a
				where a.probesetId=?
				and geneSymbol is not null''',
				[baad.probesetId])
		boolean foundGene = false

                if (genes) {
		    List<String> lowerGenes = []
                    for (gene in rowGenes) {
			lowerGenes << gene.toLowerCase()
                    }
                    for (gene in genes) {
                        if (gene.toLowerCase() in lowerGenes) {
                            foundGene = true
                            break
                        }
                    }
                }

                if (foundGene || !genes) {
                    ExportRowNew newrow = new ExportRowNew()
		    newrow.put('probe', baad.featureGroupName)
                    newrow.put('gene', rowGenes.join(', '))
		    newrow.put('pvalue', baad.rawPvalue.toString())
		    newrow.put('apvalue', baad.adjustedPvalue.toString())
		    newrow.put('teapvalue', baad.teaNormalizedPValue.toString())
		    newrow.put('foldchangeratio', baad.foldChangeRatio.toString())
		    table.putRow(baad.id.toString(), newrow)
                }
            }

            def analysisData = table.toJSON_DataTables('', 'Analysis Data')
            analysisData.put('rowCount', rows.getTotalCount())
            analysisData.put('filteredByGenes', genes.size() > 0)

	    render contentType: 'text/json', text: analysisData.toString(5)
        }
	catch (e) {
	    logger.error 'Error while building analysis table', e
        }
    }

    private List<AmTagItem> getChildMetaDataItems(FmFolder folder) {
	AmTagTemplate amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
        if (amTagTemplate) {
	    amTagItemService.getChildDisplayItems amTagTemplate.id
        }
        else {
	    logger.error 'Unable to find child amTagTemplate for object Id = {}', folder.uniqueId
        }
    }

    private List<AmTagItem> getMetaDataItems(folder, editable) {
	AmTagTemplate amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
        if (amTagTemplate) {
            if (editable) {
		amTagItemService.getEditableItems amTagTemplate.id
            }
            else {
		amTagItemService.getDisplayItems amTagTemplate.id
            }
        }
        else {
	    logger.error 'Unable to find amTagTemplate for object Id = {}', folder.uniqueId
        }
    }

    private getBioDataObject(FmFolder folder) {
        def bioDataObject

        if (folder.folderType == 'PROGRAM') {
	    return folder
        }

	FmFolderAssociation folderAssociation = FmFolderAssociation.findByFmFolder(folder)
        //for PROGRAM this would be
        //folderAssociation = FmFolderAssociation.findByFmFolder(folder)

        if (folderAssociation) {
	    bioDataObject = folderAssociation.bioObject
        }
        else {
	    logger.info 'Unable to find folderAssociation for folder Id = {}', folder.id
        }

        if (!bioDataObject) {
            bioDataObject = folder
        }

	bioDataObject
    }

    def editMetaData() {
        if (!isAdmin()) {
            logger.info 'Not an admin: ignore'
            return
        }

        def folderId = params.folderId

	FmFolder folder
        def bioDataObject
	List<AmTagItem> metaDataTagItems
        if (folderId) {
            folder = FmFolder.get(folderId)
            if (folder) {
                bioDataObject = getBioDataObject(folder)
                metaDataTagItems = getMetaDataItems(folder, true)
            }
            else {
		logger.error 'Unable to find folder for folder Id = {}', folderId
            }
        }

	List<String> measurements = BioAssayPlatform.executeQuery('SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType')
	List<String> vendors = BioAssayPlatform.executeQuery('SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor')
	List<String> technologies = BioAssayPlatform.executeQuery('SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology')
	List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery('FROM BioAssayPlatform as p ORDER BY p.name')

	render template: 'editMetaData',
	    model: [bioDataObject   : bioDataObject,
		    measurements    : measurements,
		    technologies    : technologies,
		    vendors         : vendors,
		    platforms       : platforms,
		    folder          : folder,
		    metaDataTagItems: metaDataTagItems]
    }

    def updateMetaData(FmFolder folder) {
        if (!isAdmin()) {
            return
        }

        try {
            // Get associated business object and deal with any special folderName/description inconsistencies.
	    def object
            folder.folderName = params.folderName
            folder.description = params.description
	    FmFolderAssociation assoc = FmFolderAssociation.findByFmFolder(folder)
	    if (assoc) {
		object = assoc.bioObject
                if (object instanceof Experiment) {
                    folder.folderName = params.title
                }
                else if (object instanceof BioAssayAnalysis) {
                    folder.folderName = params.name
                    folder.description = params.longDescription
                }
            }
            else {
                object = folder
            }

	    fmFolderService.saveFolder folder, object, params
	    render([id: folder.id, folderName: folder.folderName] as JSON)

	    solrFacetService.reindexFolder folder.uniqueId
        }
	catch (ValidationException e) {
	    String errors = renderErrors(bean: e.errors)
	    logger.error 'Unable to update metadata: {}', errors
	    render([errors: errors] as JSON)
        }
	catch (e) {
	    logger.error 'Exception in FmFolderController.updateMetaData', e
	    renderUnexpectedError e
        }
    }

    def subFolders() {
	ExportTableNew table = new ExportTableNew()

        table.putColumn('ident', new ExportColumn('ident', 'ID', '', 'String', 50))
        table.putColumn('name', new ExportColumn('name', 'Name', '', 'String', 50))
        table.putColumn('description', new ExportColumn('description', 'Description', '', 'String', 50))

        ExportRowNew newrow = new ExportRowNew()
        newrow.put('ident', 'foo.id')
        newrow.put('name', 'foo.name')
        newrow.put('description', 'foo.description')
        table.putRow('somerow', newrow)

	String jSONToReturn = table.toJSON_DataTables('').toString(5)

	session.gridtable = table
        [jSONForGrid: jSONToReturn]

    }

    /**
     * Calls service to import files into tranSMART filestore and index them with SOLR
     */
    def importFiles() {
        fmFolderService.importFiles()	       
    }

    /**
     * Calls service to re-index existing files with SOLR
     */
    def reindexFiles() {
	render 'Reindexing...'
        fmFolderService.reindexFiles()
	render '...complete!'
    }

    def reindexFolder() {
	solrFacetService.reindexFolder params.uid
    }

    def removeEntry() {
        fmFolderService.removeSolrEntry(params.uid)
    }

    def deleteStudy(FmFolder studyFolder) {
	fmFolderService.deleteStudy studyFolder
        render 'ok'
    }

    def deleteProgram(FmFolder programFolder) {
	fmFolderService.deleteProgram programFolder
        render 'ok'
    }

    def hasChildren(FmFolder fmFolder) {
	render([result: fmFolder.children.findAll { it.activeInd }.size() != 0] as JSON)
    }

    def deleteFolder(FmFolder folder) {
        if (!isAdmin()) {
            return
        }

        if (folder) {
	    fmFolderService.deleteFolder folder
	    render template: 'deletedFolder'
        }
        else {
	    render status: 500, text: 'FmFolder not found'
        }
    }

    def deleteFile(FmFile file) {
        if (!isAdmin()) {
            return
        }

	FmFolder folder = file.folder
        if (file) {
	    logger.debug 'deleting file {} in folder {}', file, folder
	    fmFolderService.deleteFile file
	    render template: '/fmFolder/filesTable', model: [folder: folder, hlFileIds: []]
        }
        else {
	    render status: 404, text: 'FmFile not found'
        }
    }

    def downloadFile(FmFile fmFile) {
        if (!fmFile) {
            render status: 404, text: 'FmFile not found'
            return
        }

	String mimeType = MIME_TYPES_FILES_MAP.getContentType(fmFile.originalName)
	logger.debug 'Downloading file {}, mime type {}', fmFile, mimeType

        HttpServletResponse fileResponse=new HttpServletResponseWrapper(response)
        response.setContentType mimeType

        /* This form of sending the filename seems to be compatible
         * with all major browsers, except for IE8. See:
         * http://greenbytes.de/tech/tc2231/#attwithfn2231utf8comp
         */
	header 'Content-Disposition', "attachment; filename*=UTF-8''" +
            fmFile.originalName.getBytes('UTF-8').collect {
            '%' + String.format('%02x', it)
        }.join('')
	header 'Content-length', fmFile.fileSize.toString()

	if (!enableMongo) {
	    fmFolderService.getFile(fmFile).newInputStream().withStream { InputStream stream ->
		fileResponse.outputStream << stream
            }
        }
        else {
	    if (useDriver) {
		MongoClient mongo = new MongoClient(mongoServer, mongoPort)
		GridFSDBFile gfsFile = new GridFS(mongo.getDB(mongoDbName)).findOne(fmFile.filestoreName)
		fileResponse.outputStream << gfsFile.inputStream
                mongo.close()
            }
            else {
		new HTTPBuilder(mongoApiUrl + fmFile.filestoreName + '/fsfile').request(Method.GET, ContentType.BINARY) { req ->
		    headers.'apikey' = MongoUtils.hash(mongoApiKey)
                    response.success = { resp, binary ->
                        assert resp.statusLine.statusCode == 200
                        fileResponse.outputStream << binary
                    }
                    response.failure = { resp ->
			logger.error 'Problem during connection to API: {}', resp.status
			render 'Error writing ZIP: File not found'
                    }
                }
            }
        }
    }

    def ajaxTechnologies(String measurementName, String vendorName) {
	String queryString = ' where 1=1'
	if (measurementName && measurementName != 'null') {
	    queryString += " and platformType = '" + measurementName + "'"
        }
	if (vendorName && vendorName != 'null') {
	    queryString += " and vendor = '" + vendorName + "'"
        }

	List<BioAssayPlatform> technologies = BioAssayPlatform.executeQuery(
	    'SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ' + queryString + '  ORDER BY p.platformTechnology')
	render template: 'selectTechnologies', model: [technologies: technologies, technology: params.technologyName]
    }

    def ajaxVendors(String technologyName, String measurementName) {
	String queryString = ' where 1=1'

	if (technologyName && technologyName != 'null') {
	    queryString += " and platformTechnology = '" + technologyName + "'"
        }

	if (measurementName && measurementName != 'null') {
	    queryString += " and platformType = '" + measurementName + "'"
        }

	List<String> vendors = BioAssayPlatform.executeQuery(
	    'SELECT DISTINCT vendor FROM BioAssayPlatform as p ' + queryString + '  ORDER BY p.vendor')
	render template: 'selectVendors', model: [vendors: vendors, vendor: params.vendorName]
    }

    def ajaxMeasurements(String technologyName, String vendorName, String measurementName) {
	String queryString = ' where 1=1'

	if (technologyName && technologyName != 'null') {
	    queryString += " and platformTechnology = '" + technologyName + "'"
        }

	if (vendorName && vendorName != 'null') {
	    queryString += " and vendor = '" + vendorName + "'"
        }

	List<String> measurements = BioAssayPlatform.executeQuery(
	    'SELECT DISTINCT platformType FROM BioAssayPlatform as p ' + queryString + '  ORDER BY p.platformType')
	render template: 'selectMeasurements', model: [measurements: measurements, measurement: measurementName]
    }

    def ajaxPlatforms(String measurementName, String technologyName, String vendorName) {
	String queryString = ' where 1=1'

	if (measurementName && measurementName != 'null') {
	    queryString += " and platformType = '" + measurementName + "'"
        }

	if (technologyName && technologyName != 'null') {
	    queryString += " and platformTechnology = '" + technologyName + "'"
        }

	if (vendorName && vendorName != 'null') {
	    queryString += " and vendor = '" + vendorName + "'"
        }

	List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery(
	    'FROM BioAssayPlatform as p ' + queryString + '  ORDER BY p.platformType')
	render template: 'selectPlatforms', model: [platforms: platforms]
    }

    private boolean isAdmin() {
	if (securityService.principal().isAdmin()) {
	    true
        }
        else {
	    render '''You do not have permission to edit this object's metadata.'''
	    false
        }
    }

    def getFolderFiles(String id, String accession, String folderId) {
	logger.info 'getFolderFiles id {} accession {} returnJSON {}', id, accession, params.returnJSON

	Experiment experiment
	if (id) {
	    experiment = Experiment.get(id)
        }
	else if (accession) {
            experiment = Experiment.findByAccession(accession)
        }
	else if (folderId) {
	    String acc = FmFolderAssociation.findByFmFolder(FmFolder.load(folderId as Long)).objectUid.replace('EXP:', '')
	    experiment = Experiment.findByAccession(acc)
	}

	FmFolder folder = fmFolderService.getFolderByBioDataObject(experiment)
        if (folder) {
            if (params.returnJSON) {
		Map<Long, Map> infoList = [:]
		for (FmFile file in folder.fmFiles) {
                    if (file.activeInd) {
			infoList[file.id] = [displayName: file.displayName, fileType: file.fileType]
                    }
                }
		render(infoList as JSON)
            }
            else {
		logger.debug 'rendering filesTable folder {}', folder
		render template: '/fmFolder/filesTable', model: [folder: folder]
            }
        }
    }

    private renderUnexpectedError(Exception e) {
	String error = '<ul><li>An unexpected error has occurred. If this error persits, ' +
	    'please click "Close" or "Cancel" to close this dialog box.<br><br>Error details: ' + e.message + '</li></ul>'
	render([errors: error] as JSON)

    }
}
