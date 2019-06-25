class GalaxyExportPluginGrailsPlugin {

    public static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

    // the plugin version
    def version = '19.0-SNAPSHOT'
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = '2.5.4 > *'
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            'grails-app/views/error.gsp'
    ]

    // TODO Fill in these fields
    def title = 'Galaxy Export Plugin' // Headline display name of the plugin
    def author = 'Transmart Foundation'
    def authorEmail = 'support@transmartfoundation.org'
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = 'http://github.com/tranSMART-Foundation/transmart'

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = 'GPL3'

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: 'My Company', url: 'http://www.my-company.com/' ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: 'Axel Oehmichen', email: 'ao1011@imperial.ac.uk' ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: 'JIRA', url: 'http://jira.grails.org/browse/GPMYPLUGIN' ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: 'http://svn.codehaus.org/grails-plugins/' ]

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
