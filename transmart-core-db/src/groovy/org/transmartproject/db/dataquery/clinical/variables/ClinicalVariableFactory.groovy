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

package org.transmartproject.db.dataquery.clinical.variables

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Multimap
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component
import org.transmart.plugin.shared.Utils
import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.db.dataquery.highdim.parameterproducers.BindingUtils
import org.transmartproject.db.i2b2data.ConceptDimension
import org.transmartproject.db.ontology.AbstractAcrossTrialsOntologyTerm
import org.transmartproject.db.ontology.I2b2

import static org.transmartproject.core.ontology.OntologyTerm.VisualAttributes.FOLDER
import static org.transmartproject.core.ontology.OntologyTerm.VisualAttributes.LEAF

// not scanned; explicit bean definition
@Component
class ClinicalVariableFactory {

    boolean disableAcrossTrials

    @Lazy
    private ClinicalVariableFactoryAcrossTrialsHelper helper = {
        disableAcrossTrials ?
            null :
            new ClinicalVariableFactoryAcrossTrialsHelper()
    }()

    private Map<String, Closure<ClinicalVariable>> knownTypes =
        ImmutableMap.of(
        ClinicalVariable.TERMINAL_CONCEPT_VARIABLE,
        this.&createTerminalConceptVariable,
        ClinicalVariable.CATEGORICAL_VARIABLE,
        this.&createCategoricalVariable,
        ClinicalVariable.NORMALIZED_LEAFS_VARIABLE,
        this.&createNormalizedLeafsVariable
    )

    ClinicalVariable createClinicalVariable(Map<String, Object> params,
	                                    String type) throws InvalidArgumentsException {
        Closure<ClinicalVariable> closure = knownTypes[type]
        if (!closure) {
	    throw new InvalidArgumentsException(
		"Invalid clinical variable type '$type', supported types are " +
		    knownTypes.keySet())
        }

        if (params.size() != 1) {
	    throw new InvalidArgumentsException("Expected exactly one parameter, got ${params.keySet()}")
        }

        String conceptCode
        String conceptPath
        if (params.concept_code) {
            conceptCode = BindingUtils.getParam(params, 'concept_code')
        }
        else if (params.concept_path) {
	    conceptPath = BindingUtils.getParam(params, 'concept_path')
        }
        else {
	    throw new InvalidArgumentsException("Expected the given parameter " +
						"to be one of 'concept_code', 'concept_path', got " +
						"'${params.keySet().iterator().next()}'")
        }

	closure(conceptCode, conceptPath)
    }

    private TerminalClinicalVariable createTerminalConceptVariable(String conceptCode, String conceptPath) {
        if (helper?.isAcrossTrialsPath(conceptPath)) {
	    helper.createTerminalConceptVariable conceptPath
	}
	else {
	    new TerminalConceptVariable(conceptCode: conceptCode, conceptPath: conceptPath)
        }
    }

    private CategoricalVariable createCategoricalVariable(String conceptCode, String conceptPath) {
        if (helper?.isAcrossTrialsPath(conceptPath)) {
            return helper.createCategoricalVariable(conceptPath)
        }

	List<ConceptDimension> descendantDimensions = descendantDimensions(conceptCode, conceptPath)

        // parent is the concept represented by the arguments
	ConceptDimension parent = descendantDimensions[0]
        if (descendantDimensions.size() == 1) {
            throw new InvalidArgumentsException('Concept with path ' +
						"$conceptPath was supposed to be the container for a " +
						"categorical variable, but instead no children were found")
        }

	List<ConceptDimension> children = descendantDimensions[1..-1]
	int parentNumSlashes = parent.conceptPath.count('\\')

	List<TerminalClinicalVariable> innerVariables = children.collect { ConceptDimension cd ->
	    int thisCount = cd.conceptPath.count('\\')
            if (thisCount != parentNumSlashes + 1) {
		throw new InvalidArgumentsException("Concept with path " +
						    "'$conceptPath' does not seem to be a categorical " +
						    "variable because it has grandchildren (found " +
						    "concept path '${cd.conceptPath}'")
            }

	    createTerminalConceptVariable cd.conceptCode, null
        }

	createCategoricalVariableFinal parent.conceptPath, innerVariables as List<TerminalConceptVariable>
    }

    private CategoricalVariable createCategoricalVariableFinal(String containerConceptPath,
	                                                       List<TerminalConceptVariable> innerVariables) {
	new CategoricalVariable(conceptPath: containerConceptPath, innerClinicalVariables: innerVariables)
    }

    private NormalizedLeafsVariable createNormalizedLeafsVariable(String conceptCode, String conceptPath) {
        if (helper?.isAcrossTrialsPath(conceptPath)) {
            return helper.createNormalizedLeafsVariable(conceptPath)
        }

	String resolvedConceptPath = resolveConceptPath(conceptCode, conceptPath)

        List<? extends OntologyTerm> terms = I2b2.withCriteria {
	    'like' 'fullName', Utils.asLikeLiteral(resolvedConceptPath) + '%'
            order 'fullName', 'asc'
        }

        if (!terms) {
	    throw new UnexpectedResultException("Could not find any path in " +
						"i2b2 starting with $resolvedConceptPath")
        }
        if (terms[0].fullName != resolvedConceptPath) {
	    throw new UnexpectedResultException("Expected first result to " +
						"have concept path '$resolvedConceptPath', got " +
						"'${terms[0].fullName}'")
        }

        Map<String, OntologyTerm> indexedTerms =
	    terms.collectEntries { [it.fullName, it] }

	List<TerminalConceptVariable> composingVariables = []

        Multimap<String, String> potentialCategorical = HashMultimap.create()
        // set of containers that cannot refer to categorical variables because
        // they have numerical children
	Set<String> blackListedCategorical = []

	for (OntologyTerm ot in terms.findAll { LEAF in it.visualAttributes }) {
	    ConceptFullName conceptNameObj = new ConceptFullName(ot.fullName)
            String parentName = conceptNameObj.parent?.toString()

            if (parentName && indexedTerms[parentName] &&
                !(FOLDER in indexedTerms[parentName].visualAttributes)) {
		throw new IllegalStateException('Found parent concept that is not a folder')
            }

	    if (ot.metadata?.okToUseValues) {
                // numeric leaf
		composingVariables << new TerminalConceptVariable(conceptPath: ot.fullName)
                if (parentName) {
                    blackListedCategorical << parentName
                }
		continue
            }

            // non-numeric leaf now

            if (conceptNameObj.length == 1) {
                // no parent, so not a candidate for categorical variable
		composingVariables << new TerminalConceptVariable(conceptPath: ot.fullName)
		continue
            }

            if (!indexedTerms.containsKey(parentName)) {
                // parent has NOT been selected
		composingVariables << new TerminalConceptVariable(conceptPath: ot.fullName)
		continue
            }

	    potentialCategorical.put parentName, ot.fullName
        }

	potentialCategorical.asMap().collect { String parentName, Collection<String> childrenNames ->
            List<TerminalConceptVariable> childrenVariables =
                childrenNames.collect { String childrenName ->
                new TerminalConceptVariable(conceptPath: childrenName)
            }

            if (parentName in blackListedCategorical) {
                // blacklisted
                composingVariables.addAll childrenVariables
            }
            else {
		composingVariables << createCategoricalVariableFinal(parentName, childrenVariables)
            }
        }

        composingVariables = composingVariables.sort { it.label }

	new NormalizedLeafsVariable(
	    conceptPath: resolvedConceptPath,
            innerClinicalVariables: composingVariables)
    }

    private String resolveConceptPath(String conceptCode, String conceptPath) {
	String resolvedConceptPath = conceptPath
        if (!resolvedConceptPath) {
            assert conceptCode != null
	    resolvedConceptPath = ConceptDimension.findByConceptCode(conceptCode)?.conceptPath

            if (!resolvedConceptPath) {
		throw new InvalidArgumentsException('Could not find path of concept with code ' + conceptCode)
            }
        }
        else {
            if (!ConceptDimension.findByConceptPath(conceptPath)) {
		throw new InvalidArgumentsException('Could not find concept with path ' + conceptPath)
            }
        }

        resolvedConceptPath
    }

    List<ConceptDimension> descendantDimensions(String conceptCode, String conceptPath) {

	String resolvedConceptPath = resolveConceptPath(conceptCode, conceptPath)

	List<ConceptDimension> result = ConceptDimension.withCriteria {
	    like 'conceptPath', Utils.asLikeLiteral(resolvedConceptPath) + '%'

            order 'conceptPath', 'asc'
        }

        if (result[0]?.conceptPath != resolvedConceptPath) {
            throw new UnexpectedResultException('Expected first result to have ' +
						"concept path '$resolvedConceptPath', got " +
						"${result[0]?.conceptPath} instead")
        }

        result
    }
}

@CompileStatic
class ClinicalVariableFactoryAcrossTrialsHelper {

    boolean isAcrossTrialsPath(String conceptPath) {
        if (conceptPath == null) {
            return false
        }

	new ConceptFullName(conceptPath)[0] == AbstractAcrossTrialsOntologyTerm.ACROSS_TRIALS_TOP_TERM_NAME
    }

    AcrossTrialsTerminalVariable createTerminalConceptVariable(String conceptPath) {
        new AcrossTrialsTerminalVariable(conceptPath: conceptPath)
    }

    ClinicalVariable createCategoricalVariable(String conceptPath) {
        throw new UnsupportedOperationException('Not supported yet')
    }

    ClinicalVariable createNormalizedLeafsVariable(String conceptPath) {
        throw new UnsupportedOperationException('Not supported yet')
    }
}
