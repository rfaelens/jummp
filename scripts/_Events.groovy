basePath = System.properties['jummp.basedir']
if (!basePath) {
    basePath = "./"
}
includeTargets << new File("./scripts/AST.groovy")
includeTargets << new File("./scripts/WeceemExport.groovy")

eventSetClasspath = { rootLoader ->
    ast()
}

eventCompileEnd = {
    weceem()
}
