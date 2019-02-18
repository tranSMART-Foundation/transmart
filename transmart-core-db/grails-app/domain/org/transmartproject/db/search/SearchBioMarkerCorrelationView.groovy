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

package org.transmartproject.db.search

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = [ 'domainObjectId', 'associatedBioMarkerId', 'name', 'valueMetric', 'mvId' ])
class SearchBioMarkerCorrelationView implements Serializable {

    //is a view!

    Long associatedBioMarkerId
    String correlationType
    Long domainObjectId
    Long mvId
    Long valueMetric

    static mapping = {
        table 'searchapp.search_bio_mkr_correl_view'
        id                    composite: ['domainObjectId',   'associatedBioMarkerId', 'correlationType', 'valueMetric', 'mvId']
        version               false

        associatedBioMarkerId column:    'asso_bio_marker_id'
        correlationType       column:    'correl_type'
    }

    static constraints = {
        associatedBioMarkerId nullable: true
        correlationType       nullable: true, maxSize: 19
        domainObjectId        nullable: true
        mvId                  nullable: true
        valueMetric           nullable: true
    }
}
