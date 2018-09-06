package org.transmart.plugin.custom

import org.springframework.beans.factory.annotation.Autowired

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CmsTagLib {

	static namespace = 'cms'

	private @Autowired CmsService cmsService

	/**
	 * @attr name REQUIRED the section name
	 */
	def section = { Map attrs, Closure body ->
		String name = assertAttribute('name', attrs, 'render')
		Closure renderer = cmsService.renderer(name)
		if (renderer) {
			renderer.delegate = delegate
			renderer.call()
		}
		else if (body) {
			out << body()
		}
	}

	def getProperty(String name) {
		Map<String, ?> model = cmsService.model()
		if (model.containsKey(name)) {
			model[name]
		}
		else if (metaClass.hasProperty(this, name)) {
			metaClass.getProperty this, name
		}
		else {
			null
		}
	}

	private assertAttribute(String name, Map attrs, String tag) {
		if (!attrs.containsKey(name)) {
			throwTagError "Tag [${namespace}.$tag] is missing required attribute [$name]"
		}
		attrs.remove name
	}
}
