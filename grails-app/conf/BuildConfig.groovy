grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        flatDir name: 'jummpPlugins', dirs: "./pluginlibs"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        mavenRepo "http://download.eclipse.org/jgit/maven"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
        // required by OntologyLookupResolver
        compile "org.ccil.cowan.tagsoup:tagsoup:1.2"
        runtime 'hsqldb:hsqldb:1.8.0.10'
        runtime 'mysql:mysql-connector-java:5.1.17'
        // plugin dependencies
        compile(":grails-plugin-jummp-plugin-security:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-core-api:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-git:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-subversion:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-configuration:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-sbml:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-bives:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-remote:latest.integration") {
            changing = true
        }
        runtime(":grails-plugin-jummp-plugin-dbus:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-simple-logging:latest.integration") {
            changing = true
        }
        // dependencies of plugins
        // sbml
        runtime ":jsbml:0.8-b2"
        runtime ":sbfc:1.1-20110624-109"
        runtime ":biojava-ontology:1.7"
        // miriam lib required by sbml converters
        runtime 'uk.ac.ebi.miriam:miriam-lib:1.1.2'
        // dependencies of jsbml
        runtime 'org.codehaus.woodstox:woodstox-core-lgpl:4.0.9'
        runtime 'org.codehaus.staxmate:staxmate:2.0.0'
        runtime 'org.w3c.jigsaw:jigsaw:2.2.6'
        runtime 'com.thoughtworks.xstream:xstream:1.3.1'

        // bives
        runtime ":jaxen:1.1.1"
        runtime ":jdom:1.1.1"
        runtime ":bives-fwk:0.9.0"
        runtime ":bives.diff:0.1.0"
        runtime 'org.apache.commons:commons-compress:1.1'

        // jms
        runtime('org.apache.activemq:activeio-core:3.1.2',
                'org.apache.activemq:activemq-core:5.5.0',
                'org.apache.activemq:activemq-spring:5.5.0',
                'org.apache.xbean:xbean-spring:3.7') {
            excludes 'commons-logging',
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
        }
        runtime "commons-jexl:commons-jexl:1.1"

        // dbus
        runtime ":dbus:2.7"
        runtime ":debug-disable:1.1"
        runtime ":hexdump:0.2"
        provided ":unix:0.5"

        //git
        runtime 'org.eclipse.jgit:org.eclipse.jgit:1.0.0.201106090707-r'
    }

    plugins {
        compile ":perf4j:0.1.1"
        compile ":jms:1.2"
        compile ":executor:0.3"
        compile ":mail:1.0"
        compile ":quartz:0.4.2"
        compile ":spring-security-acl:1.1"
        compile ":svn:1.0.0.M1"
        runtime ":spring-security-core:1.2.1"
        runtime ":spring-security-ldap:1.0.5"
        test ":code-coverage:1.2.4"
        test ":codenarc:0.16.1"
        test ":gmetrics:0.3.1"

        // default grails plugins
        compile ":hibernate:$grailsVersion"
        compile ":webflow:$grailsVersion"
        compile ":jquery:1.6.1.1"
        //compile ":resources:1.0.2"

        build ":tomcat:$grailsVersion"

    }
}

grails.plugin.location.'jummp-plugin-jms' = "jummp-plugins/jummp-plugin-jms"

// Remove libraries not needed in productive mode
grails.war.resources = { stagingDir ->
  // need to remove unix socket JNI library as incompatible with placing inside web-app
  delete(file:"${stagingDir}/WEB-INF/lib/unix-0.5.jar")
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
                             'jummp-plugins/*/test/integration',
                             'jummp-web-application/src/groovy',
                             'jummp-web-application/grails-app/controllers',
                             'jummp-web-application/grails-app/domain',
                             'jummp-web-application/grails-app/services',
                             'jummp-web-application/grails-app/taglib',
                             'jummp-web-application/grails-app/utils',
                             'jummp-web-application/test/unit',
                             'jummp-web-application/test/integration']
