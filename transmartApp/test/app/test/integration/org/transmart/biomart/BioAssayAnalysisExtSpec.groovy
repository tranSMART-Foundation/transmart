package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayAnalysisExtSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_ANALYSIS_EXT'
		Table table = assertTable('BIO_ASSAY_ANALYSIS_EXT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_ANALYSIS_EXT_ID'
		assertPk table, 'BIO_ASSAY_ANALYSIS_EXT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// static belongsTo = [bioAssayAnalysis: BioAssayAnalysis]
		assertForeignKeyColumn table, 'BIO_ASSAY_ANALYSIS', 'BIO_ASSAY_ANALYSIS_ID',
				'bigint', false, true// table 'BIO_ASSAY_ANALYSIS'
		// unique because of BioAssayAnalysis: static hasOne = [ext: BioAssayAnalysisExt]

		// String cellType
		// cellType nullable: true
		assertColumn table, 'CELL_TYPE', 'varchar(255)', true

		// String genomeVersion
		// genomeVersion nullable: true
		assertColumn table, 'GENOME_VERSION', 'varchar(255)', true

		// String modelDescription
		// modelDescription column: 'MODEL_DESC'
		// modelDescription nullable: true
		assertColumn table, 'MODEL_DESC', 'varchar(255)', true

		// String modelName
		// modelName nullable: true
		assertColumn table, 'MODEL_NAME', 'varchar(255)', true

		// String population
		// population nullable: true
		assertColumn table, 'POPULATION', 'varchar(255)', true

		// String researchUnit
		// researchUnit nullable: true
		assertColumn table, 'RESEARCH_UNIT', 'varchar(255)', true

		// String sampleSize
		// sampleSize nullable: true
		assertColumn table, 'SAMPLE_SIZE', 'varchar(255)', true

		// String sensitiveDesc
		// sensitiveDesc nullable: true
		assertColumn table, 'SENSITIVE_DESC', 'varchar(255)', true

		// String sensitiveFlag
		// sensitiveFlag nullable: true
		assertColumn table, 'SENSITIVE_FLAG', 'varchar(255)', true

		// String tissue
		// tissue nullable: true
		assertColumn table, 'TISSUE', 'varchar(255)', true

		// String vendor
		// vendor nullable: true
		assertColumn table, 'VENDOR', 'varchar(255)', true

		// String vendorType
		// vendorType nullable: true
		assertColumn table, 'VENDOR_TYPE', 'varchar(255)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
