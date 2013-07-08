import grails.util.Metadata

basePath = System.getProperty("jummp.basePath")
if (!basePath) {
    basePath = "../../"
}
astScriptLocation = Metadata.current.'app.name'.matches("jummp-plugin-jms") ?
        "../../scripts/AST.groovy" : "scripts/AST.groovy"

includeTargets << new File(astScriptLocation)

eventSetClasspath = { rootLoader ->
    ast()
}
