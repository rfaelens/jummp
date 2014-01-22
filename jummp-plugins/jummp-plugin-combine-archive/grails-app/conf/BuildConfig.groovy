/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





grails.servlet.version = "2.5"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.source.level = 1.7
grails.project.target.level = 1.7
// maven can't handle flatDirs, would break sbml and bives
grails.project.dependency.resolver = "maven"

grails.project.fork = [
    // configure settings for the test-app JVM, uses the daemon by default
    test: false, //[maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256]
]


grails.project.dependency.resolution = {
    inherits("global") {
    }
    log "warn"
    // circumvent http://jira.grails.org/browse/GRAILS-9984
    legacyResolve true
    repositories {
        if (System.getenv("JUMMP_ARTIFACTORY_URL")) {
            mavenRepo "${System.getenv('JUMMP_ARTIFACTORY_URL')}"
        }
        grailsCentral()
        mavenCentral()
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo_snapshots/"
    }
    dependencies {
        compile("org.mbine.co:libCombineArchive:0.1-SNAPSHOT") { 
            excludes 'junit', 'slf4j-api', 'slf4j-log4j12', 'jmock-junit4' 
        }
        //test "org.junit:junit:4.10"
        runtime("commons-jexl:commons-jexl:1.1") { excludes 'junit', 'commons-logging' }
        compile "commons-io:commons-io:2.1"
        compile 'xml-apis:xml-apis:1.4.01'
        // mime-type detection
        compile "org.apache.tika:tika-core:1.3"
        // broken Grails 2.3.2 dependecy
        compile("org.spockframework:spock-core:0.7-groovy-2.0") { excludes 'hamcrest-core' }
    }

    plugins {
        build(":tomcat:7.0.47",
              ":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
        test ":code-coverage:1.2.6"
        test(":codenarc:0.18.1") { transitive = false }
        test ":gmetrics:0.3.1"
    }
}
grails.plugin.location.'jummp-plugin-security' = "../jummp-plugin-security"
grails.plugin.location.'jummp-plugin-core-api' = "../jummp-plugin-core-api"
