/*************************************************************************
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

def forkSettingsOther = [minMemory:  256, maxMemory: 1024, maxPerm: 384, debug: false]

grails.project.fork = [test: [ *:forkSettingsOther, daemon: true ], console: forkSettingsOther]
grails.project.work.dir = 'target'

grails.project.target.level = 1.8
grails.project.source.level = 1.8

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
        grailsCentral()
        mavenCentral()
        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
    }

    dependencies {
 	compile 'com.google.guava:guava:19.0'
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'org.mapdb:mapdb:0.9.10'
        compile 'org.rosuda:Rserve:1.7.3'
	compile 'org.transmartproject:transmart-core-api:16.4-SNAPSHOT'
//	compile 'com.lowagie:itext:2.0.8' //latest 4.2.2
//	compile 'org.xhtmlrenderer:core-renderer:R8'

	// runtime instead of test due to technical limitations (referenced from resources.groovy)
        runtime 'org.gmock:gmock:0.8.3', {
            transitive = false
            export     = false
        }
        test 'org.hamcrest:hamcrest-library:1.3'
        test 'org.hamcrest:hamcrest-core:1.3'
    }

    plugins {
	compile ':codenarc:0.21'
        compile ':quartz:1.0.2'
	compile ':spring-security-core:2.0.0'

	compile ':transmart-shared:16.4-SNAPSHOT'

	build ':release:3.1.2', ':rest-client-builder:2.1.1', {
	    export = false
	}

//        sendfile breaks in grails 2.5.4
//        compile ':sendfile:0.2'

	//not used in 18.1 beta
//        runtime ':resources:1.2.14'
	compile ':asset-pipeline:2.14.1.1'
    }
}

codenarc.reports = {
    TransmartAppReport('html') {
        outputFile = 'CodeNarc-Rmodules-Report.html'
        title = 'Rmodules Report'
    }
}
