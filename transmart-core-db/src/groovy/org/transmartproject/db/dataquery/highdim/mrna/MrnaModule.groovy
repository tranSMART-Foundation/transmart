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

package org.transmartproject.db.dataquery.highdim.mrna

import grails.orm.HibernateCriteriaBuilder
import groovy.util.logging.Slf4j
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
import org.transmartproject.db.dataquery.highdim.RepeatedEntriesCollectingTabularResult
import org.transmartproject.db.dataquery.highdim.correlations.CorrelationTypesRegistry
import org.transmartproject.db.dataquery.highdim.correlations.SearchKeywordDataConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.AllDataProjectionFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.DataRetrievalParameterFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.SimpleAnnotationConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.SimpleRealProjectionsFactory

import static org.transmartproject.db.util.GormWorkarounds.createCriteriaBuilder

@Slf4j('logger')
class MrnaModule extends AbstractHighDimensionDataTypeModule {

    final String name = 'mrna'
    final String description = "Messenger RNA data (Microarray)"
    final List<String> platformMarkerTypes = ['Gene Expression']
    final Map<String, Class> dataProperties = typesMap(DeSubjectMicroarrayDataCoreDb,
						       ['trialName', 'rawIntensity', 'logIntensity', 'zscore'])
    final Map<String, Class> rowProperties = typesMap(ProbeRow,
						      ['probe', 'geneId', 'geneSymbol'])

    @Autowired DataRetrievalParameterFactory standardAssayConstraintFactory
    @Autowired DataRetrievalParameterFactory standardDataConstraintFactory
    @Autowired CorrelationTypesRegistry correlationTypesRegistry

    HibernateCriteriaBuilder prepareDataQuery(Projection projection, SessionImplementor session) {

//	logger.info '{} prepareDataQuery start', name
	
	HibernateCriteriaBuilder criteriaBuilder = createCriteriaBuilder(
	    DeSubjectMicroarrayDataCoreDb, 'mrnadata', session)

        criteriaBuilder.with {
	    createAlias 'jProbe', 'p', JoinFragment.INNER_JOIN

            projections {
                property 'assay.id',     'assayId'

                property 'p.id',         'probeId'
                property 'p.probeId',    'probeName'
                property 'p.geneSymbol', 'geneSymbol'
                property 'p.geneId',     'geneId'
                property 'p.organism',   'organism'

                property 'patient.id',   'patientId'
            }

            order 'p.id',         'asc'
            order 'p.geneSymbol', 'asc' // see below
            order 'assay.id',     'asc' // important! See assumption below

            // because we're using this transformer, every column has to have an alias
	    instance.resultTransformer = Transformers.ALIAS_TO_ENTITY_MAP
        }

//	logger.info '{} prepareDataQuery done', name

        criteriaBuilder
    }

    TabularResult transformResults(ScrollableResults results, List<AssayColumn> assays, Projection projection) {
        /* assumption here is the assays in the passed in list are in the same
         * order as the assays in the result set */
	Map assayIndexMap = createAssayIndexMap(assays)

//	logger.info 'transformResults stack trace {}', Arrays.toString(Thread.currentThread().getStackTrace()).replace( ',', '\n' )
//	logger.info 'transformResults {} assayIndexMap {}', name, assayIndexMap.size()

	DefaultHighDimensionTabularResult preliminaryResult = new DefaultHighDimensionTabularResult(
            rowsDimensionLabel:    'Probes',
            columnsDimensionLabel: 'Sample codes',
            indicesList:           assays,
            results:               results,
            allowMissingAssays:    true,
            assayIdFromRow:        { it[0].assayId },
            inSameGroup:           { a, b -> a.probeId == b.probeId && a.geneSymbol == b.geneSymbol && a.geneId == b.geneId },
            finalizeGroup:         { List list -> /* list of arrays with one element: a map */
                /* we may have nulls if allowMissingAssays is true,
                 * but we're guaranteed to have at least one non-null */
                def firstNonNullCell = list.find()
                new ProbeRow(
                    probe:         firstNonNullCell[0].probeName,
                    geneSymbol:    firstNonNullCell[0].geneSymbol,
                    geneId:        firstNonNullCell[0].geneId,
                    assayIndexMap: assayIndexMap,
                    data:          list.collect { projection.doWithResult it?.getAt(0) }
                )
            }
        )

//	logger.info 'transformResults {} preliminaryResult indicesList {}', name, preliminaryResult.getIndicesList()

        /* In some implementations, probeset_id is actually not a primary key on
         * the annotations table and several rows will be returned for the same
         * probeset_id, just with different genes.
         * Hence the order by clause and the definition of inSameGroup above */
        new RepeatedEntriesCollectingTabularResult(
            tabularResult: preliminaryResult,
            collectBy: { it.probe },
            resultItem: {collectedList ->
                if (collectedList) {
//		    logger.info 'RepeatedEntries...Result {} ({})',collectedList[0], collectedList.size()
//		    logger.info 'RepeatedEntries...genesymbol {} geneid {} Result {}',
//			collectedList*.geneSymbol.join('/'), collectedList*.geneId.join('/'), collectedList[0]
                    new ProbeRow(
                        probe:         collectedList[0].probe,
                        geneSymbol:    collectedList*.geneSymbol.join('/'),
                        geneId:        collectedList*.geneId.join('/'),
                        assayIndexMap: collectedList[0].assayIndexMap,
                        data:          collectedList[0].data
                    )
		}
            }
        )
    }

    protected List<DataRetrievalParameterFactory> createAssayConstraintFactories() {
        [ standardAssayConstraintFactory ]
    }

    protected List<DataRetrievalParameterFactory> createDataConstraintFactories() {
        [ standardDataConstraintFactory,
         new SimpleAnnotationConstraintFactory(field: 'probe', annotationClass: DeMrnaAnnotationCoreDb.class),
         new SearchKeywordDataConstraintFactory(correlationTypesRegistry,
						'GENE', 'p', 'geneId') ]
    }

    protected List<DataRetrievalParameterFactory> createProjectionFactories() {
        [ new SimpleRealProjectionsFactory(
            (Projection.LOG_INTENSITY_PROJECTION): 'logIntensity',
            (Projection.DEFAULT_REAL_PROJECTION): 'rawIntensity',
            (Projection.ZSCORE_PROJECTION):       'zscore'),
         new AllDataProjectionFactory(dataProperties, rowProperties)]
    }

    List<String> searchAnnotation(String conceptCode, String searchTerm, String searchProperty) {
	if (!getSearchableAnnotationProperties().contains(searchProperty)) {
//	    logger.info 'searchAnnotation {} no such property {}', name, searchProperty
            return []
	}

//	logger.info 'searchAnnotation {} concept {} term {} property {}', name, conceptCode, searchTerm, searchProperty

        DeMrnaAnnotationCoreDb.createCriteria().list {
	    eq 'gplId', DeSubjectSampleMapping.createCriteria().get {
		eq 'conceptCode', conceptCode
                projections {distinct 'platform.id'}
	    }
	    ilike searchProperty, searchTerm + '%'
	    projections { distinct(searchProperty) }
	    order searchProperty, 'ASC'
	    maxResults 100
        }
    }

    List<String> getSearchableAnnotationProperties() {
	['geneSymbol', 'probeId']
    }

    HighDimensionFilterType getHighDimensionFilterType() {
        HighDimensionFilterType.SINGLE_NUMERIC
    }

    List<String> getSearchableProjections() {
        [Projection.LOG_INTENSITY_PROJECTION, Projection.DEFAULT_REAL_PROJECTION, Projection.ZSCORE_PROJECTION]
    }
}
