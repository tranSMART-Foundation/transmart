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

class BioAssayAnalysisDataTea implements IExcelProfile {
    Double adjustedPvalue
    String adjustedPValueCode
    BioAssayAnalysis analysis
    BioAssayPlatform assayPlatform
    Double cutValue
    Experiment experiment
    String experimentType
    BioAssayFeatureGroup featureGroup
    String featureGroupName
    Double foldChangeRatio
    Double numericValue
    String numericValueCode
    Double preferredPvalue
    Double rawPvalue
    String resultsValue
    Double rhoValue
    Double rValue
    Double teaNormalizedPValue
    Long teaRank

    static transients = ['values']

    static hasMany = [markers: BioMarker]

    static belongsTo = [BioMarker]

    static mapping = {
	table 'BIOMART.BIO_ASSAY_ANALYSIS_DATA_TEA'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_ASY_ANALYSIS_DATA_ID'
        version false

	adjustedPValueCode column: 'ADJUSTED_P_VALUE_CODE'
        analysis column: 'BIO_ASSAY_ANALYSIS_ID'
	assayPlatform column: 'BIO_ASSAY_PLATFORM_ID'
	experiment column: 'BIO_EXPERIMENT_ID'
	experimentType column: 'BIO_EXPERIMENT_TYPE'
        featureGroup column: 'BIO_ASSAY_FEATURE_GROUP_ID'
	markers joinTable: [name: 'BIOMART.BIO_DATA_OMIC_MARKER', key: 'BIO_DATA_ID']
        teaNormalizedPValue column: 'TEA_NORMALIZED_PVALUE'
    }

    /**
     * get top analysis data records for the indicated analysis
     */
    static List<Object[]> getTop50AnalysisDataForAnalysis(long analysisId) {
	executeQuery'''
		SELECT DISTINCT baad, baad_bm
		FROM org.transmart.biomart.BioAssayAnalysisDataTea baad
		JOIN baad.featureGroup.markers baad_bm
		WHERE baad.analysis.id=:aid
		  and baad.teaRank <= 50
		ORDER BY baad.teaRank DESC''',
		[aid: analysisId], [max: 50]
    }

    List getValues() {
	[featureGroupName, foldChangeRatio, rValue, rawPvalue, teaNormalizedPValue,
	 adjustedPvalue, rhoValue, cutValue, resultsValue, numericValueCode, numericValue]
    }
}
