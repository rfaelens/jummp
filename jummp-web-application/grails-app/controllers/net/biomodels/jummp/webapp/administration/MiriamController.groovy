package net.biomodels.jummp.webapp.administration

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import net.biomodels.jummp.core.miriam.MiriamUpdateException

@Secured('ROLE_ADMIN')
class MiriamController {
    def remoteMiriamService

    def index = { }

    def updateResources = { UpdateResourcesCommand cmd ->
        Map data = [:]
        if (cmd.hasErrors()) {
            data.put("error", true)
            if (cmd.errors.getFieldError("miriamUrl")) {
                String error = cmd.errors.getFieldError("miriamUrl").code
                if (error == "blank" || error == "url.invalid") {
                    data.put("miriamUrl", g.message(code: "miriam.update.url.invalid"))
                } else {
                    data.put("miriamUrl", g.message(code: "error.unknown", args: ["miriamUrl"]))
                }
            }
            render data as JSON
        } else {
            try {
                remoteMiriamService.updateMiriamResources(cmd.miriamUrl, cmd.force)
                data.put("success", true)
            } catch (MiriamUpdateException e) {
                data.put("error", g.message(code: "miriam.update.error"))
            }
            render data as JSON
        }
    }
}

class UpdateResourcesCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String miriamUrl
    Boolean force

    static constraints = {
        miriamUrl(nullable: false, blank: false, url: true)
    }
}
