String tmVersion = '19.0'

grails.project.work.dir = 'target'

grails.project.source.level = 1.8
grails.project.target.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'
    legacyResolve false

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo 'https://repo.transmartfoundation.org/content/groups/public/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'uk.ac.ebi.mydas:mydas:1.7.0.transmart-'   + tmVersion
        compile 'org.transmartproject:transmart-core-api:' + tmVersion
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
   }

    plugins {
        build ':tomcat:8.0.50',
              ':release:3.1.2',
              ':rest-client-builder:2.1.1', {
            export = false
        }
    }
}
