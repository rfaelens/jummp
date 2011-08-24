includeTargets << grailsScript("_GrailsPluginDev")

target(jummpPlugin: "Compiles a JUMMP Plugin and moves it to the shared Plugin Directory") {
    ant.buildnumber()
    ant.property(file: "build.number")
    String jarName = "grails-plugin-${grailsSettings.baseDir.name}-0.1"
    depends(packagePlugin)
    ant.move(file: "target/${jarName}.jar", toFile: "../../pluginlibs/${jarName}-${ant.project.properties.'build.number'}.jar")
}