class TransmartMetacorePluginGrailsPlugin {
    def version = '16.4-SNAPSHOT'
    def grailsVersion = '2.5.4 > *'
    def title = 'Transmart Metacore Plugin'
    def author = 'Valeria Hohlova'
    def description = 'Transmart MetaCore support'
    def documentation = 'TODO'
    def license = 'GPL3'
    def organization = [name: 'TODO', url: 'TODO']
    def developers = [[name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
    def issueManagement = [system: 'TODO', url: 'TODO']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart-metacore-plugin']

    def doWithApplicationContext = { ctx ->
	if (application.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable) {
	    ctx.getBean('transmartExtensionsRegistry').registerAnalysisTabExtension(
		'transmart-metacore-plugin',
		'/MetacoreEnrichment/loadScripts',
		'loadMetaCoreEnrichment')
	}
    }
}
