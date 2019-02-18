package org.transmartproject.db.dataquery.highdim.dataconstraints

import grails.orm.HibernateCriteriaBuilder
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Subqueries

/**
 * @author Denny Verbeeck (dverbeec@its.jnj.com)
 */
class SubqueryInDataConstraint implements CriteriaDataConstraint {

    String field
    DetachedCriteria detachedCriteria

    void doWithCriteriaBuilder(HibernateCriteriaBuilder criteria) {
        criteria.add Subqueries.propertyIn(field, detachedCriteria)
    }
}
