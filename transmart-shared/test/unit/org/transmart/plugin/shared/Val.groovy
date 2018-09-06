package org.transmart.plugin.shared

import grails.validation.Validateable

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Validateable
class Val {
	String s
	Integer i

	static constraints = {
		s blank: false, nullable: false, maxSize: 20
		i nullable: true, min: 2, max: 5
	}
}
