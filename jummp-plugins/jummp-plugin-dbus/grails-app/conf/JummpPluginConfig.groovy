package net.biomodels.jummp.plugins.dbus

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        if (!(jummpConfig.jummp.plugins.dbus.systemBus instanceof ConfigObject)) {
            jummp.plugins.dbus.systemBus = Boolean.parseBoolean(jummpConfig.jummp.plugins.dbus.systemBus)
        } else {
            jummp.plugins.dbus.systemBus = false
        }
    }
}
