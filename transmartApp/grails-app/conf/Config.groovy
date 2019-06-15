import grails.build.logging.GrailsConsole
import grails.util.Environment

def console
if (!Environment.isWarDeployed() && Environment.isWithinShell()) {
	console = GrailsConsole.instance
}
else {
	console = [info: { println "[INFO] $it" }, warn: { println "[WARN] $it" }]
}

grails.assets.minifyJs = true

grails.assets.minifyOptions = [
    languageMode: 'ES5', // tried ECMASCRIPT6
    targetLanguage: 'ES5', //Can go from ES5 to ES6 for those bleeding edgers
    optimizationLevel: 'SIMPLE', //Or ADVANCED or WHITESPACE_ONLY
    excludes: ["**/fractalis.js","**/fractalis.unminified.js"]
]
grails.assets.minify.js.excludes = ["**/fractalis.js","**/fractalis.unminified.js"]

/**
 * Running externalized configuration
 * Assuming the following configuration files
 * - in the executing user's home at ~/.grails/<app_name>Config/[Config.groovy|DataSource.groovy]
 * - config location set path by system variable '<APP_NAME>_CONFIG_LOCATION'
 * - dataSource location set path by system environment variable '<APP_NAME>_DATASOURCE_LOCATION'
 */

/* For some reason, the externalized config files are run with a different
 * binding. None of the variables appName, userHome, appVersion, grailsHome
 * are available; the binding will actually be the root config object.
 * So store the current binding in the config object so the externalized
 * config has access to the variables mentioned.
 */
org.transmart.originalConfigBinding = getBinding()

grails.config.locations = []
List<String> defaultConfigFiles
if (Environment.current != Environment.TEST) {
    defaultConfigFiles = [
	"$userHome/.grails/${appName}Config/Config.groovy",
	"$userHome/.grails/${appName}Config/RModulesConfig.groovy",
	"$userHome/.grails/${appName}Config/DataSource.groovy"
    ]
}
else {
    // settings for the test environment

    org.transmart.configFine = true
}

for (String filePath in defaultConfigFiles) {
    File f = new File(filePath)
    if (f.exists()) {
	if (f.name == 'RModulesConfig.groovy') {
			console.warn 'RModulesConfig.groovy is deprecated, it has been merged into Config.groovy. Loading it anyway.'
        }
	grails.config.locations << 'file:' + filePath
    }
    else if (f.name != 'RModulesConfig.groovy') {
	console.info "Configuration file $filePath does not exist."
    }
}
String bashSafeEnvAppName = appName.toString().toUpperCase(Locale.ENGLISH).replaceAll(/-/, '_')

String externalConfig = System.getenv("${bashSafeEnvAppName}_CONFIG_LOCATION")
if (externalConfig) {
    grails.config.locations << 'file:' + externalConfig
}
String externalDataSource = System.getenv("${bashSafeEnvAppName}_DATASOURCE_LOCATION")
if (externalDataSource) {
    grails.config.locations << 'file:' + externalDataSource
}
for (location in grails.config.locations) {
    console.info "Including configuration file [$location] in configuration building."
}

grails {
    cache {
	enabled = true
	ehcache {
	    ehcacheXmlLocation = 'classpath:ehcache.xml'
	    reloadable = false
	}
    }
    //	controllers.defaultScope = 'singleton'
    converters.default.pretty.print = true
    converters.encoding = 'UTF-8'
    // Keep pre-2.3.0 behavior
    databinding {
	convertEmptyStringsToNull = false
	trimStrings = false
    }
    enable.native2ascii = true // enabled native2ascii conversion of i18n properties files
    exceptionresolver.params.exclude = ['password']
    hibernate.pass.readonly = false
    json.legacy.builder = false
    mime {
	disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
	file.extensions = true // enables the parsing of file extensions from URLs into the request format
	types = [
	    all          : '*/*',
	    atom         : 'application/atom+xml',
	    css          : 'text/css',
	    csv          : 'text/csv',
	    form         : 'application/x-www-form-urlencoded',
	    hal          : ['application/hal+json','application/hal+xml'],
	    html         : ['text/html', 'application/xhtml+xml'],
	    jnlp         : 'application/x-java-jnlp-file',
	    js           : 'text/javascript',
	    json         : ['application/json', 'text/json'],
	    multipartForm: 'multipart/form-data',
	    rss          : 'application/rss+xml',
	    text         : 'text-plain',
	    xml          : ['text/xml', 'application/xml']
	]
    }
    plugin {
	springsecurity {
	    kerberos.active = false
	    ldap.active = false // Disable by default to prevent authentication errors for installations without LDAP
	    oauthProvider {
		accessTokenLookup.className = 'org.transmart.oauth2.AccessToken'
		authorizationCodeLookup.className = 'org.transmart.oauth2.AuthorizationCode'
		clientLookup.className = 'org.transmart.oauth2.Client'
		refreshTokenLookup.className = 'org.transmart.oauth2.RefreshToken'
	    }
	    roleHierarchy = '''
				ROLE_ADMIN > ROLE_DATASET_EXPLORER_ADMIN
				ROLE_DATASET_EXPLORER_ADMIN > ROLE_PUBLIC_USER
				ROLE_DATASET_EXPLORER_ADMIN > ROLE_SPECTATOR
				ROLE_DATASET_EXPLORER_ADMIN > ROLE_STUDY_OWNER
				ROLE_DATASET_EXPLORER_ADMIN > ROLE_TRAINING_USER
				ROLE_STUDY_OWNER > ROLE_PUBLIC_USER
			'''
	    useSecurityEventListener = true
	}
    }
    // requires NIO connector though. If you use apache in front of tomcat in the
    // same server, you can set this to false and set .apache = true
    // Bear in mind bug GRAILS-11376 with Tomcat NIO and Grails 2.3.6+
    plugins.sendfile.tomcat = false
    project.groupId = appName
    scaffolding.templates.domainSuffix = ''
    spring.bean.packages = []
    views.default.codec = 'none' // TODO html
    views {
	gsp {
	    codecs {
		expression = 'none' // TODO html
		scriptlet = 'none' // TODO html
		taglib = 'none'
		staticparts = 'none'
	    }
	    encoding = 'UTF-8'
	    htmlcodec = 'xml'
	    javascript.library = 'jquery'
	}
    }
    web.disable.multipart = false
}

org {
    transmart {
	security {
	    ldap {
		inheritPassword = true
		mappedUsernameProperty = 'username'
	    }
	}
    }
}

com.recomdata.search.autocomplete.max = 20
com.recomdata.search.paginate.max = 20 // default paging size
com.recomdata.search.paginate.maxsteps = 5
com.recomdata.admin.paginate.max = 20

//**************************
//This is the login information for the different i2b2 projects.
//SUBJECT Data.
com.recomdata.i2b2.subject.domain = 'i2b2demo'
com.recomdata.i2b2.subject.projectid = 'i2b2demo'
com.recomdata.i2b2.subject.username = 'Demo'
com.recomdata.i2b2.subject.password = 'demouser'

//SAMPLE Data.
com.recomdata.i2b2.sample.domain = 'i2b2demo'
com.recomdata.i2b2.sample.projectid = 'i2b2demo'
com.recomdata.i2b2.sample.username = 'sample'
com.recomdata.i2b2.sample.password = 'manager'

//core-db settings
org.transmartproject.i2b2.user_id = 'i2b2'
org.transmartproject.i2b2.group_id = 'Demo'
//**************************

// max genes to display after disease search
com.recomdata.search.gene.max = 250

// set schema names for I2B2HelperService
com.recomdata.i2b2helper.i2b2hive = 'i2b2hive'
com.recomdata.i2b2helper.i2b2metadata = 'i2b2metadata'
com.recomdata.i2b2helper.i2b2demodata = 'i2b2demodata'

com.recomdata.transmart.data.export.max.export.jobs.loaded = 20

com.recomdata.transmart.data.export.dataTypesMap = [
    CLINICAL  : 'Clinical & Low Dimensional Biomarker Data',
    MRNA      : 'Gene Expression Data',
    SNP       : 'SNP data (Microarray)',
    STUDY     : 'Study Metadata',
    //	GSEA      : 'Gene Set Enrichment Analysis (GSEA)',
    ADDITIONAL: 'Additional Data'
]

// Data export FTP settings is Rserve running remote in relation to transmartApp
com.recomdata.transmart.data.export.ftp.server = ''
com.recomdata.transmart.data.export.ftp.serverport = ''
com.recomdata.transmart.data.export.ftp.username = ''
com.recomdata.transmart.data.export.ftp.password = ''
com.recomdata.transmart.data.export.ftp.remote.path = ''

// Control which gene/pathway search is used in Dataset Explorer
// A value of 'native' forces Dataset Explorer's native algorithm.
// Absence of this property or any other value forces the use of the Search Algorithm
//com.recomdata.search.genepathway='native'

// The tags in the Concept to indicate Progression-free Survival and Censor flags, used by Survival Analysis
com.recomdata.analysis.survival.survivalDataList = ['(PFS)', '(OS)', '(TTT)', '(DURTFI)']
com.recomdata.analysis.survival.censorFlagList = ['(PFSCENS)', '(OSCENS)', '(TTTCENS)', '(DURTFICS)']

com.recomdata.analysis.genepattern.file.dir = 'data' // Relative to the app root 'web-app' - deprecated - replaced with data.file.dir

com.recomdata.analysis.data.file.dir = 'data' // Relative to the app root 'web-app'

com.recomdata.disclaimer = '<p></p>'

// customization views
//com.recomdata.view.studyview='_clinicaltrialdetail'
com.recomdata.skipdisclaimer = true

org.transmart.security.spnegoEnabled = false
org.transmart.security.sniValidation = true
org.transmart.security.sslValidation = true

bruteForceLoginLock {
    allowedNumberOfAttempts = 3
    lockTimeInMinutes = 10
}

log4j.main = {
    /**
     * Configuration for writing audit metrics.
     * This needs to be placed in the out-of-tree Config.groovy, as the log4j config there will override this.
     * (and don't forget to 'import org.apache.log4j.DailyRollingFileAppender',
     * 'import org.transmart.logging.ChildProcessAppender' and 'import org.transmart.logging.JsonLayout'.)
     */
    /*
     appenders {
     // default log directory is either the tomcat root directory or the current working directory.
     String catalinaBase = System.getProperty('catalina.base') ?: '.'
     String logDirectory = "$catalinaBase/logs"

     // Use layout: JsonLayout(conversionPattern: '%m%n', singleLine: true) to get each message as a single line
     // json the same way as ChildProcessAppender sends it.
     appender new DailyRollingFileAppender(
     name: 'fileAuditLogger',
     datePattern: "'.'yyyy-MM-dd",
     fileName: "$logDirectory/audit.log",
     layout: JsonLayout(conversionPattern:'%d %m%n')
 )
     // the default layout is a JsonLayout(conversionPattern: '%m%n, singleLine: true)
     appender new ChildProcessAppender(
     name: 'processAuditLogger',
     command: ['/usr/bin/your/command/here', 'arg1', 'arg2']
 )
 }
     trace fileAuditLogger: 'org.transmart.audit'
     trace processAuditLogger: 'org.transmart.audit'
     trace stdout: 'org.transmart.audit'
     */

    environments {
	test {
	    warn 'org.codehaus.groovy.grails.commons.spring',
		'org.codehaus.groovy.grails.domain.GrailsDomainClassCleaner',
		'org.codehaus.groovy.grails.plugins.DefaultGrailsPluginManager', //info to show plugin versions
		'org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder' //info to show joined-subclass indo
	    
	    root {
		info 'stdout'
	    }
	}
    }

    warn 'org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper'
}

/**
 * Here you can set custom help pages for different pages.
 * You could open these pages by clicking the blue question mark icon.
 */
/*
org.transmartproject.helpUrls.geneSignatureList='http://www.example.com/'
org.transmartproject.helpUrls.rsIdSignatureList='http://www.example.com/'
org.transmartproject.helpUrls.search='http://www.example.com/'

org.transmartproject.helpUrls.logisticRegression='http://www.example.com/'
org.transmartproject.helpUrls.boxPlot='http://www.example.com/'
org.transmartproject.helpUrls.correlationAnalysis='http://www.example.com/'
org.transmartproject.helpUrls.hierarchicalClustering='http://www.example.com/'
org.transmartproject.helpUrls.heatMap='http://www.example.com/'
org.transmartproject.helpUrls.kMeansClustering='http://www.example.com/'
org.transmartproject.helpUrls.lineGraph='http://www.example.com/'
org.transmartproject.helpUrls.markerSelection='http://www.example.com/'
org.transmartproject.helpUrls.pca='http://www.example.com/'
org.transmartproject.helpUrls.scatterPlot='http://www.example.com/'
org.transmartproject.helpUrls.survivalAnalysis='http://www.example.com/'
org.transmartproject.helpUrls.tableWithFisher='http://www.example.com/'
org.transmartproject.helpUrls.summaryStatistics='http://www.example.com/'

org.transmartproject.helpUrls.hierarchicalClusteringMaxRows='http://www.example.com/'
org.transmartproject.helpUrls.heatMapMaxRows='http://www.example.com/'
org.transmartproject.helpUrls.kMeansClusteringMaxRows='http://www.example.com/'

org.transmartproject.helpUrls.hiDomePopUp='http://www.example.com/'
*/
