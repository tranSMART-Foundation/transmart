package org.transmartproject.db.dataquery.clinical.patientconstraints

import org.grails.datastore.mapping.query.api.Criteria
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.querytool.QtPatientSetCollection

class PatientSetsConstraint implements PatientConstraint {

    private final Iterable<QueryResult> queryResults

    PatientSetsConstraint(Iterable<QueryResult> results) {
        queryResults = results
        assert queryResults
    }

    void addToCriteria(Criteria criteria) {
	criteria.in 'id', QtPatientSetCollection.where {
	    projections {
		property 'patient.id'
	    }
	    'in' 'resultInstance.id', queryResults*.id
	}
    }
}
