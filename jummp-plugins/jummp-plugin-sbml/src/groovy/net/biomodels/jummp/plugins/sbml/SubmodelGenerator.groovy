/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* JSBML (or a modified version of that library), containing parts
* covered by the terms of GNU LGPL v2.1, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JSBML used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.sbml

import org.sbml.jsbml.*

/**
 * Class generating a sub model of an existing SBML model.
 *
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class SubmodelGenerator {

    /**
     * Generates a new SBML model as sub model of an existing one.
     *
     * @param existingModel: The existing Model
     * @param subModelId: The given sub model's id element
     * @param metaId: The meta id element
     * @param compartmentIds: The selected compartment ids
     * @param speciesIds: The selected species ids
     * @param reactionIds: The selected reaction ids
     * @param ruleIds: The selected rule ids
     * @param eventIds: The selected event ids
     * @return Generated SBMLDocument
     */
    protected String generateSubModel(
            Model existingModel, String subModelId, String metaId, List<String> compartmentIds,
            List<String> speciesIds, List<String> reactionIds, List<String> ruleIds, List<String> eventIds) {
        Model subModel = new Model(subModelId)
        subModel.setMetaId(metaId)
        SBMLDocument subModelSbmlDocument = new SBMLDocument()
        subModelSbmlDocument.setLevel(existingModel.level)
        subModelSbmlDocument.setVersion(existingModel.version)
        subModelSbmlDocument.setModel(subModel)

        reactionIds.each { reactionId ->
            if (reactionId == "") {
                return
            }
            Reaction relatedReaction = existingModel.getReaction(reactionId)
            subModel.addReaction(relatedReaction)
            if (relatedReaction.kineticLaw) {
                relatedReaction.kineticLaw.mathMLString
            }
        }
        compartmentIds.each { compartmentId ->
            if (compartmentId == "") {
                return
            }
            Compartment relatedCompartment = existingModel.getCompartment(compartmentId)
            subModel.addCompartment(relatedCompartment)

        }
        speciesIds.each { speciesId ->
            if (speciesId == "") {
                return
            }
            Species relatedSpecies = existingModel.getSpecies(speciesId)
            subModel.addSpecies(relatedSpecies)
        }
        ruleIds.each { ruleId ->
            if (ruleId == "") {
                return
            }
            Rule relatedRule = existingModel.getRule(ruleId)
            subModel.addRule(relatedRule)
        }
        eventIds.each { eventId ->
            if (eventId == "") {
                return
            }
            Event relatedEvent = existingModel.getEvent(eventId)
            subModel.addEvent(relatedEvent)
        }
        existingModel.getListOfParameters().each { parameter ->
            subModel.addParameter(parameter)
        }
        existingModel.getListOfUnitDefinitions().each { unitDefinition ->
            subModel.addUnitDefinition(unitDefinition)
        }
        existingModel.getListOfFunctionDefinitions().each { functionDefinition ->
            subModel.addFunctionDefinition(functionDefinition)
        }
        SBMLWriter sbmlWriter = new SBMLWriter()
        return sbmlWriter.writeSBMLToString(subModelSbmlDocument)
    }
}
