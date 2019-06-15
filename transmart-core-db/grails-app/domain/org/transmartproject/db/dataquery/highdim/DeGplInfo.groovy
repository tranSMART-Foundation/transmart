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

import org.transmartproject.core.dataquery.highdim.Platform

class DeGplInfo implements Platform {

    Date    annotationDate
    String  genomeReleaseId
    String  id
    String  markerType
    String  organism
    String  title

    static mapping = {
        table         schema: 'deapp'
        id            column: 'platform',   generator: 'assigned'
        version      false

        genomeReleaseId column: 'genome_build'
        myTitle column: 'title'
    }

    static constraints = {
        annotationDate  nullable: true
        genomeReleaseId nullable: true
        id               maxSize:  50
        markerType      nullable: true, maxSize: 100
        organism        nullable: true, maxSize: 100
        title           nullable: true, maxSize: 500
    }

    Iterable<?> getTemplate() {
        throw new UnsupportedOperationException()
    }
}
