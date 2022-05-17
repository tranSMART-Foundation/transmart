package org.transmartfoundation.config

import groovy.util.logging.Slf4j

@Slf4j('logger')
class ConfigInfoController {

    ConfigService configService

    def index() {

        ConfigParams configParams = configService.getConfigParams()

	logger.info 'In ConfigInfoController index'

        [configParams: configParams, paramDone: [:] ]
    }

    def authProviders() {

        def configAuthProviders = configService.getAuthProviders()

        [providers: configAuthProviders]
    }

    def oauthClients() {

        def oauthClients = configService.getOauthClients()

        [clients: oauthClients]
    }

    def sampleMapping() {

        def sampleMapping = configService.getSampleMapping()

        [mapping: sampleMapping]
    }
}
