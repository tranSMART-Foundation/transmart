import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.springframework.beans.factory.annotation.Autowired

class PluginDetectorController {

    @Autowired private GrailsPluginManager pluginManager

    def checkPlugin(String pluginName) {
	render pluginManager.hasGrailsPlugin(pluginName)
    }
}
