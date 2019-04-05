package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureReferenceDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_REF_DATA'
		Table table = assertTable('BIO_LIT_REF_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_REF_DATA_ID'
		assertPk table, 'BIO_LIT_REF_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String backReferences
		assertColumn table, 'BACK_REFERENCES', 'varchar(255)'

		// String component
		assertColumn table, 'COMPONENT', 'varchar(255)'

		// String componentClass
		assertColumn table, 'COMPONENT_CLASS', 'varchar(255)'

		// String disease
		assertColumn table, 'DISEASE', 'varchar(255)'

		// String diseaseDescription
		assertColumn table, 'DISEASE_DESCRIPTION', 'varchar(255)'

		// String diseaseGrade
		assertColumn table, 'DISEASE_GRADE', 'varchar(255)'

		// String diseaseIcd10
		assertColumn table, 'DISEASE_ICD10', 'varchar(255)'

		// String diseaseMesh
		assertColumn table, 'DISEASE_MESH', 'varchar(255)'

		// String diseaseSite
		assertColumn table, 'DISEASE_SITE', 'varchar(255)'

		// String diseaseStage
		assertColumn table, 'DISEASE_STAGE', 'varchar(255)'

		// String diseaseTypes
		assertColumn table, 'DISEASE_TYPES', 'varchar(255)'

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)'

		// String geneId
		assertColumn table, 'GENE_ID', 'varchar(255)'

		// String moleculeType
		assertColumn table, 'MOLECULE_TYPE', 'varchar(255)'

		// String physiology
		assertColumn table, 'PHYSIOLOGY', 'varchar(255)'

		// String referenceId
		assertColumn table, 'REFERENCE_ID', 'varchar(255)'

		// String referenceTitle
		assertColumn table, 'REFERENCE_TITLE', 'varchar(255)'

		// String referenceType
		assertColumn table, 'REFERENCE_TYPE', 'varchar(255)'

		// String statClinical
		assertColumn table, 'STAT_CLINICAL', 'varchar(255)'

		// String statClinicalCorrelation
		assertColumn table, 'STAT_CLINICAL_CORRELATION', 'varchar(255)'

		// String statCoefficient
		assertColumn table, 'STAT_COEFFICIENT', 'varchar(255)'

		// String statDescription
		assertColumn table, 'STAT_DESCRIPTION', 'varchar(255)'

		// String statPValue
		// statPValue column: 'STAT_P_VALUE'
		assertColumn table, 'STAT_P_VALUE', 'varchar(255)'

		// String statTests
		assertColumn table, 'STAT_TESTS', 'varchar(255)'

		// String studyType
		assertColumn table, 'STUDY_TYPE', 'varchar(255)'

		// String variant
		assertColumn table, 'VARIANT', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
