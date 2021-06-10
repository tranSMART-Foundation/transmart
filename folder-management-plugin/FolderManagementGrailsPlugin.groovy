class FolderManagementGrailsPlugin {
    def version = '19.1'
    def grailsVersion = '2.5.4 > *'
    def author = 'i2b2-tranSMART Foundation'
    def authorEmail = 'support@transmartfoundation.org'
    def title = 'Folder Management and Annotation for tranSMART'
    def description = '''\
Adds folder management features to tranSMART, allowing files to be attached to studies and analyses. Also contains annotation domain and controller
'''
    def documentation = 'https://github.com/tranSMART-Foundation/transmart'
    def license = 'GPL3'
    def organization = [name: 'i2b2-tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/folder-management-plugin']

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
