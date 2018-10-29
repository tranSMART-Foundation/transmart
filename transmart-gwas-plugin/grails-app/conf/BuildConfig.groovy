grails.project.work.dir = 'target'

grails.project.source.level = 1.7
grails.project.target.level = 1.7

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

        mavenRepo "https://repo.transmartfoundation.org/content/repositories/public/"
    }

    String tmVersion = '16.4-SNAPSHOT'

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

		// these three not included in 18.1 beta
		compile ':resources:1.2.14'
		compile ':spring-security-core:2.0.0'
		compile ':quartz:1.0.2'


		compile ':folder-management:' + tmVersion
		runtime ':search-domain:' + tmVersion
		runtime ':transmart-core:' + tmVersion
		runtime ':transmart-shared:' + tmVersion

		build   ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}


    }
}
