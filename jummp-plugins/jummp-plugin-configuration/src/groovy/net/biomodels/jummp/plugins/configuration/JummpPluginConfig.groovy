package net.biomodels.jummp.plugins.configuration

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        jummp.controllerAnnotations << ["/configuration/**": ['ROLE_ADMIN']]
    }
}