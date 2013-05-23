class JummpPluginBivesGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def loadAfter = ["jummp-plugin-security", "jummp-plugin-core-api"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = ""
    def authorEmail = ""
    def developers = [
        [ name: "Robert Haelke", email: "robert.haelke@googlemail.com"],
        [ name: "Mihai Glonț", email: "mihai.glont@ebi.ac.uk" ]
    ]
    def title = "Plugin summary/headline"
    def description = '''\\
Brief description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/jummp-plugin-bives"

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
        if (jummpConfig.jummp.plugins.bives.diffdir) {
            application.config.jummp.plugins.bives.diffdir = jummpConfig.jummp.plugins.bives.diffdir
            println("BiVeS: Diff directory set to " + jummpConfig.jummp.plugins.bives.diffdir)
        }
		bivesEventListener(net.biomodels.jummp.plugins.bives.ModelVersionCreatedListener) {
			modelDelegateService = ref("modelDelegateService")
            diffDataService = ref("diffDataService")
		}
		diffDataProvider(net.biomodels.jummp.plugins.bives.DiffDataProvider) { bean ->
			bean.autowire = "byName"
			bean.scope = "prototype"
		}
		createDiff(net.biomodels.jummp.plugins.bives.CreateDiffThread) { bean ->
			bean.autowire = "byName"
			bean.factoryMethod = "getInstance"
			bean.scope = "prototype"
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
