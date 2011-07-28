package net.biomodels.jummp.plugins.sbml

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        if (!(jummpConfig.jummp.plugins.sbml.validate instanceof ConfigObject)) {
            jummp.plugins.sbml.validate = Boolean.parseBoolean(jummpConfig.jummp.plugins.sbml.validate)
        } else {
            jummp.plugins.sbml.validate = false
        }
    }
}
