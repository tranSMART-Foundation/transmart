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

package org.transmartproject.db.clinical

import groovy.util.logging.Slf4j
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalDataResource
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.ComposedVariable
import org.transmartproject.core.dataquery.clinical.PatientRow
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.dataquery.clinical.ClinicalDataTabularResult
import org.transmartproject.db.dataquery.clinical.InnerClinicalTabularResultFactory
import org.transmartproject.db.dataquery.clinical.PatientQuery
import org.transmartproject.db.dataquery.clinical.TerminalClinicalVariablesTabularResult
import org.transmartproject.db.dataquery.clinical.patientconstraints.PatientSetsConstraint
import org.transmartproject.db.dataquery.clinical.patientconstraints.StudyPatientsConstraint
import org.transmartproject.db.dataquery.clinical.variables.ClinicalVariableFactory
import org.transmartproject.db.dataquery.clinical.variables.TerminalConceptVariable
import org.transmartproject.db.i2b2data.PatientDimension

@Slf4j('logger')
class ClinicalDataResourceService implements ClinicalDataResource {

    static transactional = false

    SessionFactory sessionFactory

    @Autowired
    ClinicalVariableFactory clinicalVariableFactory

    @Autowired
    InnerClinicalTabularResultFactory innerResultFactory

    TabularResult<ClinicalVariableColumn, PatientRow> retrieveData(List<QueryResult> queryResults,
                                           List<ClinicalVariable> variables) {
        retrieveDataImpl new PatientQuery([new PatientSetsConstraint(queryResults)]), variables
    }

    TabularResult<ClinicalVariableColumn, PatientRow> retrieveData(Study study,
                                                                   List<ClinicalVariable> variables) {
        retrieveDataImpl new PatientQuery([new StudyPatientsConstraint(study)]), variables
    }

    TabularResult<ClinicalVariableColumn, PatientRow> retrieveData(Set<Patient> patientCollection,
                                                                   List<ClinicalVariable> variables) {
        retrieveDataImpl(patientCollection, variables)
    }

    private TabularResult<ClinicalVariableColumn, PatientRow> retrieveDataImpl(Iterable<PatientDimension> patients,
                                                                   List<ClinicalVariable> variables) {

        if (!variables) {
            throw new InvalidArgumentsException('No variables passed to #retrieveData()')
        }

        StatelessSession session = sessionFactory.openStatelessSession()

        try {
            TreeMap<Long, PatientDimension> patientMap = new TreeMap<>()

	    for (PatientDimension pd in patients) {
		patientMap[pd.id] = pd
	    }

            List<TerminalConceptVariable> flattenedVariables = []
            flattenClinicalVariables flattenedVariables, variables

	    Collection<TerminalClinicalVariablesTabularResult> intermediateResults
	    if (patientMap) {
		intermediateResults = innerResultFactory.createIntermediateResults(session, patients, flattenedVariables)
	    }
	    else {
		intermediateResults = []
		logger.info 'No patients passed to retrieveData() with variables {}; will skip main queries', variables
	    }

	    new ClinicalDataTabularResult(session, intermediateResults, patientMap)
        }
        catch (Throwable t) {
            session.close()
            throw t
        }
    }

    private void flattenClinicalVariables(List<TerminalConceptVariable> target, List<ClinicalVariable> variables) {
        for (ClinicalVariable var in variables) {
            if (var instanceof ComposedVariable) {
                flattenClinicalVariables target, var.innerClinicalVariables
            }
            else {
                target << var
            }
        }
    }

    TabularResult<ClinicalVariableColumn, PatientRow> retrieveData(QueryResult patientSet,
                                                                   List<ClinicalVariable> variables) {
        assert patientSet

        retrieveData([patientSet], variables)
    }

    ClinicalVariable createClinicalVariable(Map<String, Object> params, String type) throws InvalidArgumentsException {
        clinicalVariableFactory.createClinicalVariable params, type
    }
}
