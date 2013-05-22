package net.biomodels.jummp.webapp

import net.biomodels.jummp.core.model.ModelVersionTransportCommand

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
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [reaction: sbmlService.getReaction(ver)]
    }

    def compartmentMetaOverview = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [compartment: sbmlService.getCompartment(ver)]
    }

    def parameterMetaOverview = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [parameter: sbmlService.getParameter(ver)]
    }

    def math = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [
                reactions: sbmlService.getVersions(ver),
                rules: sbmlService.getRules(ver),
                functions: sbmlService.getFunctionDefinitions(ver),
                events: sbmlService.getEvents(ver)
        ]
    }

    def entity = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [compartments: sbmlService.getCompartments(ver)]
    }

    def compartmentMeta = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [compartment: sbmlService.getCompartment(ver)]
    }

    def speciesMeta = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [species: sbmlService.getSpecies(ver)]
    }

    def parameterMeta = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [parameter: sbmlService.getParameter(ver)]
    }

    def parameter = {
        ModelVersionTransportCommand ver = modelDelegateService.getLatestVersion(params.id as Long)
        [parameters: sbmlService.getParameters(ver), reactionParameters: sbmlService.getLocalParameters(ver)]
    }

}
