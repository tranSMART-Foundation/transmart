package com.recomdata.transmart.util

import spock.lang.Specification

class RUtilSpec extends Specification {
	void basicTest() {
		given:
		String t = "alea iacta est\u5050\"' \n\t\\\u0007\b\f\r\u000b"
		String escaped = RUtil.escapeRStringContent(t)

		expect:
		"alea iacta est\\u5050\\\"\\' \\n\\t\\\\\\a\\b\\f\\r\\v" == escaped
	}
}
