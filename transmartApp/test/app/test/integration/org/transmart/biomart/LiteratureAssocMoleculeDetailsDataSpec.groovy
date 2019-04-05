package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureAssocMoleculeDetailsDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_AMD_DATA'
		Table table = assertTable('BIO_LIT_AMD_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_AMD_DATA_ID'
		assertPk table, 'BIO_LIT_AMD_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioLitAltDataId
		assertColumn table, 'BIO_LIT_ALT_DATA_ID', 'bigint'

		// String coExpNumber
		assertColumn table, 'CO_EXP_NUMBER', 'varchar(255)'

		// String coExpPercent
		assertColumn table, 'CO_EXP_PERCENT', 'varchar(255)'

		// String coExpSd
		assertColumn table, 'CO_EXP_SD', 'varchar(255)'

		// String coExpUnit
		assertColumn table, 'CO_EXP_UNIT', 'varchar(255)'

		// String coExpValue
		assertColumn table, 'CO_EXP_VALUE', 'varchar(255)'

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)'

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)'

		// String molecule
		assertColumn table, 'MOLECULE', 'varchar(255)'

		// String moleculeType
		assertColumn table, 'MOLECULE_TYPE', 'varchar(255)'

		// String mutationChange
		assertColumn table, 'MUTATION_CHANGE', 'varchar(255)'

		// String mutationNumber
		assertColumn table, 'MUTATION_NUMBER', 'varchar(255)'

		// String mutationPercent
		assertColumn table, 'MUTATION_PERCENT', 'varchar(255)'

		// String mutationSites
		assertColumn table, 'MUTATION_SITES', 'varchar(255)'

		// String mutationType
		assertColumn table, 'MUTATION_TYPE', 'varchar(255)'

		// String overExpNumber
		assertColumn table, 'OVER_EXP_NUMBER', 'varchar(255)'

		// String overExpPercent
		assertColumn table, 'OVER_EXP_PERCENT', 'varchar(255)'

		// String overExpSd
		assertColumn table, 'OVER_EXP_SD', 'varchar(255)'

		// String overExpUnit
		assertColumn table, 'OVER_EXP_UNIT', 'varchar(255)'

		// String overExpValue
		assertColumn table, 'OVER_EXP_VALUE', 'varchar(255)'

		// String targetExpNumber
		assertColumn table, 'TARGET_EXP_NUMBER', 'varchar(255)'

		// String targetExpPercent
		assertColumn table, 'TARGET_EXP_PERCENT', 'varchar(255)'

		// String targetExpSd
		assertColumn table, 'TARGET_EXP_SD', 'varchar(255)'

		// String targetExpUnit
		assertColumn table, 'TARGET_EXP_UNIT', 'varchar(255)'

		// String targetExpValue
		assertColumn table, 'TARGET_EXP_VALUE', 'varchar(255)'

		// String targetOverExpNumber
		assertColumn table, 'TARGET_OVER_EXP_NUMBER', 'varchar(255)'

		// String targetOverExpPercent
		assertColumn table, 'TARGET_OVER_EXP_PERCENT', 'varchar(255)'

		// String targetOverExpSd
		assertColumn table, 'TARGET_OVER_EXP_SD', 'varchar(255)'

		// String targetOverExpUnit
		assertColumn table, 'TARGET_OVER_EXP_UNIT', 'varchar(255)'

		// String targetOverExpValue
		assertColumn table, 'TARGET_OVER_EXP_VALUE', 'varchar(255)'

		// String techniques
		assertColumn table, 'TECHNIQUES', 'varchar(255)'

		// String totalExpNumber
		assertColumn table, 'TOTAL_EXP_NUMBER', 'varchar(255)'

		// String totalExpPercent
		assertColumn table, 'TOTAL_EXP_PERCENT', 'varchar(255)'

		// String totalExpSd
		assertColumn table, 'TOTAL_EXP_SD', 'varchar(255)'

		// String totalExpUnit
		assertColumn table, 'TOTAL_EXP_UNIT', 'varchar(255)'

		// String totalExpValue
		assertColumn table, 'TOTAL_EXP_VALUE', 'varchar(255)'

		// LiteratureAlterationData.assocMoleculeDetails joinTable: [name: 'BIO_LIT_AMD_DATA', key: 'BIO_LIT_ALT_DATA_ID', column: 'BIO_LIT_AMD_DATA_ID']
		assertForeignKey table, 'BIO_LIT_ALT_DATA', 'BIO_LIT_ALT_DATA_ID' // table 'BIO_LIT_ALT_DATA'

		// redundant fk from pk to pk
		assertForeignKey table, 'BIO_LIT_AMD_DATA', 'BIO_LIT_AMD_DATA_ID'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
