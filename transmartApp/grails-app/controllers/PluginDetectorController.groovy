import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.springframework.beans.factory.annotation.Autowired

@Slf4j('logger')
class PluginDetectorController {

    @Autowired private GrailsPluginManager pluginManager

    def checkPlugin(String pluginName) {
	logger.info 'checkPlugin {}: {}', pluginName, pluginManager.hasGrailsPlugin(pluginName)
	render pluginManager.hasGrailsPlugin(pluginName)
    }
}
