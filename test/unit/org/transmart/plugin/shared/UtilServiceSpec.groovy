package org.transmart.plugin.shared

import grails.test.mixin.TestFor
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.validation.Errors
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(UtilService)
class UtilServiceSpec extends Specification {

	void setup() {
		service.messageSource = applicationContext
	}

	void 'test errorStrings'() {
		when:
		Val val = new Val()

		then:
		!val.validate()

		when:
		Errors errors = val.errors
		Map<String, List<String>> errorStrings = service.errorStrings(val)

		then:
		errors.fieldErrors.size() == 1
		errorStrings.size() == 1
		errorStrings.s.size() == 1
		errorStrings.s[0] == 'Property [s] of class [class org.transmart.plugin.shared.Val] cannot be null'

		when:
		val.s = ''
		val.i = 1

		then:
		!val.validate()

		when:
		errors = val.errors
		errorStrings = service.errorStrings(val)

		then:
		errors.fieldErrors.size() == 2
		errorStrings.size() == 2
		errorStrings.s.size() == 1
		errorStrings.s[0] == 'Property [s] of class [class org.transmart.plugin.shared.Val] cannot be blank'
		errorStrings.i.size() == 1
		errorStrings.i[0] == 'Property [i] of class [class org.transmart.plugin.shared.Val] with value [1] is less than minimum value [2]'

		when:
		val.s = 's'
		val.i = 10

		then:
		!val.validate()

		when:
		errors = val.errors
		errorStrings = service.errorStrings(val)

		then:
		errors.fieldErrors.size() == 1
		errorStrings.size() == 1
		errorStrings.i.size() == 1
		errorStrings.i[0] == 'Property [i] of class [class org.transmart.plugin.shared.Val] with value [10] exceeds maximum value [5]'

		when:
		val.i = 3

		then:
		val.validate()
		!val.errors.fieldErrors
		!service.errorStrings(val)
	}

	void 'test sendDownload'() {
		when:
		HttpServletResponse response = new MockHttpServletResponse()
		String contentType = 'text/plain'
		String filename = 'test.txt'
		String content = 'the content'
		service.sendDownload response, contentType, filename, content.bytes

		then:
		response.contentLength == content.length()
		response.contentType == contentType
		response.contentAsString == content
		response.getHeader('Content-Disposition').contains 'filename=' + filename
	}
}
