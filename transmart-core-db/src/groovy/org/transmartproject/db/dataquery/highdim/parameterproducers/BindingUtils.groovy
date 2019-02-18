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

import com.google.common.collect.Iterables
import groovy.transform.CompileStatic
import org.transmartproject.core.exceptions.InvalidArgumentsException

/**
 * Helps the simple validation/binding done in parameter producers.
 */
@CompileStatic
class BindingUtils {

    static void validateParameterNames(Collection<String> parameterNames,
                                       Map<String, Object> params) {
	Collection<String> missingParameters = [] + parameterNames
	missingParameters.removeAll params.keySet()
        if (missingParameters) {
            if (missingParameters.size() == 1) {
                throw new InvalidArgumentsException('Missing required parameter "' +
						    Iterables.getFirst(missingParameters, null) + '"; got ' +
						    'the following parameters instead: ' + params.keySet())
            }
            else {
		throw new InvalidArgumentsException('Missing required parameters: ' +
						    missingParameters + '; got ' + params.keySet())
            }
        }

	Collection<String> extraParameters = params.keySet() - parameterNames
        if (extraParameters) {
	    throw new InvalidArgumentsException('Unrecognized parameters: ' + extraParameters +
						'; only these are allowed: ' + parameterNames)
        }
    }

    static <T> T getParam(Map params, String paramName, Class<T> type = String) {
        def result = params[paramName]

        if (result == null) {
	    throw new InvalidArgumentsException("The parameter $paramName is not in map $params")
        }

        if (!type.isAssignableFrom(result.getClass())) {
	    throw new InvalidArgumentsException("Expected parameter $paramName to be of type $type; " +
						"got class ${result.getClass()}")
        }

	(T) result
    }

    static Long convertToLong(String paramName, obj) {
        if (obj instanceof Number) {
	    obj.longValue()
        }
        else if (obj instanceof String && obj.isLong()) {
	    obj.toLong()
        }
        else {
	    throw new InvalidArgumentsException("Invalid value for $paramName: $obj")
        }
    }

    static List<String> processStringList(String paramName, Map<String, Object> params) {
	processList paramName, params[paramName], {
	    if (it instanceof CharSequence || it instanceof Number) {
                it.toString()
            }
            else {
                throw new InvalidArgumentsException("Parameter '$paramName' " +
						    "is not a list of String; found in a list an object with " +
						    "type ${it.getClass()}")
            }
        }
    }

    static List<Long> processLongList(String paramName, Map<String, Object> params) {
	processList paramName, params[paramName], {
            if (it instanceof String) {
                if (!it.isLong()) {
                    throw new InvalidArgumentsException("Parameter '$paramName' " +
							"is not a list of longs; found in a list an object " +
							"with type ${it.getClass()}")
                }
                else {
                    it as Long
                }
            }
            else if (it instanceof Number) {
                ((Number) it).longValue()
            }
            else {
                throw new InvalidArgumentsException("Parameter '$paramName' " +
						    "is not a list of longs; found in a list an object " +
						    "with type ${it.getClass()}")
	    }
	}
    }

    private static List processList(String paramName, obj, Closure closure) {
	if (!(obj instanceof List)) {
	    throw new InvalidArgumentsException("Parameter '$paramName' is not a List, got a ${obj.getClass()}")
        }

	List list = (List) obj
	if (!list) {
	    throw new InvalidArgumentsException('Value of parameter ' +
						"'$paramName' is an empty list; this is unacceptable")
	}

	list.collect { closure(it) }
    }
}
