import grails.util.Holders

class BootStrap {

    def init = { servletContext ->
        println "What are you doing in jummp-plugin-git's BootStrap#init()?"
        Holders.setServletContext(servletContext)
    }

    def destroy = { 
    }
}
