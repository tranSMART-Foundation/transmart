package com.thomsonreuters.lsps.transmart

import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.plugin.shared.SecurityService

@Slf4j('logger')
class MetacoreEnrichmentService implements InitializingBean {

    static transactional = false

    @Autowired private HttpBuilderService httpBuilderService
    @Autowired private SecurityService securityService

    @Value('${com.thomsonreuters.transmart.demoEnrichmentURL:http://pathwaymaps.com}')
    private String demoEnrichmentUrl

    @Value('${com.thomsonreuters.transmart.demoMapBaseURL:http://pathwaymaps.com/maps/}')
    private String demoMapBaseUrl

    @Value('${com.thomsonreuters.transmart.metacoreURL:}')
    private String metacoreUrl

    @Value('${com.thomsonreuters.transmart.metacoreDefaultLogin:}')
    private String metacoreDefaultLogin

    @Value('${com.thomsonreuters.transmart.metacoreDefaultPassword:}')
    private String metacoreDefaultPassword

    private Map<String, String> defaultMetacoreParams

    boolean systemMetacoreSettingsDefined() {
	metacoreUrl && metacoreDefaultLogin && metacoreDefaultPassword
    }

    boolean areSettingsConfigured() {
	UserSettings.isConfigured()
    }

    String metacoreSettingsMode() {
	if (areSettingsConfigured()) {
	    String mode = getSetting('metacoreSettingsMode')
	    if (mode == 'demo' || mode == 'system' || mode == 'user') {
		mode
	    }
	    else if (systemMetacoreSettingsDefined()) {
		'system'
	    }
	    else {
		'demo'
	    }
	}
	else {
	    'demo'
	}
    }

    void setMetacoreSettingsMode(String mode) {
	if (mode == 'demo' || mode == 'system' || mode == 'user') {
	    setSetting 'metacoreSettingsMode', mode
	}
    }

    void setMetacoreBaseUrl(String url) {
	setSetting 'metacoreURL', url
    }

    void setMetacoreLogin(String login) {
	setSetting 'metacoreLogin', login
    }

    void setMetacorePassword(String password) {
	setSetting 'metacorePassword', password
    }

    Map<String, String> getMetacoreParams() {
	if (!(defaultMetacoreParams.baseUrl && defaultMetacoreParams.login && defaultMetacoreParams.password)) {
	    defaultMetacoreParams = null
	}

	if (!securityService.loggedIn()) {
	    return defaultMetacoreParams
	}

	String settingsMode = metacoreSettingsMode()
	if (settingsMode == 'user') {
	    [baseUrl : getSetting('metacoreURL') ?: defaultMetacoreParams.baseUrl,
	     login   : getSetting('metacoreLogin') ?: defaultMetacoreParams.login,
	     password: getSetting('metacorePassword') ?: defaultMetacoreParams.password]
	}
	else {
	    if (settingsMode == 'system') {
		defaultMetacoreParams
	    }
	    else {
		null
	    }
	}
    }

    // cohortGeneList = [ IdType: id_type, Data: [list1, list2] ], where list is just a list of EntrezGene IDs
    // metacoreParams = [ 'baseUrl': url, 'login': login, 'password': password ]
    def getEnrichmentByMaps(Map cohortGeneLists, Map metacoreParams) {
	def res
	String mapBaseUrl
	// only one list is supported so far

	String settingsMode = metacoreSettingsMode()

	String baseUrl
	if (settingsMode == 'demo') {
	    baseUrl = demoEnrichmentUrl
	    mapBaseUrl = demoMapBaseUrl
	}
	else {
	    baseUrl = metacoreParams.baseUrl
	    mapBaseUrl = baseUrl
	}

	HTTPBuilder site = httpBuilderService.getInstance(baseUrl)

	if (settingsMode == 'demo') {
	    // demo enrichment

	    logger.info 'Running demo enrichment: {}', baseUrl

	    Map<String, ?> args = [path: '/enrichmentApp/enrichment',
			           body: [limit: 50,
			                  idtype: cohortGeneLists.IdType,
			                  id: cohortGeneLists.Data[0]]]
	    site.post(args) { resp, json ->
		if (json.Code == 0) {
                    res = json
		}
	    }
	}
	else {
	    // call API functions

	    logger.info 'MetaCore - logging in'
	    Map<String, ?> args = [path: '/api/rpc.cgi',
			           query: [proc: 'login',
			                   login: metacoreParams.login,
			                   passwd: metacoreParams.password,
			                   output: 'json']]
	    site.get(args) { resp, json ->
		def authKey = json?.Result[0]?.Key
		if (authKey) {
		    logger.info 'MetaCore - running enrichment'
		    args = [path: '/api/rpc.cgi',
			    body: [proc  : 'getEnrichment',
				   diagram_type: 'maps',
				   limit: 50,
				   lists_origin: 'ids',
				   list_name: 'Cohort 1',
				   idtype: cohortGeneLists.IdType,
				   includeObjectIds: 0,
				   output: 'json',
				   auth_key: authKey,
				   id: cohortGeneLists.Data[0]]]
		    site.post(args) { resp2, json2 ->
			if (json2?.Code == 0) {
			    res = json2
			}
		    }

		    logger.info 'MetaCore - logging out'
		    site.get ( path: '/api/rpc.cgi', query: [ proc: 'logout', auth_key: authKey ] )
		}
	    }
	}

	if (res?.Result) {
	    // updating URLs
	    res.Result[0].enrichment.info_url = mapBaseUrl + res.Result[0].enrichment.info_url
	}

	res
    }

    boolean userMetacoreSettingsDefined() {
	getSetting('metacoreURL') &&
	    getSetting('metacoreLogin') &&
	    getSetting('metacorePassword')
    }

    boolean metacoreSettingsDefined() {
	getMetacoreParams() != null
    }

    private String getSetting(String name) {
	UserSettings.getSetting securityService.currentUserId(), 'com.thomsonreuters.transmart.' + name
    }

    private void setSetting(String name, String value) {
	UserSettings.setSetting securityService.currentUserId(), name, value
    }

    void afterPropertiesSet() {
	defaultMetacoreParams = [
	    baseUrl : metacoreUrl,
	    login   : metacoreDefaultLogin,
	    password: metacoreDefaultPassword].asImmutable()
    }
}
