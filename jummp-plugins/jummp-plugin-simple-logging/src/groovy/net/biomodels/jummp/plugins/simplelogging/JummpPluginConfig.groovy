package net.biomodels.jummp.plugins.simplelogging

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-simple-logging]")
    }
}
