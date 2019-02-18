import grails.util.Environment

def forkSettingsRun   = [minMemory: 1536, maxMemory: 4096, maxPerm: 384, debug: false]
def forkSettingsOther = [minMemory:  256, maxMemory: 1024, maxPerm: 384, debug: false]

def dm
try {
    Class dmClass = new GroovyClassLoader().parseClass(
	new File('../transmart-dev/DependencyManagement.groovy'))
    dm = dmClass?.newInstance()
}
catch (ignored) {}

grails.project.dependency.resolver = 'maven'
grails.project.fork = [test: forkSettingsOther, run: forkSettingsRun, war: false, console: forkSettingsOther]

grails.project.source.level = 1.8
grails.project.target.level = 1.8

grails.project.war.file = 'target/' + appName + '.war'
grails.project.work.dir = 'target'
grails.servlet.version = '3.0'

grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'
    checksums true
    legacyResolve false

    if (!dm) {
	repositories {
	    mavenLocal()
	    grailsCentral()
	    mavenCentral()

	    mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}
    }
    else {
	dm.configureRepositories delegate
    }

    dependencies {
	compile 'axis:axis:1.4' // for GeneGo web services
//	compile 'com.google.guava:guava:19.0'
	compile 'com.google.guava:guava:16.0-dev-20140115-68c8348'
	compile 'commons-net:commons-net:3.3' // used for ftp transfers
	compile 'net.sf.ehcache:ehcache:2.9.0'
	compile 'net.sf.opencsv:opencsv:2.3'
	compile 'org.apache.commons:commons-math:2.2' //>2MB lib briefly used in ChartController
	compile 'org.apache.httpcomponents:httpclient:4.4.1'
	compile 'org.apache.httpcomponents:httpcore:4.4.1'
	compile 'org.apache.lucene:lucene-core:2.4.0'
	compile 'org.apache.lucene:lucene-demos:2.4.0'
	compile 'org.apache.lucene:lucene-highlighter:2.4.0'
	compile 'org.apache.solr:solr-core:5.4.1'
	compile 'org.apache.solr:solr-solrj:5.4.1'
	compile 'org.jfree:jfreechart:1.0.19'
	compile 'org.jfree:jfreesvg:2.1'
	compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.2', {
	    excludes 'groovy', 'nekohtml', 'httpclient', 'httpcore'
	}
	compile 'org.rosuda:Rserve:1.7.3'
	compile('org.springframework.security.extensions:spring-security-saml2-core:1.0.0.RELEASE') {
	    //excludes of spring security necessary because they are for an older version (3.1 branch)
	    //also remove xercesImpl because it breaks tomcat and is not otherwise needed
	    excludes 'bcprov-jdk15', 'spring-security-config', 'spring-security-core', 'spring-security-web', 'xercesImpl'
	}
	compile 'org.transmartproject:transmart-core-api:16.4-SNAPSHOT'

	runtime 'com.jcraft:jsch:0.1.42'
	runtime 'com.lowagie:itext:2.0.8', { transitive = false }
	runtime 'gov.nist.math:jama:1.0.1'
	runtime 'oauth.signpost:signpost-commonshttp4:1.2.1.1'
	runtime 'oauth.signpost:signpost-core:1.2.1.1'
	runtime 'org.apache.poi:poi-contrib:3.1-FINAL'
	runtime 'org.apache.poi:poi-scratchpad:3.1-FINAL'
	runtime 'org.apache.poi:poi:3.1-FINAL'
	runtime 'xerces:xercesImpl:2.9.1'

	// you can remove whichever you're not using
	runtime 'org.postgresql:postgresql:42.2.2.jre7'
	runtime 'com.oracle:ojdbc7:12.1.0.1'

	test 'org.gmock:gmock:0.9.0-r435-hyve2', { transitive = false }
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
	test 'org.hamcrest:hamcrest-core:1.3'
	test 'org.hamcrest:hamcrest-library:1.3'

	// unused dependencies from 16.3
	//        runtime 'org.javassist:javassist:3.16.1-GA'

	//        compile 'antlr:antlr:2.7.7'

	//        // spring security version should be in sync with that brought with
	//        // grails-spring-security-core
	//        runtime 'org.springframework.security:spring-security-config:3.2.3.RELEASE',
	//                'org.springframework.security:spring-security-web:3.2.3.RELEASE', {
	//            transitive = false
	//        }

	//        test 'junit:junit:4.11', {
	//            transitive = false /* don't bring hamcrest */
	//            export = false
	//        }

    }

    plugins {
	build ':release:3.1.2'
	build ':tomcat:8.0.50'

	compile ':cache-ehcache:1.0.5'
	compile ':codenarc:0.21' // support for static code analysis - see codenarc.reports property below
	compile ':hibernate:3.6.10.19'
	compile ':quartz:1.0.2'
	compile ':rest-client-builder:2.1.1'
	compile ':scaffolding:2.1.2'
	compile ':spring-security-core:2.0.0'
	compile ':spring-security-kerberos:1.0.0'
	compile ':spring-security-ldap:2.0.0'
	compile ':spring-security-oauth2-provider:2.0-RC5'

	runtime ':jquery-ui:1.10.4'
	runtime ':jquery:1.11.1'
	runtime ':prototype:1.0'
	runtime ':resources:1.2.14'

	//test ':code-coverage:1.2.6' // Doesn't work with forked tests yet

	String tmVersion = '16.4-SNAPSHOT'
	if (!dm) {
//	    compile ':smart-r:'                   + tmVersion
	    compile ':rdc-rmodules:'              + tmVersion
	    compile ':transmart-core:'            + tmVersion
	    compile ':transmart-gwas:'            + tmVersion
	    compile ':transmart-gwas-plink:'      + tmVersion
	    compile ':dalliance-plugin:'          + tmVersion
	    compile ':transmart-mydas:'           + tmVersion
	    compile ':transmart-rest-api:'        + tmVersion
	    compile ':transmart-shared:'          + tmVersion
	    compile ':spring-security-auth0:'     + tmVersion
	    compile ':galaxy-export-plugin:'      + tmVersion
	    compile ':transmart-metacore-plugin:' + tmVersion
	    compile ':transmart-xnat-importer:'   + tmVersion
	    compile ':xnat-viewer:'               + tmVersion

	    compile ':transmart-custom:'          + tmVersion
	    compile ':transmart-fractalis:'       + tmVersion
	    test ':transmart-core-db-tests:'      + tmVersion

	    // new transmart modules to be included
	    //			compile ':transmart-gnome:'           + tmVersion

	    // dependencies already included
	    //			compile ':biomart-domain:'            + tmVersion
	    //			compile ':folder-management:'         + tmVersion
	    //			compile ':search-domain:'             + tmVersion
	    //			compile ':transmart-java:'            + tmVersion
	    //			compile ':transmart-legacy-db:'       + tmVersion
	}
	else {
	    dm.internalDependencies delegate
	}
    }
}

dm?.with {
//    configureInternalPlugin 'runtime', 'smart-r'
    configureInternalPlugin 'compile', 'rdc-rmodules'
    configureInternalPlugin 'runtime', 'transmart-core'
    configureInternalPlugin 'compile', 'transmart-gwas'
    configureInternalPlugin 'runtime', 'transmart-gwas-plink'
    configureInternalPlugin 'runtime', 'dalliance-plugin'
    configureInternalPlugin 'runtime', 'transmart-mydas'
    configureInternalPlugin 'runtime', 'transmart-rest-api'
    configureInternalPlugin 'runtime', 'transmart-shared'
    configureInternalPlugin 'runtime', 'galaxy-export-plugin'
    configureInternalPlugin 'runtime', 'transmart-metacore-plugin'
    configureInternalPlugin 'runtime', 'transmart-xnat-importer'
    configureInternalPlugin 'runtime', 'xnat-viewer'
    configureInternalPlugin 'test',    'transmart-core-db-tests'
}

dm?.inlineInternalDependencies grails, grailsSettings

// Use new NIO connector in order to support sendfile
// This is a lovely thought, but with Tomcat running Grails 2.3.6+ NIO does not function in run-war mode
// Official bug number : GRAILS-11376
if (!grails.util.Environment.isWarDeployed()) {
    grails.tomcat.nio = true
}

codenarc.reports = {
    TransmartAppReport('html') {
        outputFile = 'CodeNarc-transmartApp-Report.html'
        title = 'transmartApp Report'
    }
}
