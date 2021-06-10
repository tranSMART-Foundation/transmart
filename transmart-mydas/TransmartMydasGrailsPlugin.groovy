class TransmartMydasGrailsPlugin {
    def version = '19.1'
    def grailsVersion = '2.5.4 > *'
    def title = 'Transmart Mydas Plugin'
    def author = 'Ruslan Forostianov'
    def authorEmail = 'ruslan@thehyve.nl'
    def description = '''\
TranSMART front-end to MYDAS
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'APACHE'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-mydas']

    def doWithWebDescriptor = {xml ->
        def servletElement = xml.'servlet'

        def lastServlet = servletElement[servletElement.size() - 1]
        lastServlet + {
            'servlet' {
                'servlet-name'('MydasServlet')
                'servlet-class'('uk.ac.ebi.mydas.controller.MydasServlet')
            }
        }

        def mappingElement = xml.'servlet-mapping'

        def lastMapping = mappingElement[mappingElement.size() - 1]
        lastMapping + {
            'servlet-mapping' {
                'servlet-name'('MydasServlet')
                'url-pattern'('/das/*')
            }
        }
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
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
