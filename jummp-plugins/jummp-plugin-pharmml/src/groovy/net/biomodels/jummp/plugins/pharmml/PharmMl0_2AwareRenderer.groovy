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

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariableType
import eu.ddmore.libpharmml.dom.commontypes.FalseBooleanType
import eu.ddmore.libpharmml.dom.commontypes.FuncParameterDefinitionType
import eu.ddmore.libpharmml.dom.commontypes.FunctionDefinitionType
import eu.ddmore.libpharmml.dom.commontypes.IdValueType
import eu.ddmore.libpharmml.dom.commontypes.IntValueType
import eu.ddmore.libpharmml.dom.commontypes.RealValueType
import eu.ddmore.libpharmml.dom.commontypes.Rhs
import eu.ddmore.libpharmml.dom.commontypes.ScalarRhs
import eu.ddmore.libpharmml.dom.commontypes.SequenceType
import eu.ddmore.libpharmml.dom.commontypes.StringValueType
import eu.ddmore.libpharmml.dom.commontypes.SymbolRefType
import eu.ddmore.libpharmml.dom.commontypes.TrueBooleanType
import eu.ddmore.libpharmml.dom.commontypes.VariableAssignmentType
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinitionType
import eu.ddmore.libpharmml.dom.commontypes.VectorType
import eu.ddmore.libpharmml.dom.dataset.ColumnDefnType
import eu.ddmore.libpharmml.dom.dataset.DataSetTableDefnType
import eu.ddmore.libpharmml.dom.dataset.DataSetTableType
import eu.ddmore.libpharmml.dom.dataset.DataSetType
import eu.ddmore.libpharmml.dom.maths.BinopType
import eu.ddmore.libpharmml.dom.maths.ConstantType
import eu.ddmore.libpharmml.dom.maths.Equation
import eu.ddmore.libpharmml.dom.maths.EquationType
import eu.ddmore.libpharmml.dom.maths.FunctionCallType
import eu.ddmore.libpharmml.dom.maths.UniopType
import eu.ddmore.libpharmml.dom.modeldefn.CategoryType
import eu.ddmore.libpharmml.dom.modeldefn.ContinuousCovariateType
import eu.ddmore.libpharmml.dom.modeldefn.CorrelatedRandomVarType
import eu.ddmore.libpharmml.dom.modeldefn.CorrelationType
import eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType
import eu.ddmore.libpharmml.dom.modeldefn.GaussianObsError
import eu.ddmore.libpharmml.dom.modeldefn.GeneralObsError
import eu.ddmore.libpharmml.dom.modeldefn.IndividualParameterType
import eu.ddmore.libpharmml.dom.modeldefn.LhsTransformationType
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinitionType
import eu.ddmore.libpharmml.dom.modeldefn.ObservationModelType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterRandomVariableType
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameterType
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityDefnBlock
import eu.ddmore.libpharmml.dom.modellingsteps.CommonModellingStepType
import eu.ddmore.libpharmml.dom.modellingsteps.DatasetMappingType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType
import eu.ddmore.libpharmml.dom.modellingsteps.OperationPropertyType
import eu.ddmore.libpharmml.dom.modellingsteps.ParameterEstimateType
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.modellingsteps.ToEstimateType
import eu.ddmore.libpharmml.dom.modellingsteps.VariableMappingType
import eu.ddmore.libpharmml.dom.trialdesign.ActivityType
import eu.ddmore.libpharmml.dom.trialdesign.BolusType
import eu.ddmore.libpharmml.dom.trialdesign.IndividualDosingType
import eu.ddmore.libpharmml.dom.trialdesign.InfusionType
import eu.ddmore.libpharmml.dom.trialdesign.PopulationType
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import eu.ddmore.libpharmml.dom.uncertml.NormalDistribution
import grails.gsp.PageRenderer
import grails.util.Holders
import net.biomodels.jummp.core.IPharmMlRenderer
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled

class PharmMl0_2AwareRenderer implements IPharmMlRenderer {
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    private static final String IS_DEBUG_ENABLED = log.isDebugEnabled()
    private static final String IS_INFO_ENABLED = log.isInfoEnabled()
    /* Dependency injection for groovyPageRenderer */
    def groovyPageRenderer = Holders.applicationContext.getBean("groovyPageRenderer")

    /* lazy-loaded instance of this class.*/
    private static PharmMl0_2AwareRenderer instance = null

    /* Enforce the singleton pattern by keeping the constructor private. */
    private PharmMl0_2AwareRenderer() {}

    @Profiled(tag = "pharmMl0_2AwareRenderer.getInstance")
    public static PharmMl0_2AwareRenderer getInstance() {
        if (instance == null) {
            synchronized(PharmMl0_2AwareRenderer.class) {
                if (instance == null) {
                    if (IS_DEBUG_ENABLED) {
                        log.debug "Initialising the renderer for PharmML 0.2"
                    }
                    instance = new PharmMl0_2AwareRenderer()
                }
            }
        } else if (IS_DEBUG_ENABLED) {
            log.debug "Returning the already-initialised instance of the PharmML 0.2 renderer"
        }
        if (IS_INFO_ENABLED) {
            log.info "Returning the PharmML 0.2 renderer."
        }
        return instance
    }

    /**
     * @param independentVariable the independent variable to render
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderIndependentVariable(String independentVariable) {
        if (IS_INFO_ENABLED) {
            log.info "Rendering independent variable $independentVariable"
        }
        return groovyPageRenderer.render(template: "/templates/common/independentVariable",
                    model: [independentVariable: independentVariable])
    }

    /**
     * @param functionDefinitions the list of
     * {@link eu.ddmore.libpharmml.dom.commontypes.FunctionDefinitionType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderFunctionDefinitions")
    String renderFunctionDefinitions(List<FunctionDefinitionType> functionDefinitions) {
        def definitionList = []
        try {
            functionDefinitions.collect(definitionList) { d ->
                def rightHandSide
                if (d.definition.equation) {
                    rightHandSide = d.definition.equation
                } else if (d.definition.scalar) {
                    rightHandSide = d.definition.scalar
                } else if (d.definition.symbRef) {
                    rightHandSide = d.definition.symbRef
                }
                //should not be null by now
                assert !!rightHandSide
                return MathMlRenderingHelper.convertToMathML(d.symbId, d.functionArgument, rightHandSide)
            }
        } catch(Exception e) {
            log.error("Error while rendering function definitions ${functionDefs.inspect()}: ${e.message}", e)
            definitionList.add("Sorry, cannot render the function definitions.")
        } finally {
            return groovyPageRenderer.render(template: "/templates/common/functionDefinitions",
                    model: [functionDefinitions: definitionList])
        }
    }

    /**
     * @param modelDefinition an instance of {@link eu.ddmore.libpharmml.dom.modeldefn.ModelDefinitionType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderModelDefinition")
    String renderModelDefinition(ModelDefinitionType modelDefinition) {}

    /**
     * @param covariates a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderCovariateModel(List<CovariateDefinitionType> covariates) {}

    /**
     * @param variabilityModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.VariabilityDefnBlock}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderVariabilityModel(List<VariabilityDefnBlock> variabilityModels) {
        def models = []
        variabilityModels.each { m ->
            def thisModel = [:]
            thisModel["blkId"] = m.blkId
            thisModel["name"] = m.name ?: "&nbsp;"
            thisModel["levels"] = formatVariabilityLevels(m.level)
            thisModel["type"] = m.type.value()
            models.add thisModel
        }
        return groovyPageRenderer.render(template: "/templates/0.2/variabilityModel",
                    model: [variabilityModels: models])
    }

    List<String> formatVariabilityLevels(List variabilityLevels) {
        def result = []
        variabilityLevels.inject(result){ r, l ->
            StringBuilder sb = new StringBuilder()
            sb.append((l.name) ? l.name.value : l.symbId)
            if (l.parentLevel) {
                sb.append(", parent level: ").append(l.parentLevel.symbRef.symbIdRef)
            }
            result.add sb.toString()
        }
        return result
    }

    /**
     * @param parameterModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderParameterModel(List<ParameterModelType> parameterModels) {}

    /**
     * @param structuralModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType}s.
     * @param iv the independent variable of the model
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderStructuralModel(List<StructuralModelType> structuralModels, String iv) {
        def model = [:]
        try {
            structuralModels.each { sm ->
                String modelName = sm.name?.value ?: sm.blkId
                model["independentVariable"] = iv
                model["name"] = modelName
                if (sm.simpleParameter) {
                    model["simpleParameters"] = sm.simpleParameter
                }
                if (sm.commonVariable) {
                    model["variableDefinitions"] = sm.commonVariable
                }
            }
        } catch(Exception e) {
            log.error("Error while rendering structural model ${sm.inspect()} ${sm.properties}:${e.message}", e)
            model["error"] = "Sorry, something went wrong while displaying the structural model."
        } finally {
            return groovyPageRenderer.render(template: "/templates/common/structuralModel",
                        model: model)
        }
    }

    String renderSimpleParameters(List simpleParameters) {
        def params = []
        def model = [:]
        try {
            simpleParameters.collect(params) { p ->
                String thisParam
                if (p.assign) {
                    thisParam = MathMlRenderingHelper.convertToMathML(p.symbId, p.assign)
                } else {
                    StringBuilder sb = new StringBuilder("<math display='inline'><mstyle>")
                    sb.append(MathMlRenderingHelper.op(p.symbId)).append("</mstyle></math>")
                    thisParam = sb.toString()
                }
                return thisParam
            }
        } catch(Exception e) {
            model["error"] = "Cannot display simple parameters."
            log.error("Error encountered while rendering simple params ${simpleParameters.inspect()}: ${e.message}", e)
        } finally {
            model["simpleParameters"] = params
            return groovyPageRenderer.render(template: "/templates/simpleParameters",
                        model: model)
        }
    }

    String renderCommonVariables(List vars, String iv) {
        def model = [:]
        // manually set this because we need it for rendering initial conditions
        model["version"] = "0.2.1"
        def initialConditions = [:]
        def variableList = []
        try {
            vars.each { v ->
                switch(v.value) {
                    case DerivativeVariableType:
                        if (v.value.initialCondition) {
                            initialConditions << [(v.value.symbId) : v.value.initialCondition]
                        }
                        def dv = MathMlRenderingHelper.convertToMathML(v.value, iv)
                        variableList.add(dv)
                        break
                    case VariableDefinitionType:
                        if (v.value.assign) {
                            def vd = MathMlRenderingHelper.convertToMathML(v.value.symbId,
                                    v.value.assign)
                            variableList.add(vd)
                        } else {
                            StringBuilder sb = new StringBuilder()
                            sb.append("<math display='inline'><mstyle>")
                            sb.append(MathMlRenderingHelper.op(v.value.symbId))
                            sb.append("</mstyle></math>")
                            variableList.add(sb.toString())
                        }
                        break
                    case FunctionDefinitionType:
                        def fd = v.value
                        variableList.add(MathMlRenderingHelper.convertToMathML(fd.symbId,
                                fd.functionArgument, fd))
                        break
                    case FuncParameterDefinitionType:
                        variableList.add(v.value.symbId)
                        break
                    default:
                        variableList.add(v.value.symbId)
                        break
                }
            }
            model["variableDefinitions"] = variableList
            model["initialConditions"] = initialConditions
        } catch(Exception e) {
            log.error("Error while displaying common variables - arguments ${vars.properties} ${iv}: ${e.message} ")
            model["error"] = "Sorry, ran into issues while trying to display variable definitions."
        } finally {
            return groovyPageRenderer.render(template: "/templates/commonVariables",
                    model: model)
        }
    }

    String renderInitialConditions(Map conditions) {
        def result = []
        conditions.keySet().each { c ->
            result.add MathMlRenderingHelper.convertToMathML(c, conditions[c].assign)
        }
        return groovyPageRenderer.render(template: "/templates/0.2/initialConditions",
                    model: [conditions: result])
    }

    /**
     * @param observationModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.ObservationModelType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderObservationModel(List<ObservationModelType> observationModels) {}

    /**
     * @param trialDesign an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.TrialDesignType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderTrialDesign(TrialDesignType trialDesign) {}

    /**
     * @param structure - an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderTrialDesignStructure(TrialStructureType structure) {}

    /**
     * @param dosing - a list of {@link eu.ddmore.libpharmml.dom.trialdesign.IndividualDosingType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderIndividualDosing(List<IndividualDosingType> dosing) {}

    /**
     * @param population an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.PopulationType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderPopulation(PopulationType population) {}

    /**
     * @param steps an instance of {@link eu.ddmore.libpharmml.dom.modellingsteps.ModellingStepsType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderModellingSteps(ModellingStepsType steps) {}

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.CommonModellingStepType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderCommonModellingSteps(List<CommonModellingStepType> steps) {}

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderSimulationSteps(List<SimulationStepType> steps) {}

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderEstimationSteps(List<EstimationStepType> steps) {}

    /**
     * @param dependencies a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndependentVariable")
    String renderStepDependencies(StepDependencyType dependencies) {}
}
