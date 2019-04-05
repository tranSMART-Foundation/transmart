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
 * ******************************************************************/

package org.transmart.biomart

class AnalysisMetadata {
    String analysisName
    String cellType
    String dataType
    String description
    Date etlDate
    String expressionPlatformIds
    String filename
    String genomeVersion
    String genotypePlatformIds
    String modelDescription
    String modelName
    String phenotypeIds
    String population
    Date processDate
    Double pValueCutoff
    String researchUnit
    String sampleSize
    String sensitiveDesc
    String sensitiveFlag = '0'
    String statisticalTest
    String status = 'NEW'
    String study
    String tissue

    static mapping = {
	table 'TM_LZ.LZ_SRC_ANALYSIS_METADATA'
	id generator: 'sequence', params: [sequence: 'TM_LZ.SEQ_ETL_ID'], column: 'ETL_ID'
	version false

	modelDescription		column:'MODEL_DESC'
	pValueCutoff column: 'PVALUE_CUTOFF'
	study column: 'STUDY_ID'
    }
	
    static constraints = {
	analysisName unique: true, maxSize: 50
	cellType nullable: true
	dataType nullable: true
	description nullable: true, maxSize: 4000
	etlDate nullable: true
	expressionPlatformIds nullable: true
	filename nullable: true
	genomeVersion nullable: true
	genotypePlatformIds nullable: true
	modelDescription nullable: true
	modelName nullable: true
	phenotypeIds nullable: true
	population nullable: true
	processDate nullable: true
	pValueCutoff nullable: true
	researchUnit nullable: true
	sampleSize nullable: true
	sensitiveDesc nullable: true
	statisticalTest nullable: true
	status nullable: true
	tissue nullable: true
    }
}
