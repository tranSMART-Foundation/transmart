class GalaxyExportPluginGrailsPlugin {

    public static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

    def version = '19.0'
    def grailsVersion = '2.5.4 > *'
    def title = 'Galaxy Export Plugin'
    def author = 'Transmart Foundation'
    def authorEmail = 'support@transmartfoundation.org'
    def description = '''\
Provides a Galaxy Export tab to load data into a Galaxy instance
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def developers = [[ name: 'Axel Oehmichen', email: 'ao1011@imperial.ac.uk' ],
		      [ name: 'Peter Rice',     email: 'ricepeterm@yahoo.co.uk']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/galaxy-export-plugin']

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        def grailsApplication = ctx.getBean('grailsApplication')
        boolean galaxyEnabled = grailsApplication.config.com.galaxy.export.galaxyEnabled
        if (galaxyEnabled && ctx.containsBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)) {
            ctx.getBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)
                    .registerAnalysisTabExtension('galaxy-export-plugin',
                    '/galaxyExportPlugin/loadScripts', 'addGalaxyPanel', )
        }
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
