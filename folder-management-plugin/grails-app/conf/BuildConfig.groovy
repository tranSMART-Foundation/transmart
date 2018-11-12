grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'
//grails.project.war.file = 'target/' + appName + '-' + appVersion + '.war'
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
        mavenCentral()
        mavenLocal()

        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        runtime 'hsqldb:hsqldb:1.8.0.10'
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1', {
            excludes 'groovy', 'nekohtml'
        }
	compile 'commons-net:commons-net:3.3'
        compile 'org.apache.httpcomponents:httpclient:4.4.1'
        compile 'org.mongodb:mongo-java-driver:2.10.1'
        compile 'org.apache.httpcomponents:httpcore:4.4.1'
        compile 'org.apache.httpcomponents:httpmime:4.4.1'
        compile 'org.apache.james:apache-mime4j:0.6'
        test 'junit:junit:4.11', {
            transitive = false /* don't bring hamcrest */
            export = false
        }

        test 'org.hamcrest:hamcrest-core:1.3',
                'org.hamcrest:hamcrest-library:1.3'

        test 'org.gmock:gmock:0.9.0-r435-hyve2', {
            transitive = false
        }
    }

    plugins {
        compile ':resources:1.2.14'
        compile ':search-domain:16.4-SNAPSHOT'
        compile ':transmart-legacy-db:16.4-SNAPSHOT'
        compile ':spring-security-core:2.0.0'
        compile ':quartz:1.0.2'
        build ':release:3.1.2'
    }
}
