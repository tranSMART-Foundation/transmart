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

package org.transmartproject.db.dataquery.highdim.rnaseqcog

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
import org.transmartproject.db.dataquery.highdim.parameterproducers.StandardAssayConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.StandardDataConstraintFactory

import static org.transmartproject.db.util.GormWorkarounds.createCriteriaBuilder

/**
 * Module for RNA-seq, as implemented for Oracle by Cognizant.
 * This name is to distinguish it from the TraIT implementation.
 */
@Slf4j('logger')
class RnaSeqCogModule extends AbstractHighDimensionDataTypeModule {

    final String name = 'rnaseq_cog'
    final String description = "Messenger RNA data (Sequencing)"
    final List<String> platformMarkerTypes = ['RNASEQCOG']
    final Map<String, Class> dataProperties = typesMap(DeSubjectRnaData,
						       ['rawIntensity', 'logIntensity', 'zscore'])
    final Map<String, Class> rowProperties = typesMap(RnaSeqCogDataRow,
						      ['annotationId', 'geneSymbol', 'geneId'])

    @Autowired StandardAssayConstraintFactory standardAssayConstraintFactory
    @Autowired StandardDataConstraintFactory standardDataConstraintFactory
    @Autowired CorrelationTypesRegistry correlationTypesRegistry

    HibernateCriteriaBuilder prepareDataQuery(Projection projection, SessionImplementor session) {

//	logger.info '{} prepareDataQuery start', name
	
	HibernateCriteriaBuilder criteriaBuilder = createCriteriaBuilder(
	    DeSubjectRnaData, 'rnadata', session)

        criteriaBuilder.with {
	    createAlias 'jAnnotation', 'ann', JoinFragment.INNER_JOIN

            projections {
                property 'assay.id',         'assayId'

                property 'ann.id',           'annotationId'  // refers to transcriptId
                property 'ann.geneSymbol',   'geneSymbol'
                property 'ann.geneId',       'geneId'
            }

            order 'ann.id',         'asc' // refers to transcriptId
            order 'ann.geneSymbol', 'asc'
            order 'assay.id',       'asc' // important! See assumption below

            // because we're using this transformer, every column has to have an alias
            instance.resultTransformer = Transformers.ALIAS_TO_ENTITY_MAP
        }

//	logger.info '{} prepareDataQuery done', name

        criteriaBuilder
    }

    TabularResult transformResults(ScrollableResults results, List<AssayColumn> assays, Projection projection) {
	Map assayIndexMap = createAssayIndexMap(assays)

//	logger.info 'transformResults stack trace {}', Arrays.toString(Thread.currentThread().getStackTrace()).replace( ',', '\n' )
//	logger.info 'transformResults assayIndexMap {} size {}', name, assayIndexMap.size()

	DefaultHighDimensionTabularResult preliminaryResult = new DefaultHighDimensionTabularResult(
            rowsDimensionLabel:    'Transcripts',
            columnsDimensionLabel: 'Sample codes',
            indicesList:           assays,
            results:               results,
            allowMissingAssays:    true,
            assayIdFromRow:        { it[0].assayId },
            inSameGroup:           { a, b -> a.annotationId == b.annotationId && a.geneSymbol == b.geneSymbol && a.geneId == b.geneId },
            finalizeGroup:         { List list -> /* list of arrays with one element: a map */
                def firstNonNullCell = list.find()
                new RnaSeqCogDataRow(
                    annotationId:  firstNonNullCell[0].annotationId,
                    geneSymbol:    firstNonNullCell[0].geneSymbol,
                    geneId:        firstNonNullCell[0].geneId,
                    assayIndexMap: assayIndexMap,
                    data:          list.collect { projection.doWithResult it?.getAt(0) }
                )
            }
        )

//	logger.info 'transformResults {} preliminaryResult indicesList {}', name, preliminaryResult.getIndicesList()

        new RepeatedEntriesCollectingTabularResult(
            tabularResult: preliminaryResult,
            collectBy: { it.annotationId },
            resultItem: { collectedList ->
                if (collectedList) {
                    new RnaSeqCogDataRow(
                        annotationId:  collectedList[0].annotationId,
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
         new SimpleAnnotationConstraintFactory(field: 'annotation', annotationClass: DeRnaAnnotation.class),
         new SearchKeywordDataConstraintFactory(correlationTypesRegistry,
						'GENE', 'ann', 'geneId') ]
    }

    protected List<DataRetrievalParameterFactory> createProjectionFactories() {
        [ new SimpleRealProjectionsFactory(
            (Projection.LOG_INTENSITY_PROJECTION): 'logIntensity',
            (Projection.DEFAULT_REAL_PROJECTION): 'rawIntensity',
            (Projection.ZSCORE_PROJECTION):       'zscore'),
         new AllDataProjectionFactory(dataProperties, rowProperties)]
    }

    /*
     * Search HDD concept annotation (by code) for a value (searchTerm) for a property (e.g. geneSymbol)
     * to get a list of matching annotation rows
     */
    List<String> searchAnnotation(String conceptCode, String searchTerm, String searchProperty) {
	if (!getSearchableAnnotationProperties().contains(searchProperty)) {
//	    logger.info 'searchAnnotation {} no such property {}', name, searchProperty
            return []
	}

//	logger.info 'searchAnnotation {} concept {} term {} property {}', name, conceptCode, searchTerm, searchProperty

        DeRnaAnnotation.createCriteria().list {
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
	['geneSymbol', 'transcriptId']
    }

    HighDimensionFilterType getHighDimensionFilterType() {
        HighDimensionFilterType.SINGLE_NUMERIC
    }

    List<String> getSearchableProjections() {
        [Projection.LOG_INTENSITY_PROJECTION, Projection.DEFAULT_REAL_PROJECTION, Projection.ZSCORE_PROJECTION]
    }
}
