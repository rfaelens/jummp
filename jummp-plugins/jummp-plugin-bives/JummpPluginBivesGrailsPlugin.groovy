/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/
import net.biomodels.jummp.plugins.configuration.ConfigurationService





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
        	ConfigurationService service = new ConfigurationService()
        	String pathToConfig=service.getConfigFilePath()
        	if (!pathToConfig) {
        		throw new Exception("No config file available, using defaults")
        	}
        	props.load(new FileInputStream(pathToConfig))
        } catch (Exception ignored) {
        }
        def jummpConfig = new ConfigSlurper().parse(props)
        if (jummpConfig.jummp.plugins.bives.diffdir) {
            application.config.jummp.plugins.bives.diffdir = jummpConfig.jummp.plugins.bives.diffdir
            println("BiVeS: Diff directory set to " + jummpConfig.jummp.plugins.bives.diffdir)
        }
        else {
        	StringBuffer tmp = new StringBuffer(System.getProperty("java.io.tmpdir"))
        	File bvs = new File(tmp.append(File.separator).append("bives").toString())
        	bvs.mkdirs()
            application.config.jummp.plugins.bives.diffdir = bvs.canonicalPath
        }
		bivesEventListener(net.biomodels.jummp.plugins.bives.RevisionCreatedListener) {
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
