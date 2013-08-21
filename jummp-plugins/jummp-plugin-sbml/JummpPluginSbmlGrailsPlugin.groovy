import org.springframework.beans.factory.NoSuchBeanDefinitionException

class JummpPluginSbmlGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.6 > *"
    // the other plugins this plugin depends on
    def loadAfter = ["jummp-plugin-core-api"]
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
    def documentation = "http://grails.org/plugin/jummp-plugin-sbml"

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
 /*
        if (jummpConfig.jummp.plugins.sbml.validation instanceof ConfigObject) {
            application.config.jummp.plugins.sbml.validation = Boolean.parseBoolean(jummpConfig.jummp.plugins.sbml.validation)
        } else {
            application.config.jummp.plugins.sbml.validation = false
        }
*/
   }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        try {
            def service = applicationContext.getBean("modelFileFormatService")
            ["", "L1V1", "L1V2", "L2V1", "L2V2", "L2V3", "L2V4", "L3V1"].each {
                def modelFormat = service.registerModelFormat("SBML", "SBML", it)
                service.handleModelFormat(modelFormat, "sbmlService", "sbml")
            }
        } catch(NoSuchBeanDefinitionException e) {
            println("ModelFileFormatService is not available!")
        }
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
