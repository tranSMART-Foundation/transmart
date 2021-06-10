class BiomartDomainGrailsPlugin {
    def version = '19.1'
    def grailsVersion = '2.5,4 > *'
    def author = 'David Newton'
    def authorEmail = 'davinewton@deloitte.com'
    def title = 'tranSMART Biomart Domain Objects'
    def description = '''\
Domain objects for the tranSMART Biomart schema
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk'],
		      [name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/biomart-domain']

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
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
}
