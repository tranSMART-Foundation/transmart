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
        mavenCentral()
        mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
	
        // runtime 'mysql:mysql-connector-java:5.1.13'
    }

    plugins {
        String tmVersion = '19.0-SNAPSHOT'

	compile ':biomart-domain:' + tmVersion
	compile ':transmart-shared:' + tmVersion
	compile ':asset-pipeline:2.14.1.1'

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}
    }
}
