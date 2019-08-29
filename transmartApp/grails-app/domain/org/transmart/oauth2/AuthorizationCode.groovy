package org.transmart.oauth2

class AuthorizationCode {

    byte[] authentication
    String code

    static constraints = {
	code blank: false, unique: true
	authentication minSize: 1, maxSize: 1024 * 32
    }

    static mapping = {
        version false
	datasource 'oauth2'
    }
}
