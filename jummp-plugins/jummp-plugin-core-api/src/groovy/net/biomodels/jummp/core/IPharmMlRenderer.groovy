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
* LibPharmml (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of LibPharmml used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinitionType
import eu.ddmore.libpharmml.dom.modellingsteps.CommonModellingStepType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.trialdesign.IndividualDosingType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import eu.ddmore.libpharmml.dom.modeldefn.ObservationModelType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityDefnBlock
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType
import eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType

/**
 * @short Interface describing the service to render a model encoded in PharmML.
 *
 * Implementations of this interface are provided by the PharmML plugin, however
 * this interface can be used to provide alternative ones.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
interface IPharmMlRenderer {

    /**
     * @param independentVariable the independent variable to render
     */
    String renderIndependentVariable(String independentVariable)

    /**
     * @param functionDefinitions the list of
     * {@link eu.ddmore.libpharmml.dom.commontypes.FunctionDefinitionType}s.
     */
    String renderFunctionDefinitions(List functionDefinitions)

    /**
     * @param modelDefinition an instance of {@link eu.ddmore.libpharmml.dom.modeldefn.ModelDefinitionType}
     */
    String renderModelDefinition(ModelDefinitionType modelDefinition)

    /**
     * @param covariates a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType}s.
     */
    String renderCovariateModel(List<CovariateDefinitionType> covariates)

    /**
     * @param variabilityModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.VariabilityDefnBlock}s.
     */
    String renderVariabilityModel(List<VariabilityDefnBlock> variabilityModels)

    /**
     * @param parameterModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType}s.
     */
    String renderParameterModel(List<ParameterModelType> parameterModels)

    /**
     * @param structuralModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType}s.
     * @param indepVar the independent variable of the model, for example time or concentration.
     */
    String renderStructuralModel(List<StructuralModelType> structuralModels, String indepVar)

    /**
     * @param observationModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.ObservationModelType}s.
     */
    String renderObservationModel(List<ObservationModelType> observationModels)

    /**
     * @param trialDesign an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType}
     */
    String renderTrialDesign(TrialDesignType trialDesign)

    /**
     * @param structure - an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType}
     */
    String renderTrialDesignStructure(TrialStructureType structure)

    /**
     * @param dosing - a list of {@link eu.ddmore.libpharmml.dom.trialdesign.IndividualDosingType}
     */
    String renderIndividualDosing(List<IndividualDosingType> dosing)

    /**
     * @param population an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.PopulationType}
     */
    String renderPopulation(PopulationType population)

    /**
     * @param steps an instance of {@link eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType}
     */
    String renderModellingSteps(ModellingStepsType steps)

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.CommonModellingStepType}s.
     */
    String renderCommonModellingSteps(List<CommonModellingStepType> steps)

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType}s.
     */
    String renderSimulationSteps(List<SimulationStepType> steps)

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType}s.
     */
    String renderEstimationSteps(List<EstimationStepType> steps)

    /**
     * @param dependencies a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType}s.
     */
    String renderStepDependencies(StepDependencyType dependencies)
}

