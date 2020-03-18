import grails.util.Holders
import org.springframework.stereotype.Component

class TransmartGwasPlinkGrailsPlugin {
    def version = '19.0'
    def grailsVersion = '2.5.4 > *'
    def title = 'Transmart Gwas Plink Plugin'
    def author = 'Alexander Bondarev'
    def authorEmail = 'alexander.bondarev@thomsonreuters.com'
    def description = '''\
GWAS Plink integration plug-in
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk']]		      
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-gwas-plink']


    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        xmlns context: 'http://www.springframework.org/schema/context'

        context.'component-scan'('base-package': 'com.thomsonreuters.lsps.transmart.jobs') {
            context.'include-filter'(
                    type: 'annotation',
                    expression: Component.canonicalName)
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        def config = Holders.config
        if (config.grails.plugin.transmartGwasPlink.enabled) {
            def extensionsRegistry = ctx.getBean('transmartExtensionsRegistry')
            extensionsRegistry.registerAnalysisTabExtension('transmartGwasPlink', '/gwasPlink/loadScripts', 'addGwasPlinkAnalysis')
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
