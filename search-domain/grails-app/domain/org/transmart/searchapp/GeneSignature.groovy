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

package org.transmart.searchapp

import com.recomdata.util.ExcelGenerator
import com.recomdata.util.ExcelSheet
import com.recomdata.util.IDomainExcelWorkbook

import groovy.util.logging.Slf4j

import org.hibernate.Hibernate
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.CellLine
import org.transmart.biomart.Compound
import org.transmart.biomart.ConceptCode

@Slf4j('logger')
class GeneSignature implements Cloneable, IDomainExcelWorkbook {

    public static final String DOMAIN_KEY = 'GENESIG'
    public static final String DISPLAY_TAG = 'Gene Signature'

    // gene list version
    public static final String DOMAIN_KEY_GL = 'GENELIST'
    public static final String DISPLAY_TAG_GL = 'Gene List'

    ConceptCode analysisMethodConceptCode
    String analysisMethodOther
    String analystName
    ConceptCode analyticCatConceptCode
    String analyticCatOther
    AuthUser createdByAuthUser
    Date dateCreated
    Boolean deletedFlag
    String description
    String experimentTypeATCCRef
    CellLine experimentTypeCellLine
    ConceptCode experimentTypeConceptCode
    String experimentTypeInVivoDescr
    GeneSignatureFileSchema fileSchema
    ConceptCode foldChgMetricConceptCode
    Date lastUpdated
    AuthUser modifiedByAuthUser
    Boolean multipleTestingCorrection = false
    String name
    ConceptCode normMethodConceptCode
    String normMethodOther
    ConceptCode ownerConceptCode
    GeneSignature parentGeneSignature
    String pmIds
    Boolean publicFlag
    ConceptCode pValueCutoffConceptCode
    ConceptCode sourceConceptCode
    String sourceOther
    ConceptCode speciesConceptCode
    String speciesMouseDetail
    ConceptCode speciesMouseSrcConceptCode
    String stimulusDescription
    String stimulusDosing
    BioAssayPlatform techPlatform
    ConceptCode tissueTypeConceptCode
    Compound treatmentCompound
    String treatmentDescription
    String treatmentDosing
    String treatmentProtocolNumber
    String uniqueId
    String uploadFile
    String versionNumber

    static transients = ['pmIdsAsList']

    static hasMany = [geneSigItems: GeneSignatureItem]

    static mapping = {
	table 'SEARCHAPP.SEARCH_GENE_SIGNATURE'
	id generator: 'sequence', params: [sequence: 'SEARCHAPP.SEQ_SEARCH_DATA_ID'], column: 'SEARCH_GENE_SIGNATURE_ID'
        version false

	analysisMethodConceptCode column: 'ANALYSIS_METHOD_CONCEPT_ID'
	analyticCatConceptCode column: 'ANALYTIC_CAT_CONCEPT_ID'
        dateCreated column: 'CREATE_DATE'
	experimentTypeATCCRef column: 'EXPERIMENT_TYPE_ATCC_REF'
	experimentTypeConceptCode column: 'EXPERIMENT_TYPE_CONCEPT_ID'
	fileSchema column: 'SEARCH_GENE_SIG_FILE_SCHEMA_ID'
	foldChgMetricConceptCode column: 'FOLD_CHG_METRIC_CONCEPT_ID'
	geneSigItems sort: 'foldChgMetric'
        lastUpdated column: 'LAST_MODIFIED_DATE'
	normMethodConceptCode column: 'NORM_METHOD_CONCEPT_ID'
	ownerConceptCode column: 'OWNER_CONCEPT_ID'
        pmIds column: 'PMID_LIST'
	pValueCutoffConceptCode column: 'P_VALUE_CUTOFF_CONCEPT_ID'
	sourceConceptCode column: 'SOURCE_CONCEPT_ID'
	speciesConceptCode column: 'SPECIES_CONCEPT_ID'
	speciesMouseSrcConceptCode column: 'SPECIES_MOUSE_SRC_CONCEPT_ID'
	techPlatform column: 'BIO_ASSAY_PLATFORM_ID'
	tissueTypeConceptCode column: 'TISSUE_TYPE_CONCEPT_ID'
	treatmentCompound column: 'TREATMENT_BIO_COMPOUND_ID'
    }

    static constraints = {
	analysisMethodConceptCode nullable: true
	analysisMethodOther nullable: true
	analystName nullable: true, maxSize: 100
	analyticCatConceptCode nullable: true
	analyticCatOther nullable: true
	description nullable: true, maxSize: 1000
	experimentTypeATCCRef nullable: true
	experimentTypeCellLine nullable: true
	experimentTypeConceptCode nullable: true
	experimentTypeInVivoDescr nullable: true
	lastUpdated nullable: true
	modifiedByAuthUser nullable: true
	name maxSize: 100
	normMethodConceptCode nullable: true
	normMethodOther nullable: true
	ownerConceptCode nullable: true
	parentGeneSignature nullable: true
	pmIds nullable: true
	sourceConceptCode nullable: true
	sourceOther nullable: true
	speciesMouseDetail nullable: true
	speciesMouseSrcConceptCode nullable: true
	stimulusDescription nullable: true, maxSize: 1000
	stimulusDosing nullable: true
	tissueTypeConceptCode nullable: true
	treatmentCompound nullable: true
	treatmentDescription nullable: true, maxSize: 1000
	treatmentDosing nullable: true
	treatmentProtocolNumber nullable: true, maxSize: 50
	uniqueId nullable: true, maxSize: 50
	versionNumber nullable: true, maxSize: 50
    }

    def beforeUpdate() {
        if(uniqueId.startsWith(DOMAIN_KEY_GL)) {
            uniqueId = DOMAIN_KEY_GL + ':' + id
        }
        else {
            uniqueId = DOMAIN_KEY + ':' + id
        }
    }

    void updateUniqueId() {
	setUniqueId DOMAIN_KEY + ':' + id
    }

    def updateUniqueIdList() {
	setUniqueId DOMAIN_KEY_GL + ':' + id
    }

    /**
     * parse comma separated Ids into a list
     */
    List<String> getPmIdsAsList() {
	if (pmIds == null) {
	    return []
        }

	List<String> ids = pmIds.split(',')*.trim()

	ids
    }

    GeneSignature clone() {

        GeneSignature clone = new GeneSignature()
	copyPropertiesTo clone
        
	clone
    }

    /**
     * create a Map with the properties and values for each property similar to a request map
     */
    Map createParamMap() {
	[name                           : name,
	 description                    : description,
	 uploadFile                     : uploadFile,
	 'fileSchema.id'                : fileSchemaId,
	 'foldChgMetricConceptCode.id'  : foldChgMetricConceptCodeId,
	 'analyticCatConceptCode.id'    : analyticCatConceptCodeId,
	 analyticCatOther               : analyticCatOther,
	 'techPlatform.id'              : techPlatformId,
	 analystName                    : analystName,
	 'normMethodConceptCode.id'     : normMethodConceptCodeId,
	 normMethodOther                : normMethodOther,
	 'analysisMethodConceptCode.id' : analysisMethodConceptCodeId,
	 analysisMethodOther            : analysisMethodOther,
	 multipleTestingCorrection      : multipleTestingCorrection,
	 'pValueCutoffConceptCode.id'   : pValueCutoffConceptCodeId,
	 uniqueId                       : uniqueId,
	 publicFlag                     : publicFlag,
	 deletedFlag                    : deletedFlag,
	 'sourceConceptCode.id'         : sourceConceptCodeId,
	 sourceOther                    : sourceOther,
	 'ownerConceptCode.id'          : ownerConceptCodeId,
	 stimulusDescription            : stimulusDescription,
	 stimulusDosing                 : stimulusDosing,
	 treatmentDescription           : treatmentDescription,
	 treatmentDosing                : treatmentDosing,
	 'treatmentCompound.id'         : treatmentCompoundId,
	 treatmentProtocolNumber        : treatmentProtocolNumber,
	 pmIds                          : pmIds,
	 'speciesConceptCode.id'        : speciesConceptCodeId,
	 'speciesMouseSrcConceptCode.id': speciesMouseSrcConceptCodeId,
	 speciesMouseDetail             : speciesMouseDetail,
	 'tissueTypeConceptCode.id'     : tissueTypeConceptCodeId,
	 'experimentTypeConceptCode.id' : experimentTypeConceptCodeId,
	 'experimentTypeCellLine.id'    : experimentTypeCellLineId,
	 experimentTypeInVivoDescr      : experimentTypeInVivoDescr,
	 experimentTypeATCCRef          : experimentTypeATCCRef,
	 'createdByAuthUser.id'         : createdByAuthUserId,
	 dateCreated                    : dateCreated,
	 'modifiedByAuthUser.id'        : modifiedByAuthUserId,
	 lastUpdated                    : lastUpdated,
	 versionNumber                  : versionNumber]
    }

    /**
     * copy properties from this instance to the specified object
     */
    void copyPropertiesTo(GeneSignature gs) {
	gs.analysisMethodConceptCode = analysisMethodConceptCode
	gs.analysisMethodOther = analysisMethodOther
	gs.analystName = analystName
	gs.analyticCatConceptCode = analyticCatConceptCode
	gs.analyticCatOther = analyticCatOther
	gs.createdByAuthUser = createdByAuthUser
	gs.dateCreated = dateCreated
	gs.deletedFlag = deletedFlag
        gs.description = description
	gs.experimentTypeATCCRef = experimentTypeATCCRef
	gs.experimentTypeCellLine = experimentTypeCellLine
	gs.experimentTypeConceptCode = experimentTypeConceptCode
	gs.experimentTypeInVivoDescr = experimentTypeInVivoDescr
        gs.fileSchema = fileSchema
        gs.foldChgMetricConceptCode = foldChgMetricConceptCode
	gs.lastUpdated = lastUpdated
	gs.modifiedByAuthUser = modifiedByAuthUser
	gs.multipleTestingCorrection = multipleTestingCorrection
	gs.name = name
        gs.normMethodConceptCode = normMethodConceptCode
        gs.normMethodOther = normMethodOther
	gs.ownerConceptCode = ownerConceptCode
	gs.pmIds = pmIds
        gs.publicFlag = publicFlag
	gs.pValueCutoffConceptCode = pValueCutoffConceptCode
        gs.sourceConceptCode = sourceConceptCode
        gs.sourceOther = sourceOther
	gs.speciesConceptCode = speciesConceptCode
	gs.speciesMouseDetail = speciesMouseDetail
	gs.speciesMouseSrcConceptCode = speciesMouseSrcConceptCode
        gs.stimulusDescription = stimulusDescription
        gs.stimulusDosing = stimulusDosing
	gs.techPlatform = techPlatform
	gs.tissueTypeConceptCode = tissueTypeConceptCode
	gs.treatmentCompound = treatmentCompound
        gs.treatmentDescription = treatmentDescription
        gs.treatmentDosing = treatmentDosing
        gs.treatmentProtocolNumber = treatmentProtocolNumber
	gs.uniqueId = uniqueId
	gs.uploadFile = uploadFile
        gs.versionNumber = versionNumber

	/*
	 * this is called from a controller so we need to encourage
	 * fetching the following data until all calls are through a service
	 */

	Hibernate.initialize gs.analysisMethodConceptCode
	Hibernate.initialize gs.analyticCatConceptCode
	Hibernate.initialize gs.experimentTypeConceptCode
	Hibernate.initialize gs.foldChgMetricConceptCode
	Hibernate.initialize gs.normMethodConceptCode
	Hibernate.initialize gs.ownerConceptCode
	Hibernate.initialize gs.pValueCutoffConceptCode
	Hibernate.initialize gs.sourceConceptCode
	Hibernate.initialize gs.speciesConceptCode
	Hibernate.initialize gs.speciesMouseSrcConceptCode
	Hibernate.initialize gs.tissueTypeConceptCode
	Hibernate.initialize gs.createdByAuthUser
	Hibernate.initialize gs.modifiedByAuthUser
	Hibernate.initialize gs.treatmentCompound
	Hibernate.initialize gs.experimentTypeCellLine
	Hibernate.initialize gs.fileSchema
	Hibernate.initialize gs.parentGeneSignature
	Hibernate.initialize gs.techPlatform

    }

    /**
     * create a workbook showing the details of this gene signature
     */
    byte[] createWorkbook() {

	logger.info 'createWorkbook'

	List values = []

        // general section
	values << ['1) General Info']
	values << []
	values << ['Name:', name]
	values << ['Description:', description]
	values << ['Public?:', publicFlag ? 'Public' : 'Private']
	values << ['Author:', createdByAuthUser?.userRealName]
	values << ['Create Date:', dateCreated]
	values << ['Modified By:', modifiedByAuthUser?.userRealName]
	values << ['Modified Date:', modifiedByAuthUser != null ? lastUpdated : '']

        // meta section
	values << []
	values << ['2) Meta-Data']
	values << []

	values << ['Source of list:', sourceConceptCodeId == 1 ? sourceOther : sourceConceptCode?.codeName]

	values << ['Owner of data:', ownerConceptCode?.codeName]

	values << ['Stimulus>>']
	values << ['- Description:', stimulusDescription]
	values << ['- Dose, units, and time:', stimulusDosing]

	values << ['Treatment>>']
	values << ['- Description:', treatmentDescription]
	values << ['- Dose, units, and time:', treatmentDosing]
	String compound = ''
	if (treatmentCompound != null) {
	    compound = treatmentCompound?.codeName + ' [' + treatmentCompound?.genericName + ' / ' + treatmentCompound?.brandName + ']'
	}
	values << ['- Compound:', compound]
	values << ['- Protocol Number:', treatmentProtocolNumber]

	values << ['PMIDs (comma separated):', pmIds]

	values << ['Species:', speciesConceptCode?.codeName]
	if (speciesMouseSrcConceptCode != null) {
	    values << ['- Mouse Source:', speciesMouseSrcConceptCode?.codeName]
	}
	if (speciesMouseDetail != null) {
	    values << ["- knockout/transgenic' or 'other' mouse strain:", speciesMouseDetail]
	}

	String platform = ''
	if (techPlatform != null) {
	    platform = techPlatform?.vendor + ' - ' + techPlatform?.array + ' [' + techPlatform?.accession + ']'
	}
	values << ['Technology Platform:', platform]

	values << ['Tissue Type:', tissueTypeConceptCode?.codeName]

	values << ['Experiment Info>>']
	values << ['- Type:', experimentTypeConceptCode?.codeName]
	if (experimentTypeCellLine != null) {
	    values << ['- Established Cell Line:', experimentTypeCellLine.cellLineName]
	}
	if (experimentTypeConceptCode?.bioConceptCode == 'IN_VIVO_ANIMAL' || experimentTypeConceptCode?.bioConceptCode == 'IN_VIVO_HUMAN') {
	    values << ["- 'in vivo' model:", experimentTypeInVivoDescr]
	}
	values << ['- ATCC Designation:', experimentTypeATCCRef]

        // analysis section
	values << []
	values << ['3) Analysis Meta-Data']
	values << []
	values << ['Analysis Performed By:', analystName]

	values << ['Normalization Method:', normMethodConceptCodeId == 1 ? normMethodOther : normMethodConceptCode?.codeName]

	values << ['Analytic Category:', analyticCatConceptCodeId == 1 ? analyticCatOther : analyticCatConceptCode?.codeName]

	values << ['Analysis Method:', analysisMethodConceptCodeId == 1 ? analysisMethodOther : analysisMethodConceptCode?.codeName]

	values << ['Multiple Testing Correction?', multipleTestingCorrection ? 'Yes' : 'No']
	values << ['P-value Cutoff:', pValueCutoffConceptCode?.codeName]
	values << ['Fold-change metric:', foldChgMetricConceptCode?.codeName]
	values << ['Original upload file:', uploadFile]

	ExcelSheet metaSheet = new ExcelSheet('Gene Signature Info', [], values)

        values = []

	//This is a quick fix. These booleans will tell us whether a gene signature was entered with probes or genes.
	// In the future we should add some indicator field to the 'gene' list to say what it is made of.
	boolean hasGenes = false
	boolean hasProbes = false
	for (GeneSignatureItem gsi in geneSigItems) {
	    if (gsi.bioMarker) {
                hasGenes = true
		values << [gsi.bioMarker.name, gsi.foldChgMetric]
            }
	    else if (gsi.probesetId != null) {
                hasProbes = true
		def annot = de.DeMrnaAnnotation.find('from DeMrnaAnnotation as a where a.probesetId=? ', [gsi.probesetId])
		if (annot) {
		    values << [annot.geneSymbol, annot.probeId, gsi.foldChgMetric]
                }
            }
        }

	List<String> headers = []
	if (hasGenes) {
	    headers = ['Gene Symbol', 'Fold Change Metric']
	}
	if (hasProbes) {
	    headers = ['Gene Symbol', 'Probe ID', 'Fold Change Metric']
	}

	ExcelGenerator.generateExcel([metaSheet, new ExcelSheet('Gene Signature Items', headers, values)])
    }
}
