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
        [
                reactions: remoteSbmlService.getReactions(params.id as long, params.revision as int),
                rules: remoteSbmlService.getRules(params.id as long, params.revision as int),
                functions: remoteSbmlService.getFunctionDefinitions(params.id as long, params.revision as int),
                events: remoteSbmlService.getEvents(params.id as long, params.revision as int)
        ]
    }

    def reactionMeta = {
        [reaction: remoteSbmlService.getReaction(params.id as long, params.revision as int, params.reactionId)]
    }

    def eventMeta = {
        [event: remoteSbmlService.getEvent(params.id as long, params.revision as int, params.eventId)]
    }

    def functionDefinitionMeta = {
        [function: remoteSbmlService.getFunctionDefinition(params.id as long, params.revision as int, params.functionDefinitionId)]
    }

    def entity = {
        [compartments: remoteSbmlService.getCompartments(params.id as long, params.revision as int)]
    }

    def compartmentMeta = {
        [compartment: remoteSbmlService.getCompartment(params.id as long, params.revision as int, params.compartmentId)]
    }

    def speciesMeta = {
        [species: remoteSbmlService.getSpecies(params.id as long, params.revision as int, params.speciesId)]
    }

    /**
     * Renders the reaction graph svg
     */
    def reactionGraphSvg = {
        byte[] bytes = remoteSbmlService.generateSvg(params.id as long, params.revision as int)
        response.setContentType("image/svg+xml")
        response.outputStream << new ByteArrayInputStream(bytes)
    }

    /**
     * Outputs the Octave file
     */
    def reactionOctave = {
        [octave: remoteSbmlService.generateOctave(params.id as long, params.revision as int)]
    }

    /**
     * Outputs the BioPax file
     */
    def reactionBioPax = {
        [biopax: remoteSbmlService.generateOctave(params.id as long, params.revision as int)]
    }

    def overview = {
        [
                reactions: remoteSbmlService.getReactions(params.id as long, params.revision as int),
                rules: remoteSbmlService.getRules(params.id as long, params.revision as int),
                parameters: remoteSbmlService.getParameters(params.id as long, params.revision as int),
                compartments: remoteSbmlService.getCompartments(params.id as long, params.revision as int)
        ]
    }

    def compartmentMetaOverview = {
        [compartment: remoteSbmlService.getCompartment(params.id as long, params.revision as int, params.compartmentId)]
    }

    def reactionMetaOverview = {
        [reaction: remoteSbmlService.getReaction(params.id as long, params.revision as int, params.reactionId)]
    }

    def parameterMetaOverview = {
        [parameter: remoteSbmlService.getParameter(params.id as long, params.revision as int, params.parameterId)]
    }

    def submodel = {
        [submodel: remoteSbmlService. triggerSubmodelGeneration(params.id as long, params.revision as int, params.submodelId,  params.metaId, params.compartmentIds as List, params.speciesIds as List, params.reactionIds as List, params.ruleIds as List, params.eventIds as List)]
    }
}
