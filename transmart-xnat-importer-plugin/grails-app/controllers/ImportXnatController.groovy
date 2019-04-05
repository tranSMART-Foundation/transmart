import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.ImportXnatConfiguration
import org.transmart.searchapp.ImportXnatVariable

@Slf4j('logger')
class ImportXnatController implements InitializingBean {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired
    private UtilService utilService

    @Value('${org.transmart.importxnatplugin.location:}')
    private String importXnatPluginLocation

    @Value('${com.recomdata.admin.paginate.max:0}')
    private int paginateMax

    def list(Integer max) {
	if (!max) {
	    params.max = paginateMax
	}
	[importXnatConfigurationList: ImportXnatConfiguration.list(params),
	 ImportXnatConfigurationCount: ImportXnatConfiguration.count()]
    }

    def show(ImportXnatConfiguration importXnatConfiguration) {
	if (!importXnatConfiguration) {
	    flash.message = "Import XNAT Configuration not found with id $params.id"
	    redirect action: 'list'
	    return
	}
	[importXnatConfiguration: importXnatConfiguration]
    }

    def delete(ImportXnatConfiguration importXnatConfiguration) {
	if (importXnatConfiguration) {
	    importXnatConfiguration.delete()
	    flash.message = "Import XNAT Configuration $params.id deleted."
	}
	else {
	    flash.message = "Import XNAT Configuration not found with id $params.id"
	}

	redirect action: 'list'
    }

    def delete_coupling(ImportXnatVariable importXnatVariable) {
	if (!importXnatVariable) {
	    flash.message = "Import XNAT Variable not found with id $params.id"
	    redirect action: 'create_coupling', id: params.configId
	    return
	}

	importXnatVariable.delete()

	flash.message = "Variable $params.id deleted."
	redirect action: 'create_coupling', id: params.configId
    }

    def edit(ImportXnatConfiguration importXnatConfiguration) {
	if (!importXnatConfiguration) {
	    flash.message = "Import XNAT Configuration not found with id $params.id"
	    redirect action: 'list'
	    return
	}

	[importXnatConfiguration: importXnatConfiguration]
    }

    def update(ImportXnatConfiguration importXnatConfiguration) {
	if (!importXnatConfiguration) {
	    flash.message = "Import XNAT Configuration not found with id $params.id"
	    redirect action: 'edit', id: params.id
	    return
	}

	long version = params.long('version', 0)
	if (importXnatConfiguration.version > version) {
	    importXnatConfiguration.errors.rejectValue 'version', 'importxnatconfiguration.optimistic.locking.failure',
		'Another user has updated this Configuration while you were editing.'
	    render view: 'edit', model: [importXnatConfiguration: importXnatConfiguration]
	    return
	}

	importXnatConfiguration.properties = params
	if (importXnatConfiguration.save()) {
	    redirect action: 'show', id: importXnatConfiguration.id
	}
	else {
	    render view: 'edit', model: [importXnatConfiguration: importXnatConfiguration]
	}
    }

    def create() {
	[importXnatConfiguration: new ImportXnatConfiguration(params)]
    }

    def create_coupling(ImportXnatConfiguration importXnatConfiguration) {
	if (!importXnatConfiguration) {
	    flash.message = "Import XNAT Configuration not found with id $params.id"
	    redirect action: 'list'
	    return
	}

	[importXnatVariable: new ImportXnatVariable(params),
	 importXnatVariableList: importXnatConfiguration.variables,
	 importXnatConfiguration: importXnatConfiguration]
    }

    def save() {
	ImportXnatConfiguration importXnatConfiguration = new ImportXnatConfiguration(params)
	if (importXnatConfiguration.save()) {
	    redirect action: 'create_coupling', id: importXnatConfiguration.id
	}
	else {
	    render view: 'create', model: [importXnatConfiguration: importXnatConfiguration]
	}
    }
	
    def save_coupling(ImportXnatConfiguration importXnatConfiguration) {
	ImportXnatVariable importXnatVariable = new ImportXnatVariable(params)
	importXnatVariable.configuration = importXnatConfiguration

	if (!importXnatVariable.save()) {
	    render view: 'create_coupling', model: [
		importXnatVariable: importXnatVariable,
		importXnatConfiguration: importXnatConfiguration,
		importXnatVariableList: importXnatConfiguration.variables]
	}
	else {
	    redirect action: 'create_coupling', id: importXnatConfiguration.id
	}
    }

    def import_wizard(ImportXnatConfiguration importXnatConfiguration) {
	if (!importXnatConfiguration) {
	    flash.message = "Import XNAT Configuration not found with id $params.id"
	    redirect action: 'list'
	    return
	}

	[importXnatConfiguration: importXnatConfiguration]
    }

    def import_variables(ImportXnatConfiguration importXnatConfiguration, String password) {
	if (!password) {
	    flash.message = 'Please enter XNAT password'
	    redirect action: 'import_wizard', id: importXnatConfiguration.id
	    return
	}
		
	export()

	String cmd = 'python ' + importXnatPluginLocation + '/xnattotransmartlink/downloadscript.py' +
	    ' ' + importXnatConfiguration.url +
	    ' ' + importXnatConfiguration.username +
	    ' ' + password +
	    ' ' + importXnatConfiguration.project +
	    ' ' + importXnatConfiguration.node +
	    ' ' + importXnatPluginLocation + '/xnattotransmartlink/' + // kettledir
	    ' ' + importXnatPluginLocation + '/xnattotransmartlink/'   // datadir
	logger.debug cmd
	Process process = cmd.execute((List)null, new File(importXnatPluginLocation, 'xnattotransmartlink'))
	process.waitFor()
	String inText = process.in.text
	String errText = process.err.text
	if (errText) {
	    logger.error errText
	    flash.message = errText + '</br>' + inText
	}
	else {
	    logger.info inText
	    flash.message = inText
	}

	redirect action: 'import_wizard', id: importXnatConfiguration.id
    }

    def export(ImportXnatConfiguration importXnatConfiguration) {
	String project = importXnatConfiguration.project
	new MarkupBuilder(new FileWriter(new File(importXnatPluginLocation, 'xnattotransmartlink/' + project + '.xml'))).project(name: project) {
	    variables {
		for (ImportXnatVariable item in importXnatConfiguration.variables) {
		    variable name: item.name, dataType: item.datatype, url: item.url
		}
	    }
	}
    }

    def downloadXml(ImportXnatConfiguration importXnatConfiguration) {
	export()

	File file = new File(importXnatPluginLocation, 'xnattotransmartlink/' + importXnatConfiguration.project + '.xml')
	utilService.sendDownload response, "application/xml;charset='utf8'", file.name, file.newInputStream()

	render view: 'create_coupling', model: [
	    importXnatVariable     : new ImportXnatVariable(params),
	    importXnatConfiguration: importXnatConfiguration,
	    importXnatVariableList : importXnatConfiguration.variables]
    }

    void afterPropertiesSet() {
	if (!importXnatPluginLocation) {
	    importXnatPluginLocation = applicationContext.getResource('/').file.parentFile.parentFile.path +
		'/transmart-xnat-importer-plugin/scripts'
	}
    }
}
