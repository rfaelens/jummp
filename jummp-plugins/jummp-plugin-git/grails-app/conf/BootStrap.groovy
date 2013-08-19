import grails.util.Holders

class BootStrap {

    def init = { servletContext ->
        Holders.setServletContext(servletContext)
    }

    def destroy = {
    }
}
