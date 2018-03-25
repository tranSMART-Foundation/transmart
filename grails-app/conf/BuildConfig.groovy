grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenLocal()
		grailsCentral()
		mavenCentral()
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-snapshots'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-releases'
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {
		compile 'com.auth0:mvc-auth-commons:1.0.0'
		compile 'us.monoid.web:resty:0.3.2'
	}

	plugins {
		compile ':hibernate:3.6.10.19', { export = false }
		compile ':mail:1.0.7'
		compile ':spring-security-core:2.0.0'
		compile ':cache:1.1.8'

		compile ':search-domain:18.1-SNAPSHOT'
		compile ':transmart-core:18.1-SNAPSHOT'

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
