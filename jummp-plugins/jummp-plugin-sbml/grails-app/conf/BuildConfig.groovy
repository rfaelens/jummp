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
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.groupId = "net.biomodels.jummp.plugins.sbml"
grails.project.source.level = 1.7
grails.project.target.level = 1.7
grails.project.dependency.resolver = "maven"

customJvmArgs = ["-server", "-noverify"]
grails.project.fork = [
    //compile: [jvmArgs: [ "-implicit:class", "-Aspi_log=target/spi_log.log", "-Aspi_verbose"]],
    // configure settings for the test-app JVM, uses the daemon by default
    test: false, //[maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512, forkReserve:false, jvmArgs: customJvmArgs],
    // configure settings for the run-war JVM
    war: [maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512, forkReserve:false, jvmArgs: customJvmArgs],
    // configure settings for the Console UI JVM
    console: [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 256, jvmArgs: customJvmArgs]
]

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
        mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        // repository for miriam lib
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo_snapshots/"
        mavenRepo "http://www.biojava.org/download/maven/"
        //flatDir name: "jummpLibs", dirs: "../../lib/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
        // miriam lib required by sbml converters
        runtime('uk.ac.ebi.miriam:miriam-lib:1.1.3')// { transitive = false }
        // dependencies of jsbml
        compile("org.sbml.jsbml:core:1.0")
        compile("org.sbml.jsbml.ext:arrays:0.9")
        compile("org.sbml.jsbml.ext:comp:1.0")
        compile("org.sbml.jsbml.ext:dyn:0.5")
        compile("org.sbml.jsbml.ext:fbc:1.0")
        compile("org.sbml.jsbml.ext:groups:0.4")
        compile("org.sbml.jsbml.ext:layout:1.0")
        compile("org.sbml.jsbml.ext:multi:0.1")
        compile("org.sbml.jsbml.ext:qual:2.1")
        compile("org.sbml.jsbml.ext:render:0.8")
        compile("org.sbml.jsbml.ext:req:0.2")
        compile("org.sbml.jsbml.ext:spatial:0.8-b1")
        //compile "com.thoughtworks.xstream:xstream:1.4.7"
        //runtime('org.codehaus.woodstox:woodstox-core-lgpl:4.0.9') { excludes 'stax2-api' }
        //runtime('org.codehaus.staxmate:staxmate:2.0.0') { excludes 'stax2-api' }
        //runtime "org.codehaus.woodstox:stax2-api:3.1.0"
        compile "org.sbfc:converter:1.1"
        //compile "org.jdom:jdom:1.1.3"
        //compile "xml-apis:xml-apis:1.4.01"
        //compile "jaxen:jaxen:1.1.4"
    }

    plugins {
        build ":tomcat:7.0.54"

        compile ":perf4j:0.1.1"
    }
}
//grails.plugin.location.'jummp-plugin-core-api'="../jummp-plugin-core-api"
grails.plugin.location.'jummp-plugin-configuration'="../jummp-plugin-configuration"
