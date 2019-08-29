package org.transmart.oauth2

class AccessToken {

    byte[] authentication
    String authenticationKey
    String clientId
    Date expiration
    String tokenType
    String username
    String value

    Map<String, Object> additionalInformation

    static hasOne = [refreshToken: String]

    static hasMany = [scope: String]

    static constraints = {
        additionalInformation nullable: true
	authentication minSize: 1, maxSize: 1024 * 32
	authenticationKey blank: false, unique: true
	clientId blank: false
	refreshToken nullable: true
	tokenType blank: false
	username nullable: true
	value blank: false, unique: true
    }

    static mapping = {
        version false
        scope lazy: false
	datasource 'oauth2'
    }
}
