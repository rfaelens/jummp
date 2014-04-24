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





scriptEnv = "test"
includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )
includeTargets << new File ( "${grailsHome}/scripts/Bootstrap.groovy" )

target(generateCoverageReport: "Generates the coverage reports") {
    depends(compile, packagePlugins)
    ant.taskdef(classpathRef: 'grails.test.classpath', resource: "tasks.properties")
    ant.mkdir(dir: "${basedir}/target/test-reports/coverage")
    ant.delete(file: "${basedir}/target/cobertura.ser")
    ant.'cobertura-merge'(datafile: "${basedir}/target/cobertura.ser") {
        fileset(dir: "${basedir}") {
            include(name: "cobertura.ser")
            include(name: "jummp-plugins/jummp-plugin-*/cobertura.ser")
        }
    }

    def pluginPath = "${basedir}/jummp-plugins/jummp-plugin-"
    def directories = ["${basedir}",
                       "${pluginPath}configuration",
                       "${pluginPath}core-api",
                       "${pluginPath}git",
                       "${pluginPath}sbml",
                       "${pluginPath}security",
                       "${pluginPath}subversion"]

    ant.'cobertura-report'(destDir: "${basedir}/target/test-reports/coverage", datafile: "${basedir}/target/cobertura.ser", format: "html") {
        //load all these dirs independently so the dir structure is flattened,
        //otherwise the source isn't found for the reports
        directories.each {
            fileset(dir: "${it}/grails-app/controllers")
            fileset(dir: "${it}/grails-app/domain")
            fileset(dir: "${it}/grails-app/services")
            fileset(dir: "${it}/grails-app/taglib")
            fileset(dir: "${it}/grails-app/utils")
            fileset(dir: "${it}/src/groovy")
            fileset(dir: "${it}/src/java")
        }
    }
}

setDefaultTarget(generateCoverageReport)
