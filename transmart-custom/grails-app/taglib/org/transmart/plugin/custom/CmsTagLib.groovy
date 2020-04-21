package org.transmart.plugin.custom

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Slf4j('logger')
class CmsTagLib {

    static namespace = 'cms'

    private @Autowired CmsService cmsService

    /**
     * Wraps a block of GSP code to be replaced with instance-specific code from
     * the database if there's an instance type match.
     *
     * @attr name REQUIRED the section name
     */
    def section = { Map attrs, Closure body ->
	String name = assertAttribute('name', attrs, 'render')
	Closure renderer = cmsService.renderer(name)
	logger.debug 'Cms section name {}', name
	if (renderer) {
	    logger.debug 'renderer defined for section {}', name
	    renderer.delegate = delegate
	    renderer.call()
	}
	else if (body) {
	    logger.debug 'no renderer, return body {}', body()
	    out << body()
	}
    }

    /**
     * Renders a link to download a CmsFile with a cache-busting querystring.
     *
     * @attr name REQUIRED the CmsFile name
     */
    def link = { Map attrs ->
	String name = assertAttribute('name', attrs, 'link')
	logger.debug 'Cms link name {} fileVersion {}', name, cmsService.fileVersion(name)
	out << createLink(uri: '/cms/file/' + name + '?v=' + cmsService.fileVersion(name))
    }

    def image = { Map attrs ->
	String name = assertAttribute('name', attrs, 'link')
	logger.debug 'Cms image name {} fileVersion {}', name, cmsService.fileVersion(name)
	CmsFile file = cmsService.findFile(name)
//	logger.debug 'data:{}'+';base64,{}', file.contentType, file.bytes.encodeBase64().toString()
	out << 'data:'+file.contentType+';base64,'+file.bytes.encodeBase64().toString()
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
