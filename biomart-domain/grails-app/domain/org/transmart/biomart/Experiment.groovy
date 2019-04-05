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

class Experiment implements IExcelProfile {
    String accession
    String accessType
    String bioMarkerType
    Date completionDate
    String country
    String description
    String design
    String institution
    String overallDesign
    String primaryInvestigator
    Date startDate
    String status
    String target
    String title
    String type

    static transients = ['compoundNames', 'diseaseNames', 'expId', 'expValues',
	                 'organismNames', 'uniqueId', 'values']

    static hasMany = [compounds: Compound,
	              diseases : Disease,
	              files    : ContentReference,
	              organisms: Taxonomy,
	              uniqueIds: BioData]

    static belongsTo = [Compound, ContentReference, Disease, Taxonomy]

    static mapping = {
	table 'BIOMART.BIO_EXPERIMENT'
        tablePerHierarchy false
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_EXPERIMENT_ID'
        version false

        bioMarkerType column: 'BIOMARKER_TYPE'
	compounds joinTable: [name: 'BIOMART.BIO_DATA_COMPOUND', key: 'BIO_DATA_ID'], cache: true
	diseases joinTable: [name: 'BIOMART.BIO_DATA_DISEASE', key: 'BIO_DATA_ID'], cache: true
	files joinTable: [name: 'BIOMART.BIO_CONTENT_REFERENCE', key: 'BIO_DATA_ID', column: 'BIO_CONTENT_REFERENCE_ID'], cache: true
	organisms joinTable: [name: 'BIOMART.BIO_DATA_TAXONOMY', key: 'BIO_DATA_ID'], cache: true
	type column: 'BIO_EXPERIMENT_TYPE'
	uniqueIds joinTable: [name: 'BIOMART.BIO_DATA_UID', key: 'BIO_DATA_ID']
    }

    static constraints = {
	accessType nullable: true
	bioMarkerType nullable: true
	completionDate nullable: true
	country nullable: true
	description nullable: true, maxSize: 4000
	design nullable: true, maxSize: 4000
	institution nullable: true
	overallDesign nullable: true, maxSize: 4000
	primaryInvestigator nullable: true, maxSize: 800
	startDate nullable: true
	status nullable: true
	target nullable: true
	title nullable: true, maxSize: 2000
	type nullable: true, maxSize: 400
    }

    String getCompoundNames() {
	getNames compounds, 'name'
    }

    String getDiseaseNames() {
	getNames diseases, 'disease'
    }

    String getOrganismNames() {
	getNames organisms, 'name'
    }

    List getValues() {
	[accession, type, title, description, design, status, overallDesign, startDate,
	 completionDate, primaryInvestigator, compoundNames, diseaseNames]
    }

    List getExpValues() {
	[accession, type, title, description, design, status, overallDesign, startDate,
	 completionDate, primaryInvestigator, compoundNames, diseaseNames]
    }

    BioData getUniqueId() {
	uniqueIds?.iterator()?.next()
    }

    /**
     * hack to get around GORM inheritance bug
     */
    Long getExpId() {
	id
    }

    String toString() {
	'id: ' + expId + '; type: ' + type + '; title: ' + title + '; description: ' + description + '; accession: ' + accession
    }

    private String getNames(Collection c, String propertyName) {
	StringBuilder names = new StringBuilder()
	for (item in c) {
	    if (item[propertyName]) {
		if (names) {
		    names << '; '
		}
		names << item[propertyName]
	    }
	}

	names
    }
}
