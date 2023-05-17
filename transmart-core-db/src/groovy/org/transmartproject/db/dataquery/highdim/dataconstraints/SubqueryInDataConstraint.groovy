package org.transmartproject.db.dataquery.highdim.dataconstraints

import grails.orm.HibernateCriteriaBuilder
import groovy.util.logging.Slf4j
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Subqueries

/**
 * @author Denny Verbeeck (dverbeec@its.jnj.com)
 */
@Slf4j('logger')
class SubqueryInDataConstraint implements CriteriaDataConstraint {

    String field
    DetachedCriteria detachedCriteria

    void doWithCriteriaBuilder(HibernateCriteriaBuilder criteria) {
        criteria.add Subqueries.propertyIn(field, detachedCriteria)
//	logger.info 'doWithCriteriaBuilder field {} detachedCriteria {}', field, detachedCriteria
    }
}
