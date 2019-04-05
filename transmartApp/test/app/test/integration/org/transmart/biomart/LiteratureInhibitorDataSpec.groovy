package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureInhibitorDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_INH_DATA'
		Table table = assertTable('BIO_LIT_INH_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_INH_DATA_ID'
		assertPk table, 'BIO_LIT_INH_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'
		assertForeignKey table, 'BIO_DATA_LITERATURE', 'BIO_LIT_INH_DATA_ID' // extends Literature, table 'BIO_DATA_LITERATURE'

		// all properties are nullable because LiteratureAlterationData extends Literature

		// String administration
		assertColumn table, 'ADMINISTRATION', 'varchar(255)', true

		// String casid
		assertColumn table, 'CASID', 'varchar(255)', true

		// String concentration
		assertColumn table, 'CONCENTRATION', 'varchar(255)', true

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)', true

		// String effectAdverse
		assertColumn table, 'EFFECT_ADVERSE', 'varchar(255)', true

		// String effectBeneficial
		assertColumn table, 'EFFECT_BENEFICIAL', 'varchar(255)', true

		// String effectDescription
		assertColumn table, 'EFFECT_DESCRIPTION', 'varchar(255)', true

		// String effectDownstream
		assertColumn table, 'EFFECT_DOWNSTREAM', 'varchar(255)', true

		// String effectMolecular
		assertColumn table, 'EFFECT_MOLECULAR', 'varchar(255)', true

		// String effectNumber
		assertColumn table, 'EFFECT_NUMBER', 'varchar(255)', true

		// String effectPercent
		assertColumn table, 'EFFECT_PERCENT', 'varchar(255)', true

		// String effectPharmacos
		assertColumn table, 'EFFECT_PHARMACOS', 'varchar(255)', true

		// String effectPotentials
		assertColumn table, 'EFFECT_POTENTIALS', 'varchar(255)', true

		// String effectResponseRate
		assertColumn table, 'EFFECT_RESPONSE_RATE', 'varchar(255)', true

		// String effectSd
		assertColumn table, 'EFFECT_SD', 'varchar(255)', true

		// String effectUnit
		assertColumn table, 'EFFECT_UNIT', 'varchar(255)', true

		// String effectValue
		assertColumn table, 'EFFECT_VALUE', 'varchar(255)', true

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)', true

		// String inhibitor
		assertColumn table, 'INHIBITOR', 'varchar(255)', true

		// String inhibitorStandardName
		assertColumn table, 'INHIBITOR_STANDARD_NAME', 'varchar(255)', true

		// String techniques
		assertColumn table, 'TECHNIQUES', 'varchar(255)', true

		// String timeExposure
		assertColumn table, 'TIME_EXPOSURE', 'varchar(255)', true

		// String treatment
		assertColumn table, 'TREATMENT', 'varchar(255)', true

		// String trialBodySubstance
		assertColumn table, 'TRIAL_BODY_SUBSTANCE', 'varchar(255)', true

		// String trialCellLine
		assertColumn table, 'TRIAL_CELL_LINE', 'varchar(255)', true

		// String trialCellType
		assertColumn table, 'TRIAL_CELL_TYPE', 'varchar(255)', true

		// String trialDescription
		assertColumn table, 'TRIAL_DESCRIPTION', 'varchar(255)', true

		// String trialDesigns
		assertColumn table, 'TRIAL_DESIGNS', 'varchar(255)', true

		// String trialExperimentalModel
		assertColumn table, 'TRIAL_EXPERIMENTAL_MODEL', 'varchar(255)', true

		// String trialInclusionCriteria
		assertColumn table, 'TRIAL_INCLUSION_CRITERIA', 'varchar(255)', true

		// String trialPatientsNumber
		assertColumn table, 'TRIAL_PATIENTS_NUMBER', 'varchar(255)', true

		// String trialPhase
		assertColumn table, 'TRIAL_PHASE', 'varchar(255)', true

		// String trialStatus
		assertColumn table, 'TRIAL_STATUS', 'varchar(255)', true

		// String trialTissue
		assertColumn table, 'TRIAL_TISSUE', 'varchar(255)', true

		// String trialType
		assertColumn table, 'TRIAL_TYPE', 'varchar(255)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
