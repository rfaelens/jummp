target(deleteJummpPlugins: "Deletes Jummp Plugins depending on user selection!") {
    Properties properties = new Properties()
    properties.load(new FileInputStream("${builddir}/jummpPlugins.properties"))
    if (!properties.containsKey("subversion") || !Boolean.parseBoolean(properties.getProperty("subversion"))) {
        ant.delete(dir: "${builddir}/jummp-plugins/jummp-plugin-subversion")
    }
    if (!properties.containsKey("git") || !Boolean.parseBoolean(properties.getProperty("git"))) {
        ant.delete(dir: "${builddir}/jummp-plugins/jummp-plugin-git")
    }
    if (!properties.containsKey("configuration") || !Boolean.parseBoolean(properties.getProperty("configuration"))) {
        ant.delete(dir: "${builddir}/jummp-plugins/jummp-plugin-configuration")
    }
    if (!properties.containsKey("sbml") || !Boolean.parseBoolean(properties.getProperty("sbml"))) {
        ant.delete(dir: "${builddir}/jummp-plugins/jummp-plugin-sbml")
    }
    if (!properties.containsKey("jms") || !Boolean.parseBoolean(properties.getProperty("jms"))) {
        ant.delete(dir: "${builddir}/jummp-plugins/jummp-plugin-jms")
        // remove the jms plugin from main application
        ant.propertyfile(file: "${builddir}/application.properties") {
            ant.entry(key: "plugins.jms", operation: "del")
        }
    }
    if (!properties.containsKey("dbus") || !Boolean.parseBoolean(properties.getProperty("dbus"))) {
        ant.delete(dir: "${builddir}/jummp-plugins/jummp-plugin-dbus")
    }
}

setDefaultTarget(deleteJummpPlugins)
