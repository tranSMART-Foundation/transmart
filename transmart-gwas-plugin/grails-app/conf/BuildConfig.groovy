grails.project.work.dir = 'target'

grails.project.source.level = 1.8
grails.project.target.level = 1.8

grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits('global') {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log 'warn' // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve false

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()

        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    String tmVersion = '19.0'

    dependencies {
    	// needed to support folder-management
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1'

        runtime 'org.transmartproject:transmart-core-api:' + tmVersion
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }
    plugins {
	compile ':cache:1.1.8'
	compile ':hibernate:3.6.10.19', { export = false }
	compile ':mail:1.0.7'
	compile ':asset-pipeline:2.14.1.1'

	compile ':folder-management:' + tmVersion
	compile ':transmart-legacy-db:' + tmVersion

	compile ':spring-security-core:2.0.0'
	compile ':quartz:1.0.2'


	build   ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}

	runtime ':search-domain:' + tmVersion
	runtime ':transmart-core:' + tmVersion
	runtime ':transmart-shared:' + tmVersion

//	runtime ':resources:1.2.14'

    }
}
