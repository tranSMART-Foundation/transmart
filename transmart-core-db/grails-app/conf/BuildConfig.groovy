grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

def defaultVMSettings = [
        maxMemory: 768,
        minMemory: 64,
        debug:     false,
        maxPerm:   256
]

grails.project.fork = [
        test:    [*: defaultVMSettings, daemon:      true],
        run:     [*: defaultVMSettings, forkReserve: false],
        war:     [*: defaultVMSettings, forkReserve: false],
        console: defaultVMSettings
]

grails.project.repos.default = 'repo.transmartfoundation.org'
grails.project.repos."${grails.project.repos.default}".url = 'https://repo.transmartfoundation.org/content/repositories/public/'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    log 'warn'
    legacyResolve false

    inherits('global') {}

    repositories {
        // grailsPlugins()
        // grailsHome()

        mavenLocal()

	grailsCentral()
        mavenCentral()

	mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
        compile 'org.transmartproject:transmart-core-api:16.4-SNAPSHOT'

	// not included in 18.1 beta
	compile group: 'com.google.guava', name: 'guava', version: '14.0.1'

	// not included in 18.1 beta
        runtime 'org.postgresql:postgresql:42.2.2.jre7', {
            transitive = false
            export     = false
        }
    }

    plugins {
        compile ':hibernate:3.6.10.19'
	compile ':transmart-shared:16.4-SNAPSHOT'

        build ':release:3.1.2', ':rest-client-builder:2.1.1', {
            export = false
        }

	// not included in 18.1 beta
	build ':tomcat:7.0.54'
        compile ':db-reverse-engineer:0.5', {
            export = false
        }

    }
}
