package org.transmartproject.core.querytool

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class ConstraintByValue {

    /**
     * The operator that will be used to compare the value with the constraint.
     */
    Operator operator

    /**
     * The second operand in the constraint.
     */
    String constraint

    /**
     * The type of constraint. Indicates whether constraint is a number or a
     * flag like 'H' (high).
     */
    ValueType valueType

    @CompileStatic
    static enum Operator {

        LOWER_THAN          ('LT'),
            LOWER_OR_EQUAL_TO   ('LE'),
            EQUAL_TO            ('EQ'),
            BETWEEN             ('BETWEEN'),
            GREATER_THAN        ('GT'),
            GREATER_OR_EQUAL_TO ('GE');

        final String value

        private Operator(String value) {
            this.value = value
        }

	static Operator forValue(String value) {
	    Operator operator = values().find { Operator it -> value == it.value }
	    if (operator) {
		operator
	    }
	    else {
		throw new IllegalArgumentException('No operator for value ' + value)
	    }
	}
    }

    @CompileStatic
    static enum ValueType {
        NUMBER,
        FLAG
    }
}
