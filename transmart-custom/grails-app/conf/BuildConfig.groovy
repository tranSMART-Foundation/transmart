grails.project.work.dir = 'target'

grails.project.source.level = 1.7
grails.project.target.level = 1.7

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
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	compile ':hibernate:3.6.10.19', { export = false }
	compile ':search-domain:16.4-SNAPSHOT'
	compile ':transmart-shared:16.4-SNAPSHOT'

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}
    }
}
