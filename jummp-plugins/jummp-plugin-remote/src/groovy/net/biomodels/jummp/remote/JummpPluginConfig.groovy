package net.biomodels.jummp.plugins.remote

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-remote]")
    }
}
