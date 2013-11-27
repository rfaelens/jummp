/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
* LibPharmml (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of LibPharmml used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.pharmml

import net.biomodels.jummp.core.model.RevisionTransportCommand
import eu.ddmore.libpharmml.dom.PharmML
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType

/**
 * Controller for handling Model files in the PharmML format.
 * @author  Raza Ali <raza.ali@ebi.ac.uk>
 * @author  Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMlController {

    def modelDelegateService
    def pharmMlService

    def show = {
        Long modelID = params.id as Long;
        List<RevisionTransportCommand> revs = modelDelegateService.getAllRevisions(modelID)
        final RevisionTransportCommand revision = revs.last()
        PharmML dom = pharmMlService.getDomFromRevision(revision)
        TrialDesignType design = dom?.trialDesign
        ModellingStepsType steps = pharmMlService.getModellingSteps(revision)

        render(view:"/model/pharmml/show", model: [
                revision: revision,
                authors: revision.model.creators,
                allRevs: revs,
                modelDefinition: dom.modelDefinition,
                trialDesign: design,
                independentVar: pharmMlService.getIndependentVariable(dom),
                functionDefs: pharmMlService.getFunctionDefinitions(dom),
                structuralModel: pharmMlService.getStructuralModel(dom),
                variabilityModel: pharmMlService.getVariabilityModel(dom),
                covariateModel: pharmMlService.getCovariateModel(dom),
                parameterModel: pharmMlService.getParameterModel(dom),
                observationModel: pharmMlService.getObservationModel(dom),
                structure: pharmMlService.getTrialDesignStructure(design),
                population: pharmMlService.getPopulation(design),
                dosing: pharmMlService.getIndividualDosing(design),
                estSteps: pharmMlService.getEstimationSteps(steps),
                simSteps: pharmMlService.getSimulationSteps(steps),
                stepDeps: pharmMlService.getStepDependencies(steps),
                flashMessage: params.flashMessage,
                canUpdate:params.canUpdate,
                showPublishOption:params.showPublishOption,
                showUnpublishOption:params.showUnpublishOption
            ]
        )
    }
}
