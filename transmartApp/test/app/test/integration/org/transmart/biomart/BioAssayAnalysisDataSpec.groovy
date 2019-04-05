package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayAnalysisDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_ANALYSIS_DATA'
		Table table = assertTable('BIO_ASSAY_ANALYSIS_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASY_ANALYSIS_DATA_ID'
		assertPk table, 'BIO_ASY_ANALYSIS_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Double adjustedPvalue
		assertColumn table, 'ADJUSTED_PVALUE', 'double'

		// String adjustedPValueCode
		// adjustedPValueCode column: 'ADJUSTED_P_VALUE_CODE'
		assertColumn table, 'ADJUSTED_P_VALUE_CODE', 'varchar(255)'

		// BioAssayAnalysis analysis
		// analysis column: 'BIO_ASSAY_ANALYSIS_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_ANALYSIS', 'BIO_ASSAY_ANALYSIS_ID' // table 'BIO_ASSAY_ANALYSIS'

		// BioAssayPlatform assayPlatform
		// assayPlatform column: 'BIO_ASSAY_PLATFORM_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_PLATFORM', 'BIO_ASSAY_PLATFORM_ID' // table 'BIO_ASSAY_PLATFORM'

		// Double cutValue
		assertColumn table, 'CUT_VALUE', 'double'

		// Experiment experiment
		// experiment column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// BioAssayFeatureGroup featureGroup
		// featureGroup column: 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_FEATURE_GROUP', 'BIO_ASSAY_FEATURE_GROUP_ID' // table 'BIO_ASSAY_FEATURE_GROUP'

		// String featureGroupName
		assertColumn table, 'FEATURE_GROUP_NAME', 'varchar(255)'

		// Double foldChangeRatio
		assertColumn table, 'FOLD_CHANGE_RATIO', 'double'

		// Double numericValue
		assertColumn table, 'NUMERIC_VALUE', 'double'

		// String numericValueCode
		assertColumn table, 'NUMERIC_VALUE_CODE', 'varchar(255)'

		// Double preferredPvalue
		assertColumn table, 'PREFERRED_PVALUE', 'double'

		// Double rawPvalue
		assertColumn table, 'RAW_PVALUE', 'double'

		// String resultsValue
		assertColumn table, 'RESULTS_VALUE', 'varchar(255)'

		// Double rhoValue
		assertColumn table, 'RHO_VALUE', 'double'

		// Double rValue
		assertColumn table, 'R_VALUE', 'double'

		// Double teaNormalizedPValue
		// teaNormalizedPValue column: 'TEA_NORMALIZED_PVALUE'
		assertColumn table, 'TEA_NORMALIZED_PVALUE', 'double'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
