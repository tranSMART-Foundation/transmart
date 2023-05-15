import grails.util.Environment
import groovy.util.logging.Slf4j
import heim.SmartRRuntimeConstants
import heim.rserve.RScriptsSynchronizer
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component


@Slf4j('logger')
class smartRGrailsPlugin {

    public static final String DEFAULT_REMOTE_RSCRIPTS_DIRECTORY = '/tmp/smart_r_scripts'
    public static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

    def version = '19.1'
    def grailsVersion = '2.5.4 > *'

    def title = 'SmartR Plugin'
    def author = 'Sascha Herzinger'
    def authorEmail = 'sascha.herzinger@uni.lu'
    def description = '''\
SmartR is a grails plugin seeking to improve the visual analytics of the tranSMART platform by using recent web technologies such as d3 
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'APACHE'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/SmartR']

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        xmlns context:'http://www.springframework.org/schema/context'

        context.'component-scan'('base-package': 'heim') {
            context.'include-filter'(
                    type:       'annotation',
                    expression: Component.canonicalName)
        }
    }

    def doWithApplicationContext = { ctx ->
        def config = application.config
        SmartRRuntimeConstants constants = ctx.getBean(SmartRRuntimeConstants)

	File smartRDir

	if (Environment.current == Environment.PRODUCTION) {
            def resource = ctx.getResource("WEB-INF")
	    smartRDir = resource.getFile()
	} else {
            smartRDir = GrailsPluginUtils.getPluginDirForName('smart-r')?.file
            if (!smartRDir) {
		String pluginPath = ctx.pluginManager.allPlugins.find {
                    it.name == 'smartR'
		}.pluginPath
		
		smartRDir = ctx.getResource(pluginPath).file
            }
            else {
		smartRDir = new File(smartRDir, 'src/main/resources')
            }
	}
        if (!smartRDir) {
            throw new RuntimeException('Could not determine directory for ' +
                    'smart-r plugin')
        }

        constants.pluginScriptDirectory = new File(smartRDir.path, 'HeimScripts')
        logger.info('Directory for heim scripts is ' + constants.pluginScriptDirectory)

        if (!skipRScriptsTransfer(config)) {
            def remoteScriptDirectory =  config.smartR.remoteScriptDirectory
            if (!remoteScriptDirectory) {
                remoteScriptDirectory = DEFAULT_REMOTE_RSCRIPTS_DIRECTORY
            }
            constants.remoteScriptDirectoryDir = remoteScriptDirectory
            logger.info('Location for R scripts in the Rserve server is ' + constants.remoteScriptDirectoryDir)

            ctx.getBean(RScriptsSynchronizer).start()
        }
        else {
            logger.info('Skipping copying of R script in development mode with local Rserve')
            constants.remoteScriptDirectoryDir = constants.pluginScriptDirectory.absoluteFile
            ctx.getBean(RScriptsSynchronizer).skip()
        }

        if (ctx.containsBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)) {
            ctx.getBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)
                    .registerAnalysisTabExtension('smartR', '/smartR/loadScripts', 'addSmartRPanel')
        }

    }

    private boolean skipRScriptsTransfer(config) {
        (!config.RModules.host ||
                config.RModules.host in ['127.0.0.1', '::1', 'localhost']) &&
                Environment.currentEnvironment == Environment.DEVELOPMENT &&
                !config.smartR.alwaysCopyScripts
    }
}
