import org.springframework.beans.factory.NoSuchBeanDefinitionException

class JummpPluginCombineArchiveGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    def loadAfter = ['jummp-plugin-security', 'jummp-plugin-core-api']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Jummp Plugin Combine Archive Plugin" // Headline display name of the plugin
    def author = "European Bioinformatics Institute (EMBL-EBI)"
    def authorEmail = ""
    def description = '''\
Adds support for the COMBINE archive into JUMMP.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jummp-plugin-combine-archive"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    def organization = [
        name: "EMBL-European Bioinformatics Institute",
        url: "http://www.ebi.ac.uk/"
    ]

    def developers = [
        [ name: "Mihai Glonț", email: "mihai.glont@ebi.ac.uk" ]
    ]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    def scm = [ url: "http://bitbucket.org/jummp/jummp/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
         try {
            def service = applicationContext.getBean("modelFileFormatService")
            ["", "0.1"].each {
                def modelFormat = service.registerModelFormat("OMEX", "COMBINE archive", it)
                service.handleModelFormat(modelFormat, "omexService")
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

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
