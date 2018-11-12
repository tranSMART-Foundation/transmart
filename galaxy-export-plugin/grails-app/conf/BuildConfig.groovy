grails.project.work.dir = 'target'

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

        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }
    dependencies {
        compile 'com.github.jmchilton.blend4j:blend4j:0.1.2' 

	compile 'org.json:json:20090211'
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'

	// not included in 18.1 beta
	runtime 'org.postgresql:postgresql:42.2.2.jre7', {
            transitive = false
            export = false
        }
        runtime 'com.oracle:ojdbc7:12.1.0.1', {
            transitive = false
            export = false
        }
    }

    plugins {
        compile ':transmart-legacy-db:16.4-SNAPSHOT'
        compile ':transmart-shared:16.4-SNAPSHOT'

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
            export = false
        }

        runtime ':hibernate:3.6.10.19'

	// not included in 18.1 beta
	runtime ':resources:1.2.14'
        compile ':rdc-rmodules:16.4-SNAPSHOT'
        build ':tomcat:7.0.54'
    }



}
