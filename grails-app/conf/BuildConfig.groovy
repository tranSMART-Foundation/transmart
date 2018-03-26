grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
		grailsCentral()
		mavenCentral()
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-snapshots'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-releases'
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {}

	plugins {
		compile ':spring-security-core:2.0.0'

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
