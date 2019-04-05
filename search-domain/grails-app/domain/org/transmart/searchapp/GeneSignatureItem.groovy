/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/
package org.transmart.searchapp

import org.transmart.biomart.BioMarker

class GeneSignatureItem {
    Long bioAssayFeatureGroupId
    String bioDataUniqueId
    BioMarker bioMarker
    Double foldChgMetric
    Long probesetId

    static transients = ['geneSymbol', 'probeset']

    static belongsTo = [geneSignature: GeneSignature]

    static mapping = {
	table 'SEARCHAPP.SEARCH_GENE_SIGNATURE_ITEM'
	id generator: 'sequence', params: [sequence: 'SEARCHAPP.SEQ_SEARCH_DATA_ID']
        version false

        bioDataUniqueId column: 'BIO_DATA_UNIQUE_ID'
	bioMarker column: 'BIO_MARKER_ID'
	foldChgMetric column: "FOLD_CHG_METRIC"
	geneSignature column: 'SEARCH_GENE_SIGNATURE_ID'
        probesetId column: 'PROBESET_ID'
    }

    static constraints = {
	bioAssayFeatureGroupId nullable: true
	bioDataUniqueId nullable: true
	bioMarker nullable: true
	foldChgMetric nullable: true
	probesetId nullable: true
    }

    String getProbeset() {
	String probename = ''
        if (probesetId != null) {
	    // TODO BB DeMrnaAnnotation is in folder-management but it's not a dependency
	    def annot = de.DeMrnaAnnotation.findByProbesetId(probesetId)
	    if (annot) {
		probename = annot.probeId
	    }
        }
        return probename
    }

    List<String> getGeneSymbol() {
        if (bioMarker != null) {
	    [bioMarker.name]
        }
        else if (probesetId != null) {
	    de.DeMrnaAnnotation.findAllByProbesetId(probesetId)*.geneSymbol
        }
    }
}
