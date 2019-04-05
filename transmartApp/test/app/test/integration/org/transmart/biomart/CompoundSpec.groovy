package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CompoundSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_COMPOUND'
		Table table = assertTable('BIO_COMPOUND')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_COMPOUND_ID'
		assertPk table, 'BIO_COMPOUND_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String brandName
		// brandName nullable: true, maxSize: 400
		assertColumn table, 'BRAND_NAME', 'varchar(400)', true

		// String casRegistry
		// casRegistry nullable: true, maxSize: 800
		assertColumn table, 'CAS_REGISTRY', 'varchar(800)', true

		// String chemicalName
		// chemicalName nullable: true, maxSize: 800
		assertColumn table, 'CHEMICAL_NAME', 'varchar(800)', true

		// String cntoNumber
		// cntoNumber nullable: true, maxSize: 400
		assertColumn table, 'CNTO_NUMBER', 'varchar(400)', true

		// String codeName
		// codeName nullable: true, maxSize: 400
		assertColumn table, 'CODE_NAME', 'varchar(400)', true

		// String description
		// description nullable: true, maxSize: 2000
		assertColumn table, 'DESCRIPTION', 'varchar(2000)', true

		// String genericName
		// genericName nullable: true, maxSize: 400
		assertColumn table, 'GENERIC_NAME', 'varchar(400)', true

		// String mechanism
		// mechanism nullable: true, maxSize: 800
		assertColumn table, 'MECHANISM', 'varchar(800)', true

		// String number
		// number column: 'JNJ_NUMBER'
		// number nullable: true, maxSize: 400
		assertColumn table, 'JNJ_NUMBER', 'varchar(400)', true

		// String productCategory
		// productCategory nullable: true, maxSize: 400
		assertColumn table, 'PRODUCT_CATEGORY', 'varchar(400)', true

		// String sourceCode
		// sourceCode column: 'SOURCE_CD'
		// sourceCode nullable: true, maxSize: 100
		assertColumn table, 'SOURCE_CD', 'varchar(100)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getName()'() {
		when:
		Compound c = new Compound()

		then: 'if genericName, brandName, number and cntoNumber are not set, return null'
		!c.name

		when:
		String cntoNumber = 'cntoNumber'
		c.cntoNumber = cntoNumber

		then: 'if cntoNumber is set but genericName, brandName, number are not, use cntoNumber'
		cntoNumber == c.name

		when:
		String number = 'number'
		c.number = number

		then: 'if number is set but genericName and brandName are not, use number'
		number == c.name

		when:
		String brandName = 'brandName'
		c.brandName = brandName

		then: 'if brandName is set but genericName is not, use brandName'
		brandName == c.name

		when:
		String genericName = 'genericName'
		c.genericName = genericName

		then: 'use genericName if it is set'
		genericName == c.name
	}
}
