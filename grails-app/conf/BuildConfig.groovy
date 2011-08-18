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
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
        // required by OntologyLookupResolver
        compile "org.ccil.cowan.tagsoup:tagsoup:1.2"
        test 'hsqldb:hsqldb:1.8.0.10'
        // plugin dependencies
        compile ":grails-plugin-jummp-plugin-security:0.1"
        compile ":grails-plugin-jummp-plugin-core-api:0.1"
        compile ":grails-plugin-jummp-plugin-git:0.1"
        compile ":grails-plugin-jummp-plugin-subversion:0.1"
        compile ":grails-plugin-jummp-plugin-configuration:0.1"
        compile ":grails-plugin-jummp-plugin-sbml:0.1"
        compile ":grails-plugin-jummp-plugin-bives:0.1"
        compile ":grails-plugin-jummp-plugin-remote:0.1"
        runtime ":grails-plugin-jummp-plugin-dbus:0.1"
        compile ":grails-plugin-jummp-plugin-simple-logging:0.1"
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
        runtime "org.apache.activemq:activemq-all:5.5.0"
        runtime "commons-jexl:commons-jexl:1.1"

        // dbus
        runtime ":dbus:2.7"
        runtime ":debug-disable:1.1"
        runtime ":hexdump:0.2"
        runtime ":unix:0.5"

        //git
        runtime 'org.eclipse.jgit:org.eclipse.jgit:1.0.0.201106090707-r'
    }

    plugins {
        compile ":perf4j:0.1.1"
        compile ":jms:1.1"
        runtime ":spring-security-core:1.2"
    }
}

grails.plugin.location.'jummp-plugin-jms' = "jummp-plugins/jummp-plugin-jms"

// Remove libraries not needed in productive mode
grails.war.resources = { stagingDir ->
  // need to remove unix socket JNI library as incompatible with placing inside web-app
  delete(file:"${stagingDir}/WEB-INF/lib/unix-0.5.jar")
}
