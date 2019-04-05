package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayDataAnnotationSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_DATA_ANNOTATION'
		Table table = assertTable('BIO_ASSAY_DATA_ANNOTATION')

		// id composite: ['bioMarker', 'probeset']
		// probeset column: 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertCompoundPk table, BIO_MARKER_ID: 'bigint', BIO_ASSAY_FEATURE_GROUP_ID: 'bigint'

		// BioMarker bioMarker
		assertForeignKeyColumn table, 'BIO_MARKER', 'BIO_MARKER_ID',
				'bigint', false, true // table 'BIO_MARKER'

		// BioAssayFeatureGroup probeset
		// probeset column: 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_FEATURE_GROUP', 'BIO_ASSAY_FEATURE_GROUP_ID',
				'bigint', false, true // table 'BIO_ASSAY_FEATURE_GROUP'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
