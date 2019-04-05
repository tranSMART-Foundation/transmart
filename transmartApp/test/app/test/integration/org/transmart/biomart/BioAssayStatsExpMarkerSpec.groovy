package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayStatsExpMarkerSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_STATS_EXP_MARKER'
		Table table = assertTable('BIO_STATS_EXP_MARKER')

		// id column: 'BIO_STATS_EXP_MARKER_ID'
		assertPk table, 'BIO_STATS_EXP_MARKER_ID', AUTOINC

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
