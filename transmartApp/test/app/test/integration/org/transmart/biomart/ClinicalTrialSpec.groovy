package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ClinicalTrialSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CLINICAL_TRIAL'
		Table table = assertTable('BIO_CLINICAL_TRIAL')

		// id column: 'BIO_EXPERIMENT_ID'
		assertPk table, 'BIO_EXPERIMENT_ID'
		assertForeignKey table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // extends Experiment, table 'BIO_EXPERIMENT'

		// all properties are nullable because ClinicalTrial extends Experiment

		// String blindingProcedure
		assertColumn table, 'BLINDING_PROCEDURE', 'varchar(255)', true

		// String dosingRegimen
		assertColumn table, 'DOSING_REGIMEN', 'varchar(255)', true

		// Long durationOfStudyWeeks
		assertColumn table, 'DURATION_OF_STUDY_WEEKS', 'bigint', true

		// String exclusionCriteria
		// exclusionCriteria type: 'text'
		assertColumn table, 'EXCLUSION_CRITERIA', 'longvarchar', true

		// String genderRestrictionMfb
		assertColumn table, 'GENDER_RESTRICTION_MFB', 'varchar(255)', true

		// String groupAssignment
		assertColumn table, 'GROUP_ASSIGNMENT', 'varchar(255)', true

		// String inclusionCriteria
		// inclusionCriteria type: 'text'
		assertColumn table, 'INCLUSION_CRITERIA', 'longvarchar', true

		// Long maxAge
		assertColumn table, 'MAX_AGE', 'bigint', true

		// Long minAge
		assertColumn table, 'MIN_AGE', 'bigint', true

		// Long numberOfPatients
		assertColumn table, 'NUMBER_OF_PATIENTS', 'bigint', true

		// Long numberOfSites
		assertColumn table, 'NUMBER_OF_SITES', 'bigint', true

		// String primaryEndPoints
		assertColumn table, 'PRIMARY_END_POINTS', 'varchar(255)', true

		// String routeOfAdministration
		assertColumn table, 'ROUTE_OF_ADMINISTRATION', 'varchar(255)', true

		// String secondaryEndPoints
		assertColumn table, 'SECONDARY_END_POINTS', 'varchar(255)', true

		// String secondaryIds
		assertColumn table, 'SECONDARY_IDS', 'varchar(255)', true

		// String studyOwner
		assertColumn table, 'STUDY_OWNER', 'varchar(255)', true

		// String studyPhase
		assertColumn table, 'STUDY_PHASE', 'varchar(255)', true

		// String studyType
		// studyType column: 'STUDYTYPE'
		assertColumn table, 'STUDYTYPE', 'varchar(255)', true

		// String subjects
		assertColumn table, 'SUBJECTS', 'varchar(255)', true

		// String trialNumber
		assertColumn table, 'TRIAL_NUMBER', 'varchar(255)', true

		// String typeOfControl
		assertColumn table, 'TYPE_OF_CONTROL', 'varchar(255)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
