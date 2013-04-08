grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/${appName}.war"
grails.project.groupId = "net.biomodels.jummp"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        if (System.getenv("JUMMP_ARTIFACTORY_URL")) {
            println "Artifactory URL: " + System.getenv("JUMMP_ARTIFACTORY_URL")
            mavenRepo "${System.getenv('JUMMP_ARTIFACTORY_URL')}"
        }
        if (System.getenv("JUMMP_ARTIFACTORY_GRAILS_PLUGINS_URL")) {
            println "Grails Repo URL: " + System.getenv("JUMMP_ARTIFACTORY_GRAILS_PLUGINS_URL")
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
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo"
        mavenRepo "http://www.ebi.ac.uk/~maven/m2repo_snapshots/"
        mavenRepo "http://download.eclipse.org/jgit/maven"
        mavenRepo "http://www.biojava.org/download/maven/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
        // required by OntologyLookupResolver
        compile "org.ccil.cowan.tagsoup:tagsoup:1.2"
        runtime 'hsqldb:hsqldb:1.8.0.10'
        runtime 'mysql:mysql-connector-java:5.1.17'
        runtime "postgresql:postgresql:9.1-901.jdbc4"
        // plugin dependencies
        // dependencies of plugins
        // sbml
        runtime("org.sbml.jsbml:jsbml:1.0-SNAPSHOT") {
            excludes 'woodstox-core-lgpl',
                        'staxmate',
                        'stax2-api',
                        'log4j',
                        'junit',
                        'commons-pool',
                        'commons-dbcp'
        }
        // miriam lib required by sbml converters
        runtime('uk.ac.ebi.miriam:miriam-lib:1.1.3') { transitive = false }
        // dependencies of jsbml
        runtime('org.codehaus.woodstox:woodstox-core-lgpl:4.0.9') { excludes 'stax2-api' }
        runtime('org.codehaus.staxmate:staxmate:2.0.0') { excludes 'stax2-api' }
        runtime "org.codehaus.woodstox:stax2-api:3.1.0"

        // bives
        runtime 'org.apache.commons:commons-compress:1.1'

        /*
         * grails dependency-report still lists version 1.6.2 as a dependency of JUMMP
         * although we don't actually explicitly require that version anywhere. It seems
         * to be a transitive dependency of ehcache-core, which is required by hibernate-ehcache,
         * which, in turn, is needed by hibernate.
         */ 
        //compile 'org.slf4j:slf4j-api:1.6.1' 
        // jms
        /*runtime('org.apache.activemq:activeio-core:3.1.2',
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
        }*/
        runtime("commons-jexl:commons-jexl:1.1") { excludes 'junit' }

        //git
        runtime 'org.eclipse.jgit:org.eclipse.jgit:1.2.0.201112221803-r'
      
        compile("net.sourceforge.cobertura:cobertura:1.9.4.1") { 
            excludes 'asm',
                      'ant',
                      'log4j'
        }

        // cobertura
        compile "asm:asm:3.1"
        compile "log4j:log4j:1.2.16"
    }

    plugins {
        compile ":perf4j:0.1.1"
        //compile ":jms:1.2"
        compile ":executor:0.3"
        compile ":mail:1.0"
        //compile ":quartz:0.4.2"
        compile(":quartz:1.0-RC6") { excludes 'hibernate-core' /* don't need 3.6.10.Final */ }
        // to see the status of quartz jobs
        //compile(":quartz-monitor:0.2") { export = false } //requires quartz plugin version 0.4.2 

        compile ":spring-security-acl:1.1"
        compile ":svn:1.0.2"
        runtime ":spring-security-core:1.2.7.2"
        runtime(":spring-security-ldap:1.0.5") { export  = false }
        compile ":lesscss:1.0.0"
        test ":code-coverage:1.2.5"
        test(":codenarc:0.16.1") { transitive = false }
        test ":gmetrics:0.3.1"
        compile(":weceem:1.1.2") { 
            excludes 'xstream', 
                        'quartz',
                        'jquery', 
                        'jquery-ui' 
        }
        compile ":jquery-datatables:1.7.5"
        compile ":jquery-ui:1.8.15"

        // default grails plugins
        compile ":hibernate:$grailsVersion"
        compile ":webflow:2.0.0"
        compile ":jquery:1.6.1.1"
        compile ":resources:1.1.6"

        build ":tomcat:$grailsVersion"

    }
}

grails.plugin.location.'jummp-plugin-security' = "jummp-plugins/jummp-plugin-security"
grails.plugin.location.'jummp-plugin-core-api' = "jummp-plugins/jummp-plugin-core-api"
grails.plugin.location.'jummp-plugin-configuration' = "jummp-plugins/jummp-plugin-configuration"
grails.plugin.location.'jummp-plugin-git' = "jummp-plugins/jummp-plugin-git"
grails.plugin.location.'jummp-plugin-subversion' = "jummp-plugins/jummp-plugin-subversion"
grails.plugin.location.'jummp-plugin-sbml' = "jummp-plugins/jummp-plugin-sbml"
grails.plugin.location.'jummp-plugin-bives' = "jummp-plugins/jummp-plugin-bives"
grails.plugin.location.'jummp-plugin-remote' = "jummp-plugins/jummp-plugin-remote"
grails.plugin.location.'jummp-plugin-dbus' = "jummp-plugins/jummp-plugin-dbus"
grails.plugin.location.'jummp-plugin-simple-logging' = "jummp-plugins/jummp-plugin-simple-logging"
grails.plugin.location.'jummp-plugin-web-application' = "jummp-plugins/jummp-plugin-web-application"
//grails.plugin.location.'jummp-plugin-jms-remote' = "jummp-plugins/jummp-plugin-jms-remote"
//grails.plugin.location.'jummp-plugin-jms' = "jummp-plugins/jummp-plugin-jms"

// Remove libraries not needed in production mode
grails.war.resources = { stagingDir ->
  // need to remove unix socket JNI library as incompatible with placing inside web-app
  // 2013-03-19 Mihai Glont: jummp-loader works fine with unix-0.5.jar in the war's WEB-INF/lib
  // delete(file:"${stagingDir}/WEB-INF/lib/unix-0.5.jar")
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

grails.tomcat.jvmArgs = ["-Xmx2G", "-XX:MaxPermSize=512M", "-XX:-UseGCOverheadLimit", "-server", "-XX:+UseParallelGC", "-XX:ParallelGCThreads=8"]

//ensure that AST.jar is put in the right place. See scripts/AST.groovy
System.setProperty("jummp.basePath", new File("./").getAbsolutePath())
