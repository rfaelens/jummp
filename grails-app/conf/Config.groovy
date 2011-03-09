import org.codehaus.groovy.grails.compiler.GrailsClassLoader

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

Properties jummpProperties = new Properties()
try {
    jummpProperties.load(new FileInputStream(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties"))
} catch (Exception e) {
    jummpProperties.setProperty("jummp.security.ldap.enabled", "false")
    jummpProperties.setProperty("jummp.security.registration.email.send", "false")
}
def jummpConfig = new ConfigSlurper().parse(jummpProperties)
List pluginsToExclude = []

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

grails.views.javascript.library="jquery"

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = jummpConfig.jummp.server.url
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
    appenders {
        // file appender that writes out the URLs of the Google Chart API graphs generated by the performanceGraphAppender
        def performanceGraphFileAppender = new org.apache.log4j.FileAppender(
            fileName: "log/perfGraphs.log",
            layout: pattern(conversionPattern: '%m%n')
        )
        appender name: 'performanceGraphFileAppender', performanceGraphFileAppender

        // this appender creates the Google Chart API graphs
        def performanceGraphAppender = new org.perf4j.log4j.GraphingStatisticsAppender(
            graphType: 'Mean',      // possible options: Mean, Min, Max, StdDev, Count or TPS
            tagNamesToGraph: 'tag1,tag2,tag3',
            dataPointsPerGraph: 5
        )
        performanceGraphAppender.addAppender(performanceGraphFileAppender)
        appender name: 'performanceGraph', performanceGraphAppender


        // file appender that writes out the textual, aggregated performance stats generated by the performanceStatsAppender
        def performanceStatsFileAppender = new org.apache.log4j.FileAppender(
            fileName: "log/perfStats.log",
            layout: pattern(conversionPattern: '%m%n')  // alternatively use the StatisticsCsvLayout to generate CSV
        )
        appender name: 'performanceStatsFileAppender', performanceStatsFileAppender


        // this is the most important appender and first in the appender chain. it aggregates all profiling data withing a certain time frame.
        // the GraphingStatisticsAppender is attached as a child to this appender and uses its aggregated data.
        def performanceStatsAppender = new org.perf4j.log4j.AsyncCoalescingStatisticsAppender(
            timeSlice: 10000    // ms
        )
        performanceStatsAppender.addAppender(performanceStatsFileAppender)
        performanceStatsAppender.addAppender(performanceGraphAppender)
        appender name: 'performanceStatsAppender', performanceStatsAppender
    }

    // configure the performanceStatsAppender to log at INFO level
    info   performanceStatsAppender: 'org.perf4j.TimingLogger'
}

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'net.biomodels.jummp.plugins.security.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'net.biomodels.jummp.plugins.security.UserRole'
grails.plugins.springsecurity.authority.className = 'net.biomodels.jummp.plugins.security.Role'

grails.plugins.springsecurity.controllerAnnotations.staticRules = [
        // protect the springsecurity ui plugin controllers
        '/aclclass/**':          ['ROLE_ADMIN'],
        '/aclentry/**':          ['ROLE_ADMIN'],
        '/aclobjectidentity/**': ['ROLE_ADMIN'],
        '/aclsid/**':            ['ROLE_ADMIN'],
        '/persistentlogin/**':   ['ROLE_ADMIN'],
        '/register/**':          ['ROLE_ADMIN'],
        '/registrationcode/**':  ['ROLE_ADMIN'],
        '/requestmap/**':        ['ROLE_ADMIN'],
        '/role/**':              ['ROLE_ADMIN'],
        '/securityinfo/**':      ['ROLE_ADMIN'],
        '/user/**':              ['ROLE_ADMIN']
]

// ldap
if ((jummpConfig.jummp.security.ldap.enabled instanceof ConfigObject) || !Boolean.parseBoolean(jummpConfig.jummp.security.ldap.enabled)) {
    jummp.security.ldap.enabled = false
    println("Excluding ldap")
    pluginsToExclude << "spring-security-ldap"
} else {
    println("using ldap")
    jummp.security.ldap.enabled = true
    grails.plugins.springsecurity.ldap.context.managerDn         = jummpConfig.jummp.security.ldap.managerDn
    grails.plugins.springsecurity.ldap.context.managerPassword   = jummpConfig.jummp.security.ldap.managerPw
    grails.plugins.springsecurity.ldap.context.server            = jummpConfig.jummp.security.ldap.server
    grails.plugins.springsecurity.ldap.search.base               = jummpConfig.jummp.security.ldap.search.base
    grails.plugins.springsecurity.ldap.authorities.searchSubtree = jummpConfig.jummp.security.ldap.search.subTree
    grails.plugins.springsecurity.ldap.search.filter             = jummpConfig.jummp.security.ldap.search.filter

    // static options
    grails.plugins.springsecurity.ldap.authorities.ignorePartialResultException = true
    grails.plugins.springsecurity.ldap.authorities.retrieveGroupRoles = true
    grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
}

// version control backend
if (jummpConfig.jummp.vcs.exchangeDirectory) {
    jummp.vcs.exchangeDirectory = jummpConfig.jummp.vcs.exchangeDirectory
}
if (jummpConfig.jummp.vcs.workingDirectory) {
    jummp.vcs.workingDirectory = jummpConfig.jummp.vcs.workingDirectory
}

// registration settings
if (!(jummpConfig.jummp.security.registration.email.send instanceof ConfigObject) && Boolean.parseBoolean(jummpConfig.jummp.security.registration.email.send)) {
    jummp.security.registration.email.send         = Boolean.parseBoolean(jummpConfig.jummp.security.registration.email.send)
    jummp.security.registration.email.sender       = jummpConfig.jummp.security.registration.email.sender
    if (!(jummpConfig.jummp.security.registration.email.sendToAdmin instanceof ConfigObject)) {
        jummp.security.registration.email.sendToAdmin = Boolean.parseBoolean(jummpConfig.jummp.security.registration.email.sendToAdmin)
    } else {
        jummp.security.registration.email.sendToAdmin = false
    }
    jummp.security.registration.email.adminAddress = jummpConfig.jummp.security.registration.email.adminAddress
    jummp.security.registration.email.subject      = jummpConfig.jummp.security.registration.email.subject
    jummp.security.registration.email.body         = jummpConfig.jummp.security.registration.email.body
    jummp.security.registration.verificationURL    = jummpConfig.jummp.security.registration.verificationURL
} else {
    jummp.security.registration.email.send = false
}

// whether a user is allowed to change the password depends on the setting an if LDAP is used
// in case of LDAP changing the password is not (yet) possible in the application
if (!(jummpConfig.jummp.security.ui.changePassword instanceof ConfigObject)) {
    jummp.security.ui.changePassword = Boolean.parseBoolean(jummpConfig.jummp.security.ui.changePassword)
} else {
    // default to true
    jummp.security.ui.changePassword = true
}
if (jummp.security.ldap.enabled) {
    // as long as our LDAP implementation does not support changing passwords we need to disable
    jummp.security.ui.changePassword = false
}

// In case of LDAP there is no need to allow users to register with a password as we cannot (yet) add anything to the LDAP
jummp.security.registration.ui.userPassword = !jummp.security.ldap.enabled

// get all Plugin Configurations
// the list of available plugins is read from the BuildConfig's plugin location
// for each plugin it is assumed that it has a JummpPluginConfig class in the package
// net.biomodels.jummp.plugins.${short-name-of-plugin}. "short-name-of-plugin" is the
// part of the plugin name without "jummp-plugin-". The JummpPluginConfig class needs
// to provide a closure configure with takes two ConfigObjects as arguments. The first
// is the ConfigObject which is just constructed, that is "jummp", the second is the
// ConfigObject containing the externalized configuration.
GrailsClassLoader classLoader = new GrailsClassLoader()
ConfigSlurper slurper = new ConfigSlurper()
ConfigObject buildConfig = slurper.parse(classLoader.loadClass("BuildConfig"))
buildConfig.grails.plugin.location.each { key, value ->
    String pluginName = key.minus("jummp-plugin-")
    try {
        def pluginConfigClass = classLoader.loadClass("net.biomodels.jummp.plugins.${pluginName}.JummpPluginConfig")
        try {
            pluginConfigClass.configure(jummp, jummpConfig)
        } catch (MissingMethodException e) {
            println "Plugin ${pluginName} does not provide the configure closure"
        }
    } catch (ClassNotFoundException e) {
        println "Plugin ${pluginName} does not provide a configuration"
    }
}

environments {
    test {
        // need to disable the plugins or tests may fail
        // if needed in the tests, mockConfig should be used
        jummp.plugins.subversion.enabled = false
        jummp.plugins.git.enabled = false
        // disable registration mail sending
        jummp.security.registration.email.send = false
    }
}

if (pluginsToExclude) {
    grails.plugin.exclude = pluginsToExclude
}

// Codenarc settings
codenarc.extraIncludeDirs = ['jummp-plugins/*/src/groovy',
                             'jummp-plugins/*/grails-app/controllers',
                             'jummp-plugins/*/grails-app/domain',
                             'jummp-plugins/*/grails-app/services',
                             'jummp-plugins/*/grails-app/taglib',
                             'jummp-plugins/*/grails-app/utils',
                             'jummp-plugins/*/test/unit',
                             'jummp-plugins/*/test/integration']
// fails the ant test script
grails.enable.native2ascii=false
