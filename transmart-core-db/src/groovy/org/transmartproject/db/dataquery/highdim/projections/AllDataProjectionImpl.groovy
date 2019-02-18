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

package org.transmartproject.db.dataquery.highdim.projections

import grails.orm.HibernateCriteriaBuilder
import groovy.util.logging.Slf4j
import org.hibernate.criterion.ProjectionList
import org.hibernate.criterion.Projections
import org.transmartproject.core.dataquery.highdim.projections.AllDataProjection

/**
 * Collects all the fields specified in the constructor as a map from field name to value.
 */
@Slf4j('logger')
class AllDataProjectionImpl implements CriteriaProjection<Map<String, Object>>, AllDataProjection {

    Map<String, Class> dataProperties
    Map<String, Class> rowProperties

    AllDataProjectionImpl(Map<String, Class> dataProps, Map<String, Class> rowProps) {
	dataProperties = dataProps
	rowProperties = rowProps
    }

    void doWithCriteriaBuilder(HibernateCriteriaBuilder builder){
        def projection = builder.instance.projection

        if (!projection) {
	    logger.debug 'Skipping criteria manipulation because projection is not set'
            return
        }
        if (!(projection instanceof ProjectionList)) {
	    logger.debug 'Skipping criteria manipulation because projection is not a ProjectionList'
            return
        }

	for (String field in dataProperties.keySet()) {
            // add an alias to make this ALIAS_TO_ENTITY_MAP-friendly
	    projection.add Projections.alias(
                Projections.property(field),
		field)
        }
    }

    Map<String, Object> doWithResult(Object obj) {
	if (obj == null) {
	    return null
	} // missing data for an assay

	Map<String, Object> map = obj.clone()
        // assay is a hibernate association, that is not supported in the stateless session we are using.
        // It is already provided by the data row
	map.remove 'assay'
        map
    }
}
