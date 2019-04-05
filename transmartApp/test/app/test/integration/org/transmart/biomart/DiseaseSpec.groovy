package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class DiseaseSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DISEASE'
		Table table = assertTable('BIO_DISEASE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_DISEASE_ID'
		assertPk table, 'BIO_DISEASE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String ccsCategory
		assertColumn table, 'CCS_CATEGORY', 'varchar(255)'

		// String disease
		assertColumn table, 'DISEASE', 'varchar(255)'

		// String icd10Code
		// icd10Code column: 'ICD10_CODE'
		assertColumn table, 'ICD10_CODE', 'varchar(255)'

		// String icd9Code
		// icd9Code column: 'ICD9_CODE'
		assertColumn table, 'ICD9_CODE', 'varchar(255)'

		// String meshCode
		assertColumn table, 'MESH_CODE', 'varchar(255)'

		// String preferredName
		// preferredName column: 'PREFERED_NAME'
		assertColumn table, 'PREFERED_NAME', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
