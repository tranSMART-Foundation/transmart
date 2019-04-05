package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class ExperimentAnalysisFilter {

    String dataSource
    Long bioDiseaseId
    String species
    String expDesign
    String expType
    Long bioCompoundId
    String tissueType
    String cellLine
    String expDescrKeyword
    Double foldChange
    Double pvalue

    boolean isUsed() {
	species ||
	    expDesign ||
	    expType ||
	    dataSource ||
	    bioCompoundId != null ||
	    bioDiseaseId != null ||
	    foldChange != null ||
	    pvalue != null ||
	    cellLine
    }

    boolean filterFoldChange() {
	foldChange > 0
    }

    boolean filterPValue() {
	pvalue > 0
    }

    boolean filterDisease() {
	bioDiseaseId > 0
    }

    boolean filterCompound() {
	bioCompoundId > 0
    }

    boolean filterSpecies() {
	species
    }

    boolean filterExpDesign() {
	expDesign
    }

    boolean filterExpType() {
	expType
    }

    boolean filterDataSource() {
	dataSource
    }
}
