import com.recomdata.genesignature.FileSchemaException
import com.recomdata.genesignature.WizardModelDetails
import com.recomdata.util.DomainObjectExcelHelper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.multipart.MultipartFile
import org.transmart.audit.AuditLogService
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.CellLine
import org.transmart.biomart.Compound
import org.transmart.biomart.ConceptCode
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.GeneSignatureFileSchema
import org.transmart.searchapp.GeneSignatureItem
import org.transmart.searchapp.SearchKeyword
import org.transmart.searchapp.SearchKeywordTerm
import org.transmartproject.core.users.User
import org.transmartproject.db.log.AccessLogService

import javax.servlet.ServletOutputStream

/**
 * @author jliu
 */
@Slf4j('logger')
class GeneSignatureController {

    private static final String GENERIC_OTHER_BIO_CONCEPT_CODE = 'OTHER'
    private static final String GENERIC_OTHER_CODE_TYPE_NAME = 'OTHER'

    private @Autowired AccessLogService accessLogService
    private @Autowired AuditLogService auditLogService
    private @Autowired GeneSignatureService geneSignatureService
    User currentUserBean
    private @Autowired SecurityService securityService
    private @Autowired UtilService utilService

    // concept code categories
    private static final String SOURCE_CATEGORY = 'GENE_SIG_SOURCE'
    private static final String OWNER_CATEGORY = 'GENE_SIG_OWNER'
    private static final String SPECIES_CATEGORY = 'SPECIES'
    private static final String MOUSE_SOURCE_CATEGORY = 'MOUSE_SOURCE'
    private static final String TISSUE_TYPE_CATEGORY = 'TISSUE_TYPE'
    private static final String EXP_TYPE_CATEGORY = 'EXPERIMENT_TYPE'
    private static final String ANALYTIC_TYPE_CATEGORY = 'ANALYTIC_CATEGORY'
    private static final String NORM_METHOD_CATEGORY = 'NORMALIZATION_METHOD'
    private static final String ANALYSIS_METHOD_CATEGORY = 'ANALYSIS_METHOD'
    private static final String P_VAL_CUTOFF_CATEGORY = 'P_VAL_CUTOFF'
    private static final String FOLD_CHG_METRIC_CATEGORY = 'FOLD_CHG_METRIC'

    // session attributes
    private static final String WIZ_DETAILS_ATTRIBUTE = 'wizDetails'

    // map species param
    static mappings = {
	"/$species"(controller: 'geneSignature', action: 'cellLineLookup')
    }

    def index() {
	accessLogService.report 'GeneSignature-Summary', 'Gene Signature summary page'
	redirect action: 'list'
    }

    /**
     * reset flash object before showing summary
     */
    def refreshSummary() {
        flash.message = null
	redirect action: 'list'
    }

    /**
     * summary page of permissioned gene signatures
     */
    def list() {
	removeSessionWizard()

	boolean admin = securityService.principal().isAdmin()
	long userId = securityService.currentUserId()

        // summary view
	List<GeneSignature> signatures = geneSignatureService.listPermissionedGeneSignatures(userId, admin)
	Map ctMap = geneSignatureService.getPermissionedCountMap(userId, admin)

        // break into owned and public
	List<GeneSignature> myItems = []
	List<GeneSignature> pubItems = []
	List<GeneSignature> myListItems = []
	List<GeneSignature> pubListItems = []

	for (GeneSignature geneSignature in signatures) {
	    if (geneSignature.uniqueId?.startsWith('GENESIG')) {
		if (userId == geneSignature.createdByAuthUserId) {
		    myItems << geneSignature
                }
                else {
		    pubItems << geneSignature
                }
            }
            else {
		if (userId == geneSignature.createdByAuthUserId) {
		    myListItems << geneSignature
                }
                else {
		    pubListItems << geneSignature
                }
            }
        }

	[user: securityService.principal(), adminFlag: admin,
	 myItems: myItems, pubItems: pubItems,
	 myListItems: myListItems, pubListItems: pubListItems,
	 ctMap: ctMap]
    }

    /**
     * initialize session for the create gs wizard
     */
    def createWizard() {
	AuthUser user = AuthUser.get(securityService.currentUserId())
	GeneSignature geneSignature = new GeneSignature(createdByAuthUser: user, publicFlag: false, deletedFlag: false)
	setSessionWizard new WizardModelDetails(loggedInUser: user, geneSigInst: geneSignature)

	redirect action: 'create1'
    }

    /**
     * initialize session for the create gs wizard
     */
    def createListWizard() {
	AuthUser user = AuthUser.get(securityService.currentUserId())
	GeneSignature geneSig = new GeneSignature(
	    createdByAuthUser: user, publicFlag: false,
	    deletedFlag: false, list: true)
	setSessionWizard new WizardModelDetails(loggedInUser: user, geneSigInst: geneSig)

	redirect action: 'createList'
    }

    /**
     * initialize session for the edit gs wizard
     */
    def editWizard(GeneSignature geneSig) {

	GeneSignature clone = geneSig.clone()
	clone.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
	if (clone.experimentTypeCellLineId == null) {
	    clone.experimentTypeCellLine = null
	}

	setSessionWizard new WizardModelDetails(
	    loggedInUser: securityService.principal(),
	    geneSigInst: clone,
	    wizardType: WizardModelDetails.WIZ_TYPE_EDIT,
	    editId: geneSig.id)

	redirect action: 'edit1', params: ["id": geneSig.id, "page": 1]
    }

    /**
     * initialize session for the clone (essentially edit) gs wizard
     */
    def cloneWizard(GeneSignature geneSig) {

	AuthUser user = AuthUser.get(securityService.currentUserId())
	GeneSignature clone = geneSignatureService.doClone(geneSig)

	clone.createdByAuthUser = user
        clone.modifiedByAuthUser = null
	clone.name += ' (clone)'
	clone.description += ' (clone)'
        clone.publicFlag = false
        clone.deletedFlag = false
        clone.dateCreated = null
        clone.lastUpdated = null
        clone.versionNumber = null
        clone.uniqueId = null
	if (clone.experimentTypeCellLineId == null) {
	    clone.experimentTypeCellLine = null
	}

        // this is a hack, don't know how to get around this!

	setSessionWizard new WizardModelDetails(
	    loggedInUser: securityService.principal(),
	    geneSigInst: clone,
	    wizardType: WizardModelDetails.WIZ_TYPE_CLONE,
	    cloneId: geneSig.id)


	redirect action: 'create1'
    }

    /**
     * set the indicated gs public for access by everyone
     */
    def makePublic(GeneSignature gs) {
	gs.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
	geneSignatureService.makePublic gs, true

	flash.message = "GeneSignature '$gs.name' was made public to everyone"
	redirect action: 'list'
    }

    /**
     * set the indicated gs private
     */
    def makePrivate(GeneSignature gs) {
	gs.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
	geneSignatureService.makePublic gs, false

	flash.message = "GeneSignature '$gs.name' was made private"
	redirect action: 'list'
    }

    /**
     * mark the indicated gs as deleted by setting deletedFlag as true
     */
    def delete(GeneSignature gs) {
	gs.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
	geneSignatureService.delete gs

	flash.message = "GeneSignature '$gs.name' was marked as deleted"
	redirect action: 'list'
    }

    /**
     * detail view
     */
    def show(GeneSignature gs) {
	render template: 'gene_sig_detail', model: [gs: gs]
    }

    def showDetail(GeneSignature gs) {
	render view: 'showDetail', model: [gs: gs]
    }

    def create1() {
	WizardModelDetails wizard = sessionWizard()

	bindGeneSigData wizard.geneSigInst

        // load data for page 1
	loadWizardItems 1, wizard

	render view: 'wizard1', model: [wizard: wizard]
    }

    def create2() {
	WizardModelDetails wizard = sessionWizard()

	bindGeneSigData wizard.geneSigInst

        // load item data
	loadWizardItems 2, wizard
	Map existingValues = createExistingValues(2, wizard.geneSigInst)

	render view: 'wizard2', model: [
	    wizard: wizard,
	    existingValues: existingValues]
    }

    def create3() {
	WizardModelDetails wizard = sessionWizard()

	bindGeneSigData wizard.geneSigInst

        // load data for page 3
	loadWizardItems 3, wizard
	Map existingValues = createExistingValues(3, wizard.geneSigInst)

	render view: 'wizard3', model: [
	    wizard: wizard,
	    existingValues: existingValues]
    }

    def createList() {
	WizardModelDetails wizard = sessionWizard()

	bindGeneSigData wizard.geneSigInst

	render view: 'wizard_list', model: [wizard: wizard]
    }

    def editList(GeneSignature geneSig) {
	WizardModelDetails wizard = new WizardModelDetails(
	    loggedInUser: AuthUser.load(securityService.currentUserId()),
	    geneSigInst: geneSig)
	setSessionWizard wizard

	bindGeneSigData wizard.geneSigInst
        wizard.wizardType = 1
	render view: 'wizard_list', model: [wizard: wizard, gs: wizard.geneSigInst, isEdit: true]
    }

    /**
     * edit gs in page 1 of wizard
     */
    def edit1() {
	WizardModelDetails wizard = sessionWizard()
	bindGeneSigData wizard.geneSigInst

        // load data for page 1
	loadWizardItems 1, wizard

	render view: 'wizard1', model: [wizard: wizard]
    }

    /**
     * edit gs in page 2 of wizard
     */
    def edit2() {
	WizardModelDetails wizard = sessionWizard()

        // save original file until final save
	String origFile = wizard.geneSigInst.uploadFile
	bindGeneSigData wizard.geneSigInst
        wizard.geneSigInst.uploadFile = origFile

        // load item data
	loadWizardItems 2, wizard
	Map existingValues = createExistingValues(2, wizard.geneSigInst)

	render view: 'wizard2', model: [
	    wizard: wizard,
	    existingValues: existingValues]
    }

    /**
     * edit gs in page 3 of wizard
     */
    def edit3() {
	WizardModelDetails wizard = sessionWizard()
	bindGeneSigData wizard.geneSigInst

        // load data for page 3
	loadWizardItems 3, wizard
	Map existingValues = createExistingValues(3, wizard.geneSigInst)

	render view: 'wizard3', model: [
	    wizard: wizard,
	    existingValues: existingValues]
    }

    /**
     * save new gene signature domain and composition of gene signature items
     */
    def save() {
	WizardModelDetails wizard = sessionWizard()
	GeneSignature gs = wizard.geneSigInst
	Map existingValues = [:]
	assert null == gs.properties.id

	bindGeneSigData gs

        // get file
	MultipartFile file = request.getFile('uploadFile')

        // load file contents, if clone check for file presence
	boolean loadFile = wizard.wizardType == WizardModelDetails.WIZ_TYPE_CREATE ||
	    (wizard.wizardType == WizardModelDetails.WIZ_TYPE_CLONE && file?.originalFilename)
	if (!loadFile) {
	    file = null
	}
	if (loadFile) {
	    gs.properties.uploadFile = file.originalFilename

            // check for empty file
            if (file.empty) {
		flash.message = "The file:'$gs.properties.uploadFile' you uploaded is empty"
		existingValues = createExistingValues(3, wizard.geneSigInst)
		render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
		return
            }

            // validate file format
	    String metricType = gs.foldChgMetricConceptCode?.bioConceptCode
	    long schemaColCt = gs.fileSchema?.numberColumns

            try {
		geneSignatureService.verifyFileFormat file, schemaColCt, metricType
            }
            catch (FileSchemaException e) {
		flash.message = e.message
		existingValues = createExistingValues(3, wizard.geneSigInst)
		render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
		return
            }
        }
        else {
            // load items from cloned object
            GeneSignature parentGS = GeneSignature.get(wizard.cloneId)
	    gs.properties.uploadFile = parentGS.uploadFile
            geneSignatureService.cloneGeneSigItems(parentGS, gs)
        }

        // good to go, call save service
        try {
            gs = geneSignatureService.saveWizard(gs, file, GeneSignature.DOMAIN_KEY)
            if (gs.hasErrors()) {
                flash.message = 'Could not save Gene Signature'
		render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
                return
            }

	    auditLogService.report 'New Gene Signature', request,
                action: actionName,
                user: currentUserBean,
                filename: file?.originalFilename,
                size: gs.geneSigItems.size()

	    removeSessionWizard()

	    flash.message = "GeneSignature '$gs.name' was " +
		(params.boolean('isEdit') ? 'edited' : 'created') + " on: $gs.dateCreated"
	    redirect action: 'list'
	}
	catch (FileSchemaException e) {
	    logger.error 'Message: {}', e.message
	    flash.message = e.message
	    existingValues = createExistingValues(3, wizard.geneSigInst)
	    render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
	}
	catch (RuntimeException e) {
	    logger.error 'RuntimeException {}: {}', e.getClass().name, e.message
	    flash.message = 'Runtime exception ' + e.getClass().name + ':<br>' + e.message
	    existingValues = createExistingValues(3, wizard.geneSigInst)
	    render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
        }
    }

    /**
     * save new gene list
     */
    def saveList() {
	WizardModelDetails wizard = sessionWizard()
	GeneSignature gs = wizard.geneSigInst
	Map existingValues = [:]

	gs.properties.list = true
        if (!params.boolean('isEdit')) {
	    assert null == gs.properties.id
        }
        else {
	    for (i in GeneSignatureItem.findAllByGeneSignature(gs)) {
                i.delete()
            }
            gs.geneSigItems.clear()
        }

	// For a list the other signature values are placeholders
	// these must exist in the database for values required in a GeneSignature

        // species
	gs.speciesConceptCode = ConceptCode.findByCodeTypeNameAndBioConceptCode(
	    'OTHER', 'OTHER')

        // technology platforms
	gs.techPlatform = BioAssayPlatform.findByName(
	    'None')
        
        // p value cutoffs
	gs.pValueCutoffConceptCode = ConceptCode.findByCodeTypeNameAndBioConceptCode(
	    P_VAL_CUTOFF_CATEGORY, 'UNDEFINED')

        // file schemas
        gs.fileSchema =  GeneSignatureFileSchema.findByName('Gene Symbol <tab> Metric Indicator')

        // fold change metrics
	gs.foldChgMetricConceptCode = ConceptCode.findByCodeTypeNameAndBioConceptCode(
	    FOLD_CHG_METRIC_CATEGORY, 'NOT_USED')

	bindData gs, params

        // get file
	MultipartFile file = request.getFile('uploadFile')
	gs.properties.name = params.name

        // load file contents, if clone check for file presence
	boolean loadFile = file?.originalFilename?.trim()
	if (!loadFile) {
	    file = null
	}
	if (loadFile) {
	    gs.properties.uploadFile = file.originalFilename

            // check for empty file
            if(file.empty) {
		flash.message = "The file:'$gs.properties.uploadFile' you uploaded is empty"
		render view: 'wizard_list', model: [wizard: wizard]
		return
            }

            // validate file format
	    String metricType = 'NOT_USED'
	    int schemaColCt = 2
            try {
                geneSignatureService.verifyFileFormat(file, schemaColCt, metricType)
            }
            catch (FileSchemaException e) {
		flash.message = e.message
		render view: 'wizard_list', model: [wizard: wizard]
		return
            }
        }
        else {
	    gs.properties.uploadFile = 'Manual Item Entry'
            // load items from list rather than file
            List<String> markers = []
	    params.each { String key, val ->
		key = key.trim()
		if (key.startsWith('biomarker_') && val) {
		    markers << val.trim()
                }
            }
	    List<GeneSignatureItem> gsItems
	    try {
		gsItems = geneSignatureService.loadGeneSigItemsFromList(markers)
	    }
	    catch (FileSchemaException e) {
		logger.error 'Message {}', e.message
		flash.message = e.message
		existingValues = createExistingValues(3, wizard.geneSigInst)
		render view: 'wizard_list', model: [wizard: wizard, existingValues: existingValues]
		return;
	    }

	    List<String> geneSigUniqueIds = gs.geneSigItems*.bioDataUniqueId
	    for (GeneSignatureItem gsi in gsItems) {
		if (!geneSigUniqueIds?.contains(gsi.bioDataUniqueId)) {
		    gs.addToGeneSigItems gsi
                }
            }
        }

        // good to go, call save service
        try {
            gs = geneSignatureService.saveWizard(gs, file, GeneSignature.DOMAIN_KEY_GL)
            if (gs.hasErrors()) {
                flash.message = 'Could not save Gene/RSID List'
		render view: 'wizard_list', model: [wizard: wizard]
                return
            }

            auditLogService.report(
		'New Gene_RSID List', request,
		action: actionName,
		user: currentUserBean,
                filename: file?.originalFilename,
                size: gs.geneSigItems?.size()
            )

	    removeSessionWizard()

	    flash.message = "Gene/RSID List '$gs.name' was " +
		(params.boolean('isEdit') ? 'edited' : 'created') + " on: $gs.dateCreated"
	    redirect action: 'list'

	}
	catch (FileSchemaException e) {
	    logger.error 'Message: {}', e.message
	    flash.message = e.message
	    render view: 'wizard_list', model: [wizard: wizard]
	}
	catch (RuntimeException e) {
	    logger.error 'RuntimeException {}: {}', e.getClass().name, e.message
	    flash.message = 'Runtime exception ' + e.getClass().name + ':<br>' + e.message
	    render view: 'wizard_list', model: [wizard: wizard]
        }
    }

    /**
     * update gene signature and the associated items (new file only)
     */
    def update() {
	WizardModelDetails wizard = sessionWizard()
	GeneSignature clone = wizard.geneSigInst

	bindGeneSigData clone

        // load real domain object, apply edit params from clone
	GeneSignature gsReal = GeneSignature.get(wizard.editId)
	String origFile = gsReal.uploadFile
	clone.copyPropertiesTo gsReal
	gsReal.modifiedByAuthUser = AuthUser.load(securityService.currentUserId())
        gsReal.uploadFile = origFile

        // refresh items if new file uploaded
	MultipartFile file = request.getFile('uploadFile')

        // file validation
	if (file?.originalFilename) {
            // empty?
            if (file.empty) {
		flash.message = flash.message = "The file:'$file.originalFilename' you uploaded is empty"
		Map existingValues = createExistingValues(3, wizard.geneSigInst)
		render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
		return
            }

            // check schema errors
	    String metricType = gsReal.foldChgMetricConceptCode?.bioConceptCode
	    long schemaColCt = gsReal.fileSchema?.numberColumns
            try {
		geneSignatureService.verifyFileFormat file, schemaColCt, metricType
		gsReal.uploadFile = file.originalFilename

            }
            catch (FileSchemaException e) {
		flash.message = e.message
		Map existingValues = createExistingValues(3, wizard.geneSigInst)
		render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
		return
            }
        }

        // good to go, call update service
        try {
	    geneSignatureService.updateWizard gsReal, file
	    removeSessionWizard()

	    flash.message = "GeneSignature '$gsReal.name' was updated on: $gsReal.lastUpdated"
	    redirect action: 'list'
	}
	catch (FileSchemaException e) {
	    flash.message = e.message
	    Map existingValues = createExistingValues(3, wizard.geneSigInst)
	    render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
	}
	catch (RuntimeException e) {
	    logger.error 'RuntimeException {}: {}', e.getClass().name, e.message
	    flash.message = 'Runtime exception ' + e.getClass().name + ':<br>' + e.message
	    Map existingValues = createExistingValues(3, wizard.geneSigInst)
	    render view: 'wizard3', model: [wizard: wizard, existingValues: existingValues]
        }
    }

    /**
     * edit screen for gs items
     */
    def showEditItems(GeneSignature gs) {
        if (params.errorFlag != null) {
	    render view: 'edit_items', model: [gs: gs, errorFlag: true]
        }
        else {
	    render view: 'edit_items', model: [gs: gs]
        }
    }

    /**
     * delete the indicated gs items
     */
    def deleteItems(GeneSignature gs) {
        def delParam = params.delete
	if (!delParam) {
	    setFlashWarning 'You did not select any item(s) to delete'
	    render view: 'edit_items', model: [gs: gs, errorFlag: true]
	    return
        }

        // if one id, request is a string
	List<String> delItems = []
        if (delParam instanceof String) {
	    delItems << delParam
        }
        else {
	    delItems.addAll delParam
        }

        // delete indicated ids
        gs = geneSignatureService.deleteGeneSigItems(gs, delItems)

	flash.message = '<div class="message">deleted ' + delItems.size() + ' gene signature item(s)</div>'
	render view: 'edit_items', model: [gs: gs]
    }

    /**
     * add items to an existing gene signature
     */
    def addItems(GeneSignature gs) {

        // reset
        flash.message = null

        // extract symbols and value metrics
	List<String> geneSymbols = []
	List<Double> valueMetrics = []
	List<String> probes = []

	boolean error = false

	// iterate through params
	params.each { String key, String value ->
	    key = key.trim()
	    String symbol = value.trim()

            // parse gene symbols
	    if (key.startsWith('biomarker_') && symbol) {
		String itemNum = key.substring(10, key.length())
		String valueMetric = params['foldChgMetric_' + itemNum]
		geneSymbols << symbol

                // parse fold chg metric
		if (valueMetric?.trim()) {
                    try {
			valueMetrics << Double.valueOf(valueMetric)
                    }
                    catch (RuntimeException e) {
			logger.error 'invalid valueMetric: {} detected!', valueMetric, e
			setFlashWarning 'The value metric "' + valueMetric +
			    '" for symbol: "' + symbol + '" is not a valid number'
			error = true
                    }
                }
            }

            // parse probeset
	    if (key.startsWith('probeset_') && symbol) {
		String itemNum = key.substring(9, key.length())
		String valueMetric = params['foldChgMetric_' + itemNum]
		probes << symbol

                // parse fold chg metric
		if (valueMetric?.trim()) {
                    try {
			valueMetrics << Double.valueOf(valueMetric)
                    }
                    catch (RuntimeException e) {
			logger.error 'invalid valueMetric: {} detected!', valueMetric, e
			setFlashWarning 'The value metric "' + valueMetric +
			    '" for symbol: "' + symbol + '" is not a valid number'
			error = true
                    }
                }
            }
        }

        // any symbols to add?
	if (!error && !geneSymbols && !probes) {
	    setFlashWarning 'You did not enter any new item(s) to add</br>Click "Expand to Add Items" to update the list'
	    error = true
        }

        // valid fold chg for each gene or probeset?
	if (!error && gs.foldChgMetricConceptCode?.bioConceptCode != GeneSignatureService.METRIC_CODE_GENE_LIST) {
	    if (gs.fileSchemaId == 3 && probes.size() != valueMetrics.size()) {
		setFlashWarning 'You must enter a valid fold change metric for each new probeset'
		error = true
            }

	    if (gs.fileSchemaId != 3 && geneSymbols.size() != valueMetrics.size()) {
		setFlashWarning 'You must enter a valid fold change metric for each new gene symbol'
		error = true
            }
        }

        // add indicated items
	if (!valueMetrics) {
	    valueMetrics = null
	}

        // add new items if no error
	if (!error) {
            try {
		int added = geneSignatureService.addGenSigItems gs, geneSymbols, probes, valueMetrics
		if(added == geneSymbols.size())
		    flash.message = '<div class="message">' + geneSymbols.size() +
		    ' gene signature item(s) were added to "' + gs.name + '"</div>'
		else
		    flash.message = '<div class="message">' + added +
		    ' gene signature item(s) were added for ' + geneSymbols.size() + ' gene(s) to "' + gs.name + '"</div>'
            }
	    catch (FileSchemaException e) {
		logger.error 'Message {}: {}', e.getClass().name, e.message
		setFlashWarning e.message
		error = true
	    }
	    catch (RuntimeException e) {
		logger.error 'RuntimeException {}: {}', e.getClass().name, e.message
		setFlashWarning 'Runtime exception ' + e.getClass().name + ':<br>' + e.message
		error = true
            }
        }

        // handle error
	if (error) {
            // build params map from current params for redirect
	    Map newParams = [id: gs.id, errorFlag: true]

            // add new gene params
	    int i = 1
	    for (it in geneSymbols) {
		newParams['biomarker_' + i] = it
                i++
            }

            // add new fold chg params
            i = 1
	    for (it in valueMetrics) {
		newParams['foldChgMetric_' + i] = it
                i++
            }
	    redirect action: 'showEditItems', params: newParams
        }
        else {
	    redirect action: 'showEditItems', params: [id: gs.id]
        }
    }

    /**
     * export gene signature to Excel
     */
    def downloadExcel(GeneSignature gs) {
	new DomainObjectExcelHelper(gs, 'gene_sig_' + gs.name + '.xls').downloadDomainObject response
    }

    /**
     * export GMT: Gene Matrix Transposed file format (*.gmt)
     */
    def downloadGMT(String id) {
	String content = geneSignatureService.getGeneSigGMTContent(id)
	String name = geneSignatureService.getGeneSigName(id)
	String fileName = 'gene_sig_' + name.replace('-', '').replace(' ','') + '.gmt'

	utilService.sendDownload response, 'application/vnd.gmt', fileName, content.bytes
    }

    /**
     * show sample upload files to user
     */
    def showUploadSamples() {

        // set headers on output stream
        response.reset()
        response.setContentType('text/plain')
	response.setCharacterEncoding('utf-8')
	response.setHeader('Content-Disposition', 'attachment; filename="gene_sig_samples.txt"')
        response.setHeader('Cache-Control', 'must-revalidate, post-check=0, pre-check=0')
        response.setHeader('Pragma', 'public')
        response.setHeader('Expires', '0')

        // send workbook to response
	ServletOutputStream os = response.outputStream
	os.println '1) Gene List Example (no tab character, exclude fold change metric):'
	os.println ''
	os.println 'TCN1'
	os.println 'IL1RN'
	os.println 'KIAA1199'
	os.println 'G0S2'
	os.println 'CXCL2'
	os.println 'IL1RN'
	os.println 'IL24'
	os.println 'APOBEC3A'
	os.println 'VNN3'
	os.println 'DSG3'
	os.println ''
	os.println ''
	os.println '2) Gene Signature Example with actual fold change (separate columns with a tab):'
	os.println ''
	os.println 'CXCL5\t-19.19385797'
	os.println 'IL8RB\t-18.21493625'
	os.println 'FPR1\t-17.6056338'
	os.println 'FCGR3A\t-15.69858713'
	os.println 'MMP3\t-15.31393568'
	os.println 'CXCL6\t-14.7275405'
	os.println 'BCL2A1\t-12.65822785'
	os.println 'CXCL2\t-12.300123'
	os.println 'SERPINB3\t-12.22493888'
	os.println 'KYNU\t-10.76426265'
	os.println ''
	os.println ''
	os.println '3) Gene Signature Example for composite lists (separate columns with a tab):'
	os.println ''
	os.println 'CXCL5\t-1'
	os.println 'IL8RB\t-1'
	os.println 'MMP3\t-1'
	os.println 'SOD2\t0'
	os.println 'PI3\t0'
	os.println 'CDH3\t0'
	os.println 'LAIR2\t1'
	os.println 'ZBP1\t1'
	os.println 'SPRR1B\t1'
	os.println 'APOL1\t1'
    }

    /**
     * retrieve cell lines for indicated species
     */
    def cellLineLookup(String id) {

	List<CellLine> cellLines
	if (!id) {
	    cellLines = CellLine.list(sort: 'cellLineName')
        }
        else {
	    ConceptCode species = ConceptCode.get(id)
	    String speciesFilter = species.codeName
	    if (speciesFilter.contains('Mouse')) {
		speciesFilter = 'Mouse'
	    }
	    if (speciesFilter.contains('monkey')) {
		speciesFilter = 'Monkey'
	    }

            // match on species
            cellLines = CellLine.findAllBySpeciesIlike(speciesFilter + '%', [sort: 'cellLineName'])
        }

	render view: 'cell_line_lookup', model: [cellLines: cellLines]
    }

    /**
     * bind form parameters to GeneSignature domain instance
     */
    private void bindGeneSigData(GeneSignature gs) {
        // skip if page param not specified
	long pageNum = params.long('page', -1)

	if (pageNum == -1) {
	    return
	}

	if (pageNum == 3) {
	    if (params.multipleTestingCorrection == null) {
		params.multipleTestingCorrection = false
	    }
        }

	bindData gs, params
    }

    /**
     * load required data for wizard page
     */
    private void loadWizardItems(int pageNum, WizardModelDetails wizard) {
        switch (pageNum) {

            case 1:
                break

            case 2:
                // sources
		wizard.sources = ConceptCode.findAllByCodeTypeName(
		    SOURCE_CATEGORY, [sort: 'bioConceptCode'])
		wizard.sources << genericOtherConceptCode

                // owners
		wizard.owners = ConceptCode.findAllByCodeTypeName(
		    OWNER_CATEGORY, [sort: 'bioConceptCode'])

                // species
		wizard.species = ConceptCode.findAllByCodeTypeName(
		    SPECIES_CATEGORY, [sort: 'bioConceptCode'])

                // mouse sources
		wizard.mouseSources = ConceptCode.findAllByCodeTypeName(
		    MOUSE_SOURCE_CATEGORY, [sort: 'bioConceptCode'])

                // tissue types
		wizard.tissueTypes = ConceptCode.findAllByCodeTypeName(
		    TISSUE_TYPE_CATEGORY, [sort: 'bioConceptCode'])

                // experiment types
		wizard.expTypes = ConceptCode.findAllByCodeTypeName(
		    EXP_TYPE_CATEGORY, [sort: 'bioConceptCode'])

                // technology platforms
		wizard.platforms = BioAssayPlatform.executeQuery('''
					from BioAssayPlatform as p
					where p.vendor is not null
					order by p.vendor, p.array, p.accession''')
//					and p.accession in (select distinct id from DeGplInfo)

                // compounds
		wizard.compounds = Compound.executeQuery('''
					from Compound c
					where c.brandName is not null
					   or c.genericName is not null
					order by codeName''')

                break

            case 3:
                // normalization methods
		wizard.normMethods = ConceptCode.findAllByCodeTypeName(
		    NORM_METHOD_CATEGORY, [sort: 'bioConceptCode'])
		wizard.normMethods << genericOtherConceptCode

                // analytic categories
		wizard.analyticTypes = ConceptCode.findAllByCodeTypeName(
		    ANALYTIC_TYPE_CATEGORY, [sort: 'bioConceptCode'])
		wizard.analyticTypes << genericOtherConceptCode

                // analysis methods
		wizard.analysisMethods = ConceptCode.findAllByCodeTypeName(
		    ANALYSIS_METHOD_CATEGORY, [sort: 'bioConceptCode'])
		wizard.analysisMethods << genericOtherConceptCode

                // file schemas
		wizard.schemas = GeneSignatureFileSchema.findAllBySupported(
		    true, [sort: 'name'])

                // p value cutoffs
		wizard.pValCutoffs = ConceptCode.findAllByCodeTypeName(
		    P_VAL_CUTOFF_CATEGORY, [sort: 'bioConceptCode'])

                // fold change metrics
		wizard.foldChgMetrics = ConceptCode.findAllByCodeTypeName(
		    FOLD_CHG_METRIC_CATEGORY, [sort: 'bioConceptCode'])
                break

            default:
                logger.warn 'invalid page requested!'
        }
    }

    private ConceptCode getGenericOtherConceptCode() {
	ConceptCode res = ConceptCode.findByBioConceptCodeAndCodeTypeName(
            GENERIC_OTHER_BIO_CONCEPT_CODE,
            GENERIC_OTHER_CODE_TYPE_NAME)
        if (!res) {
	    throw new IllegalStateException('Database does not contain generic "other" concept')
        }
        res
    }

    /**
     * testing this concept - attaches GeneSignature domain object to hibernate session between requests in the wizard
     */
    void mergeGeneSigInstToHibernate(WizardModelDetails wizard) {
	GeneSignature gs = wizard.geneSigInst
        // merge the current domain instance into the persistence context with the wizard changes
        wizard.geneSigInst = gs.merge()
    }

    private Map createExistingValues(int pageNum, GeneSignature gs) {
        switch (pageNum) {
            case 1:
                break

            case 2:
		return ['experimentTypeConceptCode.bioConceptCode': gs.experimentTypeConceptCode?.bioConceptCode ?: '',
			'sourceConceptCode.id': gs.sourceConceptCodeId ?: '',
			'sourceConceptCode.bioConceptCode': gs.sourceConceptCode?.bioConceptCode ?: '',
			'ownerConceptCode.id': gs.ownerConceptCodeId ?: '',
			'treatmentCompound.id': gs.treatmentCompoundId ?: '',
			'speciesConceptCode.id': gs.speciesConceptCodeId ?: '',
			'speciesConceptCode.bioConceptCode': gs.speciesConceptCode?.bioConceptCode ?: '',
			'techPlatform.id': gs.techPlatformId ?: '',
			'experimentTypeCellLine.cellLineName': gs.experimentTypeCellLine?.cellLineName ?: '',
			'experimentTypeCellLine.attcNumber': gs.experimentTypeCellLine?.attcNumber ?: '',
			'speciesMouseSrcConceptCode.id': gs.speciesMouseSrcConceptCodeId ?: '',
			'tissueTypeConceptCode.id': gs.tissueTypeConceptCodeId ?: '',
			'experimentTypeConceptCode.id': gs.experimentTypeConceptCodeId ?: '']
		
            case 3:
		return ['normMethodConceptCode.id': gs.normMethodConceptCodeId ?: '',
			'normMethodConceptCode.bioConceptCode': gs.normMethodConceptCode?.bioConceptCode ?: '',
			'analyticCatConceptCode.id': gs.analyticCatConceptCodeId ?: '',
			'analyticCatConceptCode.bioConceptCode': gs.analyticCatConceptCode?.bioConceptCode ?: '',
			'analysisMethodConceptCode.id': gs.analysisMethodConceptCodeId ?: '',
			'analysisMethodConceptCode.bioConceptCode': gs.analysisMethodConceptCode?.bioConceptCode ?: '',
			'pValueCutoffConceptCode.id': gs.pValueCutoffConceptCodeId ?: '',
			'fileSchema.id': gs.fileSchemaId ?: '',
			'foldChgMetricConceptCode.id': gs.foldChgMetricConceptCodeId ?: '']
                break
            default:
                logger.warn 'invalid page requested!'
        }

	[:]
    }

    def checkGene(String geneName) {
	SearchKeywordTerm skt = SearchKeywordTerm.findByKeywordTerm(geneName?.toUpperCase())
        SearchKeyword sk = skt?.searchKeyword
	if (sk && (sk?.dataCategory == 'GENE' || sk?.dataCategory == 'SNP')) {
            render '{"found": "' + sk.dataCategory + '"}'
        }
        else {
            render '{"found": "none"}'
        }
    }

    private void setFlashWarning(String message) {
	flash.message = '<div class="warning">' + message + '</div>'
    }

    private WizardModelDetails sessionWizard() {
	session[WIZ_DETAILS_ATTRIBUTE]
    }

    private void setSessionWizard(WizardModelDetails wizardModelDetails) {
	session.setAttribute WIZ_DETAILS_ATTRIBUTE, wizardModelDetails
    }

    private void removeSessionWizard() {
	session.removeAttribute WIZ_DETAILS_ATTRIBUTE
    }
}
