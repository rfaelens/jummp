import net.biomodels.jummp.plugins.subversion.SvnManagerFactory
import grails.util.Environment

class JummpPluginSubversionGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.5 > *"
    // the other plugins this plugin depends on
    def loadAfter = ["jummp-plugin-security", "jummp-plugin-core-api"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Your name"
    def authorEmail = ""
    def title = "Plugin summary/headline"
    def description = '''\\
Brief description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jummp-plugin-subversion"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        Properties props = new Properties()
        try {
            props.load(new FileInputStream(System.getProperty("user.home") + System.getProperty("file.separator") +
                    ".jummp.properties"))
        } catch (Exception ignored) {
        }
        def jummpConfig = new ConfigSlurper().parse(props)
        if (jummpConfig.jummp.vcs.plugin == "subversion") {
            println("using subversion as vcs backend")
            application.config.jummp.vcs.pluginServiceName = "svnManagerFactory"
            application.config.jummp.plugins.subversion.enabled = true
            application.config.jummp.plugins.subversion.localRepository = jummpConfig.jummp.plugins.subversion.localRepository
        }

//        if (Environment.getCurrent() == Environment.TEST) {
            servletContext(org.springframework.mock.web.MockServletContext)
//        }
        svnManagerFactory(SvnManagerFactory) {
            grailsApplication = ref("grailsApplication")
            servletContext = ref("servletContext")
        }
        if (!(application.config.jummp.plugins.subversion.enabled instanceof ConfigObject) && application.config.jummp.plugins.subversion.enabled) {
            vcsManager(svnManagerFactory: "getInstance")
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
