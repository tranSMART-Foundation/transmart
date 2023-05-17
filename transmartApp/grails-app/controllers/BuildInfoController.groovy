import grails.util.Environment
import grails.util.Metadata

import groovy.util.logging.Slf4j

import org.codehaus.groovy.grails.plugins.GrailsPluginManager

@Slf4j('logger')
class BuildInfoController {

    private static final List<String> buildInfoProperties = [
        'scm.version',
        'build.date',
        'build.timezone',
        'build.java',
        'env.os',
        'env.username',
        'env.computer',
        'env.proc.type',
	'env.proc.cores'].asImmutable()

    GrailsPluginManager pluginManager

    def index() {
	List<String> customProperties = [] + buildInfoProperties
//	logger.info 'BuildInfo customProperties size {}', customProperties.size()
	if (grailsApplication.config.buildInfo.properties.exclude) {
	    customProperties.removeAll grailsApplication.config.buildInfo.properties.exclude
//	    logger.info 'BuildInfo customProperties exclude "{}" size {}', grailsApplication.config.buildInfo.properties.exclude, customProperties.size()
        }
	
	if (grailsApplication.config.buildInfo.properties.include) {
	    customProperties.addAll grailsApplication.config.buildInfo.properties.include
//	    logger.info 'BuildInfo customProperties include "{}" size {}', grailsApplication.config.buildInfo.properties.include, customProperties.size()
        }

	[buildInfoProperties: customProperties.sort(),
	 envName            : Environment.current.name,
	 javaVersion        : System.getProperty('java.version'),
	 plugins            : pluginManager.allPlugins.sort({ it.name.toUpperCase() }),
	 warDeployed        : Metadata.current.isWarDeployed()]
    }
}
