package net.biomodels.jummp.plugins.configuration

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-configuration]")
        jummp.controllerAnnotations << ["/configuration/**": ['ROLE_ADMIN']]
    }
} 
