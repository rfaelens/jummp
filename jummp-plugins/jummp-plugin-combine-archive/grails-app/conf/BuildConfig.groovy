/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

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
        if (System.getenv("JUMMP_ARTIFACTORY_GRAILS_PLUGINS_URL")) {
            grailsRepo "${System.getenv('JUMMP_ARTIFACTORY_GRAILS_PLUGINS_URL')}"
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
        compile "commons-io:commons-io:2.4"
        compile 'xml-apis:xml-apis:1.4.01'
    }

    plugins {
        build(":tomcat:$grailsVersion",
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
