package com.thomsonreuters.lsps.transmart

import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import org.apache.http.util.EntityUtils

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.transmart.plugin.shared.SecurityService

@Slf4j('logger')
class MetacoreEnrichmentService {

    boolean transactional = true

	def springSecurityService
	def grailsApplication
	def httpBuilderService

	def systemMetacoreSettingsDefined() {
		return grailsApplication.config.com.thomsonreuters.transmart.metacoreURL \
				&& grailsApplication.config.com.thomsonreuters.transmart.metacoreDefaultLogin \
				&& grailsApplication.config.com.thomsonreuters.transmart.metacoreDefaultPassword
	}

	def areSettingsConfigured() {
		return UserSettings.isConfigured()
	}

	def metacoreSettingsMode() {
		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		if (areSettingsConfigured()) {

			def mode = UserSettings.getSetting(userid, "com.thomsonreuters.transmart.metacoreSettingsMode")

			if (mode == 'demo' || mode == 'system' || mode == 'user')
				return mode
			else
				if (systemMetacoreSettingsDefined())
					return 'system'
				else
					return 'demo'
		}
		else
			return 'demo'
	}

	def setMetacoreSettingsMode(mode) {
		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		if (mode == 'demo' || mode == 'system' || mode == 'user')
			UserSettings.setSetting(userid, "com.thomsonreuters.transmart.metacoreSettingsMode", mode)
	}

	def setMetacoreBaseUrl(url) {
		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		UserSettings.setSetting(userid, "com.thomsonreuters.transmart.metacoreURL", url)
	}

	def setMetacoreLogin(login) {
		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		UserSettings.setSetting(userid, "com.thomsonreuters.transmart.metacoreLogin", login)
	}

	def setMetacorePassword(pass) {
		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		UserSettings.setSetting(userid, "com.thomsonreuters.transmart.metacorePassword", pass)
	}

	def getMetacoreParams() {
		def defaultMetacoreParams = [
			baseUrl: grailsApplication?.config?.com?.thomsonreuters?.transmart?.metacoreURL,
			login: grailsApplication?.config?.com?.thomsonreuters?.transmart?.metacoreDefaultLogin,
			password: grailsApplication?.config?.com?.thomsonreuters?.transmart?.metacoreDefaultPassword
		]

		if (! (defaultMetacoreParams.baseUrl && defaultMetacoreParams.login && defaultMetacoreParams.password))
			defaultMetacoreParams = null

		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		if (userid) {
			def settingsMode = metacoreSettingsMode()

			if (settingsMode == 'user') {
				return [
					baseUrl: UserSettings.getSetting(userid, "com.thomsonreuters.transmart.metacoreURL")?:defaultMetacoreParams['baseUrl'],
					login: UserSettings.getSetting(userid, 'com.thomsonreuters.transmart.metacoreLogin')?:defaultMetacoreParams['login'],
					password: UserSettings.getSetting(userid, 'com.thomsonreuters.transmart.metacorePassword')?:defaultMetacoreParams['password']
				]
			}
			else {
				if (settingsMode == 'system')
					return defaultMetacoreParams
				else
					return null
			}
		}
		else
			return defaultMetacoreParams
	}


	// cohortGeneList = [ IdType: id_type, Data: [list1, list2] ], where list is just a list of EntrezGene IDs
	// metacoreParams = [ "baseUrl": url, "login": login, "password": password ]
    def getEnrichmentByMaps(cohortGeneLists, metacoreParams) {
		def res
		def baseUrl = ''
		def mapBaseUrl = ''
		// only one list is supported so far

		def settingsMode = metacoreSettingsMode()

		if (settingsMode == 'demo') {
			baseUrl = grailsApplication?.config?.com?.thomsonreuters?.transmart?.demoEnrichmentURL?:'http://pathwaymaps.com'
			mapBaseUrl = grailsApplication?.config?.com?.thomsonreuters?.transmart?.demoMapBaseURL?:'http://pathwaymaps.com/maps/'
		}
		else {
			baseUrl = metacoreParams['baseUrl']
			mapBaseUrl = baseUrl
		}

		def site = httpBuilderService.getInstance(baseUrl)

		if (settingsMode == 'demo') {
			// demo enrichment

			logger.info "Running demo enrichment: ${baseUrl}"

            site.post( path: '/enrichmentApp/enrichment',
                    body: [ limit: 50, idtype: cohortGeneLists['IdType'], id: cohortGeneLists['Data'][0] ]) {
                resp, json ->

                    if (json.Code == 0)
                        res = json
            }
		}
		else {
			// call API functions

			logger.info 'MetaCore - logging in'
			site.get( path: '/api/rpc.cgi',
			  query: [ proc: 'login', login: metacoreParams.login, passwd: metacoreParams.password, output: 'json' ] ) {
			  	resp, json ->

				  def authKey = json?.Result[0]?.Key
				  if (authKey) {
					  logger.info 'MetaCore - running enrichment'

					  EntityUtils.consumeQuietly(resp.entity) // avoid "IllegalStateException: Invalid use of BasicClientConnManager: connection still allocated" thanks to http://stackoverflow.com/a/16211729/535203

					  site.post( path: '/api/rpc.cgi',
						body: [ proc: 'getEnrichment', diagram_type: 'maps', limit: 50, lists_origin: 'ids', list_name: 'Cohort 1',
						idtype: cohortGeneLists['IdType'], includeObjectIds: 0, output: 'json', auth_key: authKey, id: cohortGeneLists['Data'][0] ]) {
						  resp2, json2 ->

						  if (json2?.Code == 0) {
						  	res = json2
						  }

						  EntityUtils.consumeQuietly(resp2.entity) // avoid "IllegalStateException: Invalid use of BasicClientConnManager: connection still allocated" thanks to http://stackoverflow.com/a/16211729/535203
					  }

					  logger.info 'MetaCore - logging out'
					  site.get ( path: '/api/rpc.cgi', query: [ proc: 'logout', auth_key: authKey ] )
				  }
			}
		}

		if (res?.Result)
			// updating URLs
			res.Result[0].enrichment.info_url = mapBaseUrl + res.Result[0].enrichment.info_url

		return res
    }

	def userMetacoreSettingsDefined() {
		def user = springSecurityService.getPrincipal()
		def userid = user?.id

		def res =
			UserSettings.getSetting(userid, "com.thomsonreuters.transmart.metacoreURL") \
			&& UserSettings.getSetting(userid, 'com.thomsonreuters.transmart.metacoreLogin') \
			&& UserSettings.getSetting(userid, 'com.thomsonreuters.transmart.metacorePassword')

		return res
	}

	def metacoreSettingsDefined() {
		def settings = getMetacoreParams()
		return settings != null
	}


}
