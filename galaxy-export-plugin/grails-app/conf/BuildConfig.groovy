String tmVersion  = '19.1'

grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits('global') {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log 'warn' // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()

        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }
    dependencies {
        compile 'com.github.jmchilton.blend4j:blend4j:0.1.2' 

	compile 'org.json:json:20090211'

        build 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.1', {
            excludes 'groovy', 'nekohtml'
        }
        build 'org.apache.httpcomponents:httpclient:4.4.1'

	// not included in 18.1 beta
	runtime 'org.postgresql:postgresql:42.7.2', {
            transitive = false
            export = false
        }
        runtime 'com.oracle.database.jdbc:ojdbc8:23.2.0.0', {
            transitive = false
            export = false
        }

	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	compile ':asset-pipeline:2.14.1.1'

        compile ':transmart-legacy-db:' + tmVersion
        compile ':transmart-shared:'    + tmVersion

        build ':tomcat:8.0.50'
	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
            export = false
        }

        runtime ':hibernate:3.6.10.19'

	// not included in 18.1 beta
//	runtime ':resources:1.2.14'

        compile ':rdc-rmodules:' + tmVersion
    }



}
