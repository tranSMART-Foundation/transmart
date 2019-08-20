grails.project.work.dir = 'target'

grails.project.source.level = 1.8
grails.project.target.level = 1.8

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
	compile 'commons-net:commons-net:3.3'
        compile 'org.apache.httpcomponents:httpclient:4.4.1'
        compile 'org.apache.httpcomponents:httpcore:4.4.1'
        compile 'org.apache.httpcomponents:httpmime:4.4.1'
        compile 'org.apache.james:apache-mime4j:0.6'
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1', {
            excludes 'groovy', 'nekohtml'
        }
        compile 'org.mongodb:mongo-java-driver:2.10.1'
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
    }

    plugins {
	compile ':asset-pipeline:2.14.1.1'
        compile ':search-domain:19.0-SNAPSHOT'
	compile ':transmart-core:19.0-SNAPSHOT'

//        compile ':transmart-legacy-db:19.0-SNAPSHOT'
//        compile ':spring-security-core:2.0.0'
        compile ':quartz:1.0.2'

        build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}

//        runtime ':resources:1.2.14'
    }
}
