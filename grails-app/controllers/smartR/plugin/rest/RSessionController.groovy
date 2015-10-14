package smartR.plugin.rest

import heim.session.SessionService
import org.transmartproject.core.exceptions.InvalidArgumentsException

class RSessionController {

    static scope = 'prototype'

    static allowedMethods = [
            create: 'POST',
            delete: 'POST',
    ]

    SessionService sessionService

    /**
     * Creates a new R session
     */
    def create() {
        def json = request.JSON
        if (!json.workflow) {
            throw new InvalidArgumentsException('Workflow not given')
        }

        UUID sessionId = sessionService.createSession(
                null /* user not used now */,
                json.workflow)

        response.status = 201
        render(contentType: 'text/json') {
            [sessionId: sessionId.toString()]
        }
    }

    /**
     * Deletes an R session
     */
    def delete() {
        def json = request.JSON
        def sessionId
        if (!json.sessionId || !(json.sessionId instanceof String)) {
            throw new InvalidArgumentsException(
                    "No session id provided or not string: ${json.sessionId}")
        }
        try {
            sessionId = UUID.fromString(json.sessionId)
        } catch (IllegalArgumentException iae) {
            throw new InvalidArgumentsException(
                    "Invalid session id (not a UUID): ${json.sessionId}")
        }

        sessionService.destroySession(sessionId)

        response.status = 202 /* session shutdown runs asynchronously */
        render ''
    }

}
