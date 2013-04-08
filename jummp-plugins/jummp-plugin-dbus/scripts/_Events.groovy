import grails.util.Metadata
basePath = System.getProperty("jummp.basePath")
if (!basePath) {
    basePath = "./"
}
astScriptLocation = "jummp-plugin-dbus".equals(Metadata.current.'app.name') ?
        "../../scripts/AST.groovy" : "scripts/AST.groovy"

includeTargets << new File(astScriptLocation)

eventSetClasspath = { rootLoader ->
    ast()
}
