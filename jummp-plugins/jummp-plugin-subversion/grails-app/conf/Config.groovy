// configuration for plugin testing - will not be included in the plugin zip
Properties jummpProps = new Properties()
try {
    jummpProps.load(new FileInputStream("${userHome}${System.getProperty('file.separator')}.jummp.properties"))
}
catch (Exception ignored) {
}
def cfg = new ConfigSlurper().parse(jummpProps)
jummp.vcs.workingDirectory = cfg.jummp.vcs.workingDirectory
jummp.vcs.exchangeDirectory = cfg.jummp.vcs.exchangeDirectory
 
log4j = {
    // as of Grails 2.2.2, the stacktrace goes to java.io.tmpdir or Tomcat's logs folder
    appenders {
        file name: "stacktrace", append: true, file: "logs/jummp-plugin-subversion_stacktrace.log"
    }

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
    rollingFile name: "pluginAppender", file: "logs/jummp-plugin-subversion.log", threshold: org.apache.log4j.Level.ALL
    info  pluginAppender: [ 'grails.app', 'net.biomodels.jummp.plugins.subversion' ]
}

grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.enable.native2ascii=true
