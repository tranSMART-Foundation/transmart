def defaultVMSettings = [
        maxMemory: 768,
        minMemory: 64,
        debug:     true,
        maxPerm:   256
]

grails.project.fork = [
        test:    false,
        run:     [*: defaultVMSettings, forkReserve: false],
        war:     [*: defaultVMSettings, forkReserve: false],
        console: defaultVMSettings
]

final def CLOVER_VERSION = '4.0.1'
def enableClover = System.getenv('CLOVER')

if (enableClover) {
    grails.project.fork.test = false

    clover {
        on = true

        srcDirs = ['../src/java', '../src/groovy', '../grails-app',
                    'test/unit', 'test/integration']

        // work around bug in compile phase in groovyc
        // see CLOV-1466 and GROOVY-7041
        excludes = [
                '**/ClinicalDataTabularResult.*',
        ]

        reporttask = { ant, binding, plugin ->
            def reportDir = '' + binding.projectTargetDir + '/clover/report'
            ant.'clover-report' {
                ant.current(outfile: reportDir, title: 'transmart-core-db') {
                    format(type: 'html', reportStyle: 'adg')
                    testresults(dir: 'target/test-reports', includes: '*.xml')
                    ant.columns {
                        lineCount()
                        filteredElements()
                        uncoveredElements()
                        totalPercentageCovered()
                    }
                }
                ant.current(outfile: '' + reportDir + '/clover.xml') {
                    format(type: 'xml')
                    testresults(dir: 'target/test-reports', includes: '*.xml')
                }
            }
        }
    }
}

grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        mavenLocal() // Note: use './grailsw maven-install' to install required plugins locally
	grailsCentral()
	mavenCentral()
        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
	compile 'org.transmartproject:transmart-core-api:19.0-SNAPSHOT'

	test 'org.hamcrest:hamcrest-library:1.3'
        test 'org.hamcrest:hamcrest-core:1.3'
//        test 'com.h2database:h2:1.3.175'

        if (enableClover) {
            compile ':clover:' + CLOVER_VERSION, {
                export = false
            }
        }

        test 'junit:junit:4.11', {
            transitive = false /* don't bring hamcrest */
            export     = false
        }

        test 'org.gmock:gmock:0.9.0-r435-hyve2', {
            transitive = false /* don't bring groovy-all */
            export     = false
        }

        /* for reasons I don't want to guess (we'll move away from ivy soon
         * anyway), javassist is not being included in the test classpath
         * when running test-app in Travis even though the hibernate plugin
         * depends on it */
        test 'org.javassist:javassist:3.16.1-GA', {
            export = false
        }

	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'

    }

    plugins {
    	compile ':hibernate:3.6.10.19', { export = false }
	compile ':transmart-core:19.0-SNAPSHOT'
	compile ':transmart-shared:19.0-SNAPSHOT'
        build ':release:3.1.2', ':rest-client-builder:2.1.1', {
            export = false
        }

        if (enableClover) {
            compile ':clover:' + CLOVER_VERSION, {
                export = false
            }
        }
    }
}
