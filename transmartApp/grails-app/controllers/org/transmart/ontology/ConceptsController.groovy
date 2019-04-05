package org.transmart.ontology

import grails.converters.JSON
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.ontology.ConceptsResource

class ConceptsController {

    ConceptsResource conceptsResourceService

    def getCategories() {
        render conceptsResourceService.allCategories as JSON
    }

    def getChildren(String concept_key) {
	def parent = conceptsResourceService.getByKey(concept_key)
	render(parent.children as JSON)
    }

    def getResource(String concept_key) {
	render(conceptsResourceService.getByKey(concept_key) as JSON)
    }

    def getModifierChildren(String modifier_key, String applied_path, String qualified_term_key) {
	if (!modifier_key || !applied_path || !qualified_term_key) {
            throw new InvalidArgumentsException('Missing arguments')
        }

	// TODO: method needs to be added to the interface
	/*
	 if (conceptsResourceService.respondsTo('getModifier')) {
	 BoundModifier modifier = conceptsResourceService.getModifier(modifierKey, appliedPath, qualifiedTermKey)
	 render(modifier.children as JSON)
     }
	 else {
         throw new OperationNotSupportedException()
     }
	 */
    }
}
