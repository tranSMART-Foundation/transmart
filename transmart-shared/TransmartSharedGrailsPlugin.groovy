class TransmartSharedGrailsPlugin {
    String version = '19.0'
    String grailsVersion = '2.5.4 > *'
    String title = 'Transmart Shared'
    String author = 'Burt Beckwith'
    String authorEmail = 'burt_beckwith@hms.harvard.edu'
    String description = '''\
Classes and artifacts usable by transmartApp and various plugins
'''
    String documentation = 'https://wiki.transmartfoundation.org/'
    String license = 'APACHE'
    def organization = [name: 'i2b2-tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',       email: 'ricepeterm@yahoo.co.uk']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-shared']
}
