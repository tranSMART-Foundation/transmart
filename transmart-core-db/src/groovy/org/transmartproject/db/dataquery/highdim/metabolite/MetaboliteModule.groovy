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

package org.transmartproject.db.dataquery.highdim.metabolite

import grails.orm.HibernateCriteriaBuilder
import org.hibernate.ScrollableResults
import org.hibernate.engine.SessionImplementor
import org.hibernate.sql.JoinFragment
import org.hibernate.transform.Transformers
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.querytool.HighDimensionFilterType
import org.transmartproject.db.dataquery.highdim.AbstractHighDimensionDataTypeModule
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.DefaultHighDimensionTabularResult
import org.transmartproject.db.dataquery.highdim.correlations.CorrelationType
import org.transmartproject.db.dataquery.highdim.correlations.CorrelationTypesRegistry
import org.transmartproject.db.dataquery.highdim.correlations.SearchKeywordDataConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.AllDataProjectionFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.DataRetrievalParameterFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.SimpleAnnotationConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.SimpleRealProjectionsFactory

import javax.annotation.PostConstruct

import static org.transmartproject.db.util.GormWorkarounds.createCriteriaBuilder

class MetaboliteModule extends AbstractHighDimensionDataTypeModule {

    final String name = 'metabolite'
    final List<String> platformMarkerTypes = ['METABOLOMICS']
    final String description = 'Metabolomics data (Mass Spec)'
    final Map<String, Class> dataProperties = typesMap(DeSubjectMetabolomicsData,
						       ['rawIntensity', 'logIntensity', 'zscore'])
    final Map<String, Class> rowProperties = typesMap(MetaboliteDataRow,
						      ['hmdbId', 'biochemicalName'])

    @Autowired DataRetrievalParameterFactory standardAssayConstraintFactory
    @Autowired DataRetrievalParameterFactory standardDataConstraintFactory
    @Autowired CorrelationTypesRegistry correlationTypesRegistry

    @PostConstruct
    void registerCorrelations() {
        correlationTypesRegistry.registerConstraint 'METABOLITE',              'metabolites'
        correlationTypesRegistry.registerConstraint 'METABOLITE_SUBPATHWAY',   'metabolite_subpathways'
        correlationTypesRegistry.registerConstraint 'METABOLITE_SUPERPATHWAY', 'metabolite_superpathways'

        correlationTypesRegistry.registerCorrelation new CorrelationType(
            name:       'METABOLITE',
            sourceType: 'METABOLITE',
            targetType: 'METABOLITE')

        correlationTypesRegistry.registerCorrelation new CorrelationType(
            name:             'SUPERPATHWAY TO METABOLITE',
            sourceType:       'METABOLITE_SUPERPATHWAY',
            targetType:       'METABOLITE',
            correlationTable: 'BIOMART.BIO_METAB_SUPERPATHWAY_VIEW',
            leftSideColumn:   'SUPERPATHWAY_ID')

        correlationTypesRegistry.registerCorrelation new CorrelationType(
            name:             'SUBPATHWAY TO METABOLITE',
            sourceType:       'METABOLITE_SUBPATHWAY',
            targetType:       'METABOLITE',
            correlationTable: 'BIOMART.BIO_METAB_SUBPATHWAY_VIEW',
            leftSideColumn:   'SUBPATHWAY_ID')
    }

    protected List<DataRetrievalParameterFactory> createAssayConstraintFactories() {
        [ standardAssayConstraintFactory ]
    }

    protected List<DataRetrievalParameterFactory> createDataConstraintFactories() {
        [ standardDataConstraintFactory,
         new SimpleAnnotationConstraintFactory(field: 'annotation', annotationClass: DeMetaboliteAnnotation.class),
         new SearchKeywordDataConstraintFactory(correlationTypesRegistry,
						'METABOLITE', 'a', 'hmdbId')]
    }

    protected List<DataRetrievalParameterFactory> createProjectionFactories() {
        [ new SimpleRealProjectionsFactory(
            (Projection.LOG_INTENSITY_PROJECTION): 'logIntensity',
            (Projection.DEFAULT_REAL_PROJECTION): 'rawIntensity',
            (Projection.ZSCORE_PROJECTION):       'zscore'),
         new AllDataProjectionFactory(dataProperties, rowProperties)]
    }

    HibernateCriteriaBuilder prepareDataQuery(Projection projection, SessionImplementor session) {
	HibernateCriteriaBuilder criteriaBuilder = createCriteriaBuilder(
	    DeSubjectMetabolomicsData, 'metabolitedata', session)

        criteriaBuilder.with {
	    createAlias 'jAnnotation', 'a', JoinFragment.INNER_JOIN

            projections {
                property 'assay.id',          'assayId'
                property 'a.id',              'annotationId'
                property 'a.hmdbId',          'hmdbId'
                property 'a.biochemicalName', 'biochemicalName'
            }

            order 'a.id',     'asc'
            order 'assay.id', 'asc'
            instance.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        }
        criteriaBuilder
    }

    TabularResult transformResults(ScrollableResults results, List<AssayColumn> assays, Projection projection) {
	Map assayIndexes = createAssayIndexMap(assays)

        new DefaultHighDimensionTabularResult(
            rowsDimensionLabel: 'Metabolites',
            columnsDimensionLabel: 'Sample codes',
            indicesList: assays,
            results: results,
            allowMissingAssays: true,
            assayIdFromRow: { it[0].assayId },
            inSameGroup: { a, b -> a.annotationId == b.annotationId },
            finalizeGroup: { List list -> /* list of arrays with one element: a map */
                def firstNonNullCell = list.find()
                new MetaboliteDataRow(
                    biochemicalName: firstNonNullCell[0].biochemicalName,
                    hmdbId: firstNonNullCell[0].hmdbId,
                    assayIndexMap: assayIndexes,
                    data: list.collect { projection.doWithResult it?.getAt(0) }
                )
            }
        )
    }

    List<String> searchAnnotation(String concept_code, String search_term, String search_property) {
	if (!getSearchableAnnotationProperties().contains(search_property)) {
            return []
	}

        DeMetaboliteAnnotation.createCriteria().list {
            dataRows {
                'in'('assay', DeSubjectSampleMapping.createCriteria().listDistinct {eq('conceptCode', concept_code)} )
            }
            ilike(search_property, search_term + '%')
            projections { distinct(search_property) }
            maxResults 100 
            order(search_property, 'ASC')
        }
    }

    List<String> getSearchableAnnotationProperties() {
        ['hmdbId', 'biochemicalName']
    }

    HighDimensionFilterType getHighDimensionFilterType() {
        HighDimensionFilterType.SINGLE_NUMERIC
    }

    List<String> getSearchableProjections() {
        [Projection.LOG_INTENSITY_PROJECTION, Projection.DEFAULT_REAL_PROJECTION, Projection.ZSCORE_PROJECTION]
    }
}
