package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AnalysisMetadataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'LZ_SRC_ANALYSIS_METADATA'
		Table table = assertTable('LZ_SRC_ANALYSIS_METADATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_ETL_ID'], column: 'ETL_ID'
		assertPk table, 'ETL_ID'
		assertSequence 'SEQ_ETL_ID'

		// String analysisName
		// analysisName unique: true, maxSize: 50
		assertColumn table, 'ANALYSIS_NAME', 'varchar(50)', false, true

		// String cellType
		// cellType nullable: true
		assertColumn table, 'CELL_TYPE', 'varchar(255)', true

		// String dataType
		// dataType nullable: true
		assertColumn table, 'DATA_TYPE', 'varchar(255)', true

		// String description
		// description nullable: true, maxSize: 4000
		assertColumn table, 'DESCRIPTION', 'varchar(4000)', true

		// Date etlDate
		// etlDate nullable: true
		assertColumn table, 'ETL_DATE', 'timestamp', true

		// String expressionPlatformIds
		// expressionPlatformIds nullable: true
		assertColumn table, 'EXPRESSION_PLATFORM_IDS', 'varchar(255)', true

		// String filename
		// filename nullable: true
		assertColumn table, 'FILENAME', 'varchar(255)', true

		// String genomeVersion
		// genomeVersion nullable: true
		assertColumn table, 'GENOME_VERSION', 'varchar(255)', true

		// String genotypePlatformIds
		// genotypePlatformIds nullable: true
		assertColumn table, 'GENOTYPE_PLATFORM_IDS', 'varchar(255)', true

		// String modelDescription
		// modelDescription column: 'MODEL_DESC'
		// modelDescription nullable: true
		assertColumn table, 'MODEL_DESC', 'varchar(255)', true

		// String modelName
		// modelName nullable: true
		assertColumn table, 'MODEL_NAME', 'varchar(255)', true

		// String phenotypeIds
		// phenotypeIds nullable: true
		assertColumn table, 'PHENOTYPE_IDS', 'varchar(255)', true

		// String population
		// population nullable: true
		assertColumn table, 'POPULATION', 'varchar(255)', true

		// Date processDate
		// processDate nullable: true
		assertColumn table, 'PROCESS_DATE', 'timestamp', true

		// Double pValueCutoff
		// pValueCutoff column: 'PVALUE_CUTOFF'
		// pValueCutoff nullable: true
		assertColumn table, 'PVALUE_CUTOFF', 'double', true

		// String researchUnit
		// researchUnit nullable: true
		assertColumn table, 'RESEARCH_UNIT', 'varchar(255)', true

		// String sampleSize
		// sampleSize nullable: true
		assertColumn table, 'SAMPLE_SIZE', 'varchar(255)', true

		// String sensitiveDesc
		// sensitiveDesc nullable: true
		assertColumn table, 'SENSITIVE_DESC', 'varchar(255)', true

		// String sensitiveFlag = '0'
		assertColumn table, 'SENSITIVE_FLAG', 'varchar(255)'

		// String statisticalTest
		// statisticalTest nullable: true
		assertColumn table, 'STATISTICAL_TEST', 'varchar(255)', true

		// String status = 'NEW'
		// status nullable: true
		assertColumn table, 'STATUS', 'varchar(255)', true

		// String study
		// study column: 'STUDY_ID'
		assertColumn table, 'STUDY_ID', 'varchar(255)'

		// String tissue
		// tissue nullable: true
		assertColumn table, 'TISSUE', 'varchar(255)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
