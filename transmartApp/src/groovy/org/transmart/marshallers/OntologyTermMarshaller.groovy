package org.transmart.marshallers

import org.transmartproject.core.ontology.OntologyTerm

class OntologyTermMarshaller {

    static targetType = OntologyTerm

    Map convert(OntologyTerm term) {
	[key               : term.key,
         level             : term.level,
         fullName          : term.fullName,
         name              : term.name,
         tooltip           : term.tooltip,
         visualAttributes  : term.visualAttributes,
         metadata          : term.metadata,

         /* TODO I'm not comfortable having this here; the web frontend
             * should only be worried about adding and removing terms
             * from sets, not care about how the panels are built and how
             * the terms map to patient sets.
             * Unfortunately, that is not the way the frontend is setup
             * right now, as right now it needs this data */
         dimensionCode     : term.dimensionCode,
	 dimensionTableName: term.dimensionTableName]
    }
}
