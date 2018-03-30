package org.transmart.plugin.shared

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class UtilService {

	@Autowired private MessageSource messageSource

	/**
	 * Resolves validation errors from message bundles and collects them by property name.
	 * @param o a domain class instance or other Validateable
	 * @return resolved error strings
	 */
	Map<String, List<String>> errorStrings(GroovyObject o, Locale locale = Locale.getDefault()) {
		Map<String, List<String>> stringsByField = [:].withDefault { [] }
		if (o.hasProperty('errors')) {
			Errors errors = (Errors) o['errors']
			for (FieldError fieldError in errors.fieldErrors) {
				stringsByField[fieldError.field] << messageSource.getMessage(fieldError, locale)
			}
		}
		[:] + stringsByField
	}

	void sendDownload(HttpServletResponse response, String contentType, String filename, byte[] content) {
		response.contentType = contentType
		response.setHeader 'Content-Disposition', 'attachment;filename=' + filename
		response.setHeader 'Content-Length', content.length as String
		response.outputStream << content
		response.outputStream.flush()
	}
}
