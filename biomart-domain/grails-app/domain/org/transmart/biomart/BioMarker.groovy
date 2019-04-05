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
package org.transmart.biomart

import com.recomdata.util.IExcelProfile

class BioMarker implements IExcelProfile {
    String bioMarkerType
    String description
    String name
    String organism
    String primaryExternalId
    String primarySourceCode

    String uniqueId
    
    static transients = ['gene', 'pathway', 'uniqueId', 'values']

    static hasMany = [assayAnalysisData: BioAssayAnalysisData,
	              assayDataStats   : BioAssayDataStatistics,
	              associatedCorrels: BioDataCorrelation,
	              correlations     : BioDataCorrelation,
	              literatures      : Literature]

    static mapping = {
	table 'BIOMART.BIO_MARKER'
	id column: 'BIO_MARKER_ID'
        version false

	assayAnalysisData joinTable: [name: 'BIOMART.BIO_DATA_OMIC_MARKER', key: 'BIO_MARKER_ID']
	assayDataStats joinTable: [name: 'BIOMART.BIO_DATA_OMIC_MARKER', key: 'BIO_MARKER_ID']
	associatedCorrels joinTable: [name: 'BIOMART.BIO_DATA_CORRELATION', key: 'ASSO_BIO_DATA_ID', column: 'BIO_DATA_CORREL_ID']
	correlations joinTable: [name: 'BIOMART.BIO_DATA_CORRELATION', key: 'BIO_DATA_ID', column: 'BIO_DATA_CORREL_ID']
        description column: 'BIO_MARKER_DESCRIPTION'
	literatures joinTable: [name: 'BIOMART.BIO_DATA_OMIC_MARKER', key: 'BIO_MARKER_ID']
	name column: 'BIO_MARKER_NAME'
    }

    static constraints = {
	bioMarkerType maxSize: 400
	description nullable: true, maxSize: 2000
	name nullable: true, maxSize: 400
	organism nullable: true, maxSize: 400
	primaryExternalId nullable: true, maxSize: 400
	primarySourceCode nullable: true, maxSize: 400
    }

    static BioMarker findByUniqueId(String uniqueId) {
	executeQuery('from BioMarker where id=(select id from BioData where uniqueId=:uniqueId)',
		     [uniqueId: uniqueId])[0]
    }
    
    boolean isGene() {
	'GENE'.equalsIgnoreCase bioMarkerType
    }

    boolean isPathway() {
	'PATHWAY'.equalsIgnoreCase bioMarkerType
    }

    List getValues() {
	[name, description, organism]
    }

    String getUniqueId() {
	if (uniqueId) {
            return uniqueId
        }
    
	String bioDataUid = BioData.where { id == this.id }.uniqueId.get()
	if (bioDataUid) {
	    uniqueId = bioDataUid
	    return uniqueId
        }
    }
}
