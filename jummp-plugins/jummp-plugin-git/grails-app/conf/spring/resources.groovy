import net.biomodels.jummp.plugins.git.GitManagerFactory

beans = {
    gitManagerFactory(GitManagerFactory) {
        grailsApplication = ref("grailsApplication")
        servletContext = ref("servletContext")
        //servletContext(org.springframework.mock.web.MockServletContext)
    }
    vcsManager(gitManagerFactory: "getInstance")
}
