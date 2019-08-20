grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

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
    // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
    repositories {
        grailsCentral()
        mavenCentral()

        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	mavenRepo 'http://52north.org/maven/repo/releases' // to resolve the excluded gnujaxp dependency
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

	compile 'axis:axis:1.4', {
		excludes 'axis-saaj', 'commons-logging'
	}
	compile 'com.jcraft:jsch:0.1.54'
	compile 'com.thoughtworks.xstream:xstream:1.3', {
		excludes 'xpp3_min'
	}
	compile 'commons-net:commons-net:3.3'
	compile 'jfree:jfreechart:1.0.11', {
		excludes 'gnujaxp', 'junit'
	}

	compile 'net.sf.opencsv:opencsv:2.3'
	compile 'org.apache.lucene:lucene-core:2.4.0'
	compile 'org.apache.lucene:lucene-highlighter:2.4.0'

	// runtime 'mysql:mysql-connector-java:5.1.21'
        runtime 'com.oracle:ojdbc7:12.1.0.1', {
            export = false
        }

    }

    plugins {
	compile ':asset-pipeline:2.14.1.1'
        build ':release:3.1.2',
                ':rest-client-builder:2.1.1', {
            export = false
        }
    }
}
