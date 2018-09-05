import org.transmart.searchapp.ImportXnatConfiguration;
import org.transmart.searchapp.ImportXnatVariable;
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

/**
 * ImportXnat controller.
 */
class ImportXnatController {

	def grailsApplication

	// the delete, save and update actions only accept POST requests
	static Map allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

	def index = {
		redirect action: list, params: params
	}

	def list = {
		if (!params.max) {
			params.max = grailsApplication.config.com.recomdata.admin.paginate.max
		}
		[importXnatConfigurationList: ImportXnatConfiguration.list(params)]
	}

	def show = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect action:list
			return
		}
		[importXnatConfiguration: importXnatConfiguration]
	}

	def delete = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect action:list
			return
		}

		importXnatConfiguration.delete()

		flash.message = "Import XNAT Configuration $params.id deleted."
		redirect(action: list)
	}

	def delete_coupling = {
		def importXnatVariable = ImportXnatVariable.get(params.id)
		if (!importXnatVariable) {
			flash.message = "Import XNAT Variable not found with id $params.id"
			redirect action: create_coupling, id: params.configId
			return
		}

		importXnatVariable.delete()

		flash.message = "Variable $params.id deleted."
		redirect action: create_coupling, id: params.configId
	}

	def edit = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: list)
			return
		}

		[importXnatConfiguration: importXnatConfiguration]
	}

	/**
	 * Update action, called when an existing Requestmap is updated.
	 */
	def update = {
		def importXnatConfiguration= ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: edit, id :params.id)
			return
		}

		long version = params.version.toLong()
		if (importXnatConfiguration.version > version) {
                        importXnatConfiguration.errors.rejectValue 'version', "importxnatconfiguration.optimistic.locking.failure",
				"Another user has updated this Configuration while you were editing."
			render view: 'edit', model: [importXnatConfiguration: importXnatConfiguration]
			return
		}

		importXnatConfiguration.properties = params
		if (importXnatConfiguration.save()) {
                        redirect action: show, id: importXnatConfiguration.id
		}
		else {
			render view: 'edit', model: [importXnatConfiguration: importXnatConfiguration]
		}
	}

	def create = {
		[importXnatConfiguration: new ImportXnatConfiguration(params)]
	}

	def create_coupling = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariable = new ImportXnatVariable(params)

		if (!importXnatConfiguration) {
                        flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: list)
			return
		}

		def importXnatVariableList = importXnatConfiguration.variables

                [importXnatVariable: importXnatVariable, importXnatVariableList: importXnatVariableList, importXnatConfiguration: importXnatConfiguration]
	}

	/**
	 * Save action, called when a new Requestmap is created.
	 */
	def save = {
		def importXnatConfiguration = new ImportXnatConfiguration(params)
		if (importXnatConfiguration.save()) {
			redirect action: create_coupling, id: importXnatConfiguration.id
		}
		else {
			render view: 'create', model: [importXnatConfiguration: importXnatConfiguration]
		}
	}
	
	def save_coupling = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariable = new ImportXnatVariable(params)
		def importXnatVariableList = importXnatConfiguration.variables
		importXnatVariable.configuration = importXnatConfiguration

		if (!importXnatVariable.save()) {
			render view: 'create_coupling', model: [importXnatVariable: importXnatVariable, importXnatConfiguration: importXnatConfiguration, importXnatVariableList: importXnatVariableList]
		} else {
			redirect action: create_coupling, id: importXnatConfiguration.id
		}
	}

	def import_wizard = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		if (!importXnatConfiguration) {
			flash.message = "Import XNAT Configuration not found with id $params.id"
			redirect(action: list)
			return
		}

		[importXnatConfiguration: importXnatConfiguration]
	}

	def import_variables = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		
		def password = params.password
		if (password == "") {
			flash.message = "Please enter XNAT password"
			redirect action: import_wizard, id: importXnatConfiguration.id
		} else {
		
			export()

			def url = importXnatConfiguration.url
			def username = importXnatConfiguration.username
			def project = importXnatConfiguration.project
			def node = importXnatConfiguration.node
			def datadir = (getTransmartWorkingdir() + "/xnattotransmartlink/")
			def etldir = getTransmartETLdir()
			def kitchendir = getTransmartKitchendir()
			def scriptdir = (getScriptsLocation() + "/xnattotransmartlink/")
			def kettlehome = getKettleHome()

                        // make any missing datadir levels
                        def testFile = new File(datadir)
                        if(!testFile.exists()){
                            testFile.mkdirs()
                        }

			def cmd = ["python",
                                   getScriptsLocation() + "/xnattotransmartlink/downloadscript.py",
                                   url, username, password, project, node, datadir, kitchendir, etldir, scriptdir, kettlehome]
			log.info("Running cmd ${cmd}")
                        def process = new ProcessBuilder(cmd).directory(new File(datadir)).start()
                        log.info("process started")
			process.waitFor()
                        log.info("process ended")
			def inText = process.in.text
			def errText = process.err.text
			if (errText) {
				log.error(errText)
				flash.message = "${errText}</br>${inText}"
			} else {
				log.info(inText)
				flash.message = inText
			}
			redirect action: import_wizard, id: importXnatConfiguration.id
		}

		return
	}

	def export = {
		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariableList = importXnatConfiguration.variables
		def project = importXnatConfiguration.project
		def xmlFile =  (getTransmartWorkingdir() + "/xnattotransmartlink/${project}.xml")
                def writer = new FileWriter(new File(xmlFile))
		def xml = new MarkupBuilder(writer)

		xml.project(name: project) {
			variables {
				importXnatVariableList.each{item->
					variable(
						name: item.name,
						dataType: item.datatype,
						url: item.url
					)
				}
				
			}
		}
	}

	def downloadXml = {
		export();

		def importXnatConfiguration = ImportXnatConfiguration.get(params.id)
		def importXnatVariable = new ImportXnatVariable(params)
		def importXnatVariableList = importXnatConfiguration.variables
		def project = importXnatConfiguration.project

		def xmlFile = (getTransmartWorkingDir() + "/xnattotransmartlink/${project}.xml")

		def file = new File(xmlFile)
		response.setContentType("application/xml;charset='utf8'")
		response.setHeader("Content-disposition", "attachment;filename=${file.getName()}")
		response.outputStream << file.newInputStream()
		
		render view: 'create_coupling', model: [importXnatVariable: importXnatVariable, importXnatConfiguration: importXnatConfiguration, importXnatVariableList: importXnatVariableList]
	}

	def getTransmartDataLocation = {
            def dir = grailsApplication.config.org.transmart.data.location
            if (dir.isEmpty()) {
                dir = grailsAttributes.getApplicationContext().getResource("/").getFile().getParentFile().getParentFile().toString() + "/transmart-data"
                if (dir.isEmpty()) {
                    log.info("getTransmartDataLocation no value, set empty")
                    dir = ""
                }
            }
            return dir
	}

	def getTransmartETLdir = {
            def dir = grailsApplication.config.org.transmart.importxnatplugin.etldir
            if (dir.isEmpty()) {
                dir = getTransmartDataLocation()+"/env/tranSMART-ETL/Kettle/postgres/Kettle-ETL"
                if (dir.isEmpty()) {
                    log.info("getTransmartETLdir no value, set empty")
                    dir = ""
                }
            }
            return dir
	}

	def getTransmartKitchendir = {
            def dir = grailsApplication.config.org.transmart.importxnatplugin.etldir
            if (dir.isEmpty()) { 
                dir = getTransmartDataLocation()+"/env/data-integration"
                if (dir.isEmpty()) {
                    log.info("getTransmartKitchendir no value, set empty")
                    dir = ""
                }
            }
            return dir
	}

	def getKettleHome = {
            def dir = grailsApplication.config.org.transmart.importxnatplugin.kettlehome
            if (dir.isEmpty()) {
                dir = getTransmartDataLocation()+"/samples/postgres/kettle-home"
                if (dir.isEmpty()) {
                    log.info("getKettleHome no value, set empty")
                    dir = ""
                }
            }
            return dir
	}

	def getTransmartWorkingdir = {
            def dir = grailsApplication.config.org.transmart.importxnatplugin.workingdir

            if (dir.isEmpty()) {
                dir = grailsApplication.config.RModules.tempFolderDirectory
                if (dir.isEmpty()) {
                    log.info("getTransmartWorkingdir no value, set empty")
                    dir = ""
                }
            }

            return dir
	}

	def getScriptsLocation = {
            def servletContext = grailsApplication.mainContext.servletContext
            def tsAppRScriptsDir
            // Find the top level directory
            def basePath = ((String[])[
                                servletContext.getRealPath("/"),
                                servletContext.getRealPath("/") + "../",
                                servletContext.getResource("/")?.file,
                                "webapps${servletContext.contextPath}",
                                "web-app/"
                            ]).find { obj ->
                                obj && (tsAppRScriptsDir = new File(obj, 'dataExportRScripts')).isDirectory()
            }
            File xnatImportModulesDir = GrailsPluginUtils.getPluginDirForName('transmart-xnat-importer')?.file
            if (!xnatImportModulesDir) {
                // it actually varies...
                xnatImportModulesDir = GrailsPluginUtils.getPluginDirForName('transmartXnatImporterPlugin')?.file
            }
            if (!xnatImportModulesDir) {
                String version = grailsApplication.mainContext.pluginManager.allPlugins.find {
                    it.name == 'transmart-xnat-importer' || it.name == 'transmartXnatImporter'
                }?.version
                xnatImportModulesDir = new File("$basePath/plugins", "transmart-xnat-importer-${version}")
            }
            if (!xnatImportModulesDir) {
                throw new RuntimeException('Could not determine directory for ' +
                                           'transmart-xnat-importer plugin')
            }

            def dir = "";

            if(xnatImportModulesDir) {
                dir = xnatImportModulesDir.getPath()
            }

            if (!xnatImportModulesDir) {
                dir = grailsApplication.config.org.transmart.importxnatplugin.location
                if (dir.isEmpty()) {
                    dir = grailsAttributes.getApplicationContext().getResource("/").getFile().getParentFile().getParentFile().toString() + "/xnattotransmartlink/"
                    if (dir.isEmpty()) {
                        log.info("getScriptsLocation no value, set empty")
                        dir = ""
                    }
                }
            }

            if(dir != "") {
                def testFile = new File(dir)
                if(!testFile.exists()){
                    testFile.mkdirs()
                }
            }

            return dir
	}
}
