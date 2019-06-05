package org.transmart.plugin.custom

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.codec.Hex
import org.springframework.util.FileCopyUtils

import javax.servlet.http.HttpServletResponse
import java.security.MessageDigest

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class CmsService {

    static transactional = false

    private static final String CACHE_CONTROL = 'public, max-age=' + 60 * 60 * 24 * 365 // 1 year
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

    void sendFile(String name, HttpServletResponse response) {

	CmsFile cmsFile = findFile(name)
	if (!cmsFile) {
	    logger.error 'CmsFile not found with name "{}"', name
	    response.sendError SC_NOT_FOUND
	    return
	}

	response.contentLength = cmsFile.bytes.length
	response.contentType = cmsFile.contentType
	response.setDateHeader 'Last-Modified', cmsFile.lastUpdated.time
	response.setHeader 'ETag', 'W/"' + fileVersion(cmsFile) + '"'
	response.setHeader 'Cache-Control', CACHE_CONTROL

	FileCopyUtils.copy new ByteArrayInputStream(cmsFile.bytes), response.outputStream
    }

    String fileVersion(String name) {
	fileVersion findFile(name)
    }

    String fileVersion(CmsFile cmsFile) {
	cmsFile ? md5(cmsFile.name + '\n' + cmsFile.lastUpdated.time) : ''
    }

    private String md5(String s) {
	new String(Hex.encode(MessageDigest.getInstance('MD5').digest(s.bytes)))
    }

    @CompileDynamic
    private CmsFile findFile(String name) {
	CmsFile.findByNameAndInstanceType(name, customizationConfig.instanceType, [cache: true])
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
