import net.biomodels.jummp.model.ModelFormat

class BootStrap {
    def springSecurityService;
    def vcsService

    def init = { servletContext ->
        vcsService.init()

        ModelFormat format = ModelFormat.findByIdentifier("UNKNWON")
        if (!format) {
            format = new ModelFormat(identifier: "UNKNOWN", name: "Unknown format")
            format.save(flush: true)
        }
    }
    def destroy = {
    }
}
