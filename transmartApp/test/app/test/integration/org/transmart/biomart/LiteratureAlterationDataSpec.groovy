package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureAlterationDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_ALT_DATA'
		Table table = assertTable('BIO_LIT_ALT_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_ALT_DATA_ID'
		assertPk table, 'BIO_LIT_ALT_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'
		assertForeignKey table, 'BIO_DATA_LITERATURE', 'BIO_LIT_ALT_DATA_ID' // extends Literature, table 'BIO_DATA_LITERATURE'

		// all properties are nullable because LiteratureAlterationData extends Literature

		// String alterationType
		assertColumn table, 'ALTERATION_TYPE', 'varchar(255)', true

		// String clinAsmMarkerType
		assertColumn table, 'CLIN_ASM_MARKER_TYPE', 'varchar(255)', true

		// String clinAsmUnit
		assertColumn table, 'CLIN_ASM_UNIT', 'varchar(255)', true

		// String clinAsmValue
		assertColumn table, 'CLIN_ASM_VALUE', 'varchar(255)', true

		// String clinAtopy
		assertColumn table, 'CLIN_ATOPY', 'varchar(255)', true

		// String clinBaselinePercent
		assertColumn table, 'CLIN_BASELINE_PERCENT', 'varchar(255)', true

		// String clinBaselineValue
		assertColumn table, 'CLIN_BASELINE_VALUE', 'varchar(255)', true

		// String clinBaselineVariable
		assertColumn table, 'CLIN_BASELINE_VARIABLE', 'varchar(255)', true

		// String clinCellularCount
		assertColumn table, 'CLIN_CELLULAR_COUNT', 'varchar(255)', true

		// String clinCellularSource
		assertColumn table, 'CLIN_CELLULAR_SOURCE', 'varchar(255)', true

		// String clinCellularType
		assertColumn table, 'CLIN_CELLULAR_TYPE', 'varchar(255)', true

		// String clinPriorMedDose
		assertColumn table, 'CLIN_PRIOR_MED_DOSE', 'varchar(255)', true

		// String clinPriorMedName
		assertColumn table, 'CLIN_PRIOR_MED_NAME', 'varchar(255)', true

		// String clinPriorMedPercent
		assertColumn table, 'CLIN_PRIOR_MED_PERCENT', 'varchar(255)', true

		// String clinSmoker
		assertColumn table, 'CLIN_SMOKER', 'varchar(255)', true

		// String clinSubmucosaMarkerType
		assertColumn table, 'CLIN_SUBMUCOSA_MARKER_TYPE', 'varchar(255)', true

		// String clinSubmucosaUnit
		assertColumn table, 'CLIN_SUBMUCOSA_UNIT', 'varchar(255)', true

		// String clinSubmucosaValue
		assertColumn table, 'CLIN_SUBMUCOSA_VALUE', 'varchar(255)', true

		// String control
		assertColumn table, 'CONTROL', 'varchar(255)', true

		// String controlExpNumber
		assertColumn table, 'CONTROL_EXP_NUMBER', 'varchar(255)', true

		// String controlExpPercent
		assertColumn table, 'CONTROL_EXP_PERCENT', 'varchar(255)', true

		// String controlExpSd
		assertColumn table, 'CONTROL_EXP_SD', 'varchar(255)', true

		// String controlExpUnit
		assertColumn table, 'CONTROL_EXP_UNIT', 'varchar(255)', true

		// String controlExpValue
		assertColumn table, 'CONTROL_EXP_VALUE', 'varchar(255)', true

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)', true

		// String effect
		assertColumn table, 'EFFECT', 'varchar(255)', true

		// String epigeneticRegion
		assertColumn table, 'EPIGENETIC_REGION', 'varchar(255)', true

		// String epigeneticType
		assertColumn table, 'EPIGENETIC_TYPE', 'varchar(255)', true

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)', true

		// String glcControlPercent
		assertColumn table, 'GLC_CONTROL_PERCENT', 'varchar(255)', true

		// String glcMolecularChange
		assertColumn table, 'GLC_MOLECULAR_CHANGE', 'varchar(255)', true

		// String glcNumber
		assertColumn table, 'GLC_NUMBER', 'varchar(255)', true

		// String glcPercent
		assertColumn table, 'GLC_PERCENT', 'varchar(255)', true

		// String glcType
		assertColumn table, 'GLC_TYPE', 'varchar(255)', true

		// LiteratureModelData inVitroModel
		assertForeignKeyColumn table, 'BIO_LIT_MODEL_DATA', 'IN_VITRO_MODEL_ID',
				'bigint', true // table 'BIO_LIT_MODEL_DATA'

		// LiteratureModelData inVivoModel
		assertForeignKeyColumn table, 'BIO_LIT_MODEL_DATA', 'IN_VIVO_MODEL_ID',
				'bigint', true // table 'BIO_LIT_MODEL_DATA'

		// String lohLoci
		assertColumn table, 'LOH_LOCI', 'varchar(255)', true

		// String lossExpNumber
		assertColumn table, 'LOSS_EXP_NUMBER', 'varchar(255)', true

		// String lossExpPercent
		assertColumn table, 'LOSS_EXP_PERCENT', 'varchar(255)', true

		// String lossExpSd
		assertColumn table, 'LOSS_EXP_SD', 'varchar(255)', true

		// String lossExpUnit
		assertColumn table, 'LOSS_EXP_UNIT', 'varchar(255)', true

		// String lossExpValue
		assertColumn table, 'LOSS_EXP_VALUE', 'varchar(255)', true

		// String mutationChange
		assertColumn table, 'MUTATION_CHANGE', 'varchar(255)', true

		// String mutationSites
		assertColumn table, 'MUTATION_SITES', 'varchar(255)', true

		// String mutationType
		assertColumn table, 'MUTATION_TYPE', 'varchar(255)', true

		// String overExpNumber
		assertColumn table, 'OVER_EXP_NUMBER', 'varchar(255)', true

		// String overExpPercent
		assertColumn table, 'OVER_EXP_PERCENT', 'varchar(255)', true

		// String overExpSd
		assertColumn table, 'OVER_EXP_SD', 'varchar(255)', true

		// String overExpUnit
		assertColumn table, 'OVER_EXP_UNIT', 'varchar(255)', true

		// String overExpValue
		assertColumn table, 'OVER_EXP_VALUE', 'varchar(255)', true

		// String patientsNumber
		assertColumn table, 'PATIENTS_NUMBER', 'varchar(255)', true

		// String patientsPercent
		assertColumn table, 'PATIENTS_PERCENT', 'varchar(255)', true

		// String popBodySubstance
		assertColumn table, 'POP_BODY_SUBSTANCE', 'varchar(255)', true

		// String popCellType
		assertColumn table, 'POP_CELL_TYPE', 'varchar(255)', true

		// String popDescription
		assertColumn table, 'POP_DESCRIPTION', 'varchar(255)', true

		// String popExclusionCriteria
		assertColumn table, 'POP_EXCLUSION_CRITERIA', 'varchar(255)', true

		// String popExperimentalModel
		assertColumn table, 'POP_EXPERIMENTAL_MODEL', 'varchar(255)', true

		// String popInclusionCriteria
		assertColumn table, 'POP_INCLUSION_CRITERIA', 'varchar(255)', true

		// String popLocalization
		assertColumn table, 'POP_LOCALIZATION', 'varchar(255)', true

		// String popNumber
		assertColumn table, 'POP_NUMBER', 'varchar(255)', true

		// String popPhase
		assertColumn table, 'POP_PHASE', 'varchar(255)', true

		// String popStatus
		assertColumn table, 'POP_STATUS', 'varchar(255)', true

		// String popTissue
		assertColumn table, 'POP_TISSUE', 'varchar(255)', true

		// String popType
		assertColumn table, 'POP_TYPE', 'varchar(255)', true

		// String popValue
		assertColumn table, 'POP_VALUE', 'varchar(255)', true

		// String ptmChange
		assertColumn table, 'PTM_CHANGE', 'varchar(255)', true

		// String ptmRegion
		assertColumn table, 'PTM_REGION', 'varchar(255)', true

		// String ptmType
		assertColumn table, 'PTM_TYPE', 'varchar(255)', true

		// String techniques
		assertColumn table, 'TECHNIQUES', 'varchar(255)', true

		// String totalExpNumber
		assertColumn table, 'TOTAL_EXP_NUMBER', 'varchar(255)', true

		// String totalExpPercent
		assertColumn table, 'TOTAL_EXP_PERCENT', 'varchar(255)', true

		// String totalExpSd
		assertColumn table, 'TOTAL_EXP_SD', 'varchar(255)', true

		// String totalExpUnit
		assertColumn table, 'TOTAL_EXP_UNIT', 'varchar(255)', true

		// String totalExpValue
		assertColumn table, 'TOTAL_EXP_VALUE', 'varchar(255)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
