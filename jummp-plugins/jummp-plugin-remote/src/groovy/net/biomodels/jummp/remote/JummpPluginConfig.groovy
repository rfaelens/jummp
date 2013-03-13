package net.biomodels.jummp.remote

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-remote]")
    }
}
