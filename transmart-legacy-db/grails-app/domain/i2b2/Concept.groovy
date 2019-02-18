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

class Concept {
    String baseCode
    String columnDataType
    String columnName
    String comment
    String dimCode
    Date downloadDate
    String factTableColumn
    String fullName
    Date importDate
    Integer level
    String metaDataXml
    String name
    String operator
    String sourceSystem
    String synonym
    String tableName
    String toolTip
    Integer totalNum
    Date updateDate
    String valueType
    String visualAttributes

    static mapping = {
	table 'I2B2METADATA.i2b2'
	id column: 'i2b2_id', generator: 'sequence', params: [sequence: 'I2B2METADATA.I2B2_ID_SEQ']
        version false

	baseCode column: 'C_BASECODE'
	columnDataType column: 'C_COLUMNDATATYPE'
	columnName column: 'C_COLUMNNAME'
	comment column: 'C_COMMENT'
	dimCode column: 'C_DIMCODE'
	downloadDate column: 'DOWNLOAD_DATE'
	factTableColumn column: 'C_FACTTABLECOLUMN'
	fullName column: 'C_FULLNAME'
	importDate column: 'IMPORT_DATE'
	level column: 'C_HLEVEL'
	metaDataXml column: 'C_METADATAXML'
	name column: 'C_NAME'
	operator column: 'C_OPERATOR'
	sourceSystem column: 'SOURCESYSTEM_CD'
	synonym column: 'C_SYNONYM_CD'
	tableName column: 'C_TABLENAME'
	toolTip column: 'C_TOOLTIP'
	totalNum column: 'C_TOTALNUM'
	updateDate column: 'UPDATE_DATE'
	valueType column: 'VALUETYPE_CD'
	visualAttributes column: 'C_VISUALATTRIBUTES'
    }
}
