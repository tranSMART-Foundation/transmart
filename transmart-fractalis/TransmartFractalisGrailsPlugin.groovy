class TransmartFractalisGrailsPlugin {

	private static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

	def version = '19.0-SNAPSHOT'
	def grailsVersion = '2.5.4 > *'
	def title = 'Transmart Fractalis Plugin'
	def author = 'Sascha Herzinger'
	def authorEmail = 'Sascha_Herzinger@hms.harvard.edu'
	def description = 'Connects i2b2-tranSMART with https://git-r3lab.uni.lu/Fractalis'
	def documentation = ''
	def license = 'APACHE'
	def organization = [name: '', url: '']
	def developers = [[name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
	def issueManagement = [url: 'TODO']
	def scm = [url: 'https://github.com/i2b2-tranSMART/transmart-fractalis']

	def doWithApplicationContext = { ctx ->
		if (true.is(application.config.fractalis.active) &&
				ctx.containsBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)) {
			ctx.getBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME).registerAnalysisTabExtension(
                    'transmart-fractalis', '/Fractalis/loadScripts', 'addFractalisPanel')
		}
	}
}
