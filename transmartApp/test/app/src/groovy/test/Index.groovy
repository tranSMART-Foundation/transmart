package test

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a 'create index ...' statement from the exported schema DDL.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@EqualsAndHashCode(excludes='create')
@ToString(includeNames=true, includePackage=false)
class Index extends AbstractSqlType {

	String name
	String table
	String column

	Index(String name, String table, String column, String create = null) {
		super(create)
		this.name = name.toUpperCase()
		this.table = table.toUpperCase()
		this.column = column.toUpperCase()
	}

	String asSql() {
		'create index ' + name + ' on ' + table + ' (' + column + ')'
	}
}
