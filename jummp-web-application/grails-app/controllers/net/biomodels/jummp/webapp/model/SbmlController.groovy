package net.biomodels.jummp.webapp.model

class SbmlController {
    /**
     * Dependency injection of remoteSbmlService
     */
    def remoteSbmlService
    /**
     * Dependency injection of springSecurityService
     */
    def springSecurityService

    def parameter = {
        if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "parameter", id: params.id)+ "?revision=${params.revision}", callback: "loadModelTabCallback"])
            return
        }
        [parameters: remoteSbmlService.getParameters(params.id as long, params.revision as int),
        reactionParameters: remoteSbmlService.getLocalParameters(params.id as long, params.revision as int)]
    }

    def parameterMeta = {
        [parameter: remoteSbmlService.getParameter(params.id as long, params.revision as int, params.parameterId)]
    }

    def math = {
        [reactions: remoteSbmlService.getReactions(params.id as long, params.revision as int)]
    }
}
