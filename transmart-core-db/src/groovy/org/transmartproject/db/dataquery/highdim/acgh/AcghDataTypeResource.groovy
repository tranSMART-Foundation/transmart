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

package org.transmartproject.db.dataquery.highdim.acgh

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.transmartproject.core.dataquery.highdim.acgh.ChromosomalSegment
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.exceptions.EmptySetException
import org.transmartproject.db.dataquery.highdim.AssayQuery
import org.transmartproject.db.dataquery.highdim.HighDimensionDataTypeResourceImpl
import org.transmartproject.db.dataquery.highdim.chromoregion.DeChromosomalRegion

/**
 * @author glopes
 */
@InheritConstructors
@Slf4j('logger')
class AcghDataTypeResource extends HighDimensionDataTypeResourceImpl {

    List<ChromosomalSegment> retrieveChromosomalSegments(List<AssayConstraint> assayConstraints) {

        AssayQuery assayQuery = new AssayQuery(assayConstraints)
        def assayPlatformsQuery = assayQuery.forEntities().where {
            projections {
                distinct 'platform.id'
                id()
            }
        }

        Set platformIds = assayPlatformsQuery.list().collect { it[0] }
        if (!platformIds) {
            throw new EmptySetException('No assays satisfy the provided criteria')
        }

        logger.debug 'Now getting regions for platforms: {}', platformIds

        List<Object[]> rows = DeChromosomalRegion.executeQuery('''
            SELECT region.chromosome, min(region.start), max(region.end)
            FROM DeChromosomalRegion region
            WHERE region.platform.id in (:platformIds)
            GROUP BY region.chromosome''',
		[platformIds: platformIds])

	if (!rows) {
	    throw new EmptySetException("No regions found for platform ids: $platformIds")
	}

	rows.collect { Object[] it ->
	    new ChromosomalSegment(chromosome: (String) it[0], start: (long) it[1], end: (long) it[2])
	}
    }
}
