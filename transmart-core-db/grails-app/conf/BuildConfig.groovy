String tmVersion = '19.0'

grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'
    legacyResolve false

    repositories {
	grailsCentral()
	mavenLocal()
	mavenCentral()
	mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
	compile 'org.transmartproject:transmart-core-api:' + tmVersion
    }

    plugins {
	compile ':hibernate:3.6.10.19'
	compile ':transmart-shared:' + tmVersion

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}
    }
}
