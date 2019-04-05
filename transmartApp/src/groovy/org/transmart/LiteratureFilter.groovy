package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class LiteratureFilter {

    String dataType

    // Reference
    Long bioDiseaseId
    Set diseaseSite = []
    Set componentList = []
    List pairCompList = []
    List pairGeneList = []

    // Alteration
    String mutationType
    String mutationSite
    String epigeneticType
    String epigeneticRegion
    Map<String, Boolean> alterationTypes = [
	'Epigenetic Event'    : true,
	Expression            : true,
	'Gene Amplification'  : true,
	'Genomic Level Change': true,
	LOH                   : true,
	Mutation              : true,
	PTM                   : true]
    String moleculeType
    String regulation
    String ptmType
    String ptmRegion

    // Interaction
    String source
    String target
    String experimentalModel
    String mechanism

    // Inhibitor
    String trialType
    String trialPhase
    String inhibitorName
    String trialExperimentalModel

    boolean hasDisease() {
	bioDiseaseId > 0
    }

    boolean hasDiseaseSite() {
	diseaseSite && diseaseSite.iterator().next()
    }

    boolean hasComponent() {
	componentList && componentList.iterator().next()
    }

    boolean hasMutationType() {
	mutationType
    }

    boolean hasMutationSite() {
	mutationSite
    }

    boolean hasEpigeneticType() {
	epigeneticType
    }

    boolean hasEpigeneticRegion() {
	epigeneticRegion
    }

    boolean hasAlterationType() {
        int count = 0
	for (boolean value in alterationTypes.values()) {
	    if (value) {
                count++
            }
        }
        // NOTE: Only want to filter if any of the types are not selected.
	count < alterationTypes.size()
    }

    boolean hasMoleculeType() {
	moleculeType
    }

    boolean hasRegulation() {
	regulation
    }

    boolean hasPtmType() {
	ptmType
    }

    boolean hasPtmRegion() {
	ptmRegion
    }

    boolean hasSource() {
	source
    }

    boolean hasTarget() {
	target
    }

    boolean hasExperimentalModel() {
	experimentalModel
    }

    boolean hasMechanism() {
	mechanism
    }

    boolean hasTrialType() {
	trialType
    }

    boolean hasTrialPhase() {
	trialPhase
    }

    boolean hasInhibitorName() {
	inhibitorName
    }

    boolean hasTrialExperimentalModel() {
	trialExperimentalModel
    }

    /**
     * The alteration types that are selected in the filter
     *
     * @return alteration types that the user has selected
     */
    Set<String> getSelectedAlterationTypes() {
	Set<String> types = []
	for (String key in alterationTypes.keySet()) {
	    if (alterationTypes[key]) {
		types << key.toUpperCase().replace('_', ' ')
            }
        }
	types
    }

    void parseDiseaseSite(list) {
        if (list != null) {
            if (list instanceof String) {
		diseaseSite << list
            }
            else {
		diseaseSite.addAll list
            }
        }
    }

    void parseComponentList(list) {
        pairCompList.clear()
        pairGeneList.clear()
        componentList.clear()
	if (list) {
	    if (list instanceof String && list.trim()) {
		componentList << list
		String[] compArray = list.split(',')
		pairCompList << compArray[0].replace('[', '').trim()
		pairGeneList << compArray[1].replace(']', '').trim()
            }
            else {
		for (String item in list) {
		    componentList << item
		    String[] compArray = item.split(',')
		    pairCompList << compArray[0].replace('[', '').trim()
		    pairGeneList << compArray[1].replace(']', '').trim()
                }
            }
        }
    }
}
