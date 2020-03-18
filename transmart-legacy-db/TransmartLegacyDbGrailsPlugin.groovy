class TransmartLegacyDbGrailsPlugin {
    def version = '19.0'
    def grailsVersion = '2.5.4 > *'
    def title = 'Transmart Legacy DB Plugin'
    def author = 'Florian Guitton'
    def authorEmail = 'f.guitton@imperial.ac.uk'
    def description = ''''\
Legacy components from early tranSMART releases
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk'],
		      [name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-legacy-db']
}
