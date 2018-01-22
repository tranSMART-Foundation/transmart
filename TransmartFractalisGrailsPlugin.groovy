import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class TransmartFractalisGrailsPlugin {

	public static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

	def version = "0.1-SNAPSHOT"
	def grailsVersion = "2.3 > *"
    def title = 'Transmart Fractalis Plugin'
	def author = "Burt Beckwith & Sascha Herzinger"
	def authorEmail = "Burt_Beckwith@hms.harvard.edu & Sascha_Herzinger@hms.harvard.edu"
	def description = 'A plugin to connect i2b2-tranSMART with https://git-r3lab.uni.lu/Fractalis'
	def documentation = ''
    def license = 'APACHE'
    def organization = [name: '', url: '']
    def issueManagement = [url: 'TODO' ]
    def scm = [url: 'TODO']

	def doWithApplicationContext = { ctx ->
		File fractalisDir = GrailsPluginUtils.getPluginDirForName('transmart-fractalis')?.file
		if (!fractalisDir) {
			String pluginPath = ctx.pluginManager.allPlugins.find {
				it.name == 'transmart-fractalis'
			}.pluginPath

			fractalisDir = ctx.getResource(pluginPath).file
		} else {
			fractalisDir = new File(fractalisDir, 'web-app')
		}
		if (!fractalisDir) {
			throw new RuntimeException('Could not determine directory for transmart-fractalis plugin')
		}

		if (ctx.containsBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)) {
			ctx.getBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME).registerAnalysisTabExtension(
                    'transmart-fractalis', '/Fractalis/loadScripts', 'addFractalisPanel')
		}

	}
}