grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    log "warn"
    legacyResolve false
    inherits('global') {}
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
        mavenRepo 'https://repo.thehyve.nl/content/repositories/public/'
    }
    dependencies {
        // compile 'org.apache.ant:ant:1.9.6'
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'org.rosuda:Rserve:1.7.3'
        compile 'org.mapdb:mapdb:0.9.10'

        /* serializable ImmutableMap only on guava 16 */
        compile group: 'com.google.guava', name: 'guava', version: '16.0-dev-20140115-68c8348'
        compile 'org.transmartproject:transmart-core-api:1.2.2-SNAPSHOT'
    }
    plugins {
        runtime ':transmart-rest-api:1.2.2-SNAPSHOT'
    }
}