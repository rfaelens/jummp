/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


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
