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





grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.groupId = "net.biomodels.jummp.jms.remote"
grails.project.source.level = 1.7
grails.project.target.level = 1.7
// maven can't handle flatDirs, would break sbml and bives
grails.project.dependency.resolver = "ivy"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve true
    repositories {
        if (System.getenv("JUMMP_ARTIFACTORY_URL")) {
            mavenRepo "${System.getenv('JUMMP_ARTIFACTORY_URL')}"
        }
        if (System.getenv("JUMMP_ARTIFACTORY_GRAILS_PLUGINS_URL")) {
            grailsRepo "${System.getenv('JUMMP_ARTIFACTORY_GRAILS_PLUGINS_URL')}"
        }
        grailsPlugins()
        grailsHome()
        grailsCentral()
        // necessary because core-api depends on libPharmML
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo_snapshots/"

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        flatDir name: "jummpLibs", dirs: "../../lib/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
        runtime('org.apache.activemq:activeio-core:3.1.2',
                'org.apache.activemq:activemq-core:5.5.0',
                'org.apache.activemq:activemq-spring:5.5.0',
                'org.apache.xbean:xbean-spring:3.7') {
            excludes 'commons-logging',
                    'commons-io',
                    'commons-pool',
                    'groovy-all',
                    'howl-logger',
                    'log4j',
                    'spring-beans',
                    'spring-context',
                    'spring-core',
                    'spring-test',
                    'slf4j-api',
                    'xalan'//, 'xml-apis'
        }
        compile 'commons-io:commons-io:2.1'
        compile 'org.springframework:spring-jms:3.2.4.RELEASE'

        test 'hsqldb:hsqldb:1.8.0.10'
    }
    plugins {
        compile ":spring-security-core:1.2.7.3"
        compile ":perf4j:0.1.1"
        compile ":jms:1.2"

        // default grails plugins
        compile ":hibernate:3.6.10.3"
        compile ":jquery:1.10.0"
        //compile ":resources:1.0.2"

        build ":tomcat:7.0.42"
    }
}
grails.plugin.location.'jummp-plugin-core-api'="../jummp-plugin-core-api"
grails.plugin.location.'jummp-plugin-jms'="../jummp-plugin-jms"
grails.plugin.location.'jummp-plugin-remote'="../jummp-plugin-remote"
grails.plugin.location.'jummp-plugin-sbml'="../jummp-plugin-sbml"
grails.plugin.location.'jummp-plugin-security'="../jummp-plugin-security"
