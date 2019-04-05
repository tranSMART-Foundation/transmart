package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_DATA'
		Table table = assertTable('BIO_ASSAY_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_DATA_ID'
		assertPk table, 'BIO_ASSAY_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioAssayDatasetId
		assertColumn table, 'BIO_ASSAY_DATASET_ID', 'bigint'

		// Long bioAssayId
		assertColumn table, 'BIO_ASSAY_ID', 'bigint'

		// Long bioSampleId
		assertColumn table, 'BIO_SAMPLE_ID', 'bigint'

		// Experiment experiment
		// experiment column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// String featureGroupName
		assertColumn table, 'FEATURE_GROUP_NAME', 'varchar(255)'

		// Double floatValue
		assertColumn table, 'FLOAT_VALUE', 'double'

		// Double log10Value
		// log10Value column: 'LOG10_VALUE'
		assertColumn table, 'LOG10_VALUE', 'double'

		// Double log2Value
		// log2Value column: 'LOG2_VALUE'
		assertColumn table, 'LOG2_VALUE', 'double'

		// Long numericValue
		assertColumn table, 'NUMERIC_VALUE', 'bigint'

		// String textValue
		assertColumn table, 'TEXT_VALUE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
