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
import grails.util.Environment

class BioAssayAnalysis implements IExcelProfile {
    String analysisMethodCode
    BioAssayAnalysisPlatform analysisPlatform
    String analystId
    String assayDataType
    Date createDate
    Long dataCount
    String etlId
    Double foldChangeCutoff
    String longDescription
    String name
    Double pValueCutoff
    String qaCriteria
    Double rValueCutoff
    String shortDescription
    Long teaDataCount
    String type

    static transients = ['uniqueId', 'values']

    static hasOne=[ext:BioAssayAnalysisExt]

    static hasMany = [datasets: BioAssayDataset,
	              diseases: Disease,
	              files: ContentReference,
	              observations: Observation,
	              platforms: BioAssayPlatform,
	              uniqueIds: BioData]

    static belongsTo = [BioAssayPlatform, ContentReference, Disease, Observation]

    static mapping = {
	table 'BIOMART.BIO_ASSAY_ANALYSIS'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_ANALYSIS_ID'
	version false

	analysisMethodCode column: 'ANALYSIS_METHOD_CD'
	analysisPlatform column: 'BIO_ASY_ANALYSIS_PLTFM_ID'
	assayDataType column: 'BIO_ASSAY_DATA_TYPE'
	createDate column:'ANALYSIS_CREATE_DATE'
	datasets joinTable: [name: 'BIOMART.BIO_ASY_ANALYSIS_DATASET', key: 'BIO_ASSAY_ANALYSIS_ID']
	diseases joinTable: [name: 'BIOMART.BIO_DATA_DISEASE', key: 'BIO_DATA_ID'], cache: true
	ext joinTable: [name: 'BIOMART.BIO_ASSAY_ANALYSIS_EXT', key: 'BIO_ASSAY_ANALYSIS_ID']
	files joinTable: [name: 'BIOMART.BIO_CONTENT_REFERENCE', key: 'BIO_DATA_ID', column: 'BIO_CONTENT_REFERENCE_ID'], cache: true
	name column: 'ANALYSIS_NAME'
	observations joinTable: [name: 'BIOMART.BIO_DATA_OBSERVATION', key: 'BIO_DATA_ID'], cache: true
	platforms joinTable: [name: 'BIOMART.BIO_DATA_PLATFORM', key: 'BIO_DATA_ID'], cache: true
	pValueCutoff column:'PVALUE_CUTOFF'
	rValueCutoff column:'RVALUE_CUTOFF'
	type column:'ANALYSIS_TYPE'
	uniqueIds joinTable: [name: 'BIOMART.BIO_DATA_UID', key: 'BIO_DATA_ID']
    }

    static constraints = {
	analysisPlatform nullable: true
	analystId nullable: true, maxSize: 1020
	createDate nullable: true
	ext nullable: true
	foldChangeCutoff nullable: true
	longDescription nullable: true, maxSize: 4000
	name nullable: true, maxSize: 1000
	pValueCutoff nullable: true
	qaCriteria nullable: true, maxSize: 4000
	rValueCutoff nullable: true
	shortDescription nullable: true, maxSize: 1020
	type nullable: true, maxSize: 400
    }

    /**
     * get top analysis data records for the indicated analysis
     */
    static List<Object[]> getTopAnalysisDataForAnalysis(long analysisId, int topCount) {
	// ordering fails in H2 (and possibly when deployed), so exclude it to test most of the query
	String orderBy = Environment.current == Environment.TEST ? '' :
	    'ORDER BY ABS(baad.foldChangeRatio) desc, baad.rValue, baad.rhoValue DESC'
	executeQuery '''
		SELECT DISTINCT baad, baad_bm
		FROM org.transmart.biomart.BioAssayAnalysisData baad
		JOIN baad.featureGroup.markers baad_bm
		WHERE baad.analysis.id=:aid
		''' + orderBy,
		[aid: analysisId], [max: topCount]
    }

    List getValues() {
	[shortDescription, longDescription, pValueCutoff, foldChangeCutoff, qaCriteria,
	 analysisPlatform == null ? '' : analysisPlatform.platformName, analysisMethodCode, assayDataType]
    }
    
    BioData getUniqueId() {
	uniqueIds?.iterator()?.next()
    }
}
