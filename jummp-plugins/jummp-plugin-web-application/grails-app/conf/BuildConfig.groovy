/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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

//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.groupId = "net.biomodels.jummp.webapp"

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
        grailsCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenCentral()
        mavenLocal()
        // compass 2.2.1
        mavenRepo "http://repo.grails.org/grails/core"
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        //flatDir name: "jummpLibs", dirs: "../../lib/"
    }
    dependencies {
        // swagger
        compile 'com.wordnik:swagger-servlet_2.10:1.3.1'
    }

    plugins {
        build ":tomcat:7.0.54"

        runtime(":weceem:1.2") {
            excludes 'ckeditor'
        }
        runtime ":ckeditor:3.6.6.1.1"
        runtime ":jquery:1.11.1"
        runtime ":jquery-datatables:1.7.5"
    }
}
grails.plugin.location.'jummp-plugin-security'="../jummp-plugin-security"
grails.plugin.location.'jummp-plugin-core-api'="../jummp-plugin-core-api"
