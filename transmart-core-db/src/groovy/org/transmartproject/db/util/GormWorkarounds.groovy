package org.transmartproject.db.util

import grails.orm.HibernateCriteriaBuilder
import org.codehaus.groovy.grails.orm.hibernate.query.HibernateQuery
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Subqueries
import org.hibernate.engine.SessionImplementor
import org.hibernate.impl.CriteriaImpl

/**
 * Helps overcome limitations in current GORM implementation.
 */
class GormWorkarounds {

    static HibernateCriteriaBuilder createCriteriaBuilder(Class targetClass, String alias,
							  SessionImplementor session, boolean readOnly = true,
							  boolean cacheable = false, int fetchSize = 10000) {

        HibernateCriteriaBuilder builder = new HibernateCriteriaBuilder(targetClass, session.factory)

        // we have to write a private here
        if (session) {
            //force usage of a specific session (probably stateless)
            builder.criteria = new CriteriaImpl(targetClass.canonicalName, alias, session)
            builder.criteriaMetaClass = GroovySystem.metaClassRegistry.getMetaClass(builder.criteria.getClass())
        }
        else {
            builder.createCriteriaInstance()
        }

        // builder.instance.is(builder.criteria)
        builder.instance.readOnly = readOnly
        builder.instance.cacheable = cacheable
        builder.instance.fetchSize = fetchSize

        builder
    }

    static Criterion getHibernateInCriterion(String property, QueryableCriteria<?> queryableCriteria) {
        Subqueries.propertyIn property, getHibernateDetachedCriteria(queryableCriteria)
    }

    static DetachedCriteria getHibernateDetachedCriteria(QueryableCriteria<?> queryableCriteria) {

        String alias = queryableCriteria.getAlias()
        PersistentEntity persistentEntity = queryableCriteria.persistentEntity
        Class targetClass = persistentEntity.javaClass
        DetachedCriteria detachedCriteria
        if(alias) {
            detachedCriteria = DetachedCriteria.forClass(targetClass, alias)
        }
        else {
            detachedCriteria = DetachedCriteria.forClass(targetClass)
        }

        HibernateQuery hq = new HibernateQuery(detachedCriteria)
        //To escape NPE we have to set this private field
        //This fix is the main reason to have this method @{see HibernateCriteriaBuilder.getHibernateDetachedCriteria}
        hq.entity = persistentEntity
        HibernateCriteriaBuilder.populateHibernateDetachedCriteria hq, detachedCriteria, queryableCriteria

        detachedCriteria
    }
}
