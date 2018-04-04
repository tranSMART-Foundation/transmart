import org.springframework.context.ApplicationContext
import org.transmart.plugin.custom.CustomizationConfig

class TransmartCustomGrailsPlugin {
	String version = '18.1-SNAPSHOT'
	String grailsVersion = '2.3 > *'
	String title = 'Transmart Customization Plugin'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt_beckwith@hms.harvard.edu'
	String description = 'Transmart Customization Plugin'
	String documentation = 'TODO'
	String license = 'APACHE'
	def organization = [name: 'TODO', url: 'TODO']
	def issueManagement = [url: 'TODO']
	def scm = [url: 'TODO']

	def doWithSpring = {
		customizationConfig(CustomizationConfig)
	}

	def doWithApplicationContext = { ApplicationContext ctx ->
		CustomizationConfig customizationConfig = ctx.customizationConfig
		customizationConfig.init()
	}
}
