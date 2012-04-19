basePath = "../"
includeTargets << new File("../scripts/AST.groovy")

eventSetClasspath = { rootLoader ->
    ast()
}

/**
 * Script to write the mercurial id of the build into _version.gsp
 *
 * Based upon an example from
 * http://blog.armbruster-it.de/2011/07/embedding-the-githgsvns-revision-number-inside-a-grails-application/
 */
eventCompileStart = { msg ->
    def proc = "git rev-parse HEAD".execute()
    proc.waitFor()
    new FileOutputStream("grails-app/views/templates/_version.gsp", false) << proc.in.text
}
