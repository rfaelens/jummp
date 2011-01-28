import net.biomodels.jummp.model.ModelFormat

class BootStrap {
    def springSecurityService;

    def init = { servletContext ->
        ModelFormat format = ModelFormat.findByIdentifier("UNKNWON")
        if (!format) {
            format = new ModelFormat(identifier: "UNKNOWN", name: "Unknown format")
            format.save(flush: true)
        }
    }
    def destroy = {
    }
}
