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

import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.UnsupportedByDataTypeException
import org.transmartproject.db.dataquery.highdim.parameterproducers.DataRetrievalParameterFactory

import javax.annotation.PostConstruct

abstract class AbstractHighDimensionDataTypeModule implements HighDimensionDataTypeModule {

    @Autowired
    SessionFactory sessionFactory

    protected List<DataRetrievalParameterFactory> assayConstraintFactories
    protected List<DataRetrievalParameterFactory> dataConstraintFactories
    protected List<DataRetrievalParameterFactory> projectionFactories

    @Autowired
    HighDimensionResourceService highDimensionResourceService

    static Map<String, Class> typesMap(Class domainClass, List<String> fields, Map<String, String> translationMap = [:]) {
        fields.collectEntries({
            [(it): domainClass.metaClass.getMetaProperty(translationMap.get(it, it)).type]
        }).asImmutable()
    }

    @PostConstruct
    void init() {
        highDimensionResourceService.registerHighDimensionDataTypeModule(
            name, this.&createHighDimensionResource)
    }

    HighDimensionDataTypeResource createHighDimensionResource(Map params) {
        // params are unused; at least for now
        new HighDimensionDataTypeResourceImpl(this)
    }

    @Lazy
    volatile Set<String> supportedAssayConstraints = {
        initializeFactories()
        assayConstraintFactories.inject(new HashSet()) { Set<String> accum, DataRetrievalParameterFactory elem ->
                    accum.addAll elem.supportedNames
                    accum
        }
    }()

    @Lazy
    volatile Set<String> supportedDataConstraints = {
        initializeFactories()
        dataConstraintFactories.inject(new HashSet()) { Set<String> accum, DataRetrievalParameterFactory elem ->
                    accum.addAll elem.supportedNames
                    accum
        }
    }()

    @Lazy
    volatile Set<String> supportedProjections = {
        initializeFactories()
        projectionFactories.inject(new HashSet()) { Set<String> accum, DataRetrievalParameterFactory elem ->
                    accum.addAll elem.supportedNames
                    accum
        }
    }()

    protected synchronized final initializeFactories() {
        if (assayConstraintFactories != null) {
            return // already initialized
        }

        assayConstraintFactories = createAssayConstraintFactories()
        dataConstraintFactories  = createDataConstraintFactories()
        projectionFactories      = createProjectionFactories()
    }

    protected abstract List<DataRetrievalParameterFactory> createAssayConstraintFactories()

    protected abstract List<DataRetrievalParameterFactory> createDataConstraintFactories()

    protected abstract List<DataRetrievalParameterFactory> createProjectionFactories()

    AssayConstraint createAssayConstraint(Map<String, Object> params, String name) {
        initializeFactories()
        for (factory in assayConstraintFactories) {
            if (factory.supports(name)) {
                return factory.createFromParameters(name, params, this.&createAssayConstraint)
            }
        }

        throw new UnsupportedByDataTypeException("The data type ${name} does not support the assay constraint $name")
    }

    DataConstraint createDataConstraint(Map<String, Object> params, String name) {
        initializeFactories()
        for (factory in dataConstraintFactories) {
            if (factory.supports(name)) {
                return factory.createFromParameters(name, params, this.&createDataConstraint)
            }
        }

        throw new UnsupportedByDataTypeException("The data type $name does not support the data constraint $name")
    }

    Projection createProjection(Map<String, Object> params, String name) {
        initializeFactories()
        for (factory in projectionFactories) {
            if (factory.supports(name)) {
                return factory.createFromParameters(name, params, this.&createProjection)
            }
        }

        throw new UnsupportedByDataTypeException("The data type $name does not support the projection $name")
    }

    protected final Map createAssayIndexMap(List assays) {
        int i = 0
        assays.collectEntries { [ it, i++ ] }
    }
}
