package net.biomodels.jummp.webapp

import net.biomodels.jummp.core.model.RevisionTransportCommand

class SbmlController {
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService
    /**
     * Dependency injection of sbmlService.
     */
    def sbmlService

    def reactionMetaOverview = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [reaction: sbmlService.getReaction(rev)]
    }

    def compartmentMetaOverview = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [compartment: sbmlService.getCompartment(rev)]
    }

    def parameterMetaOverview = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [parameter: sbmlService.getParameter(rev)]
    }

    def math = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [
                reactions: sbmlService.getReactions(rev),
                rules: sbmlService.getRules(rev),
                functions: sbmlService.getFunctionDefinitions(rev),
                events: sbmlService.getEvents(rev)
        ]
    }

    def entity = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [compartments: sbmlService.getCompartments(rev)]
    }

    def compartmentMeta = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [compartment: sbmlService.getCompartment(rev)]
    }

    def speciesMeta = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [species: sbmlService.getSpecies(rev)]
    }

    def parameterMeta = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [parameter: sbmlService.getParameter(rev)]
    }

    def parameter = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [parameters: sbmlService.getParameters(rev), reactionParameters: sbmlService.getLocalParameters(rev)]
    }

}
