package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ObservationSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_OBSERVATION'
		Table table = assertTable('BIO_OBSERVATION')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_OBSERVATION_ID'
		assertPk table, 'BIO_OBSERVATION_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String code
		// code column: 'OBS_CODE'
		assertColumn table, 'OBS_CODE', 'varchar(255)'

		// String codeSource
		// codeSource column: 'OBS_CODE_SOURCE'
		assertColumn table, 'OBS_CODE_SOURCE', 'varchar(255)'

		// String description
		// description column: 'OBS_DESCR'
		assertColumn table, 'OBS_DESCR', 'varchar(255)'

		// Long etlId
		assertColumn table, 'ETL_ID', 'bigint'

		// String name
		// name column: 'OBS_NAME'
		assertColumn table, 'OBS_NAME', 'varchar(255)'

		// String type
		// type column: 'OBS_TYPE'
		assertColumn table, 'OBS_TYPE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
