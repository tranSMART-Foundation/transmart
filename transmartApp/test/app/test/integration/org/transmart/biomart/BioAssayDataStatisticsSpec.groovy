package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayDataStatisticsSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_DATA_STATS'
		Table table = assertTable('BIO_ASSAY_DATA_STATS')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_FACT_ID'], column: 'BIO_ASSAY_DATA_STATS_ID'
		assertPk table, 'BIO_ASSAY_DATA_STATS_ID'
		assertSequence 'SEQ_BIO_DATA_FACT_ID'

		// BioAssayDataset dataset
		// dataset column: 'BIO_ASSAY_DATASET_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_DATASET', 'BIO_ASSAY_DATASET_ID' // table 'BIO_ASSAY_DATASET'

		// Experiment experiment
		// experiment column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// BioAssayFeatureGroup featureGroup
		// featureGroup column: 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_FEATURE_GROUP', 'BIO_ASSAY_FEATURE_GROUP_ID' // table 'BIO_ASSAY_FEATURE_GROUP'

		// String featureGroupName
		assertColumn table, 'FEATURE_GROUP_NAME', 'varchar(255)'

		// Double maxValue
		assertColumn table, 'MAX_VALUE', 'double'

		// Double meanValue
		assertColumn table, 'MEAN_VALUE', 'double'

		// Double minValue
		assertColumn table, 'MIN_VALUE', 'double'

		// Double quartile1
		// quartile1 column: 'QUARTILE_1'
		assertColumn table, 'QUARTILE_1', 'double'

		// Double quartile2
		// quartile2 column: 'QUARTILE_2'
		assertColumn table, 'QUARTILE_2', 'double'

		// Double quartile3
		// quartile3 column: 'QUARTILE_3'
		assertColumn table, 'QUARTILE_3', 'double'

		// BioSample sample
		// sample column: 'BIO_SAMPLE_ID'
		assertForeignKeyColumn table, 'BIO_SAMPLE', 'BIO_SAMPLE_ID' // table 'BIO_SAMPLE'

		// Long sampleCount
		// sampleCount column: 'BIO_SAMPLE_COUNT'
		assertColumn table, 'BIO_SAMPLE_COUNT', 'bigint'

		// Double stdDevValue
		assertColumn table, 'STD_DEV_VALUE', 'double'

		// String valueNormalizeMethod
		assertColumn table, 'VALUE_NORMALIZE_METHOD', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
