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
    jummpProperties.setProperty("jummp.export.dbus", "false")
    jummpProperties.setProperty("jummp.server.url", "http://localhost:8080/${appName}")
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

    appenders {
        // file appender that writes out the URLs of the Google Chart API graphs generated by the performanceGraphAppender
        def performanceGraphFileAppender = new org.apache.log4j.FileAppender(
            fileName: "logs/perfGraphs.log",
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
            fileName: "logs/perfStats.log",
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

        rollingFile name: "jummpAppender", file: "logs/jummp-core.log", threshold: org.apache.log4j.Level.WARN
        rollingFile name: "eventsAppender", file: "logs/jummp-events.log", threshold: org.apache.log4j.Level.INFO

        // change the threshold to DEBUG to have debug output in development modus
        console name: "stdout", threshold: org.apache.log4j.Level.ERROR
    }

    // configure the performanceStatsAppender to log at INFO level
    info   performanceStatsAppender: 'org.perf4j.TimingLogger'
    warn  jummpAppender: [
            'grails.app', //everything provided by grails-app, e.g. services
            'net.biomodels.jummp' // everything from jummp
    ]
    // Simple Logging goes to its own file
    info   eventsAppender: 'net.biomodels.jummp.plugins.simplelogging'

    error  jummpAppender: [
            'org.codehaus.groovy.grails.web.servlet',  //  controllers
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
    ]

    warn   jummpAppender: 'org.mortbay.log'
}

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'net.biomodels.jummp.plugins.security.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'net.biomodels.jummp.plugins.security.UserRole'
grails.plugins.springsecurity.authority.className = 'net.biomodels.jummp.plugins.security.Role'

jummp.controllerAnnotations = [
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
        '/user/**':              ['ROLE_ADMIN'],
        '/wcm-tools/**':         ['ROLE_ADMIN'],
        '/searchable/**':        ['ROLE_ADMIN'],
        '/ck/**':                ['ROLE_ADMIN'],
        "/wcmEditor/**":         ["hasRole('ROLE_ADMIN')"],
        "/wcmPortal/**":         ["hasRole('ROLE_ADMIN')"],
        "/wcmRepository/**":     ["hasRole('ROLE_ADMIN')"],
        "/wcmSpace/**":          ["hasRole('ROLE_ADMIN')"],
        "/wcmSynchronization/**": ["hasRole('ROLE_ADMIN')"],
        "/wcmVersion/**":        ["hasRole('ROLE_ADMIN')"],
        "/wcm*/**":              ["permitAll"],
        "/WeceemFiles/**":       ["permitAll"],
        "/css/**":               ["permitAll"],
        "/images/**":            ["permitAll"],
        "/js/**":                ["permitAll"],
        "/plugins/jquery*/**":   ["permitAll"],
        "/plugins/navigation*/**": ["permitAll"],
        "/plugins/blueprint*/**": ["permitAll"],
        "/plugins/ckeditor*/**":  ["permitAll"],
        "/plugins/weceem*/**":    ["permitAll"]
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
    jummp.security.activation.email.subject        = jummpConfig.jummp.security.activation.email.subject
    jummp.security.activation.email.body           = jummpConfig.jummp.security.activation.email.body
    jummp.security.activation.activationURL        = jummpConfig.jummp.security.activation.activationURL
} else {
    jummp.security.registration.email.send = false
}

// reset password settings
if (!(jummpConfig.jummp.security.resetPassword.email.send instanceof ConfigObject) && Boolean.parseBoolean(jummpConfig.jummp.security.resetPassword.email.send)) {
    jummp.security.resetPassword.email.send    = Boolean.parseBoolean(jummpConfig.jummp.security.resetPassword.email.send)
    jummp.security.resetPassword.email.sender  = jummpConfig.jummp.security.resetPassword.email.sender
    jummp.security.resetPassword.email.body    = jummpConfig.jummp.security.resetPassword.email.body
    jummp.security.resetPassword.email.subject = jummpConfig.jummp.security.resetPassword.email.subject
    jummp.security.resetPassword.url           = jummpConfig.jummp.security.resetPassword.url
} else {
    jummp.security.resetPassword.email.send = false
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

if (!(jummpConfig.jummp.export.dbus instanceof ConfigObject) && Boolean.parseBoolean(jummpConfig.jummp.export.dbus)) {
    jummp.plugin.dbus.export = true
} else {
    jummp.plugin.dbus.export = false
}

// In case of LDAP there is no need to allow users to register with a password as we cannot (yet) add anything to the LDAP
jummp.security.registration.ui.userPassword = !jummp.security.ldap.enabled

// whether users are allowed to register themselves or not.
// if not only an administrator can create a new user account
// default to users can register themselves
if (!(jummpConfig.jummp.security.anonymousRegistration instanceof ConfigObject)) {
    jummp.security.anonymousRegistration = Boolean.parseBoolean(jummpConfig.jummp.security.anonymousRegistration)
} else {
    jummp.security.anonymousRegistration = true
}

// For the job, removing authentication hashes that are unused for a configurable time
// Used by AuthenticationHashService
if (!(jummpConfig.jummp.authenticationHash.startRemoveOffset instanceof ConfigObject)) {
    jummp.authenticationHash.startRemoveOffset = Long.parseLong(jummpConfig.jummp.authenticationHash.startRemoveOffset)
} else {
    jummp.authenticationHash.startRemoveOffset = 5*60*1000
}
if (!(jummpConfig.jummp.authenticationHash.removeInterval instanceof ConfigObject)) {
    jummp.authenticationHash.removeInterval = Long.parseLong(jummpConfig.jummp.authenticationHash.removeInterval)
} else {
    jummp.authenticationHash.removeInterval = 30*60*1000
}
if (!(jummpConfig.jummp.authenticationHash.maxInactiveTime instanceof ConfigObject)) {
    jummp.authenticationHash.maxInactiveTime = jummpConfig.jummp.authenticationHash.maxInactiveTime
} else {
    jummp.authenticationHash.maxInactiveTime = 30*60*1000
}

if (!(jummpConfig.jummp.threadPool.size instanceof ConfigObject)) {
    jummp.threadPool.size = jummpConfig.jummp.threadPool.size as Integer
} else {
    jummp.threadPool.size = 10
}

if (!(jummpConfig.model.history.maxElements instanceof ConfigObject)) {
    jummp.model.history.maxElements = jummpConfig.model.history.maxElements as Integer
} else {
    jummp.model.history.maxElements = 10
}

// For the appearance of the web front-end defines the color for internal usage
if (!(jummpConfig.jummp.branding.internalColor instanceof ConfigObject)) {
    jummp.branding.internalColor = jummpConfig.jummp.branding.internalColor
}
// For the appearance of the web front-end defines the color for external usage
if (!(jummpConfig.jummp.branding.externalColor instanceof ConfigObject)) {
    jummp.branding.externalColor = jummpConfig.jummp.branding.externalColor
}
// The type of the database server
if (!(jummpConfig.jummp.database.type instanceof ConfigObject)) {
    jummp.database.type = jummpConfig.jummp.database.type
} else {
    jummp.database.type = "MYSQL"
}
// The location of the database server
if (!(jummpConfig.jummp.database.server instanceof ConfigObject)) {
    jummp.database.server = jummpConfig.jummp.database.server
} else {
    jummp.database.server = "localhost"
}
// The port of the database server
if (!(jummpConfig.jummp.database.port instanceof ConfigObject)) {
    jummp.database.port = jummpConfig.jummp.database.port as Integer
} else {
    jummp.database.port = 3306
}
// The name of the database
if (!(jummpConfig.jummp.database.database instanceof ConfigObject)) {
    jummp.database.database = jummpConfig.jummp.database.database
} else {
    jummp.database.database = "jummp"
}
// The user of the database server
if (!(jummpConfig.jummp.database.username instanceof ConfigObject)) {
    jummp.database.username = jummpConfig.jummp.database.username
} else {
    jummp.database.username = "jummp"
}
// The user's password of the database server
if (!(jummpConfig.jummp.database.password instanceof ConfigObject)) {
    jummp.database.password = jummpConfig.jummp.database.password
} else {
    jummp.database.password = "jummp"
}

if (jummpConfig.jummp.firstRun instanceof ConfigObject || !Boolean.parseBoolean(jummpConfig.jummp.firstRun)) {
    // only add side protection if not in first run mode
    if (!(jummpConfig.jummp.server.protection instanceof ConfigObject) && Boolean.parseBoolean(jummpConfig.jummp.server.protection)) {
        jummp.controllerAnnotations.put("/login/**", ['IS_AUTHENTICATED_ANONYMOUSLY'])
        jummp.controllerAnnotations.put("/**", ['ROLE_USER'])
    }
}

// get all Plugin Configurations
// the list of available plugins is read from the BuildConfig's plugin location
// for each plugin it is assumed that it has a JummpPluginConfig class in the package
// net.biomodels.jummp.plugins.${short-name-of-plugin}. "short-name-of-plugin" is the
// part of the plugin name without "jummp-plugin-". The JummpPluginConfig class needs
// to provide a closure configure with takes two ConfigObjects as arguments. The first
// is the ConfigObject which is just constructed, that is "jummp", the second is the
// ConfigObject containing the externalized configuration.

// Resolving the Plugins is non-trivial:
// * We get a GrailsClassLoader and use it to parse the BuildConfig file
// * The BuildConfig contains a closure "resolution" which contains at least one closure "dependencies"
// * the dependencies closure calls methods "compile", "runtime" etc.
// * We are interested in the methods of the dependencies closure
// * for that we need to execute the resolution closure and afterwards the dependencies closure
// * PROBLEM: we cannot execute the closure as the methods used in the closure are not known here
// * To circumvent this problem a class "BuildConfigParser" is implemented (see below) which implements methodMissing
// * An instance of the BuildConfigParser class is used as the delegate for the resolution closure and the closure is
//   changed to resolve methods first through the delegate
// * The methodMissing method looks at the method name which has been invoked and does the same game again for the
//   dependency closure
// * For all methods called "compile" or "runtime" the first argument is inspected, split at ":" and looked whether it
//   refers to a JUMMP Plugin
// * If it is a JUMMP Plugin, the name is extracted, it is tried to load a JummpPluginConfig class and it's closure is executed
//
// With other words: MAGIC HAPPENS HERE
GrailsClassLoader classLoader = new GrailsClassLoader()
ConfigSlurper slurper = new ConfigSlurper()
ConfigObject buildConfig = slurper.parse(classLoader.loadClass("BuildConfig"))
resolutionClosure = buildConfig.grails.project.dependency.resolution

class BuildConfigParser {
    private ClassLoader classLoader
    private def jummp
    private def jummpConfig

    BuildConfigParser(ClassLoader cl, def jummp, def jummpConfig) {
        this.classLoader = cl
        this.jummp = jummp
        this.jummpConfig = jummpConfig
    }

    def methodMissing(String name, args) {
        if (name == "dependencies") {
            def dependenciesClosure = args[0]
            dependenciesClosure.resolveStrategy = Closure.DELEGATE_FIRST
            dependenciesClosure.delegate = this
            dependenciesClosure.call()
        }
        if (name == "compile" || name == "runtime") {
            // this is a jar
            String jarName = (args[0] as String).split(":")[1]
            if (jarName.startsWith("grails-plugin-jummp-plugin-")) {
                String pluginName = jarName.minus("grails-plugin-jummp-plugin-")
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
        }
    }
}
resolutionClosure.resolveStrategy = Closure.DELEGATE_FIRST
resolutionClosure.delegate = new BuildConfigParser(classLoader, jummp, jummpConfig)
resolutionClosure.call()

grails.plugins.springsecurity.controllerAnnotations.staticRules = jummp.controllerAnnotations

environments {
    test {
        // need to disable the plugins or tests may fail
        // if needed in the tests, mockConfig should be used
        jummp.plugins.subversion.enabled = false
        jummp.plugins.git.enabled = false
        // disable registration mail sending
        jummp.security.registration.email.send = false
        jummp.security.resetPassword.email.send = false
        // disable dbus
//        jummp.plugin.dbus.export = true
    }
}

if (pluginsToExclude) {
    grails.plugin.exclude = pluginsToExclude
}
// fails the ant test script
grails.enable.native2ascii=false

// weceem
grails.mime.file.extensions = false
weceem.content.prefix = 'content'
weceem.tools.prefix = 'wcm-tools'
weceem.admin.prefix = 'wcm-admin'
weceem.create.default.space = true
weceem.default.space.template = "classpath:/weceem-jummp-default-space.zip"

environments {
    development {
        weceem.security.policy.path = null //"scripts/weceem-security.groovy"
    }
    test {
        weceem.security.policy.path = null
    }
    production {
        weceem.security.policy.path = null
    }
}
