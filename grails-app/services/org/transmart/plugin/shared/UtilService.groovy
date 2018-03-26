package org.transmart.plugin.shared

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

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
}
