package org.transmart.plugin.custom

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class CmsService {

	static transactional = false

	private static final ThreadLocal<Map<String, ?>> MODEL = new ThreadLocal<>()

	private Map<RendererCacheKey, Closure> renderers = [:]

	@Autowired private CustomizationConfig customizationConfig
	@Autowired private GrailsApplication grailsApplication

	void init() {
		for (CmsSection cmsSection in CmsSection.list()) {
			if (customizationConfig.instanceType == cmsSection.instanceType) {
				GroovyShell groovyShell = new GroovyShell(grailsApplication.classLoader, new Binding())
				renderers[newRendererCacheKey(cmsSection.name)] = (Closure) groovyShell.evaluate(cmsSection.closure)
			}
		}
		logger.debug 'Cached renderers: {}', renderers.keySet()
	}

	Closure renderer(String name) {
		Closure c = renderers[newRendererCacheKey(name)]
		c ? (Closure) c.clone() : null
	}

	void setModel(Map<String, ?> model) {
		MODEL.set model
	}

	Map<String, ?> model() {
		MODEL.get()
	}

	void clearModel() {
		MODEL.remove()
	}

	private RendererCacheKey newRendererCacheKey(String name) {
		new RendererCacheKey(instanceType: customizationConfig.instanceType, name: name)
	}

	@CompileStatic
	@EqualsAndHashCode(cache = true)
	@Immutable
	private static class RendererCacheKey {
		String instanceType
		String name
	}
}
