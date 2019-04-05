package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioMarkerCorrelationMVSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_MARKER_CORREL_MV'
		Table table = assertTable('BIO_MARKER_CORREL_MV')

		// id column: 'MV_ID'
		assertPk table, 'MV_ID', AUTOINC

		// Long assoBioMarkerId
		assertColumn table, 'ASSO_BIO_MARKER_ID', 'bigint'

		// Long bioMarkerId
		assertColumn table, 'BIO_MARKER_ID', 'bigint'

		// String correlType
		assertColumn table, 'CORREL_TYPE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
