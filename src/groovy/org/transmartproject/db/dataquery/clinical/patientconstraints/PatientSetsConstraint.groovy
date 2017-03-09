package org.transmartproject.db.dataquery.clinical.patientconstraints

import org.grails.datastore.mapping.query.api.Criteria
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.querytool.QtPatientSetCollection
import org.transmartproject.db.support.InQuery

class PatientSetsConstraint implements PatientConstraint {

    private final Iterable<QueryResult> queryResults

    PatientSetsConstraint(final Iterable<QueryResult> queryResults) {
        this.queryResults = queryResults

        assert this.queryResults
    }

    @Override
    void addToCriteria(Criteria criteria) {
        criteria.in 'id', QtPatientSetCollection.where {
            projections {
                property 'patient.id'
            }
            InQuery.addIn('resultInstance.id', this.queryResults*.id)
        }
    }

}
