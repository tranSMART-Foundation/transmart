package org.transmart.oauth2

class RefreshToken {

    byte[] authentication
    Date expiration
    String value

    static mapping = {
        version false
	datasource 'oauth2'
    }

    static constraints = {
	authentication minSize: 1, maxSize: 1024 * 4
	expiration nullable: true
	value blank: false, unique: true
    }
}
