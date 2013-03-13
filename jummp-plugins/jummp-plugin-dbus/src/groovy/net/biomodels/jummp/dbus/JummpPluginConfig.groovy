package net.biomodels.jummp.dbus

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-dbus]")
        if (!(jummpConfig.jummp.plugins.dbus.systemBus instanceof ConfigObject)) {
            jummp.plugins.dbus.systemBus = Boolean.parseBoolean(jummpConfig.jummp.plugins.dbus.systemBus)
        } else {
            jummp.plugins.dbus.systemBus = false
        }
    }
}
