basePath = System.properties['jummp.basedir']
if (!basePath) {
    basePath = "./"
}
includeTargets << new File("./scripts/AST.groovy")
includeTargets << new File("./scripts/WeceemExport.groovy")

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
    ant.mkdir(dir: "grails-app/views/templates/")
    new FileOutputStream("grails-app/views/templates/_version.gsp", false) << proc.in.text

    // copy the messages.properties
    ant.mkdir(dir: "web-app/js/i18n/")
    ant.copy(file: "grails-app/i18n/messages.properties", todir: "web-app/js/i18n/")
}

eventCompileEnd = {
    weceem()
}
