package org.transmart.oauth2

class Client {

    private static final String NO_CLIENT_SECRET = ''

    transient springSecurityService

    Integer accessTokenValiditySeconds
    String clientId
    String clientSecret
    Integer refreshTokenValiditySeconds

    Map<String, Object> additionalInformation

    static transients = ['springSecurityService']

    static hasMany = [
        authorities: String,
        authorizedGrantTypes: String,
        resourceIds: String,
        scopes: String,
        autoApproveScopes: String,
        redirectUris: String
    ]

    static mapping = {
        datasource 'oauth2'
    }

    static constraints = {
        accessTokenValiditySeconds nullable: true
	additionalInformation nullable: true
        authorities nullable: true
        authorizedGrantTypes nullable: true
        autoApproveScopes nullable: true
	clientId blank: false, unique: true
	clientSecret nullable: true
        redirectUris nullable: true
	refreshTokenValiditySeconds nullable: true
	resourceIds nullable: true
	scopes nullable: true
    }

    def beforeInsert() {
        encodeClientSecret()
    }

    def beforeUpdate() {
        if (isDirty('clientSecret')) {
            encodeClientSecret()
        }
    }

    protected void encodeClientSecret() {
        clientSecret = clientSecret ?: NO_CLIENT_SECRET
        clientSecret = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(clientSecret) : clientSecret
    }
}
