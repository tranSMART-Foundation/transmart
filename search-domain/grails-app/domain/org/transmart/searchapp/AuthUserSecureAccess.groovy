package org.transmart.searchapp

class AuthUserSecureAccess {
    SecureAccessLevel accessLevel
    AuthUser authUser
    String objectAccessName
    String principalAccessName
    SecureObject secureObject

    static transients = ['objectAccessName', 'principalAccessName']

    static mapping = {
	table 'SEARCHAPP.SEARCH_AUTH_USER_SEC_ACCESS_V'
        id column: 'SEARCH_AUTH_USER_SEC_ACCESS_ID'
	version false

	accessLevel column: 'SEARCH_SEC_ACCESS_LEVEL_ID'
        authUser column: 'SEARCH_AUTH_USER_ID'
        secureObject column: 'SEARCH_SECURE_OBJECT_ID'
    }

    static constraints = {
	authUser nullable: true
    }

    String getObjectAccessName() {
	secureObject.displayName + ' (' + accessLevel.accessLevelName + ')'
    }

    String getPrincipalAccessName() {
	authUser.name + ' (' + accessLevel.accessLevelName + ')'
    }
}
