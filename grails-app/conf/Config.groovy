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
// JQuery config needed to fix broken version number in plugin version 1.4.4
jquery {
    sources = 'jquery' // Holds the value where to store jQuery-js files /web-app/js/
    version = '1.4.4' // The jQuery version in use
}

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
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
}

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'net.biomodels.jummp.security.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'net.biomodels.jummp.security.UserRole'
grails.plugins.springsecurity.authority.className = 'net.biomodels.jummp.security.Role'

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
if (!Boolean.parseBoolean(jummpConfig.jummp.security.ldap.enabled)) {
    println("Excluding ldap")
    pluginsToExclude << "spring-security-ldap"
} else {
    println("using ldap")
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

if (pluginsToExclude) {
    grails.plugin.exclude = pluginsToExclude
}
