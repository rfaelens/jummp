includeTargets << new File("./scripts/WeceemExport.groovy")

/**
 * Script to write the git id of the build into _version.gsp
 */
eventCompileStart = { msg ->
    def proc = "git log -1 --pretty=format:\"%h|%aD\"".execute()
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
