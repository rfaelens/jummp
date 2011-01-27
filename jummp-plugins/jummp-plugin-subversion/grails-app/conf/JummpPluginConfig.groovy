package net.biomodels.jummp.plugins.subversion

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        // set the svn service it's configuration
        if (jummpConfig.jummp.vcs.plugin == "subversion") {
            println("using subversion as vcs backend")
            jummp.vcs.pluginServiceName = "svnService"
            jummp.plugins.subversion.enabled = true
            jummp.plugins.subversion.localRepository = jummpConfig.plugins.subversion.localRepository
        }
    }
}