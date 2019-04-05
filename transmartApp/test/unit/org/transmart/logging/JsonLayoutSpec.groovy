/*
 * Copyright © 2013-2016 The Hyve B.V.
 *
 * This file is part of Transmart.
 *
 * Transmart is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Transmart.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmart.logging

import groovy.json.JsonSlurper
import org.apache.log4j.Category
import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import spock.lang.Specification

class JsonLayoutSpec extends Specification {

	void 'test single line'() {
		when:
		JsonLayout j = new JsonLayout(singleLine: true, conversionPattern: '%m%n')

		then:
		j.format(makeEvent([1, 2, 3])) == String.format('[1,2,3]%n')
		j.format(makeEvent([foo: 'bar', baz: 42, qux: null])) == String.format('{"foo":"bar","baz":42,"qux":null}%n')
	}

	void 'test multi line'() {
		when:
		JsonLayout j = new JsonLayout(conversionPattern: '%m')
		List<Integer> obj = [1, 2, 3]

		then:
		new JsonSlurper().parseText(j.format(makeEvent(obj))) == obj

		when:
		Map obj2 = [foo: 'bar', baz: 42, qux: null]

		then:
		new JsonSlurper().parseText(j.format(makeEvent(obj2))) == obj2
	}

	void 'test date'() {
		when:
		Date d = new Date(1454412462729)
		JsonLayout j = new JsonLayout(conversionPattern: '%m')
		TimeZone defaultzone = TimeZone.default

		TimeZone.setDefault TimeZone.getTimeZone('GMT+1')

		then:
		j.format(makeEvent(d)) == '"2016-02-02 12:27:42.729+01"'

		cleanup:
		TimeZone.setDefault defaultzone
	}

	private LoggingEvent makeEvent(msg) {
		new LoggingEvent('', new Category('debug'), Level.DEBUG, msg, null)
	}
}
