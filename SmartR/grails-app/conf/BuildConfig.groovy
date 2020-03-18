String tmVersion = '19.0'

def forkSettingsRun = [
        minMemory: 1536,
        maxMemory: 4096,
        maxPerm  : 384,
        debug    : false,
]
def forkSettingsOther = [
        minMemory: 256,
        maxMemory: 1024,
        maxPerm  : 384,
        debug    : false,
]

grails.project.fork = [
        test   : false,
        run    : forkSettingsRun,
        war    : false,
        console: forkSettingsOther]

grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

def dm, dmClass
try {
    dmClass = new GroovyClassLoader().parseClass(
            new File('../transmart-dev/SmartRDependencyManagement.groovy'))
}
catch (Exception e) {
}
if (dmClass) {
    dm = dmClass.newInstance()
}

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    log 'warn'
    legacyResolve false
    inherits('global') {}
    if (!dm) {
        repositories {
            grailsCentral()
            mavenLocal()
            mavenCentral()
            mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
        }
    }
    else {
        dm.configureRepositories delegate
    }
    dependencies {
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1'
        // compile 'org.apache.ant:ant:1.9.6'
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'org.rosuda:Rserve:1.7.3'
        compile 'org.mapdb:mapdb:0.9.10'
        compile 'org.apache.commons:commons-lang3:3.4'

        compile 'com.google.guava:guava:19.0'
        compile 'org.transmartproject:transmart-core-api:' + tmVersion
        //test 'com.jayway.restassured:rest-assured:2.4.1'

        runtime 'org.javassist:javassist:3.16.1-GA'
        runtime 'com.ittm_solutions.ipacore:IpaApi:'       + tmVersion

	test 'org.hamcrest:hamcrest-library:1.3'
        test 'org.hamcrest:hamcrest-core:1.3'

        test 'org.gmock:gmock:0.9.0-r435-hyve2', {
            transitive = false /* don't bring groovy-all */
            export     = false
        }
    }
    plugins {
    	// for release support (e.g. to 'publish' plugin to nexus)
        build 'org.grails.plugins:release:3.1.2'

        // FIXME: Advanced workflows gets buggy when updating resources plugin to 1.2.14
//        runtime ':resources:1.2.14'

        //compile ':sendfile:0.2'
        build ':tomcat:8.0.50', {
            export = false
        }
        test ':functional-test:2.0.0'
        test ':karma-test-runner:0.2.4'

        if (!dm) {
            runtime ':transmart-core:'       + tmVersion

            test ':transmart-core:'          + tmVersion
            test ':transmart-core-db-tests:' + tmVersion
        }
        else {
            dm.internalDependencies delegate
        }

    }
}

dm?.with {
    configureInternalPlugin 'runtime', 'transmart-core'
    configureInternalPlugin 'test', 'transmart-core'
    configureInternalPlugin 'test', 'transmart-core-db-tests'
}

dm?.inlineInternalDependencies grails, grailsSettings
