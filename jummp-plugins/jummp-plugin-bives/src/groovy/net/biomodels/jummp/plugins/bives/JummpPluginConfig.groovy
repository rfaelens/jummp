package net.biomodels.jummp.plugins.bives

/**
 * //TODO add description for class JummpPluginConfig.groovy
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 27.06.2011
 * @year 2011
 */
class JummpPluginConfig {
    static def configure = { ConfigObject jummp, ConfigObject jummpConfig ->
        println("Loading configuration for module [jummp-plugin-bives]")
//		jummpConfig.jummp.bives.plugin == "bives"
        jummp.plugins.bives.diffdir = jummpConfig.jummp.plugins.bives.diffdir
        println("BiVeS: Diff directory set to " + jummpConfig.jummp.plugins.bives.diffdir)
    }
}
