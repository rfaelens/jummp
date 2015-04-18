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
* Apache Tika, Apache Commons, LibPharmml, Perf4j (or a modified version of these
* libraries), containing parts covered by the terms of Apache License v2.0,
* the licensors of this Program grant you additional permission to convey the
* resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Tika, Apache Commons,
* LibPharmml, Perf4j used as well as that of the covered work.}
**/

package net.biomodels.jummp.plugins.pharmml
import net.biomodels.jummp.core.model.RevisionTransportCommand
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.trialdesign.IndividualDosingType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesign
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import eu.ddmore.libpharmml.dom.modeldefn.ObservationModelType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityDefnBlock
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType
import eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType
import net.biomodels.jummp.core.IPharmMlRenderer
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled

/**
 * @short Service that routes PharmML rendering requests to the appropriate delegate class.
 *
 * This class  for rendering PharmML representations.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMlRenderingService {
    /* disable transactional behaviour */
    static transactional = false
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)

    @Profiled(tag = "pharmMlRenderingService.renderIndependentVariable")
    void renderIndependentVariable(String iv, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderIndependentVariable(iv)
    }

    @Profiled(tag = "pharmMlRenderingService.renderFunctionDefinitions")
    void renderFunctionDefinitions(List defs, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderFunctionDefinitions(defs)
    }

    @Profiled(tag = "pharmMlRenderingService.renderStructuralModel")
    void renderStructuralModel(List structuralModels, String iv, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderStructuralModel(structuralModels, iv)
    }

    @Profiled(tag = "pharmMlRenderingService.renderCommonVariables")
    void renderCommonVariables(List vars, String iv, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderCommonVariables(vars, iv)
    }

    void renderInitialConditions(Map initialConditions, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderInitialConditions(initialConditions)
    }

    @Profiled(tag = "pharmMlRenderingService.renderSimpleParameters")
    void renderSimpleParameters(List simpleParameters, Map transf, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderSimpleParameters(simpleParameters, transf)
    }

    @Profiled(tag = "pharmMlRenderingService.renderVariabilityModel")
    void renderVariabilityModel(List variabilityModels, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderVariabilityModel(variabilityModels)
    }

    @Profiled(tag = "pharmMlRenderingService.renderCovariateModel")
    void renderCovariateModel(List covariates, Map transfMap, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderCovariateModel(covariates, transfMap)
    }

    @Profiled(tag = "pharmMlRenderingService.renderCovariates")
    void renderCovariates(List cov, String id, Map transf, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderCovariates(cov, id, transf)
    }

    @Profiled(tag = "pharmMlRenderingService.renderParameterModel")
    void renderParameterModel(List parameterModel, List covariateModel, Map transfMap,
                String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderParameterModel(parameterModel, covariateModel, transfMap)
    }

    @Profiled(tag = "pharmMlRenderingService.renderObservationModel")
    void renderObservationModel(List obsModel, List covModel, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderObservationModel(obsModel, covModel)
    }

    @Profiled(tag = "pharmMlRenderingService.renderTrialDesignStructure")
    void renderTrialDesignStructure(TrialStructureType struct, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderTrialDesignStructure(struct)
    }

    @Profiled(tag = "pharmMlRenderingService.renderIndividualDosing")
    void renderIndividualDosing(List<IndividualDosingType> doses, String version, Writer o, RevisionTransportCommand rev, String link) {
    	IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        o << renderer.renderIndividualDosing(doses, rev, link)
    }

    @Profiled(tag = "pharmMlRenderingService.renderPopulation")
    void renderPopulation(PopulationType population, String version, Writer out, RevisionTransportCommand rev, String link) {
    	IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderPopulation(population, rev, link)
    }

    @Profiled(tag = "pharmMlRenderingService.renderEstimationSteps")
    void renderEstimationSteps(List<EstimationStepType> steps, String version, Writer out, RevisionTransportCommand rev, String link) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderEstimationSteps(steps, rev, link)
    }

    @Profiled(tag = "pharmMlRenderingService.renderSimulationSteps")
    void renderSimulationSteps(List<SimulationStepType> steps, String independentVariable,
                    String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderSimulationSteps(steps, independentVariable)
    }

    @Profiled(tag = "pharmMlRenderingService.renderStepDependencies")
    void renderStepDependencies(StepDependencyType deps, String version, Writer out) {
        IPharmMlRenderer renderer = PharmMlVersionAwareRendererFactory.getRenderer(version)
        out << renderer.renderStepDependencies(deps)
    }
}
