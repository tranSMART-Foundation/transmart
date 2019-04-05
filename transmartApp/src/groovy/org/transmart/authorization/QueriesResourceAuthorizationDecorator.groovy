package org.transmart.authorization

import com.google.common.collect.Sets
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.users.User

import javax.annotation.Resource

import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.BUILD_COHORT
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.READ

@CompileStatic
@Slf4j('logger')
class QueriesResourceAuthorizationDecorator implements QueriesResource, AuthorizationDecorator<QueriesResource> {

    @Resource(name = CurrentUserBeanProxyFactory.BEAN_BAME)
    User user

    @Autowired
    QueriesResource delegate

    QueryResult runQuery(QueryDefinition definition) throws InvalidRequestException {
        if (!user.canPerform(BUILD_COHORT, definition)) {
	    throw new AccessDeniedException('Denied ' + user.username +
					    ' access for building cohort based on ' + definition)
        }

        delegate.runQuery definition
    }

    QueryResult runQuery(QueryDefinition definition, String username) throws InvalidRequestException {
        if (!user.canPerform(BUILD_COHORT, definition)) {
	    throw new AccessDeniedException('Denied ' + user.username +
					    ' access for building cohort based on ' + definition)
        }
        if (username != user.username) {
	    throw new AccessDeniedException('Denied ' + user.username +
					    ' access to building a cohort in name of ' + username)
        }

        delegate.runQuery definition, username
    }

    QueryResult getQueryResultFromId(Long id) throws NoSuchResourceException {
	QueryResult res = delegate.getQueryResultFromId(id)

        if (!user.canPerform(READ, res)) {
	    throw new AccessDeniedException('Denied ' + user.username +
					    ' access to query result with id ' + id)
        }

        res
    }

    @CompileStatic
    static class LegacyQueryResultAccessCheckRequestCache {
        // should be request-scope bean
        Set<Long> resultInstanceIdsAllowed = Sets.newHashSet()
    }

    /**
     * Access checks to the query results should be made by injecting the bean
     * queriesResourceAuthorizationDecorator instance of
     * {@link org.transmartproject.db.querytool.QueriesResourceService}.
     *
     * However, legacy code has embedded queries to qt_patient_collection and
     * other queries involving result instance ids that don't go through
     * core-api. This is a transitional method that allows checks to be
     * inserted in that legacy code.
     *
     * @deprecated for legacy code only. Inject the bean
     * queriesResourceAuthorizationDecorator instead and use it to retrieve the
     * {@link QueryResult}.
     * @throws AccessDeniedException if access is denied
     * @param arguments zero or more result instance ids to check
     */
    @CompileDynamic
    @Deprecated
    static void checkQueryResultAccess(Object... arguments) {
	List<Long> ids = arguments.findAll().collect { it as long }

        QueriesResourceAuthorizationDecorator thiz =
            Holders.applicationContext.queriesResourceAuthorizationDecorator

        LegacyQueryResultAccessCheckRequestCache cache

        /* if we're in the context of a request (as opposed to say, a Quartz
         * job), we can take advantage of a cache (we're guaranteed it's the
         * same user across the whole request */
        if (RequestContextHolder.requestAttributes) {
	    cache = Holders.applicationContext.legacyQueryResultAccessCheckRequestCache
        }

	for (long id in ids) {
	    if (cache?.resultInstanceIdsAllowed?.contains(id)) {
		logger.debug 'Request cache for legacy access to result instance ids includes entry for rid {}', id
                return
            }

	    thiz.getQueryResultFromId id //may throw
	    logger.debug 'Request access to rid {} from legacy checkQueryResultAccess', id

            if (cache) {
		cache.resultInstanceIdsAllowed << id
            }
        }
    }

    QueryDefinition getQueryDefinitionForResult(QueryResult result) throws NoSuchResourceException {
        /* the gatekeeping is done when fetching the query result only.
         * Odd that this method is not in QueryResult anyway */
	delegate.getQueryDefinitionForResult result
    }
}
