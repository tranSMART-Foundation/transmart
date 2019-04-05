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

import org.apache.commons.io.FileUtils
import org.apache.log4j.Category
import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static ChildProcessAppender.ChildFailedException

class ChildProcessAppenderSpec extends Specification {

	private static String TESTSTRING = 'hello world! testing org.transmart.logging.ChildProcessAppender\n'

	@Rule
	public TemporaryFolder temp = new TemporaryFolder()

	void 'test logging event'() {
		when:
		File output = temp.newFile('output')
		ChildProcessAppender p = new ChildProcessAppender(command: sh('cat >' + path(output)))
		LoggingEvent e = new LoggingEvent('', new Category('debug'), Level.DEBUG, [foo: 'bar', baz: 'quux'], null)
		p.doAppend e
		p.close()
		waitForChild p

		then:
		FileUtils.readFileToString(output) == String.format('{"foo":"bar","baz":"quux"}%n')
	}

	void 'test output'() {
		when:
		File output = temp.newFile('output')
		ChildProcessAppender p = new ChildProcessAppender(command: sh('cat > ' + path(output)))
		p.write TESTSTRING
		waitForChild p

		then:
		FileUtils.readFileToString(output) == TESTSTRING
	}

	void 'test fail'() {
		when:
		ChildProcessAppender p = new ChildProcessAppender(command: ['false'], restartLimit: 3, throwOnFailure: true)
		p.write TESTSTRING
		waitForChild p
		p.write TESTSTRING
		waitForChild p
		p.write TESTSTRING
		waitForChild p
		p.write TESTSTRING
		waitForChild p

		then:
		thrown ChildFailedException
	}

	void 'test restart'() {
		expect:
		do_testRestart 3, 15
	}

	void 'test restart limit'() {
		when:
		do_testRestart 5, 3

		then:
		thrown ChildFailedException
	}

	private boolean do_testRestart(int restarts, int limit) {
		File runcount = temp.newFile('count')
		File output = temp.newFile('output')
		FileUtils.writeStringToFile runcount, '0\n'
		String command = """
            countfile=${path(runcount)}
            count=`cat "\$countfile"`
            if [ "\$count" -le ${restarts} ]
            then
                echo `expr "\$count" + 1` > "\$countfile"
                exit
            else
                cat > ${path(output)}
            fi"""
		ChildProcessAppender p = new ChildProcessAppender(command: sh(command), restartLimit: limit, throwOnFailure: true)
		int count = -1
		while (count <= restarts) {
			p.write TESTSTRING
			String countstr = FileUtils.readFileToString(runcount).trim()
			if (countstr) {
				count = Integer.parseInt(countstr)
			}
		}
		p.write TESTSTRING
		p.close()
		waitForChild p

		// restarting a child process may lose some messages, so we can not be sure of how many copies of TESTSTRING there are
		FileUtils.readFileToString(output).contains TESTSTRING

		true
	}

	private List<String> sh(String cmd) {
		['sh', '-c', cmd]
	}

	// escape shell strings, based on http://stackoverflow.com/a/1250279/264177
	private String path(File file) {
		"'${file.path.replaceAll("'", "'\"'\"'").replaceAll("\\\\", "\\\\")}'"
	}

	private void waitForChild(ChildProcessAppender a) {
		a.input.close()
		a.process.waitFor()
	}
}
