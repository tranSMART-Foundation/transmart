package org.transmartproject.security

import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent

@CompileStatic
class BadCredentialsEventListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    BruteForceLoginLockService bruteForceLoginLockService

    void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        //We have to check for AuthenticationFailureServiceExceptionEvent because of bug in current version of OAuth provider
        //see https://github.com/spring-projects/spring-security-oauth/pull/383
	if ([AuthenticationFailureBadCredentialsEvent, AuthenticationFailureServiceExceptionEvent].any { Class c ->
	    c.isAssignableFrom event.class
        }) {
	    bruteForceLoginLockService.failLogin event.authentication.name
        }
    }
}
