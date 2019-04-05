package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class PatientSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_PATIENT'
		Table table = assertTable('BIO_PATIENT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_PATIENT_ID'
		assertPk table, 'BIO_PATIENT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String addressZipCode
		// addressZipCode nullable: true, maxSize: 400
		assertColumn table, 'ADDRESS_ZIP_CODE', 'varchar(400)', true

		// Long bioClinicalTrialPGroupId
		// bioClinicalTrialPGroupId column: 'BIO_CLINICAL_TRIAL_P_GROUP_ID'
		// bioClinicalTrialPGroupId nullable: true
		assertColumn table, 'BIO_CLINICAL_TRIAL_P_GROUP_ID', 'bigint', true

		// Long bioExperimentId
		// bioExperimentId nullable: true
		assertColumn table, 'BIO_EXPERIMENT_ID', 'bigint', true

		// Date birthDate
		// birthDate nullable: true
		assertColumn table, 'BIRTH_DATE', 'timestamp', true

		// String birthDateOrig
		// birthDateOrig nullable: true, maxSize: 400
		assertColumn table, 'BIRTH_DATE_ORIG', 'varchar(400)', true

		// String countryCode
		// countryCode nullable: true, maxSize: 400
		assertColumn table, 'COUNTRY_CODE', 'varchar(400)', true

		// String ethnicGroupCode
		// ethnicGroupCode nullable: true, maxSize: 400
		assertColumn table, 'ETHNIC_GROUP_CODE', 'varchar(400)', true

		// String firstName
		// firstName nullable: true, maxSize: 400
		assertColumn table, 'FIRST_NAME', 'varchar(400)', true

		// String genderCode
		// genderCode nullable: true, maxSize: 400
		assertColumn table, 'GENDER_CODE', 'varchar(400)', true

		// String informedConsentCode
		// informedConsentCode nullable: true, maxSize: 400
		assertColumn table, 'INFORMED_CONSENT_CODE', 'varchar(400)', true

		// String lastName
		// lastName nullable: true, maxSize: 400
		assertColumn table, 'LAST_NAME', 'varchar(400)', true

		// String middleName
		// middleName nullable: true, maxSize: 400
		assertColumn table, 'MIDDLE_NAME', 'varchar(400)', true

		// String raceCode
		// raceCode nullable: true, maxSize: 400
		assertColumn table, 'RACE_CODE', 'varchar(400)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
