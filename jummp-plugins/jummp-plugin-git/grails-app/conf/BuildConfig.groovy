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
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://download.eclipse.org/jgit/maven"
        flatDir name: 'jummpPlugins', dirs: "../../pluginlibs"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
        compile 'org.eclipse.jgit:org.eclipse.jgit:1.2.0.201112221803-r'
        // plugin dependencies
        compile(":grails-plugin-jummp-plugin-security:latest.integration") {
            changing = true
        }
        compile(":grails-plugin-jummp-plugin-core-api:latest.integration") {
            changing = true
        }
    }
    plugins {
        compile ":spring-security-core:1.2.7.2"
        test ":code-coverage:1.2.5"

        // default grails plugins
        compile ":hibernate:$grailsVersion"
        compile ":jquery:1.6.1.1"
        //compile ":resources:1.0.2"

        build ":tomcat:$grailsVersion"
    }
}
