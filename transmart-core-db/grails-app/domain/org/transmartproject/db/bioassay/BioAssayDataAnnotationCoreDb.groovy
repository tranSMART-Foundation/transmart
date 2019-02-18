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

package org.transmartproject.db.bioassay

import groovy.transform.EqualsAndHashCode
import org.transmartproject.db.biomarker.BioMarkerCoreDb

@EqualsAndHashCode(includes = [ 'bioMarker', 'probeSet' ])
class BioAssayDataAnnotationCoreDb implements Serializable {
    private static final long serialVersionUID = 1

    String dataTable

    static belongsTo = [bioMarker: BioMarkerCoreDb,
			probeSet:  BioAssayFeatureGroupCoreDb]

    static mapping = {
        table 'biomart.bio_assay_data_annotation'
        id composite: [ 'bioMarker', 'probeSet' ]
        version false

        probeSet  column: 'bio_assay_feature_group_id'
    }

    static constraints = {
        dataTable nullable: true
    }
}
