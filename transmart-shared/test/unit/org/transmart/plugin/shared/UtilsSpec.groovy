package org.transmart.plugin.shared

import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class UtilsSpec extends Specification {

    void 'test asLikeLiteral'(String input, String expected) {

	expect:
	    Utils.asLikeLiteral(input) == expected

	where:
	    input | expected
	    ''            | ''
	    'foo'         | 'foo'
	    '\\'          | '\\\\'
	    '%'           | '\\%'
	    '_'           | '\\_'
	    '\\%'         | '\\\\\\%'
	    'f%\\_oo\\\\' | 'f\\%\\\\\\_oo\\\\\\\\'
    }
}
