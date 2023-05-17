package org.transmartproject.db.dataquery.highdim.parameterproducers

import groovy.util.logging.Slf4j
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.dataconstraints.SubqueryInDataConstraint
import org.transmartproject.db.i2b2data.ConceptDimension

/**
 * @author Denny Verbeeck (dverbeec@its.jnj.com)
 */
@Slf4j('logger')
class SimpleAnnotationConstraintFactory extends AbstractMethodBasedParameterFactory {

    String field
    Class annotationClass

    @ProducerFor(DataConstraint.ANNOTATION_CONSTRAINT)
    DataConstraint createAnnotationConstraint(Map<String, Object> params) {

        if (!params.keySet().containsAll(['property','term']) ||
	    !(params.keySet().contains('concept_code') || params.keySet().contains('concept_key'))) {
	    throw new InvalidArgumentsException(
		"SimpleAnnotationDataConstraint needs the following parameters: " +
		    "['property','term','concept_key' OR 'concept_code'], but got " + params)
	}

//	logger.info 'createAnnotationConstraint params {}', params

        DetachedCriteria dc = DetachedCriteria.forClass(annotationClass)
	dc.setProjection Projections.distinct(Projections.property('id'))
	dc.add Restrictions.eq(params.property, params.term)
        if (params.containsKey('concept_code')) {
//	    logger.info 'createAnnotationConstraint concept_code platform {}',
//		DeSubjectSampleMapping.findByConceptCode(params.concept_code).platform
	    dc.add Restrictions.eq(
		'platform',
		DeSubjectSampleMapping.findByConceptCode(params.concept_code).platform)
        }
        else if (params.containsKey('concept_key')) {
	    String conceptPath = keyToPath(params.concept_key)
//	    logger.info 'createAnnotationConstraint concept_key code {} platform {}',
//		ConceptDimension.findByConceptPath(conceptPath).conceptCode,
//		DeSubjectSampleMapping.findByConceptCode(ConceptDimension.findByConceptPath(conceptPath).conceptCode).platform
	    dc.add Restrictions.eq(
		'platform',
		DeSubjectSampleMapping.findByConceptCode(
		    ConceptDimension.findByConceptPath(conceptPath).conceptCode).platform)
        }

	new SubqueryInDataConstraint(field: field + '.id', detachedCriteria: dc)
    }

    private String keyToPath(String conceptKey) {
	String fullname = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())
	if (fullname.endsWith('\\')) {
	    fullname
	}
	else {
	    fullname + '\\'
        }
    }
}
