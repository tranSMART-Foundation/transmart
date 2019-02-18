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

package org.transmartproject.db.dataquery.clinical

import com.google.common.collect.HashMultiset
import com.google.common.collect.Lists
import com.google.common.collect.Multiset
import grails.orm.HibernateCriteriaBuilder
import org.hibernate.ScrollMode
import org.hibernate.ScrollableResults
import org.hibernate.engine.SessionImplementor
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.db.dataquery.clinical.variables.TerminalConceptVariable
import org.transmartproject.db.i2b2data.ConceptDimension
import org.transmartproject.db.i2b2data.ObservationFact
import org.transmartproject.db.i2b2data.PatientDimension

import static org.transmartproject.db.util.GormWorkarounds.createCriteriaBuilder
import static org.transmartproject.db.util.GormWorkarounds.getHibernateInCriterion

class TerminalConceptVariablesDataQuery {

    private boolean initialized

    List<TerminalConceptVariable> clinicalVariables
    Iterable<PatientDimension> patients

    SessionImplementor session

    void init() {
        fillInTerminalConceptVariables()
        initialized = true
    }

    ScrollableResults openResultSet() {
        if (!initialized) {
            throw new IllegalStateException('init() not called successfully yet')
        }

        HibernateCriteriaBuilder criteriaBuilder = createCriteriaBuilder(ObservationFact, 'obs', session)
        criteriaBuilder.with {
            projections {
                property 'patient.id'
                property 'conceptCode'
                property 'valueType'
                property 'textValue'
                property 'numberValue'
            }
            order 'patient.id'
            order 'conceptCode'
        }

        if (patients instanceof PatientQuery) {
	    criteriaBuilder.add getHibernateInCriterion('patient.id', patients.forIds())
        }
        else {
            criteriaBuilder.in 'patient',  Lists.newArrayList(patients)
        }

        clinicalVariables.collate(1000).each { criteriaBuilder.in('conceptCode', it*.code) } // used to avoid "ORA-01795: maximum number of expressions in a list is 1000" thanks to https://stackoverflow.com/a/21837744/535203

        criteriaBuilder.scroll ScrollMode.FORWARD_ONLY
    }

    private void fillInTerminalConceptVariables() {
        if (!clinicalVariables) {
            throw new InvalidArgumentsException('No clinical variables specified')
        }

	Map<String, TerminalConceptVariable> conceptPaths = [:]
	Map<String, TerminalConceptVariable> conceptCodes = [:]

	for (ClinicalVariable var in clinicalVariables) {
	    if (!(var instanceof TerminalConceptVariable)) {
                throw new InvalidArgumentsException(
                    'Only terminal concept variables are supported')
            }

	    if (var.conceptCode) {
		if (conceptCodes.containsKey(var.conceptCode)) {
		    throw new InvalidArgumentsException("Specified multiple " +
							"variables with the same concept code: " +
							var.conceptCode)
		}
		conceptCodes[var.conceptCode] = var
	    }
	    else if (var.conceptPath) {
		if (conceptPaths.containsKey(var.conceptPath)) {
		    throw new InvalidArgumentsException("Specified multiple " +
							"variables with the same concept path: " +
							var.conceptPath)
                }
		conceptPaths[var.conceptPath] = var
            }
        }

        // find the concepts
        List<String[]> res = ConceptDimension.withCriteria {
            projections {
                property 'conceptPath'
                property 'conceptCode'
            }

            or {
                if (conceptPaths.keySet()) {
                    conceptPaths.keySet().asList().collate(1000).each { 'in' 'conceptPath', it } // used to avoid "ORA-01795: maximum number of expressions in a list is 1000" thanks to https://stackoverflow.com/a/21837744/535203
                }
                if (conceptCodes.keySet()) {
                    conceptCodes.keySet().asList().collate(1000).each { 'in' 'conceptCode', it } // used to avoid "ORA-01795: maximum number of expressions in a list is 1000" thanks to https://stackoverflow.com/a/21837744/535203
                }
            }
        }

	for (String[] concept in res) {
	    String conceptPath = concept[0]
	    String conceptCode = concept[1]

            if (conceptPaths[conceptPath]) {
		conceptPaths[conceptPath].conceptCode = conceptCode
            }
            if (conceptCodes[conceptCode]) {
		conceptCodes[conceptCode].conceptPath = conceptPath
            }
            // if both ifs manage we have the variable repeated (specified once
            // with concept code and once with concept path), and we'll catch
            // that further down
        }

        // check we found all the concepts
        for (var in conceptPaths.values()) {
            if (var.conceptCode == null) {
		throw new InvalidArgumentsException("Concept path " +
						    "'${var.conceptPath}' did not yield any results")
            }
        }
        for (var in conceptCodes.values()) {
            if (var.conceptPath == null) {
		throw new InvalidArgumentsException("Concept code " +
						    "'${var.conceptCode}' did not yield any results")
            }
        }

        Multiset multiset = HashMultiset.create clinicalVariables
        if (multiset.elementSet().size() < clinicalVariables.size()) {
	    throw new InvalidArgumentsException("Repeated variables in the " +
						"query (though once their concept path was specified and " +
						"on the second time their concept code was specified): " +
						multiset.elementSet().findAll {
                    multiset.count(it) > 1
                })
        }
    }
}
