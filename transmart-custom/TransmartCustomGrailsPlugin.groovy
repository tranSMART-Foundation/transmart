import org.springframework.context.ApplicationContext
import org.transmart.plugin.custom.CmsService
import org.transmart.plugin.custom.CustomizationConfig

class TransmartCustomGrailsPlugin {
    String version = '19.1'
    String grailsVersion = '2.5.4 > *'
    String title = 'tranSMART Customization Plugin'
    String author = 'Burt Beckwith'
    String authorEmail = 'burt_beckwith@hms.harvard.edu'
    String description = '''\
tranSMART Customization Plugin
'''
    String documentation = 'https://wiki.transmartfoundation.org/'
    String license = 'APACHE'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-custom']

    def doWithSpring = {
	customizationConfig(CustomizationConfig)
    }

    def doWithApplicationContext = { ApplicationContext ctx ->
	CustomizationConfig customizationConfig = ctx.customizationConfig
	customizationConfig.init()

	CmsService cmsService = ctx.cmsService
	cmsService.init()
    }
}
