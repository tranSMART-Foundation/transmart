package test

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a column definition inside a 'create table ...' statement from the exported schema DDL.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@EqualsAndHashCode(excludes='create')
@ToString(includeNames=true, includePackage=false)
class Column extends AbstractSqlType {

	String name
	String type
	boolean nullable
	boolean unique

	Column(String name, String type, boolean nullable, boolean unique, String create = null) {
		super(create)
		this.name = name.toUpperCase()
		this.type = type.toUpperCase()
		this.nullable = nullable
		this.unique = unique
	}

	String asSql() {
		name + ' ' + type + (nullable ? ' not null' : '') + (nullable ? ' unique' : '')
	}
}
