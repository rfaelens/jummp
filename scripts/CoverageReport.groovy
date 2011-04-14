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
