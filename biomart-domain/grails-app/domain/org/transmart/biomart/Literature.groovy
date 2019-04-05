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

class Literature {
    Long bioCurationDatasetId
    String dataType
    LiteratureReferenceData reference
    String statement
    String statementStatus

    static hasMany = [compounds: Compound,
	              diseases: Disease,
	              files: ContentReference,
	              markers: BioMarker]

    static belongsTo = [BioMarker, Compound, ContentReference, Disease]

    static mapping = {
	table 'BIOMART.BIO_DATA_LITERATURE'
        tablePerHierarchy false
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_DATA_ID'
        version false

	compounds joinTable: [name: 'BIOMART.BIO_DATA_COMPOUND', key: 'BIO_DATA_ID']
	diseases joinTable: [name: 'BIOMART.BIO_DATA_DISEASE', key: 'BIO_DATA_ID']
	files joinTable: [name: 'BIOMART.BIO_CONTENT_REFERENCE', key: 'BIO_DATA_ID', column: 'BIO_CONTENT_REFERENCE_ID']
	markers joinTable: [name: 'BIOMART.BIO_DATA_OMIC_MARKER', key: 'BIO_DATA_ID']
        reference column: 'BIO_LIT_REF_DATA_ID'
    }
}
