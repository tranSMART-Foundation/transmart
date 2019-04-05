import grails.util.Environment
import grails.util.Metadata
import org.codehaus.groovy.grails.plugins.GrailsPluginManager

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
	if (grailsApplication.config.buildInfo.exclude) {
	    customProperties.removeAll grailsApplication.config.buildInfo.exclude
        }
	if (grailsApplication.config.buildInfo.include) {
	    customProperties.addAll grailsApplication.config.buildInfo.include
        }

	[buildInfoProperties: customProperties.sort(),
	 envName            : Environment.current.name,
	 javaVersion        : System.getProperty('java.version'),
	 plugins            : pluginManager.allPlugins.sort({ it.name.toUpperCase() }),
	 warDeployed        : Metadata.current.isWarDeployed()]
    }
}
