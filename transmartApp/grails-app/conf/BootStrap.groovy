import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.springframework.security.authentication.RememberMeAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import org.springframework.util.Assert
import org.transmart.plugin.shared.SecurityService
import org.transmartproject.security.OAuth2SyncService
import transmartapp.LoggingService

import javax.servlet.ServletContext
import java.util.logging.Level

@Slf4j('logger')
class BootStrap {

    GrailsApplication grailsApplication
    LoggingService loggingService
    OAuth2SyncService OAuth2SyncService
    SecurityContextPersistenceFilter securityContextPersistenceFilter
    SecurityService securityService

    def init = { ServletContext servletContext ->
	configureJwt()
	configureLogging()
	configureSecurity()
	checkConfigFine()
	fixupConfig servletContext
	forceMarshallerRegistrarInitialization()
	checkRequiredConfig()
    }

    private void fixupConfig(ServletContext servletContext) {
	ConfigObject config = grailsApplication.config

	// rScriptDirectory
	def val = config.com.recomdata.transmart.data.export.rScriptDirectory
	if (val) {
	    logger.warn 'com.recomdata.transmart.data.export.rScriptDirectory should not be explicitly set, value "{}" ignored', val
	}

	File tsAppRScriptsDir = null

	String basePath = [
	    servletContext.getRealPath('/'),
	    servletContext.getRealPath('/') + '../',
	    servletContext.getResource('/')?.file,
	    'webapps' + servletContext.contextPath,
	    'web-app/'
	].find { String s ->
	    s && (tsAppRScriptsDir = new File(s, 'dataExportRScripts')).isDirectory()
	}

	if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
	    throw new RuntimeException('Could not determine proper for com.recomdata.transmart.data.export.rScriptDirectory')
	}
	config.com.recomdata.transmart.data.export.rScriptDirectory = tsAppRScriptsDir.canonicalPath

	logger.info 'com.recomdata.transmart.data.export.rScriptDirectory = {}',
	    config.com.recomdata.transmart.data.export.rScriptDirectory

	// set RModules.external=true in config in cases like running
	// in Docker where a localhost temp directory isn't needed
	if (config.RModules.external != true) {
	    // RModules.pluginScriptDirectory
	    File rScriptsDir
	    val = config.RModules.pluginScriptDirectory
	    if (val) {
		rScriptsDir = new File(val.toString())
	    }
	    else {
		File rdcModulesDir = GrailsPluginUtils.getPluginDirForName('rdc-rmodules')?.file
		if (!rdcModulesDir) {
		    // it actually varies...
		    rdcModulesDir = GrailsPluginUtils.getPluginDirForName('rdcRmodules')?.file
		}
		if (!rdcModulesDir) {
		    String version = grailsApplication.mainContext.pluginManager.allPlugins.find {
			it.name == 'rdc-rmodules' || it.name == 'rdcRmodules'
		    }.version
		    rdcModulesDir = new File(basePath + '/plugins', 'rdc-rmodules-' + version)
		}
		if (!rdcModulesDir) {
		    throw new RuntimeException('Could not determine directory for rdc-rmodules plugin')
		}

		rScriptsDir = new File(rdcModulesDir, 'Rscripts')
		if (!rScriptsDir || !rScriptsDir.isDirectory()) {
		    rScriptsDir = new File(rdcModulesDir, 'web-app/Rscripts')
		}
		config.RModules.pluginScriptDirectory = rScriptsDir.canonicalPath
	    }

	    Assert.isTrue rScriptsDir.isDirectory(), 'RModules.pluginScriptDirectory value "' +
		config.RModules.pluginScriptDirectory + '" is not a directory'

	    String pluginScriptDirectory = config.RModules.pluginScriptDirectory
	    if (!pluginScriptDirectory.endsWith('/')) {
		pluginScriptDirectory += '/'
	    }
	    logger.info 'RModules.pluginScriptDirectory = {}', pluginScriptDirectory
	}

	// At this point we assume c.RModules exists
	if (!config.RModules.containsKey('host')) {
	    config.RModules.host = '127.0.0.1'
	    logger.info 'RModules.host fixed to localhost'
	}
	if (!config.RModules.containsKey('port')) {
	    config.RModules.port = 6311
	    logger.info 'RModules.port fixed to default'
	}

	// Making sure we have default timeout and heartbeat values
	// At this point we assume c.recomdata exists
	if (!config.com.recomdata.containsKey('sessionTimeout')) {
	    config.com.recomdata.sessionTimeout = 3600
	}
	if (!config.com.recomdata.containsKey('heartbeatLaps')) {
	    config.com.recomdata.heartbeatLaps = 60
	}
    }

    private void configureJwt() {
	UsernamePasswordAuthenticationToken.metaClass.getJwtToken = { -> securityService.jwtToken() }
	RememberMeAuthenticationToken.metaClass.getJwtToken = { -> securityService.jwtToken() }
    }

    private void configureLogging() {
	if (grailsApplication.config.grails.logging.jul.usebridge) {
	    Sql.LOG.level = Level.FINE
	}

	loggingService.duplicateSymlinkedAppenders()
    }

    private void configureSecurity() {
	securityContextPersistenceFilter.forceEagerSessionCreation = true

	SpringSecurityUtils.clientRegisterFilter(
	    'concurrentSessionFilter', SecurityFilterPosition.CONCURRENT_SESSION_FILTER)

	if (grailsApplication.config.org.transmart.security.samlEnabled) {
	    SpringSecurityUtils.clientRegisterFilter(
		'metadataGeneratorFilter', SecurityFilterPosition.FIRST)
	    SpringSecurityUtils.clientRegisterFilter(
		'samlFilter', SecurityFilterPosition.BASIC_AUTH_FILTER)
	}

	if ('clientCredentialsAuthenticationProvider' in
	    grailsApplication.config.grails.plugin.springsecurity.providerNames) {
	    OAuth2SyncService.syncOAuth2Clients()
	}
    }

    private void checkConfigFine() {
	if (!grailsApplication.config.org.transmart.configFine.is(true)) {
	    logger.error 'Something wrong happened parsing the externalized ' +
		'Config.groovy, because we could not find the ' +
		'''configuration setting 'org.transmart.configFine' ''' +
		'set to true.\n' +
		'Tip: on ~/.grails/transmartConfig, run\n' +
		'''groovy -e 'new ConfigSlurper().parse(new File("Config.groovy").toURL())'\n''' +
		'to detect compile errors. Other errors can be detected ' +
		'with a breakpoing on the catch block in ConfigurationHelper::mergeInLocations().\n' +
		'Alternatively, you can change the console logging settings by editing ' +
		'$GRAILS_HOME/scripts/log4j.properties, adding a proper appender and log ' +
		'org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper at level WARN'
	    throw new GrailsConfigurationException('Configuration magic setting not found')
	}
    }

    private void forceMarshallerRegistrarInitialization() {
	grailsApplication.mainContext.getBean 'marshallerRegistrarService'
    }

    private void checkRequiredConfig() {
	checkRequiredConfigString 'com.recomdata.adminEmail'
	checkRequiredConfigString 'com.recomdata.contactUs'
    }

    private void checkRequiredConfigString(String attributeName) {
	String value = ''
	Map flat = grailsApplication.config.flatten()
	if (flat.containsKey(attributeName)) {
	    value = flat[attributeName] ?: ''
	}
	Assert.hasLength value, 'Config setting "' + attributeName + '" is required; please specify a value in Config.groovy'
    }
}
