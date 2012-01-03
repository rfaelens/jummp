basePath = "./"
includeTargets << new File("./scripts/AST.groovy")

eventSetClasspath = { rootLoader ->
    ast()
}
