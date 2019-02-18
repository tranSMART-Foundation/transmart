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

package org.transmartproject.db.biomarker

class BioMarkerCoreDb {

    String description
    String externalId
    String name
    String organism
    String sourceCode
    String type

    static mapping = {
        table 'biomart.bio_marker'
        id          column: 'bio_marker_id', generator: 'assigned'
        version false

        description column: 'bio_marker_description'
        externalId  column: 'primary_external_id'
        name        column: 'bio_marker_name'
        sourceCode  column: 'primary_source_code'
        type        column: 'bio_marker_type'
    }

    static constraints = {
        description       nullable: true, maxSize: 2000
        externalId        nullable: true, maxSize: 400
        name              nullable: true, maxSize: 400
        organism          nullable: true, maxSize: 400
        sourceCode        nullable: true, maxSize: 400
        type              maxSize:  400
    }
}
