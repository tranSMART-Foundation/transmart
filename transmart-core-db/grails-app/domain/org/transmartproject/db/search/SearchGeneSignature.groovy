/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.search

import org.transmartproject.db.user.User

// This is a very basic mapping; It's not supposed to support inserts, unlike the version in transmartApp
class SearchGeneSignature {

    Long analysisMethodConceptId
    String analysisMethodOther
    String analystName
    Long analyticCatConceptId
    String analyticCatOther
    Long bioAssayPlatformId
    Date createDate
    User creator
    Boolean deletedFlag = false
    String description
    String experimentTypeAtccRef
    Long experimentTypeCellLineId
    Long experimentTypeConceptId
    String experimentTypeInVivoDescr
    Long foldChgMetricConceptId
    Date lastModifiedDate
    User modifier
    Boolean multipleTestingCorrection
    String name
    Long normMethodConceptId
    String normMethodOther
    Long ownerConceptId
    SearchGeneSignature parentGeneSignature
    String pmidList
    Boolean publicFlag = false
    Long PValueCutoffConceptId
    Long searchGeneSigFileSchemaId //should actually reference search_gene_sig_file_schema
    Long sourceConceptId
    String sourceOther
    Long speciesConceptId
    String speciesMouseDetail
    Long speciesMouseSrcConceptId
    String stimulusDescription
    String stimulusDosing
    Long tissueTypeConceptId
    Long treatmentBioCompoundId
    String treatmentDescription
    String treatmentDosing
    String treatmentProtocolNumber
    String uniqueId
    String uploadFile
    String versionNumber

    static hasMany = [ searchGeneSignatures: SearchGeneSignature ]

    static mapping = {
        table schema: 'searchapp'
	id                      column: 'search_gene_signature_id',  generator: 'assigned'
	version false

        creator                 column: 'created_by_auth_user_id'
        modifier                column: 'modified_by_auth_user_id'
        parentGeneSignature     column: 'parent_gene_signature_id'
        'PValueCutoffConceptId' column: 'p_value_cutoff_concept_id'
    }

    static constraints = {
	analysisMethodConceptId nullable: true
	analysisMethodOther nullable: true
	analystName nullable: true, maxSize: 100
	analyticCatConceptId nullable: true
	analyticCatOther nullable: true
	deletedFlag nullable: true
	description nullable: true, maxSize: 1000
	experimentTypeAtccRef nullable: true
	experimentTypeCellLineId nullable: true
	experimentTypeConceptId nullable: true
	experimentTypeInVivoDescr nullable: true
	foldChgMetricConceptId nullable: true
	lastModifiedDate nullable: true
	modifier nullable: true
	multipleTestingCorrection nullable: true
	name maxSize: 100
	normMethodConceptId nullable: true
	normMethodOther nullable: true
	ownerConceptId nullable: true
	pmidList nullable: true
	publicFlag nullable: true
	searchGeneSigFileSchemaId nullable: true
	sourceConceptId nullable: true
	sourceOther nullable: true
	speciesMouseDetail nullable: true
	speciesMouseSrcConceptId nullable: true
	stimulusDescription nullable: true, maxSize: 1000
	stimulusDosing nullable: true
	tissueTypeConceptId nullable: true
	treatmentBioCompoundId nullable: true
	treatmentDescription nullable: true, maxSize: 1000
	treatmentDosing nullable: true
	treatmentProtocolNumber nullable: true, maxSize: 50
	uniqueId nullable: true, maxSize: 50
	versionNumber nullable: true, maxSize: 50
    }
}
