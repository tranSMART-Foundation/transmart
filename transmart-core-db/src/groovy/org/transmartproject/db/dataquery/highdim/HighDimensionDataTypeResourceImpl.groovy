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

package org.transmartproject.db.dataquery.highdim

import grails.orm.HibernateCriteriaBuilder
import groovy.util.logging.Slf4j
import org.hibernate.ScrollMode
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.EmptySetException
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.UnsupportedByDataTypeException
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.querytool.ConstraintByOmicsValue
import org.transmartproject.core.querytool.HighDimensionFilterType
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.dataquery.highdim.assayconstraints.MarkerTypeCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.dataconstraints.CriteriaDataConstraint
import org.transmartproject.db.dataquery.highdim.projections.CriteriaProjection
import org.transmartproject.db.ontology.I2b2

import static org.transmartproject.db.util.GormWorkarounds.getHibernateInCriterion

@Slf4j('logger')
class HighDimensionDataTypeResourceImpl implements HighDimensionDataTypeResource {

    private static final int FETCH_SIZE = 10000

    HighDimensionResource highDimensionResource
    protected HighDimensionDataTypeModule module

    HighDimensionDataTypeResourceImpl(HighDimensionDataTypeModule module) {
        this.module = module
    }

    // Lazy otherwise EqualsAndHashCode does not pick it up
    @Lazy
    String dataTypeName = module.name

    String getDataTypeDescription() {
        module.description
    }

    protected StatelessSession openSession() {
        SessionFactory sessionFactory = module.sessionFactory
        StatelessSession statelessSession = sessionFactory.openStatelessSession()
        //To prevent out of memory exception for big data sets: fetch data from the database in the cursor mode.
        statelessSession.connection().autoCommit = false
        statelessSession
    }

    protected String getAssayProperty() {
        /* we could change this to inspect the associations of the root type and
         * find the name of the association linking to DeSubjectSampleMapping */
        'assay'
    }

    TabularResult retrieveData(List<AssayConstraint> assayConstraints, List<DataConstraint> dataConstraints,
                               Projection projection) {

        // Each module should only return assays that match 
        // the markertypes specified, in addition to the 
        // constraints given
        assayConstraints << new MarkerTypeCriteriaConstraint(platformNames: module.platformMarkerTypes)

        AssayQuery assaysQuery = new AssayQuery(assayConstraints)

        List<Assay> assays = assaysQuery.list()
        if (!assays) {
            throw new EmptySetException('No assays satisfy the provided criteria')
        }

        HibernateCriteriaBuilder criteriaBuilder = module.prepareDataQuery(projection, openSession())

        //We have to specify projection explicitly because of the grails bug
        //https://jira.grails.org/browse/GRAILS-12107
        criteriaBuilder.add getHibernateInCriterion('assay.id', assaysQuery.forIds())

        /* apply changes to criteria from projection, if any */
        if (projection instanceof CriteriaProjection) {
            projection.doWithCriteriaBuilder criteriaBuilder
        }

        /* apply data constraints */
        for (CriteriaDataConstraint dataConstraint in dataConstraints) {
            dataConstraint.doWithCriteriaBuilder criteriaBuilder
        }

        criteriaBuilder.instance.fetchSize = FETCH_SIZE

        module.transformResults(
                criteriaBuilder.instance.scroll(ScrollMode.FORWARD_ONLY),
                assays.collect { new AssayColumnImpl(it) },
                projection)
    }

    Set<String> getSupportedAssayConstraints() {
        module.supportedAssayConstraints
    }

    Set<String> getSupportedDataConstraints() {
        module.supportedDataConstraints
    }

    Set<String> getSupportedProjections() {
        module.supportedProjections
    }

    AssayConstraint createAssayConstraint(Map<String, Object> params, String name)
            throws UnsupportedByDataTypeException {
        module.createAssayConstraint params, name
    }

    DataConstraint createDataConstraint(Map<String, Object> params, String name)
            throws UnsupportedByDataTypeException {
        module.createDataConstraint params, name
    }

    Projection createProjection(Map<String, Object> params, String name)
            throws UnsupportedByDataTypeException {
        module.createProjection params, name
    }

    Projection createProjection(String name) throws UnsupportedByDataTypeException{
        createProjection([:], name)
    }

    boolean matchesPlatform(Platform platform) {
        platform.markerType in module.platformMarkerTypes
    }

    Set<OntologyTerm> getAllOntologyTermsForDataTypeBy(QueryResult queryResult) {
        I2b2.executeQuery '''
            from I2b2 where code in
                (select distinct ssm.conceptCode
                from QtPatientSetCollection ps, DeSubjectSampleMapping ssm
                inner join ssm.platform as p
                where p.markerType in (:markerTypes)
                    and ssm.patient = ps.patient
                    and ps.resultInstance.id = :resultInstanceId)
        ''', [markerTypes: module.platformMarkerTypes, resultInstanceId: queryResult.id]
    }

    List<String> searchAnnotation(String conceptCode, String searchTerm, String searchProperty) {
        if (!getSearchableAnnotationProperties().contains(searchProperty)) {
            throw new InvalidArgumentsException("Expected searchProperty to be one of ${getSearchableAnnotationProperties()}, got $searchProperty")
	}
        module.searchAnnotation(conceptCode, searchTerm, searchProperty)
    }

    List<String> getSearchableAnnotationProperties() {
        module.searchableAnnotationProperties
    }

    HighDimensionFilterType getHighDimensionFilterType() {
        module.highDimensionFilterType
    }

    List<String> getSearchableProjections() {
        module.searchableProjections
    }

    Map<Long, List> getDistribution(ConstraintByOmicsValue constraint, String conceptKey, Long resultInstanceId = null) {
        // first translate the ConstraintByOmicsValue to DataConstraints and AssayConstraints
        List<DataConstraint> dataConstraints = []
        dataConstraints << createDataConstraint(
	    [property: constraint.property, term: constraint.selector, concept_key: conceptKey],
	    DataConstraint.ANNOTATION_CONSTRAINT)

        List<AssayConstraint> assayConstraints = []
        assayConstraints << createAssayConstraint([concept_key: conceptKey], AssayConstraint.ONTOLOGY_TERM_CONSTRAINT)
        if (resultInstanceId != null) {
            assayConstraints << createAssayConstraint([result_instance_id: resultInstanceId], AssayConstraint.PATIENT_SET_CONSTRAINT)
	}

        // create the requested projection
        Projection projection = createProjection([:], constraint.projectionType)

        // get the data
        TabularResult retrieved = retrieveData(assayConstraints, dataConstraints, projection)
        Map<Long, List> data = [:]

        // transform to a map where the keys are patient ids, and the values are the values of probes of the patient
        for (row in retrieved.rows) {
            row.assayIndexMap.each { assay, index ->
                if (row.data[index] != null) {
		    data.get(assay.patient.id, []) << row.data[index]
		}
            }
        }
        retrieved.close()

        // get the aggregator for our marker type
        Closure aggregator = highDimensionConstraintValuesAggregator(constraint)
        data.each {it.value = aggregator(it.value)} // set the value of each Map.Entry to the aggregated value

        // get the filter so we can apply it to the aggregated values
        Closure filter = highDimensionConstraintClosure(constraint)
        data.findAll {filter(it.value)}
    }

    /**
     * Creates a closure that takes a List and returns a single value, to be used as an aggregator for the given
     * ConstraintByOmicsValue. In many cases there will be multiple values per patient (e.g. multiple probes for
     * a given gene name), and the values need to be aggregated. Currently this method always returns a closure
     * that calculates the average.
     * @param constraint The omics value constraint
     * @return The aggregator closure
     */
    protected Closure highDimensionConstraintValuesAggregator(ConstraintByOmicsValue constraint) {
        {values -> values.sum() / values.size()}
    }

    /**
     * Creates a closure that takes a value and returns a boolean. It will return true if the value meets the given
     * constraint defined in the ConstraintByOmicsValue, and false otherwise. Ideally we would want to do this at
     * the database, however we can not create a 'HAVING' SQL statement in Hibernate
     */
    protected Closure highDimensionConstraintClosure(ConstraintByOmicsValue constraint) {
        if (getHighDimensionFilterType() == HighDimensionFilterType.SINGLE_NUMERIC ||
                getHighDimensionFilterType() == HighDimensionFilterType.ACGH) {
            // default aggregator for numeric is average
            // this should be parameterized in the future
            if (constraint.operator != null && constraint.constraint != null) {
                switch (constraint.operator) {
                    case ConstraintByOmicsValue.Operator.BETWEEN:
                        List<Double> limits = constraint.constraint.split(':')*.toDouble()
                        return {value -> limits[0] <= value && value <= limits[1]}
                    case ConstraintByOmicsValue.Operator.EQUAL_TO:
                        double limit = constraint.constraint.toDouble()
                        return {value -> limit == value}
                    case ConstraintByOmicsValue.Operator.GREATER_OR_EQUAL_TO:
                        double limit = constraint.constraint.toDouble()
                        return {value -> limit <= value}
                    case ConstraintByOmicsValue.Operator.GREATER_THAN:
                        double limit = constraint.constraint.toDouble()
                        return {value -> limit < value}
                    case ConstraintByOmicsValue.Operator.LOWER_OR_EQUAL_TO:
                        double limit = constraint.constraint.toDouble()
                        return {value -> limit >= value}
                    case ConstraintByOmicsValue.Operator.LOWER_THAN:
                        double limit = constraint.constraint.toDouble()
                        return {value -> limit > value}
                }
            }
        }
        {row -> true}
    }

    String toString() {
        'HighDimensionDataTypeResourceImpl{dataTypeName=' + dataTypeName +
                ', identity=' + System.identityHashCode(this) + '}'
    }

    boolean equals(o) {
        if (is(o)) {
	    return true
	}
        if (getClass() != o.class) {
	    return false
	}

        dataTypeName == ((HighDimensionDataTypeResourceImpl) o).dataTypeName
    }

    int hashCode() {
        dataTypeName.hashCode()
    }
}
