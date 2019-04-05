package test

import groovy.transform.CompileStatic

/**
 * Abstract base class for the data classes that represent statements from exported schema DDL.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
abstract class AbstractSqlType {

	/** The original SQL from the exported schema DDL. */
	String create

	protected AbstractSqlType(String create) {
		this.create = create
	}

	/**
	 * Re-generate SQL equivalent to (or the same as) what was parsed, for debugging.
	 *
	 * @return SQL
	 */
	abstract String asSql()
}
