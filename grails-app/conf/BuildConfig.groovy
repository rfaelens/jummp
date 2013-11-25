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





grails.servlet.version = "3.0"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.war.file = "target/${appName}.war"
grails.project.groupId = "net.biomodels.jummp"
grails.project.source.level = 1.7
grails.project.target.level = 1.7
// maven can't handle flatDirs, would break sbml and bives
grails.project.dependency.resolver = "maven"

customJvmArgs = ["-server", "-noverify", "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC" ]
grails.project.fork = [
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
            println "Artifactory URL: " + System.getenv("JUMMP_ARTIFACTORY_URL")
            mavenRepo "${System.getenv('JUMMP_ARTIFACTORY_URL')}"
        }
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo_snapshots/"
        mavenRepo "http://download.eclipse.org/jgit/maven"
        // compass 2.2.1
        mavenRepo "http://repo.grails.org/grails/core"
        mavenRepo "http://www.biojava.org/download/maven/"
        //flatDir name: "jummpLibs", dirs: "lib/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
        // required by OntologyLookupResolver
        compile "org.ccil.cowan.tagsoup:tagsoup:1.2"
        compile "com.googlecode.multithreadedtc:multithreadedtc:1.01"
        runtime 'hsqldb:hsqldb:1.8.0.10'
        runtime 'mysql:mysql-connector-java:5.1.17'
        runtime "postgresql:postgresql:9.1-901.jdbc4"
        // plugin dependencies
        // dependencies of plugins
        // miriam lib required by sbml converters
        runtime('uk.ac.ebi.miriam:miriam-lib:1.1.3') { transitive = false }
        // dependencies of jsbml
        runtime('org.codehaus.woodstox:woodstox-core-lgpl:4.0.9') { excludes 'stax2-api' }
        runtime('org.codehaus.staxmate:staxmate:2.0.0') { excludes 'stax2-api' }
        runtime "org.codehaus.woodstox:stax2-api:3.1.0"
        //compile ('org.apache.lucene:lucene-core:4.4.0')
        //compile ('org.apache.lucene:lucene-analyzers-common:4.4.0')
        //compile ('org.apache.lucene:lucene-queryparser:4.4.0')
        //compile ('org.apache.lucene:lucene-misc:4.4.0')

        compile("org.mbine.co:libCombineArchive:0.1-SNAPSHOT") {
            excludes 'junit', 'slf4j-api', 'slf4j-log4j12', 'jmock-junit4', 'xercesImpl'
        }

        compile("eu.ddmore.pharmml:libPharmML:0.1-SNAPSHOT"){
            excludes 'junit-dep'
        }

        // sbml
        runtime("org.sbml.jsbml:jsbml:1.0-a2") {
            excludes 'woodstox-core-lgpl',
                        'staxmate',
                        'stax2-api',
                        'log4j',
                        'junit',
                        'commons-pool',
                        'commons-dbcp',
                        'xstream'
        }

        // bives
        //runtime('org.apache.commons:commons-compress:1.1') { excludes 'commons-io' }

        /* jms
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
                    'xalan',
                    'xml-apis'
        }*/
        compile "xml-apis:xml-apis:1.4.01"

        compile "org.jdom:jdom:1.1.3"

        runtime("commons-jexl:commons-jexl:1.1") { excludes 'junit', 'commons-logging' }

        //git
        runtime 'org.eclipse.jgit:org.eclipse.jgit:1.2.0.201112221803-r'

        compile("net.sourceforge.cobertura:cobertura:1.9.4.1") { 
            excludes 'asm',
                      'ant',
                      'log4j'
        }
        //weceem, feeds
        runtime("rome:rome:1.0RC2") { excludes 'junit', 'jdom' }

        // cobertura
        compile "asm:asm:3.1"
        compile "log4j:log4j:1.2.16"
        compile "com.thoughtworks.xstream:xstream:1.4.3"

        compile "org.apache.tika:tika-core:1.3"

        // broken Grails 2.3.2 dependecies
        compile("org.spockframework:spock-core:0.7-groovy-2.0") { excludes 'hamcrest-core' }
        compile "org.springframework:spring-test:3.2.4.RELEASE"
    }

    plugins {
        compile ":webxml:1.4.1"
        compile ":perf4j:0.1.1"
        //compile ":jms:1.2"
        compile ":executor:0.3"
        compile(":mail:1.0.1") { excludes 'spring-test' }
        compile ":simple-captcha:0.9.4"
        //compile ":quartz:0.4.2"
        compile(":quartz:1.0-RC6") { excludes 'hibernate-core' /* don't need 3.6.10.Final */ }
        // to see the status of quartz jobs
        //compile(":quartz-monitor:0.2") { export = false } //requires quartz plugin version 0.4.2 

        compile ":spring-security-acl:1.1.1"
        //compile ":svn:1.0.2"
        compile ":spring-security-core:1.2.7.3"
        compile ":spring-security-ldap:1.0.6"
        test ":code-coverage:1.2.6"
        test(":codenarc:0.18.1") { transitive = false }
        test ":gmetrics:0.3.1"
        runtime(":weceem:1.1.3-SNAPSHOT") {
            excludes 'xstream',
                     'quartz',
                     'jquery',
                     'jquery-ui',
                     'ant',     // 1.7 is too old
                     //also exclude java feeds API rome in order to avoid conflicting revisions
                     'feeds',
                     'ckeditor',
                     'searchable' // 0.6.5+ needed for Grails 2.3
        }
        compile(":searchable:0.6.6")
        runtime(":feeds:1.6") { excludes 'rome', 'jdom' }
        runtime(":ckeditor:3.6.3.0") { excludes 'svn' }
        compile ":jquery-datatables:1.7.5"
        compile ":jquery-ui:1.8.24"
        // Locale plugin
        compile ":locale-variant:0.1"
        // default grails plugins
        compile ":hibernate:3.6.10.3"
        compile ":webflow:2.0.8.1"
        compile ":jquery:1.10.2"
        //compile ":resources:1.2"

        build ":tomcat:7.0.42"

    }
}

grails.plugin.location.'jummp-plugin-security' = "jummp-plugins/jummp-plugin-security"
grails.plugin.location.'jummp-plugin-core-api' = "jummp-plugins/jummp-plugin-core-api"
grails.plugin.location.'jummp-plugin-configuration' = "jummp-plugins/jummp-plugin-configuration"
grails.plugin.location.'jummp-plugin-git' = "jummp-plugins/jummp-plugin-git"
// Disconnect SVN for now because of the changes to the VcsManager interface and lack of time
//grails.plugin.location.'jummp-plugin-subversion' = "jummp-plugins/jummp-plugin-subversion"
grails.plugin.location.'jummp-plugin-sbml' = "jummp-plugins/jummp-plugin-sbml"
grails.plugin.location.'jummp-plugin-combine-archive' = "jummp-plugins/jummp-plugin-combine-archive"
grails.plugin.location.'jummp-plugin-pharmml' = "jummp-plugins/jummp-plugin-pharmml"
//grails.plugin.location.'jummp-plugin-bives' = "jummp-plugins/jummp-plugin-bives"
//grails.plugin.location.'jummp-plugin-search' = "jummp-plugins/jummp-plugin-search"
grails.plugin.location.'jummp-plugin-simple-logging' = "jummp-plugins/jummp-plugin-simple-logging"
grails.plugin.location.'jummp-plugin-web-application' = "jummp-plugins/jummp-plugin-web-application"
//grails.plugin.location.'jummp-plugin-jms-remote' = "jummp-plugins/jummp-plugin-jms-remote"
if ("jms".equalsIgnoreCase(System.getenv("JUMMP_EXPORT"))) {
    println "Enabling JMS remoting..."
    grails.plugin.location.'jummp-plugin-remote' = "jummp-plugins/jummp-plugin-remote"
    grails.plugin.location.'jummp-plugin-jms' = "jummp-plugins/jummp-plugin-jms"
} else {
    println "JMS disabled"
}

// Remove any files not needed in production mode
grails.war.resources = { stagingDir ->
}

codenarc.reports = {
    CodeNarcXmlReport('xml') {
        outputFile = 'target/CodeNarc-Report.xml'
        title = "JUMMP CodeNarc Report"
    }
    CodeNarcHtmlReport('html') {
        outputFile = 'target/CodeNarc-Report.html'
        title = "JUMMP CodeNarc Report"
    }
}
codenarc.extraIncludeDirs = ['jummp-plugins/*/src/groovy',
                             'jummp-plugins/*/grails-app/controllers',
                             'jummp-plugins/*/grails-app/domain',
                             'jummp-plugins/*/grails-app/services',
                             'jummp-plugins/*/grails-app/taglib',
                             'jummp-plugins/*/grails-app/utils',
                             'jummp-plugins/*/test/unit',
                             'jummp-plugins/*/test/integration']


//ensure that AST.jar is put in the right place. See scripts/AST.groovy
System.setProperty("jummp.basePath", new File("./").getAbsolutePath())
