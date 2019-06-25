grails.project.work.dir = 'target'

grails.project.source.level = 1.8
grails.project.target.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
	mavenLocal()
	grailsCentral()
	mavenCentral()
	mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
	compile 'com.auth0:mvc-auth-commons:1.0.0'
	compile 'us.monoid.web:resty:0.3.2'
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	compile ':hibernate:3.6.10.19', { export = false }
	compile ':mail:1.0.7'
	compile ':spring-security-core:2.0.0'
	compile ':cache:1.1.8'

	String tmVersion = '19.0-SNAPSHOT'
	compile ':search-domain:'    + tmVersion
	compile ':transmart-core:'   + tmVersion
	compile ':transmart-custom:' + tmVersion
	compile ':transmart-shared:' + tmVersion

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}
    }
}
