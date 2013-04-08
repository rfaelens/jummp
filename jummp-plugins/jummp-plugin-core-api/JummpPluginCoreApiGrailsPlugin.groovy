class JummpPluginCoreApiGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.5 > *"
    // the other plugins this plugin depends on
    def loadAfter = ["jummp-plugin-security"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = ""
    def authorEmail = ""
    def developers = [
        [ name: "Martin Gräßlin", email: "m.graesslin@dkfz.de"],
        [ name: "Mihai Glonț", email: "mihai.glont@ebi.ac.uk" ]
    ]
    def title = "JUMMP Plugin Core API"
    def description = '''\\
This plugin provides the API for the JUMMP core plugins.
All other plugins providing core functionality depend on this plugin and the core itself depnds on it. 
'''

    // URL to the plugin's documentation
    def documentation = "https://bitbucket.org/jummp/jummp/wiki"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
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
