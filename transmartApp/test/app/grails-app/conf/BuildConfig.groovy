grails.plugin.location.'biomart-domain' = '../..'

grails.project.source.level = 1.7
grails.project.target.level = 1.7
grails.project.work.dir = 'target'
grails.servlet.version = '3.0'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
	inherits 'global'
	log 'warn'
	checksums true
	legacyResolve false

	repositories {
		inherits true

		mavenLocal()
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenCentral()
	}

	dependencies {
		test 'org.grails:grails-datastore-test-support:1.0-grails-2.3'
	}

	plugins {
		runtime ':hibernate:3.6.10.19'
	}
}
