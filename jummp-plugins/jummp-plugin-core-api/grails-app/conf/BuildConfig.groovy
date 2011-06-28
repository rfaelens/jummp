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
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
        // perf4j dependency
        compile "org.perf4j:perf4j:0.9.13"
        runtime "commons-jexl:commons-jexl:1.1"
    }
}

// depending on whether a war is generated or test-app is executed the path to the dependency plugin differs
File directory = new File(".")
String path = directory.getCanonicalPath()
if (path.tokenize(File.separatorChar).last() == "jummp") {
    path = "../../jummp-plugins"
} else {
    // are in plugin directory
    path = ".."
}
grails.plugin.location.'jummp-plugin-security' = path + File.separator + "jummp-plugin-security"
