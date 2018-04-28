package otg.grails.plugin.transmart.shared

import groovy.transform.CompileStatic

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Utils {
	private Utils() {
		// static only
	}

	static String asLikeLiteral(String s) {
		s.replaceAll(/[\\%_]/, '\\\\$0')
	}
}
