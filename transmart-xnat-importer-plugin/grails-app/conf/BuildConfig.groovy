String tmVersion = '19.1'

grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits('global') {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log 'warn' // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenRepo 'http://repository.codehaus.org'
        //mavenRepo 'http://download.java.net/maven/2/'
        //mavenRepo 'http://repository.jboss.com/maven2/'
        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.27'

	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	runtime ':biomart-domain:'   + tmVersion
	runtime ':transmart-shared:' + tmVersion

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
            export = false
        }
    }
}
