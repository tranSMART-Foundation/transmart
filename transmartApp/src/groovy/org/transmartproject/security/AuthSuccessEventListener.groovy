package org.transmartproject.security

import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent

@CompileStatic
class AuthSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    BruteForceLoginLockService bruteForceLoginLockService

    void onApplicationEvent(AuthenticationSuccessEvent event) {
	bruteForceLoginLockService.loginSuccess event.authentication.name
    }
}
