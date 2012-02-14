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
        // repository for miriam lib
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        flatDir name: 'jummpPlugins', dirs: "../../pluginlibs"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
        // miriam lib required by sbml converters
        runtime 'uk.ac.ebi.miriam:miriam-lib:1.1.3'
        // dependencies of jsbml
        runtime 'org.codehaus.woodstox:woodstox-core-lgpl:4.0.9'
        runtime 'org.codehaus.staxmate:staxmate:2.0.0'
        runtime 'org.w3c.jigsaw:jigsaw:2.2.6'
        runtime 'com.thoughtworks.xstream:xstream:1.3.1'
        compile ":jsbml:0.8-rc1"
        compile ":sbfc:1.1-20110624-109"
        compile ":biojava-ontology:1.7"

        // plugin dependencies
        compile(":grails-plugin-jummp-plugin-core-api:latest.integration") {
            changing = true
        }
    }

    plugins {
        compile ":perf4j:0.1.1"
        compile ":spring-security-core:1.2.7.2"
        test ":code-coverage:1.2.5"

        // default grails plugins
        compile ":hibernate:$grailsVersion"
        compile ":jquery:1.6.1.1"
        //compile ":resources:1.0.2"

        build ":tomcat:$grailsVersion"
    }
}
