package net.biomodels.jummp.plugins.sbml

class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        if (jummpConfig.jummp.plugins.sbml.validate instanceof ConfigObject) {
            jummp.plugins.sbml.validation = Boolean.parseBoolean(jummpConfig.jummp.plugins.sbml.validation)
        } else {
            jummp.plugins.sbml.validation = false
        }
    }
}
