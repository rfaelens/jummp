/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





includeTargets << new File("./scripts/WeceemExport.groovy")

/**
 * Script to write the git id of the build into _version.gsp
 */
eventCompileStart = { msg ->
    def proc = "git log -1 --pretty=format:\"%h|%aD\"".execute()
    proc.waitFor()
    ant.mkdir(dir: "grails-app/views/templates/")
    new FileOutputStream("grails-app/views/templates/_version.gsp", false) << "<BuildFormat:formatter build="+proc.in.text+"/>"

    // copy the messages.properties
    ant.mkdir(dir: "web-app/js/i18n/")
    ant.copy(file: "grails-app/i18n/messages.properties", todir: "web-app/js/i18n/")
}

eventCompileEnd = {
    weceem()
}

/**
 * Since version 2.3.2, Grails needs a little help to produce the WAR file.
 * The error message that is produced can be safely ignored.
 */
final String warName

eventCreateWarStart = { name, d ->
    println "Intercepted CreateWarStart event."
    warName = name
    ant.jar(destfile:name, basedir:d, manifest:"${d}/META-INF/MANIFEST.MF")
}

eventStatusError = { msg ->
    File war = new File(warName)
    if (war.exists()) {
        String relativeWarPath = war.absolutePath - (grailsSettings.baseDir.absolutePath + File.separator)
        if (msg) {
            println "\n\nThe above error message can be ignored."
        }
        println "Successfully created $relativeWarPath."
    }
}
