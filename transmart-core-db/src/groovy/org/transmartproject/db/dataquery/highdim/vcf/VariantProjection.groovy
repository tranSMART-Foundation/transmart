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

package org.transmartproject.db.dataquery.highdim.vcf

import grails.orm.HibernateCriteriaBuilder
import org.hibernate.criterion.ProjectionList
import org.hibernate.criterion.Projections
import org.transmartproject.db.dataquery.highdim.projections.CriteriaProjection

/**
 * @author j.hudecek
 */
class VariantProjection implements CriteriaProjection<String> {

    void doWithCriteriaBuilder(HibernateCriteriaBuilder builder) {
        // Retrieving the criteriabuilder projection (which contains
        // the fields to be retrieved from the database)
        // N.B. This is a different object than the object we are 
        // currently in, although they are both called Projection!
        def projection = builder.instance.projection

        if (!(projection instanceof ProjectionList)) {
	    throw new IllegalArgumentException(
		'doWithCriteriaBuilder method requires a Hibernate Projectionlist to be set.')
        }

        // add an alias to make this ALIAS_TO_ENTITY_MAP-friendly
	projection.add Projections.alias(
            Projections.property( 'summary.variant'),
	    'variant')
    }

    String doWithResult(object) {
	object?.variant
    }
}
