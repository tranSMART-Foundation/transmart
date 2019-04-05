package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureSummaryDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_SUM_DATA'
		Table table = assertTable('BIO_LIT_SUM_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_SUM_DATA_ID'
		assertPk table, 'BIO_LIT_SUM_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String alterationType
		assertColumn table, 'ALTERATION_TYPE', 'varchar(255)'

		// String dataType
		assertColumn table, 'DATA_TYPE', 'varchar(255)'

		// String diseaseSite
		assertColumn table, 'DISEASE_SITE', 'varchar(255)'

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)'

		// String summary
		assertColumn table, 'SUMMARY', 'varchar(255)'

		// String target
		assertColumn table, 'TARGET', 'varchar(255)'

		// String totalAffectedCases
		assertColumn table, 'TOTAL_AFFECTED_CASES', 'varchar(255)'

		// String totalFrequency
		assertColumn table, 'TOTAL_FREQUENCY', 'varchar(255)'

		// String variant
		assertColumn table, 'VARIANT', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
