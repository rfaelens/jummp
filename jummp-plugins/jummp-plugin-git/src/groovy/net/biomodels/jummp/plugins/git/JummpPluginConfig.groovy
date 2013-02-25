package net.biomodels.jummp.plugins.git

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-git]")
        if (jummpConfig.jummp.vcs.plugin == "git") {
            println("using git as vcs backend")
            jummp.vcs.pluginServiceName = "gitManagerFactory"
            jummp.plugins.git.enabled = true
        }
    }
}
