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

class BioAssayData {
    Long bioAssayDatasetId
    Long bioAssayId
    Long bioSampleId
    Experiment experiment
    String featureGroupName
    Double floatValue
    Double log10Value
    Double log2Value
    Long numericValue
    String textValue

    static hasMany = [compounds: Compound,
	              diseases: Disease,
	              markers: BioMarker]

    static mapping = {
	table 'BIOMART.BIO_ASSAY_DATA'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_DATA_ID'
        version false

	compounds joinTable: [name: 'BIOMART.BIO_DATA_COMPOUND', key: 'BIO_DATA_ID', column: 'BIO_COMPOUND_ID']
	diseases joinTable: [name: 'BIOMART.BIO_DATA_DISEASE', key: 'BIO_DATA_ID', column: 'BIO_DISEASE_ID']
        experiment column: 'BIO_EXPERIMENT_ID'
        log10Value column: 'LOG10_VALUE'
	log2Value column: 'LOG2_VALUE'
	markers joinTable: [name: 'BIOMART.BIO_DATA_OMIC_MARKER', key: 'BIO_DATA_ID', column: 'BIO_MARKER_ID']
    }
}
