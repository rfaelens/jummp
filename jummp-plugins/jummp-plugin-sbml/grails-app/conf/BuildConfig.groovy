grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.groupId = "net.biomodels.jummp.plugins.sbml"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
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
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo_snapshots/"
        mavenRepo "http://www.biojava.org/download/maven/"
        flatDir name: 'jummpPlugins', dirs: "../../pluginlibs"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
        // miriam lib required by sbml converters
        runtime('uk.ac.ebi.miriam:miriam-lib:1.1.3') { transitive = false }
        // dependencies of jsbml
        compile("org.sbml.jsbml:jsbml:1.0-SNAPSHOT") {
            excludes 'woodstox-core-lgpl',
                        'staxmate',
                        'stax2-api',
                        'log4j',
                        'junit',
                        'commons-pool',
                        'commons-dbcp'
        }
        runtime('org.codehaus.woodstox:woodstox-core-lgpl:4.0.9') { excludes 'stax2-api' }
        runtime('org.codehaus.staxmate:staxmate:2.0.0') { excludes 'stax2-api' }
        runtime "org.codehaus.woodstox:stax2-api:3.1.0"
        compile ":sbfc:1.1-20110624-109"

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
