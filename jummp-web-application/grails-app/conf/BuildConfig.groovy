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
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
    }
}
Properties jummpProperties = new Properties()
try {
    jummpProperties.load(new FileInputStream(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties"))
} catch (Exception e) {
    // ignore
}
grails.plugin.location.'jummp-plugin-ast' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-ast"
if (new File("${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-dbus").exists()) {
    grails.plugin.location.'jummp-plugin-dbus' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-dbus"
}
if (new File("${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-jms-remote").exists()) {
    grails.plugin.location.'jummp-plugin-jms-remote' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-jms-remote"
}
grails.plugin.location.'jummp-plugin-remote' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-remote"
grails.plugin.location.'jummp-plugin-security' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-security"
grails.plugin.location.'jummp-plugin-core-api' = "${grails.project.jummp.dir}/jummp-plugins/jummp-plugin-core-api"

// Remove libraries not needed in productive mode
grails.war.resources = { stagingDir ->
  delete(file:"${stagingDir}/WEB-INF/lib/hsqldb-1.8.0.10.jar")
  // need to remove unix socket JNI library as incompatible with placing inside web-app
  delete(file:"${stagingDir}/WEB-INF/lib/unix-0.5.jar")
}
