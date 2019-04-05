package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayAnalysisPlatformSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASY_ANALYSIS_PLTFM'
		Table table = assertTable('BIO_ASY_ANALYSIS_PLTFM')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASY_ANALYSIS_PLTFM_ID'
		assertPk table, 'BIO_ASY_ANALYSIS_PLTFM_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String platformDescription
		// platformDescription nullable: true, maxSize: 2000
		assertColumn table, 'PLATFORM_DESCRIPTION', 'varchar(2000)', true

		// String platformName
		// platformName nullable: true, maxSize: 400
		assertColumn table, 'PLATFORM_NAME', 'varchar(400)', true

		// String platformVersion
		// platformVersion nullable: true, maxSize: 400
		assertColumn table, 'PLATFORM_VERSION', 'varchar(400)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
