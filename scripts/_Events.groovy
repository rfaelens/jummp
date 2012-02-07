basePath = System.properties['jummp.basedir']
if (!basePath) {
    basePath = "./"
}
includeTargets << new File("./scripts/AST.groovy")

eventSetClasspath = { rootLoader ->
    ast()
}
