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

class Compound {
    String brandName
    String casRegistry
    String chemicalName
    String cntoNumber
    String codeName
    String description
    String genericName
    String mechanism
    String number
    String productCategory
    String sourceCode

    static transients = ['name']

    static hasMany = [experiments: Experiment,
	              literatures: Literature]

    static mapping = {
	table 'BIOMART.BIO_COMPOUND'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_COMPOUND_ID'
        version false
        cache usage: 'read-only'

	experiments joinTable: [name: 'BIOMART.BIO_DATA_COMPOUND', key: 'BIO_COMPOUND_ID']
	literatures joinTable: [name: 'BIOMART.BIO_DATA_COMPOUND', key: 'BIO_COMPOUND_ID']
        number column: 'JNJ_NUMBER'
        sourceCode column: 'SOURCE_CD'
    }

    static constraints = {
	brandName nullable: true, maxSize: 400
	casRegistry nullable: true, maxSize: 800
	chemicalName nullable: true, maxSize: 800
	cntoNumber nullable: true, maxSize: 400
	codeName nullable: true, maxSize: 400
	description nullable: true, maxSize: 2000
	genericName nullable: true, maxSize: 400
	mechanism nullable: true, maxSize: 800
	number nullable: true, maxSize: 400
	productCategory nullable: true, maxSize: 400
	sourceCode nullable: true, maxSize: 100
    }

    String getName() {
	genericName ?: brandName ?: number ?: cntoNumber
    }
}
