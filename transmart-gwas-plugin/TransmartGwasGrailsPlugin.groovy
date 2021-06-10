class TransmartGwasGrailsPlugin {
    def version = '19.1'
    def grailsVersion = '2.5.4 > *'
    def title = 'tranSMART GWAS Plugin'
    def author = 'David Newton'
    def authorEmail = 'davinewton@deloitte.com'
    def description = '''\
Supports the display of GWAS data, including eQTL and metabolic GWAS
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk'],
    		      [name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-gwas-plugin']
}
