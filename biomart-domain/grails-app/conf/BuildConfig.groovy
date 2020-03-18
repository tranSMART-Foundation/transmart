grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'
//grails.project.war.file = 'target/' + appName + '-' + appVersion + '.war'

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
    legacyResolve false
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()

        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.13'
    }
    plugins {
        String tmVersion = '19.0'

        compile ':transmart-java:' + tmVersion

        compile ':hibernate:3.6.10.19'
        build ':release:3.1.2',
              ':rest-client-builder:2.1.1', {
            export = false
        }
    }

}
