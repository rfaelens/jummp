includeTargets << grailsScript("_GrailsCompile")

target(ast: "AST transformation for plugins") {
    classpathSet = false
    println "Set Classpath handler"
    String buildDir = "./ast-build"
    String astLib = "./ast-build/ast.jar"
    ant.delete(dir: buildDir)
    ant.mkdir(dir: buildDir)
    ant.groovyc(srcdir: "${basePath}/jummp-plugins/jummp-plugin-ast/src/groovy/", destdir: buildDir)
    ant.jar(destfile: astLib, basedir: buildDir)
    grailsSettings.compileDependencies << new File(astLib)

    classpath()
}
