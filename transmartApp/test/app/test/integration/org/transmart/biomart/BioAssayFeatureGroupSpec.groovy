package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayFeatureGroupSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_FEATURE_GROUP'
		Table table = assertTable('BIO_ASSAY_FEATURE_GROUP')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertPk table, 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String name
		// name column: 'FEATURE_GROUP_NAME'
		assertColumn table, 'FEATURE_GROUP_NAME', 'varchar(255)'

		// String type
		// name column: 'FEATURE_GROUP_NAME'
		assertColumn table, 'FEATURE_GROUP_TYPE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
