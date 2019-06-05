grails.project.work.dir = 'target'

grails.project.source.level = 1.8
grails.project.target.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
	mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
	grailsCentral()
	mavenCentral()
	mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
	compile 'com.auth0:java-jwt:3.2.0'
	compile 'com.fasterxml.jackson.core:jackson-core:2.8.5'
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	compile ':spring-security-core:2.0.0'

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}
    }
}
