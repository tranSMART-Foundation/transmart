package org.transmart.plugin.shared

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.util.StreamUtils
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class UtilService {

	static transactional = false

	@Autowired private MessageSource messageSource

	/**
	 * Resolves validation errors from message bundles and collects them by property name.
	 * @param o a domain class instance or other Validateable
	 * @return resolved error strings
	 */
	Map<String, List<String>> errorStrings(GroovyObject o, Locale locale = Locale.getDefault()) {
		Map<String, List<String>> stringsByField = [:].withDefault { [] } as Map
		if (o.hasProperty('errors')) {
			Errors errors = (Errors) o['errors']
			for (FieldError fieldError in errors.fieldErrors) {
				stringsByField[fieldError.field] << messageSource.getMessage(fieldError, locale)
			}
		}
		[:] + stringsByField
	}

	void sendDownload(HttpServletResponse response, String contentType, String filename, byte[] content) {
		response.setHeader 'Content-Length', content.length as String
		sendDownload response, contentType, filename, new ByteArrayInputStream(content)
	}

	void sendDownload(HttpServletResponse response, String contentType, String filename, InputStream inputStream) {
		if (contentType) {
			response.contentType = contentType
		}
		response.setHeader 'Content-Disposition', 'attachment;filename=' + filename
		StreamUtils.copy inputStream, response.outputStream
		response.outputStream.flush()
	}
}
