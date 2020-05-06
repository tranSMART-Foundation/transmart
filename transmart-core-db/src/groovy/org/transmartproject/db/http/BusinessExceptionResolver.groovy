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

package org.transmartproject.db.http

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.mapping.DefaultUrlMappingInfo
import org.codehaus.groovy.grails.web.mapping.UrlMappingData
import org.codehaus.groovy.grails.web.mapping.UrlMappingInfo
import org.codehaus.groovy.grails.web.mapping.UrlMappingUtils
import org.springframework.core.Ordered
import org.springframework.web.context.ServletContextAware
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.exceptions.EmptySetException
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.exceptions.UnsupportedByDataTypeException

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

@Slf4j('logger')
class BusinessExceptionResolver implements ServletContextAware, HandlerExceptionResolver, Ordered {

    ServletContext servletContext
    int order = HIGHEST_PRECEDENCE

    String controllerName = 'businessException'
    String actionName = 'index'
    boolean handleAll = false

    private final ModelAndView EMPTY_MV = new ModelAndView()

    public final static String REQUEST_ATTRIBUTE_STATUS = this.name + '.STATUS'
    public final static String REQUEST_ATTRIBUTE_EXCEPTION = this.name + '.EXCEPTION'

    private static Map<Exception, Integer> statusCodeMappings = [
        (NoSuchResourceException):        SC_NOT_FOUND,
        (InvalidRequestException):        SC_BAD_REQUEST,
        (InvalidArgumentsException):      SC_BAD_REQUEST,
        (EmptySetException):              SC_NOT_FOUND,
        (UnsupportedByDataTypeException): SC_BAD_REQUEST,
        (UnexpectedResultException):      SC_INTERNAL_SERVER_ERROR,
        (AccessDeniedException):          SC_FORBIDDEN].asImmutable()

    private Throwable resolveCause(Throwable t) {
        if (t.cause && t.cause != t) {
            t.cause
        }
        else if (t.metaClass.hasProperty('target')) {
            t.target
        }
    }

    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, handler, Exception e) {

        logger.info 'Asked BusinessExceptionResolver to resolve exception from handler {}', handler, e

	Throwable t = e // the resolveCause result might be an Error

	Map exceptionPlusStatus = null
        while (!exceptionPlusStatus && t) {
            exceptionPlusStatus = statusCodeMappings.findResult {
                if (it.key.isAssignableFrom(t.getClass())) {
                    return [(REQUEST_ATTRIBUTE_EXCEPTION): t, (REQUEST_ATTRIBUTE_STATUS): it.value]
                }
            }

            t = resolveCause(t)
        }

        if (!exceptionPlusStatus && handleAll) {
            exceptionPlusStatus = [(REQUEST_ATTRIBUTE_EXCEPTION): t, (REQUEST_ATTRIBUTE_STATUS): SC_INTERNAL_SERVER_ERROR]
        }

        // we know this exception
        if (exceptionPlusStatus) {
            logger.debug 'BusinessExceptionResolver will handle exception {}', t.message
            Map model = exceptionPlusStatus

            UrlMappingInfo info = new DefaultUrlMappingInfo(
                    (Object) null, // redirectInfo
                    controllerName,
                    actionName,
                    (Object) null, // namespace
                    (Object) null, // pluginName
                    (Object) null, // viewName
                    (String) null, // method
                    (String) null, // version
                    [:],           // params
                    (UrlMappingData) null,
                    servletContext)

            UrlMappingUtils.forwardRequestForUrlMappingInfo request, response, info, model, true

            return EMPTY_MV
        }
    }
}
