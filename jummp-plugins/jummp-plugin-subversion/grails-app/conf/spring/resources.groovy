import net.biomodels.jummp.plugins.subversion.SvnManagerFactory

beans = {
    svnManagerFactory(SvnManagerFactory) {
        grailsApplication = ref("grailsApplication")
        servletContext = ref("servletContext")
    }
    vcsManager(svnManagerFactory: "getInstance")
}
