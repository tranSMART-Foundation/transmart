grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits('global') {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log 'warn' // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo 'https://repo.transmartfoundation.org/content/groups/public/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile 'uk.ac.ebi.mydas:mydas:1.7.0.transmart-16.4-SNAPSHOT'
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'org.transmartproject:transmart-core-api:16.4-SNAPSHOT'
    }

    plugins {
        build ':tomcat:7.0.54',
              ':release:3.1.2',
              ':rest-client-builder:2.1.1', {
            export = false
        }
    }
}
