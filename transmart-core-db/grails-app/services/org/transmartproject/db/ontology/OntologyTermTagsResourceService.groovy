package org.transmartproject.db.ontology

import org.transmart.plugin.shared.Utils
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.core.ontology.OntologyTermTagsResource

class OntologyTermTagsResourceService implements OntologyTermTagsResource {

    static transactional = false

    Map<OntologyTerm, List<OntologyTermTag>> getTags(Set<OntologyTerm> ontologyTerms, boolean includeDescendantsTags) {
        if (!ontologyTerms) {
            return [:]
        }

        List<I2b2Tag> orderedTags = I2b2Tag.createCriteria().list {
            or {
                for (OntologyTerm term in ontologyTerms) {
                    if (includeDescendantsTags) {
                        like 'ontologyTermFullName', Utils.asLikeLiteral(term.fullName) + '%'
                    }
		    else {
                        eq 'ontologyTermFullName', term.fullName
                    }
                }
            }
            order 'ontologyTermFullName'
            order 'position'
        }

        List<I2b2> terms = I2b2.findAllByFullNameInList((orderedTags*.ontologyTermFullName).unique())
        Map<String, I2b2> termsMap = terms.collectEntries { I2b2 i2b2 -> [i2b2.fullName, i2b2] }

        Map<OntologyTerm, List<OntologyTermTag>> result  = orderedTags.groupBy { I2b2Tag i2b2Tag -> termsMap[i2b2Tag.ontologyTermFullName] }
        //remove tags that point to non-existing concept
        result.remove null
        result
    }
}
