package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioDataCorrelationSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DATA_CORRELATION'
		Table table = assertTable('BIO_DATA_CORRELATION')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_DATA_CORREL_ID'
		assertPk table, 'BIO_DATA_CORREL_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long associatedBioDataId
		// associatedBioDataId column: 'ASSO_BIO_DATA_ID'
		assertColumn table, 'ASSO_BIO_DATA_ID', 'bigint'

		// Long bioDataId
		assertColumn table, 'BIO_DATA_ID', 'bigint'

		// BioDataCorrelationDescr correlationDescr
		// correlationDescr column: 'BIO_DATA_CORREL_DESCR_ID'
		assertForeignKeyColumn table, 'BIO_DATA_CORREL_DESCR', 'BIO_DATA_CORREL_DESCR_ID' // table 'BIO_DATA_CORREL_DESCR'

		// BioMarker.associatedCorrels joinTable: [name: 'BIO_DATA_CORRELATION', key: 'ASSO_BIO_DATA_ID', column: 'BIO_DATA_CORREL_ID']
		assertForeignKey table, 'BIO_MARKER', 'ASSO_BIO_DATA_ID' // table 'BIO_MARKER'

		// BioMarker.correlations joinTable: [name: 'BIO_DATA_CORRELATION', key: 'BIO_DATA_ID', column: 'BIO_DATA_CORREL_ID']
		assertForeignKey table, 'BIO_MARKER', 'BIO_DATA_ID' // table 'BIO_MARKER'

		// redundant fk from pk to pk
		assertForeignKey table, 'BIO_DATA_CORRELATION', 'BIO_DATA_CORREL_ID'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
