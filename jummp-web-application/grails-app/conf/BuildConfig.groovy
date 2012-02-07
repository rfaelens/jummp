grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
// Relative path to JUMMP core application - this can be adjusted by users if they don't use the reference filesystem layout
grails.project.jummp.dir = "../"
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
        flatDir name: 'jummpPlugins', dirs: "${grails.project.jummp.dir}/pluginlibs"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
        // plugin dependencies
        compile(":grails-plugin-jummp-plugin-security:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-core-api:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-remote:latest.integration") {
            changing = true
        }
        runtime(":grails-plugin-jummp-plugin-dbus:latest.integration") {
            changing = true
        }

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
    }

    plugins {
        compile ":spring-security-core:1.2.7"
        runtime ":spring-security-ldap:1.0.5"
        compile ":jquery-ui:1.8.11"
        compile ":jquery-datatables:1.7.5"
        compile ":perf4j:0.1.1"
        compile ":jms:1.2"

        // default grails plugins
        compile ":hibernate:$grailsVersion"
        compile ":jquery:1.6.1.1"
        //compile ":resources:1.0.2"

        build ":tomcat:$grailsVersion"
    }
}

grails.plugin.location.'jummp-plugin-jms-remote' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-jms-remote"

// Remove libraries not needed in productive mode
grails.war.resources = { stagingDir ->
  // need to remove unix socket JNI library as incompatible with placing inside web-app
  delete(file:"${stagingDir}/WEB-INF/lib/unix-0.5.jar")
}
