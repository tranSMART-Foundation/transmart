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

	File tsAppRScriptsDir = null

	// Find and copy or define directory for dataExportRscripts
	// 1. Check parameter org.transmartproject.rmodules.deployment.dataexportRscripts
	//    If found, copy scripts to this directory
	// 2. Find production scripts in war file path
	// 3. Find source directory if in development

	String desRval = config.org.transmartproject.rmodules.deployment.dataexportRscripts
	if (desRval) {
	    // 1. Found parameter
	    logger.info('Copy to org.transmartproject.rmodules.deployment.dataexportRscripts {}',
			 config.org.transmartproject.rmodules.deployment.dataexportRscripts)
	}

	logger.info('Searching for dataExportRScripts in paths')
	logger.info('servletContext.getRealPath("/"): {}', servletContext.getRealPath('/'))
	logger.info('servletContext.getRealPath("/")../: {}', servletContext.getRealPath('/')+'../')
	logger.info('servletContext.getResource("/")?.file: {}', servletContext.getResource('/')?.file)
	logger.info('"webapps" + servletContext.contextPath: {}', 'webapps' + servletContext.contextPath)
	logger.info('"web-app/": {}', 'web-app/')
	String basePath = [
	    servletContext.getRealPath('/'),
	    servletContext.getRealPath('/') + '../',
	    servletContext.getResource('/')?.file,
	    'webapps' + servletContext.contextPath,
	    'web-app/'
	].find { String s ->
	    s && (tsAppRScriptsDir = new File(s, 'dataExportRScripts')).isDirectory()
	}
	if (tsAppRScriptsDir && tsAppRScriptsDir.isDirectory()) {
	    logger.info('basePath found dataExportRscripts directory {}', tsAppRScriptsDir.canonicalPath)
	}

	try { // find location of data export R scripts
	    if (Environment.current == Environment.PRODUCTION) {
		logger.info('config.org.transmartproject.rmodules.deployment.dataexportRscripts {}',
			    config.org.transmartproject.rmodules.deployment.dataexportRscripts)
		File targetDirectory = config.org.transmartproject.rmodules.deployment.dataexportRscripts as File
		logger.info('running in a production environment, try copyResources to {}', targetDirectory.canonicalPath)
		if (copyResources('WEB-INF/dataExportRscripts', targetDirectory)) {
		    logger.info('copyResources succeeded')
		    tsAppRScriptsDir = targetDirectory
		}
	    } else {	// non-production environment
		tsAppRScriptsDir = new File("src/main/resources/dataExportRScripts")
		logger.info('Not in production environment, look for Rscripts in {}', tsAppRScriptsDir.canonicalPath)
	    }

	    if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
                throw new RuntimeException('Could not determine directory for dataExportRScripts')
	    }
	    config.com.recomdata.transmart.data.export.rScriptDirectory = tsAppRScriptsDir.canonicalPath
	    logger.info('com.recomdata.transmart.data.export.rScriptDirectory = "{}"',
			config.com.recomdata.transmart.data.export.rScriptDirectory)
        } catch(Exception e) {
	    logger.warn('No location found for com.recomdata.transmart.data.export.rScriptDirectory: {}', e.message)
        }

	if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
	    logger.warn('Could not find dataExportRscripts directory by name, trying parameters')
	}
	if (tsAppRScriptsDir && tsAppRScriptsDir.isDirectory()) {
	    logger.info('Found dataExportRscripts directory {}', tsAppRScriptsDir.canonicalPath)
	}

        try { // find location of R scripts of RModules

            def rmoduleScriptDir

            if (Environment.current == Environment.PRODUCTION) {
                def targetDirectory = config.org.transmartproject.rmodules.deployment.rscripts as File
                if (copyResources('WEB-INF/Rscripts', targetDirectory)) {
                    rmoduleScriptDir = targetDirectory
                }
            } else {
                rmoduleScriptDir = new File('../Rmodules/src/main/resources/Rscripts')
            }

            if (!rmoduleScriptDir || !rmoduleScriptDir.isDirectory()) {
                throw new RuntimeException('Could not determine property for Rscript directory')
            }

            config.RModules.pluginScriptDirectory = rmoduleScriptDir.absolutePath
            logger.info('RModules.pluginScriptDirectory = {}', config.RModules.pluginScriptDirectory)

        } catch(Exception e) {
            logger.warn('No location found for RModules.pluginScriptDirectory: {}', e.message)
        }

	// At this point we assume config.RModules exists
	if (!config.RModules.containsKey('host')) {
	    config.RModules.host = '127.0.0.1'
	    logger.info('RModules.host fixed to localhost')
	}
	if (!config.RModules.containsKey('port')) {
	    config.RModules.port = 6311
	    logger.info('RModules.port fixed to default')
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
        logger.info('Copying resources from {} to {} ...', root, targetDirectory.absolutePath)
        ApplicationContext ctx = grailsApplication.getMainContext()
        def resources = ctx.getResources("${root}/**")
	logger.info('resources from getMainContext size {}', resources.size())
	logger.info('resources class {}', resources.getClass())
        try {
            if (!targetDirectory.exists()) {
                logger.info('Creating directory {}', targetDirectory.absolutePath)
                targetDirectory.mkdirs()
            }
	    int iref = 0;
            for (res in resources) {
		ires++
                def resource = res as ServletContextResource
                String filePath = resource.path - ('/' + root + '/')
		logger.info('copyResource resource {}/{}', ires, resources.size())
		logger.info('copyResource resource.path {}', resource.path)
		logger.info('copyResource filePath {}', filePath)
                File target = new File(targetDirectory, filePath)
		File targetPath = new File(target.canonicalPath)
		File targetDir = targetPath.getParentFile()
		logger.info('copyResource target {}', target.canonicalPath)
		logger.info('copyResource targetPath {}', targetPath.canonicalPath)
                if (targetPath.exists()) {
                    logger.info('Path already exists: {}', targetPath.absolutePath)
                } else {
                    if (filePath.endsWith('/')) {
                        logger.info('Creating directory {}', filePath.absolutePath)
                        target.mkdirs()
                    } else {
			if(!targetDir.exists()){
			    logger.info('Creating directory {} for new file', targetDir)
			    targetDir.mkdirs()
			}
			logger.info('Creating file {}', target)
                        target.createNewFile()
                        if (!target.canWrite()) {
                            logger.error('File {} not writeable', target.absolutePath)
                            return false
                        } else {
                            logger.info('Copying resource: {} to {}', resource.path, target.absolutePath)
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
