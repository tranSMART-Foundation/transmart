package test

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a 'create sequence ...' statement from the exported schema DDL.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@EqualsAndHashCode(excludes='create')
@ToString(includeNames=true, includePackage=false)
class Sequence extends AbstractSqlType {

	String name

	Sequence(String name, String create = null) {
		super(create)
		this.name = name.toUpperCase()
	}

	String asSql() {
		'create sequence ' + name
	}
}
