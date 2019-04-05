package com.recomdata.security

import groovy.transform.CompileStatic
import org.transmart.searchapp.AuthUser

@CompileStatic
class UsernameUtils {
    static final String FEDERATED_ID_PLACEHOLDER = '<FEDERATED_ID>'
    static final String ID_PLACEHOLDER = '<ID>'

    static String randomName() {
	UUID.randomUUID()
    }

    static String patternHasId(String pattern) {
	pattern.contains ID_PLACEHOLDER
    }

    static String evaluatePattern(AuthUser user, String pattern) {
	pattern.replace(FEDERATED_ID_PLACEHOLDER, user.federatedId).replace(ID_PLACEHOLDER, user.id.toString())
    }
}
