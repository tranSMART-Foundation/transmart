package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioMarkerExpAnalysisMVSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_MARKER_EXP_ANALYSIS_MV'
		Table table = assertTable('BIO_MARKER_EXP_ANALYSIS_MV')

		// id column: 'MV_ID'
		assertPk table, 'MV_ID', AUTOINC

		// BioAssayAnalysis analysis
		// analysis column: 'BIO_ASSAY_ANALYSIS_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_ANALYSIS', 'BIO_ASSAY_ANALYSIS_ID' // table 'BIO_ASSAY_ANALYSIS'

		// Experiment experiment
		// experiment column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// BioMarker marker
		// marker column: 'BIO_MARKER_ID'
		assertForeignKeyColumn table, 'BIO_MARKER', 'BIO_MARKER_ID' // table 'BIO_MARKER'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
