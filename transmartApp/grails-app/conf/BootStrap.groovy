import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.RememberMeAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import org.springframework.web.context.support.ServletContextResource
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
	String val = config.com.recomdata.transmart.data.export.rScriptDirectory
	if (val) {
	    logger.warn 'com.recomdata.transmart.data.export.rScriptDirectory should not be explicitly set, value "{}" ignored', val
	}

	File tmAppRScriptsDir = null

	// Find and copy or define directory for dataExportRscripts
	// 1. Check parameter RModules.deployment.dataexportRscripts
	//    If found, copy scripts to this directory
	// 2. Find production scripts in war file path
	// 3. Find source directory if in development

	String desRval = config.RModules.deployment.dataexportRscripts

	String pluginRmodules
	String versionRmodules
	String pluginSmartR
	String pluginFractalis
	String versionSmartR
	String versionFractalis

	File rmodulesScriptDir

	// find plugin names
        grailsApplication.mainContext.pluginManager.allPlugins.each {
	    if(it.name == 'rdcRmodules' || it.name == 'rdc-rmodules') {
		pluginRmodules = it.name
		versionRmodules = it.version
		logger.warn 'pluginRmodules found: {} version {}', pluginRmodules, versionRmodules
	    }
	    if(it.name == 'smartR' || it.name == 'smart-r') {
		pluginSmartR = it.name
		versionSmartR = it.version
		logger.warn 'pluginSmartR found: {} version {}', pluginSmartR, versionSmartR
	    }
	    if(it.name == 'transmartFractalis' || it.name == 'transmart-fractalis') {
		pluginFractalis = it.name
		versionFractalis = it.version
		logger.warn 'pluginFractalis found: {} version {}', pluginFractalis, versionFractalis
	    }
	}

        try { // find location of R scripts of Rmodules, SmartR, Fractalis

	    // Rmodules scripts are in an Rscripts directory
	    // We need to point to them so that scripts can be launched

            if (Environment.current == Environment.PRODUCTION) {
		String sourceRscriptsPath = "plugins/rdc-rmodules-" + versionRmodules + '/Rscripts'
                File targetRscriptsDirectory = config.RModules.deployment.rscripts as File
                if (copyResources(sourceRscriptsPath, targetRscriptsDirectory)) {
                    rmodulesScriptDir = targetRscriptsDirectory
                }
            } else {
                rmodulesScriptDir = new File('../Rmodules/web-app/Rscripts')
            }

	    config.RModules.pluginScriptDirectory = rmodulesScriptDir.absolutePath

        } catch(Exception e) {
            logger.warn('No location found for RModules.pluginScriptDirectory: {}', e.message)
        }


	try { // find location of data export R scripts
	    if (Environment.current == Environment.PRODUCTION) {
		File targetExportDirectory = config.RModules.deployment.dataexportRscripts as File
		if (copyResources('WEB-INF/dataExportRscripts', targetExportDirectory)) {
		    logger.info('copyResources succeeded')
		    tmAppRScriptsDir = targetExportDirectory
		}
	    } else {	// non-production environment
		tmAppRScriptsDir = new File("src/main/resources/dataExportRScripts")
	    }

	    if (!tmAppRScriptsDir || !tmAppRScriptsDir.isDirectory()) {
                throw new RuntimeException('Could not determine directory for dataExportRScripts')
	    }

	    config.com.recomdata.transmart.data.export.rScriptDirectory = tmAppRScriptsDir.canonicalPath

        } catch(Exception e) {
	    logger.warn('No location found for com.recomdata.transmart.data.export.rScriptDirectory: {}', e.message)
        }

	if (!tmAppRScriptsDir || !tmAppRScriptsDir.isDirectory()) {
	    logger.warn('Could not find dataExportRscripts directory by name, trying parameters')
	}

	// Default values
	// make sure configuration values are defined
	// set default values if they are missing

 	// At this point we assume config.RModules exists
	if (!config.RModules.containsKey('host')) {
	    config.RModules.host = '127.0.0.1'
	}
	if (!config.RModules.containsKey('port')) {
	    config.RModules.port = 6311
	}

	// Making sure we have default timeout and heartbeat values
	// At this point we assume config.com.recomdata exists
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

    private boolean copyResources(String root, File targetDirectory) {
	int ires = 0
        ApplicationContext ctx = grailsApplication.getMainContext()
        def resources = ctx.getResources("${root}/**")
        try {
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs()
            }
	    int iref = 0;
            for (res in resources) {
		ires++
                def resource = res as ServletContextResource
                String filePath = resource.path - ('/' + root + '/')
                File target = new File(targetDirectory, filePath)
		File targetPath = new File(target.canonicalPath)
		File targetDir = targetPath.getParentFile()
                if (!targetPath.exists()) {
                    if (filePath.endsWith('/')) {
                        target.mkdirs()
                    } else {
			if(!targetDir.exists()){
			    targetDir.mkdirs()
			}
                        target.createNewFile()
                        if (!target.canWrite()) {
                            logger.error('File {} not writeable', target.absolutePath)
                            return false
                        } else {
                            target.withOutputStream { out_s ->
                                out_s << resource.inputStream
                                out_s.flush()
                            }
                        }
                    }
                }
            }
        } catch(IOException e) {
            logger.error('Error while copying: {}', e.message)
            return false
        }
        return true
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
		'with a breakpoint on the catch block in ConfigurationHelper::mergeInLocations().\n' +
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
