grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'
	legacyResolve false

	repositories {
		mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
		grailsCentral()
		mavenCentral()
//		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
		mavenRepo 'http://localhost/content/repositories/public/'
	}

	dependencies {
		compile 'org.transmartproject:transmart-core-api:19.0-SNAPSHOT'
	}

	plugins {
		compile ':hibernate:3.6.10.19'
		compile ':transmart-shared:19.0-SNAPSHOT'

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
