grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
	mavenLocal()
	grailsCentral()
	mavenCentral()
    }

    dependencies {
    }

    plugins {
	compile ':spring-security-core:2.0.0'
	compile ':asset-pipeline:2.14.1.1'

	build ':release:3.1.2', ':rest-client-builder:2.1.1' {
	    export = false
	}
    }
}
