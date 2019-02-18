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

import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.vcf.VcfCohortInfo
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import org.transmartproject.db.dataquery.highdim.AbstractDataRow

class VcfDataRow extends AbstractDataRow implements VcfValues, RegionRow, BioMarkerDataRow {
    String datasetId
    
    // Chromosome to define the position
    String chromosome
    Long position
    String rsId
    
    // Reference and alternatives for this position
    String referenceAllele
    String alternatives
    Boolean reference
    
    // Study level properties
    String quality
    String filter
    String info
    String format
    String variants

    // Gene data
    String geneName
    
    List<String> getAlternativeAlleles() {
		alternatives.split ','
    }
    
    @Lazy
    Double qualityOfDepth = { [infoFields.QD, quality].find { it?.double } as Double }()

    @Lazy
    Map<String, String> infoFields = { parseVcfInfo info }()
    
    @Lazy
    List<String> formatFields = { format.split ':' }()

    @Lazy
    VcfCohortInfo cohortInfo = { new VcfCohortStatistics(this) }()

    @Lazy
    private Map<String,String> allOriginalSubjectData = {
	Map<String, String> subjectVariants = [:]
	List<String> variantsInOrder = variants.tokenize '\t'
        
	for (d in data) {
	    if (d && d.subjectPosition != null && d.subjectId != null) {
                // Position starts at 1
		int index = (int) d.subjectPosition - 1
                if (index < variantsInOrder.size()) {
		    subjectVariants[d.subjectId] = variantsInOrder[index]
                }
            }
        }

        subjectVariants
    }()

    String getOriginalSubjectData(Assay assay) {
        allOriginalSubjectData[assay.sampleCode]
    }

    //RegionRow implementation
    String getLabel() { 'VCF: ' + chromosome + ':' + position }

    Long getId() { rsId }

    String getName() { rsId }

    String getCytoband() { rsId }

    Platform getPlatform() {}

    Long getStart() { position }

    Long getEnd() { position }

    Integer getNumberOfProbes() { 1 }
    
    private Map parseVcfInfo(String info) {
        if (!info) {
            return [:]
        }

        info.split(';').collectEntries {
	    String[] keyValues = it.split('=')
            [(keyValues[0]): keyValues.length > 1 ? keyValues[1] : true]
        }
    }

    String getBioMarker() {
        geneName
    }
}
