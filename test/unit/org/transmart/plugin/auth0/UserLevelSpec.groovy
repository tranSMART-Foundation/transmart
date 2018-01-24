package org.transmart.plugin.auth0

import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class UserLevelSpec extends Specification {

	void 'verify that the order is correct for the Comparable interface'() {
		expect:
		UserLevel.values().length == 5
		UserLevel.ADMIN > UserLevel.TWO
		UserLevel.TWO > UserLevel.ONE
		UserLevel.ONE > UserLevel.ZERO
		UserLevel.ZERO > UserLevel.UNREGISTERED
	}
}
