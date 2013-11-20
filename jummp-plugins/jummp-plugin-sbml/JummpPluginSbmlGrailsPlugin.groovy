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
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Spring Framework (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Framework used as well as
* that of the covered work.}
**/





import org.springframework.beans.factory.NoSuchBeanDefinitionException
import net.biomodels.jummp.plugins.configuration.ConfigurationService


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
        	ConfigurationService service = new ConfigurationService()
        	String pathToConfig=service.getConfigFilePath()
        	if (!pathToConfig) {
        		throw new Exception("No config file available, using defaults")
        	}
        	props.load(new FileInputStream(pathToConfig))
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
