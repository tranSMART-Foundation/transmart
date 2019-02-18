/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.dataquery.highdim.parameterproducers

import groovy.util.logging.Slf4j
import org.transmartproject.core.exceptions.InvalidArgumentsException

@Slf4j('logger')
class DisjunctionConstraintFactory extends AbstractMethodBasedParameterFactory {

    private static final String SUBCONSTRAINTS_PARAM = 'subconstraints'

    Class disjunctionConstraintClass
    Class noopConstraintClass

    DisjunctionConstraintFactory(Class disjunctionConstraintClass, Class noopConstraintClass) {
        this.disjunctionConstraintClass = disjunctionConstraintClass
        this.noopConstraintClass = noopConstraintClass
    }

    @ProducerFor('disjunction')
    def createDisjunctionConstraint(Map<String, Object> params, createConstraint) {
	BindingUtils.validateParameterNames([SUBCONSTRAINTS_PARAM], params)
	Map<String, Object> subConstraintsSpecs = BindingUtils.getParam(params, SUBCONSTRAINTS_PARAM, Map)

        List constraints = new LinkedList()

	if (!subConstraintsSpecs) {
	    logger.info "Sub-constraints map that was provided is empty; this disjunction constraint will be a no-op"
            constraints << noopConstraintClass.newInstance()
        }
        else {
	    subConstraintsSpecs.each { String key, value ->
                checkValue value
                if (value instanceof Map) {
                    constraints << createConstraint(value, key)
		}
		else { //* value is List of maps
		    for (innerValue in value) {
                        checkIsMap innerValue
                        constraints << createConstraint(innerValue, key)
                    }
                }
            }
        }

        if (constraints.size() == 1) {
            constraints[0]
        }
        else {
            def ret = disjunctionConstraintClass.newInstance()
            ret.constraints = constraints
            ret
        }
    }

    private static void checkIsMap(value) {
        if (!(value instanceof Map)) {
	    throw new InvalidArgumentsException("On a disjunction constraint, " +
						"the $SUBCONSTRAINTS_PARAM parameter must be a map whose " +
						"values are themselves Maps or lists of maps; got a list " +
						"that instead of a map had a ${value.getClass().name} with value $value")
        }
    }

    private static void checkValue(value) {
        if (!(value instanceof Map) && !(value instanceof List)) {
	    throw new InvalidArgumentsException("On a disjunction constraint, " +
						"the $SUBCONSTRAINTS_PARAM parameter must be a map whose " +
						"values are themselves Maps or lists of maps; got a " +
						"${value.getClass().name} with value $value")
        }
    }
}
