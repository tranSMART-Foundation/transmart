package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CgdcpDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CGDCP_DATA'
		Table table = assertTable('BIO_CGDCP_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID']
		assertPk table, 'id'
		assertSequence 'SEQ_BIO_DATA_ID'
		assertForeignKey table, 'BIO_DATA_LITERATURE', 'id' // extends Literature, table 'BIO_DATA_LITERATURE'

		// all properties are nullable because CgdcpData extends Literature

		// Long cellLineId
		assertColumn table, 'CELL_LINE_ID', 'bigint', true

		// String evidenceCode
		// evidenceCode maxSize: 400
		assertColumn table, 'EVIDENCE_CODE', 'varchar(400)', true

		// String nciDiseaseConceptCode
		// nciDiseaseConceptCode maxSize: 400
		assertColumn table, 'NCI_DISEASE_CONCEPT_CODE', 'varchar(400)', true

		// String nciDrugConceptCode
		// nciDrugConceptCode maxSize: 400
		assertColumn table, 'NCI_DRUG_CONCEPT_CODE', 'varchar(400)', true

		// String nciRoleCode
		// nciRoleCode maxSize: 400
		assertColumn table, 'NCI_ROLE_CODE', 'varchar(400)', true

		// String negationIndicator
		// negationIndicator maxSize: 1
		assertColumn table, 'NEGATION_INDICATOR', 'varchar(1)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
