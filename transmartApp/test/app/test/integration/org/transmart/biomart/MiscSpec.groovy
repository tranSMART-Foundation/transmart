package org.transmart.biomart

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class MiscSpec extends AbstractDomainSpec {

	void 'call populateData to ensure that simple instance persistence works'() {
		expect:
		populateData()
	}
}
