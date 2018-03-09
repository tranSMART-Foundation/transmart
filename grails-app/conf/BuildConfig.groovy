grails.project.work.dir = 'target'

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
	}

	plugins {
		compile ':mail:1.0.7'
		compile ':spring-security-core:2.0.0'
		compile ':search-domain:16.2'
		compile ':cache:1.1.8'

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
