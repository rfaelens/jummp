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
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }
}
if (new File("jummp-plugins/jummp-plugin-subversion").exists()) {
    grails.plugin.location.'jummp-plugin-subversion' = "jummp-plugins/jummp-plugin-subversion"
}
if (new File("jummp-plugins/jummp-plugin-git").exists()) {
    grails.plugin.location.'jummp-plugin-git' = "jummp-plugins/jummp-plugin-git"
}
if (new File("jummp-plugins/jummp-plugin-configuration").exists()) {
    grails.plugin.location.'jummp-plugin-configuration' = "jummp-plugins/jummp-plugin-configuration"
}
if (new File("jummp-plugins/jummp-plugin-sbml").exists()) {
    grails.plugin.location.'jummp-plugin-sbml' = "jummp-plugins/jummp-plugin-sbml"
}
grails.plugin.location.'jummp-plugin-remote' = "jummp-plugins/jummp-plugin-remote"
grails.plugin.location.'jummp-plugin-core-api' = "jummp-plugins/jummp-plugin-core-api"
grails.plugin.location.'jummp-plugin-security' = "jummp-plugins/jummp-plugin-security"
if (new File("jummp-plugins/jummp-plugin-dbus").exists()) {
    grails.plugin.location.'jummp-plugin-dbus' = "jummp-plugins/jummp-plugin-dbus"
}
if (new File("jummp-plugins/jummp-plugin-jms").exists()) {
    grails.plugin.location.'jummp-plugin-jms' = "jummp-plugins/jummp-plugin-jms"
}
grails.plugin.location.'jummp-plugin-simple-logging' = "jummp-plugins/jummp-plugin-simple-logging"

// Remove libraries not needed in productive mode
grails.war.resources = { stagingDir ->
  delete(file:"${stagingDir}/WEB-INF/lib/hsqldb-1.8.0.10.jar")
}
