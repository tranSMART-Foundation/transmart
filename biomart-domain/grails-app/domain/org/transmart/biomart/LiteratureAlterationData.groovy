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

class LiteratureAlterationData extends Literature {
    String alterationType
    String clinAsmMarkerType
    String clinAsmUnit
    String clinAsmValue
    String clinAtopy
    String clinBaselinePercent
    String clinBaselineValue
    String clinBaselineVariable
    String clinCellularCount
    String clinCellularSource
    String clinCellularType
    String clinPriorMedDose
    String clinPriorMedName
    String clinPriorMedPercent
    String clinSmoker
    String clinSubmucosaMarkerType
    String clinSubmucosaUnit
    String clinSubmucosaValue
    String control
    String controlExpNumber
    String controlExpPercent
    String controlExpSd
    String controlExpUnit
    String controlExpValue
    String description
    String effect
    String epigeneticRegion
    String epigeneticType
    String etlId
    String glcControlPercent
    String glcMolecularChange
    String glcNumber
    String glcPercent
    String glcType
    LiteratureModelData inVitroModel
    LiteratureModelData inVivoModel
    String lohLoci
    String lossExpNumber
    String lossExpPercent
    String lossExpSd
    String lossExpUnit
    String lossExpValue
    String mutationChange
    String mutationSites
    String mutationType
    String overExpNumber
    String overExpPercent
    String overExpSd
    String overExpUnit
    String overExpValue
    String patientsNumber
    String patientsPercent
    String popBodySubstance
    String popCellType
    String popDescription
    String popExclusionCriteria
    String popExperimentalModel
    String popInclusionCriteria
    String popLocalization
    String popNumber
    String popPhase
    String popStatus
    String popTissue
    String popType
    String popValue
    String ptmChange
    String ptmRegion
    String ptmType
    String techniques
    String totalExpNumber
    String totalExpPercent
    String totalExpSd
    String totalExpUnit
    String totalExpValue

    static hasMany = [assocMoleculeDetails: LiteratureAssocMoleculeDetailsData]

    static mapping = {
	table 'BIOMART.BIO_LIT_ALT_DATA'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_LIT_ALT_DATA_ID'
        version false

	assocMoleculeDetails joinTable: [name: 'BIOMART.BIO_LIT_AMD_DATA', key: 'BIO_LIT_ALT_DATA_ID', column: 'BIO_LIT_AMD_DATA_ID']
    }
}
