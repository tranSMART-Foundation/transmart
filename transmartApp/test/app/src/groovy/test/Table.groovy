package test

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a 'create table ...' statement from the exported schema DDL, and contains
 * associated columns, FKs, and indexes.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@EqualsAndHashCode(excludes='create')
@ToString(includeNames=true, includePackage=false)
class Table extends AbstractSqlType {

	String schema
	String name
	List<String> primaryKeyColumns
	Collection<Column> columns = []
	Collection<ForeignKey> foreignKeys = []
	Collection<Index> indexes = []

	Table(String schema, String name, List<String> primaryKeyColumns, String create) {
		super(create)
		this.schema = schema?.toUpperCase()
		this.name = name.toUpperCase()
		this.primaryKeyColumns = primaryKeyColumns
	}

	/**
	 * Operator overload to support adding a Column with <<
	 *
	 * @return this for chaining
	 */
	Table leftShift(Column column) {
		columns << column
		this
	}

	/**
	 * Operator overload to support adding a ForeignKey with <<
	 *
	 * @return this for chaining
	 */
	Table leftShift(ForeignKey foreignKey) {
		assert !foreignKeys.contains(foreignKey)
		foreignKeys << foreignKey
		this
	}

	/**
	 * Operator overload to support adding an Index with <<
	 *
	 * @return this for chaining
	 */
	Table leftShift(Index index) {
		assert !indexes.contains(index)
		indexes << index
		this
	}

	Column column(String columnName) {
		columns.find { Column c -> c.name == columnName }
	}

	String fullName() {
		schema ? schema + '.' + name : name
	}

	String asSql() {
		StringBuilder sb = new StringBuilder()

		sb << 'create table ' << fullName() << ' ('

		for (Column c in columns) {
			sb << c.asSql() << ', '
		}

		sb << 'primary key (' << primaryKeyColumns.join(', ') << '));\n'

		if (foreignKeys) {
			sb << '\n'
			for (ForeignKey fk in foreignKeys) {
				sb << fk.asSql() << ';\n'
			}
		}

		if (indexes) {
			sb << '\n'
			for (Index i in indexes) {
				sb << i.asSql() << ';\n'
			}
		}

		sb
	}
}
