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

package i2b2

class OntNode {
    String basecode
    String columndatatype
    String columnname
    String comment
    String dimcode
    Date downloaddate
    String facttablecolumn
    Long hlevel
    String id
    Date importdate
    String metadataxml
    String name
    String operator
    String securitytoken
    String sourcesystemcd
    String synonymcd
    String tablename
    String tooltip
    Long totalnum
    Date updatedate
    String valuetypecd
    String visualattributes

    static hasMany = [tags: OntNodeTag]

    static mapping = {
	table 'I2B2METADATA.I2B2_SECURE'
        id column: 'C_FULLNAME'
	version false

        basecode column: 'C_BASECODE'
        columndatatype column: 'C_COLUMNDATATYPE'
	columnname column: 'C_COLUMNNAME'
        comment column: 'C_COMMENT'
	dimcode column: 'C_DIMCODE'
        downloaddate column: 'DOWNLOAD_DATE'
	facttablecolumn column: 'C_FACTTABLECOLUMN'
	hlevel column: 'C_HLEVEL'
        importdate column: 'IMPORT_DATE'
	metadataxml column: 'C_METADATAXML'
	name column: 'C_NAME'
	operator column: 'C_OPERATOR'
        securitytoken column: 'SECURE_OBJ_TOKEN'
	sourcesystemcd column: 'SOURCESYSTEM_CD'
	synonymcd column: 'C_SYNONYM_CD'
	tablename column: 'C_TABLENAME'
        tags column: 'PATH', sort: 'relativePosition'
	tooltip column: 'C_TOOLTIP'
	totalnum column: 'C_TOTALNUM'
	updatedate column: 'UPDATE_DATE'
	valuetypecd column: 'VALUETYPE_CD'
	visualattributes column: 'C_VISUALATTRIBUTES'
    }

    static constraints = {
	basecode nullable: true, maxSize: 450
	columndatatype nullable: true, maxSize: 50
	columnname nullable: true, maxSize: 50
	comment nullable: true, maxSize: 4000
	dimcode nullable: true, maxSize: 900
	downloaddate nullable: true
	facttablecolumn nullable: true, maxSize: 50
	hlevel nullable: true
	importdate nullable: true
	metadataxml nullable: true, maxSize: 4000
	name nullable: true, maxSize: 2000
	operator nullable: true, maxSize: 10
	securitytoken nullable: true, maxSize: 50
	sourcesystemcd nullable: true, maxSize: 50
	synonymcd nullable: true, maxSize: 1
	tablename nullable: true, maxSize: 50
	tooltip nullable: true, maxSize: 900
	totalnum nullable: true
	updatedate nullable: true
	valuetypecd nullable: true, maxSize: 50
	visualattributes nullable: true, maxSize: 3
    }
}
