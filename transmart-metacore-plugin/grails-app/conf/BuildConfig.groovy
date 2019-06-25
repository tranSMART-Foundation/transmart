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
        //mavenRepo 'https://repo.thehyve.nl/content/repositories/public/'
        //mavenRepo 'http://download.java.net/maven/2/'
        //mavenRepo 'http://repository.jboss.com/maven2/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // http-builder
        build 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.1', {
            excludes 'groovy', 'nekohtml'
        }
        build 'org.apache.httpcomponents:httpclient:4.4.1'
        build 'net.sf.opencsv:opencsv:2.3'
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	compile ':transmart-shared:19.0-SNAPSHOT'

        build ':release:3.1.2', ':rest-client-builder:2.1.1', {
            export = false
        }
        //runtime ':rest:0.8'
        //compile ':rest:0.8'
    }
}
