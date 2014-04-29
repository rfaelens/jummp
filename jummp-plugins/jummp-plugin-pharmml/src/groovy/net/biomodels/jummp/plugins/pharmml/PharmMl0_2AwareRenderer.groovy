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
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import net.biomodels.jummp.core.IPharmMlRenderer
import net.biomodels.jummp.plugins.pharmml.maths.FunctionSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsUtil
import net.biomodels.jummp.plugins.pharmml.maths.OperatorSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PieceSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PiecewiseSymbol
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
                return convertToMathML(d.symbId, d.functionArgument, rightHandSide)
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
    String renderCovariateModel(List<CovariateDefinitionType> covModel, Map transfMap) {
        def model = [:]
        def result = []
        try {
            covModel.each { cm ->
                def thisCovModel = [:]
                thisCovModel["blkId"] = cm.blkId
                if (cm.simpleParameter) {
                    thisCovModel["parameters"] = cm.simpleParameter
                }
                if (cm.covariate) {
                    thisCovModel["covariates"] = cm.covariate
                }
                result.add thisCovModel
            }
        } catch (Exception e) {
            model["error"] = "Sorry, something went wrong while rendering the covariates."
            log.error("Error rendering the covariates ${covModel.inspect()} ${covModel.properties} ${transfMap.inspect()}: ${e.message}", e)

        } finally {
            model["covariateModels"] = result
            model["version"] = "0.2.1"
            model["transfMap"] = transfMap
            return groovyPageRenderer.render(template: "/templates/0.2/covariateModel",
                        model: model)
        }
    }

    String renderCovariates(List<CovariateDefinitionType> cov, String blkId, Map transfMap) {
        def model = [:]
        def covariates = []
        try {
            cov.each { c ->
                def thisCov = [:]
                String symbol = c.symbId
                thisCov["symbId"] = symbol
                if (c.continuous) {
                    def cc = c.continuous
                    def ccMap = [:]
                    final EquationType TRANSF_EQ = cc.transformation.equation
                    final String TRANSF = convertToMathML("Transformation", TRANSF_EQ)
                    final def COV_DISTRIB = cc.abstractContinuousUnivariateDistribution
                    if (COV_DISTRIB) {
                        final String DISTRIB = distributionAssignment(symbol, COV_DISTRIB)
                        ccMap["dist"] = DISTRIB
                    }
                    ccMap["transf"] = TRANSF
                    thisCov["continuous"] = ccMap
                    final String COV_KEY = "${blkId}_${symbol}"
                    transfMap[COV_KEY] = TRANSF_EQ
                } else if (c.categorical) {
                    List cc = c.categorical.category
                    List categoryList = []
                    cc.each{ cat ->
                        StringBuilder sb = new StringBuilder(cat.catId)
                        if (cat.name) {
                            sb.append("(").append(cat.name.value).append(")")
                        }
                        if (cat.probability) {
                            sb.append("&emdash;").append(scalarRhs(cat.probability))
                        }
                        categoryList.add(sb.toString())
                    }
                    String categories = categoryList.join("; ")
                    thisCov["categorical"] = categories
                }
                covariates.add(thisCov)
            }
        } catch (Exception e) {
            log.error("Error rendering the covariates ${covariates.inspect()}:${e.message}", e)
            model["error"] = "Sorry, something went wrong while rendering the covariates."
        } finally {
            model["covariates"] = covariates
            model["version"] = "0.2.1"
            model["transfMap"] = transfMap
            return groovyPageRenderer.render(template: "/templates/0.2/covariates", model: model)
        }
    }

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

    String renderSimpleParameters(List simpleParameters, Map transf = [:]) {
        def params = []
        def model = [:]
        try {
            simpleParameters.collect(params) { p ->
                String thisParam
                if (p.assign) {
                    thisParam = convertToMathML(p.symbId, p.assign, transf)
                } else {
                    StringBuilder sb = new StringBuilder("<math display='inline'><mstyle>")
                    sb.append(op(p.symbId)).append("</mstyle></math>")
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
                        def dv = convertToMathML(v.value, iv)
                        variableList.add(dv)
                        break
                    case VariableDefinitionType:
                        if (v.value.assign) {
                            def vd = convertToMathML(v.value.symbId,
                                    v.value.assign)
                            variableList.add(vd)
                        } else {
                            StringBuilder sb = new StringBuilder()
                            sb.append("<math display='inline'><mstyle>")
                            sb.append(op(v.value.symbId))
                            sb.append("</mstyle></math>")
                            variableList.add(sb.toString())
                        }
                        break
                    case FunctionDefinitionType:
                        def fd = v.value
                        variableList.add(convertToMathML(fd.symbId,
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
            result.add convertToMathML(c, conditions[c].assign)
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

    // BEGIN C-P FROM TAGLIB
    StringBuilder distributionAssignment(String l, def d) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(l))
        builder.append(op("&sim;"))
        builder.append(distribution(d))
        builder.append("</mstyle></math>")
        return builder
    }

    StringBuilder distribution(def d) {
        if (!d) {
            return
        }
        def StringBuilder result = new StringBuilder(oprand("N"))
        result.append(op("("))
        def distributionType = d.value
        if (distributionType instanceof NormalDistribution) {
            result.append(normalDistribution(d))
        }
        result.append(op(")"))
        return result
    }

    String normalDistribution(def dist) {
        StringBuilder result = new StringBuilder()
        NormalDistribution d = dist.value
        String mean = (d.mean.var?.varId) ? (d.mean.var.varId) : (d.mean.rVal)
        result.append(oprand(mean))
        String stdDev = d.stddev?.var?.varId ? d.stddev.var.varId : d.stddev?.prVal
        if (stdDev) {
            result.append(op(",")).append(oprand(stdDev))
        }
        String variance = d.variance?.var?.varId ? d.variance.var.varId : d.variance?.prVal
        if (variance) {
            result.append(op(",")).append(oprand(variance))
        }

        return result.toString()
    }

    StringBuilder scalarRhs(def r) {
        StringBuilder text = new StringBuilder()
        if (r.scalar) {
            text.append(scalar(r.scalar.value))
        } else if (r.equation) {
            text.append(convertToMathML(r.equation))
        } else if (r.symbRef) {
            text.append(r.symbRef.symbIdRef)
        }
        return text
    }

    StringBuilder rhs(Rhs r, StringBuilder text) {
        if (r.equation) {
            return text.append(convertToMathML(r.equation))
        }
        if (r.sequence) {
            return text.append(sequence(r.sequence))
        }
        if (r.vector) {
            return text.append(jaxbVector(r.vector))
        }
        if (r.symbRef) {
            return text.append(r.symbRef.symbIdRef)
        }
        if (r.scalar) {
            return text.append(scalar(r.scalar.value))
        } else {
            return text.append(r.toString())
        }
    }

    String scalar(def s) {
        switch(s) {
            case RealValueType:
            case IntValueType:
            case StringValueType:
            case IdValueType:
                return s.value as String
                break
            case TrueBooleanType:
                return "true"
                break
            case FalseBooleanType:
                return "false"
                break
            default:
                return s.toString()
        }
    }

    String sequence(SequenceType s) {
        return [s.begin, s.stepSize, s.end].collect{rhs(it, new StringBuilder())}.join(":")
    }

    StringBuilder sequenceAsMathML(def s) {
        return new StringBuilder(op("[")).append(oprand(s.begin)).append(op(":")).
                   append(oprand(s.stepSize)).append(op(":")).
                   append(oprand(s.end)).append(op("]"))
    }

    StringBuilder vectorAsMathML(def v) {
        def result = new StringBuilder()
        if (!v) {
            return result.append(" ")
        }
        result.append(op("["))
        def iterator = v.vector.sequenceOrScalar.iterator()
        while (iterator.hasNext()) {
            //can be a scalar or a sequence
            def vectorElement = iterator.next().value
            switch (vectorElement) {
                case SequenceType:
                    result.append(sequence(vectorElement))
                    break
                case IdValueType:
                case StringValueType:
                case IntValueType:
                case RealValueType:
                case TrueBooleanType:
                case FalseBooleanType:
                    result.append(oprand(vectorElement.value.toString()))
                    break
                default:
                    assert false, "Vectors can only contain scalars or sequences."
                    log.error("Vectors cant contain ${vectorElement.inspect()} ${vectorElement.properties}")
                    break
            }
            if (iterator.hasNext()) {
                result.append(op(","))
            }
        }
        result.append(op("]"))
    }

    StringBuilder vector(def v) {
        def result = new StringBuilder()
        if (!v) {
            return result.append(" ")
        }
        result.append("[")
        def iterator = v.sequenceOrScalar.iterator()
        while (iterator.hasNext()) {
            //can be a scalar or a sequence
            def vectorElement = iterator.next()
            def item
            try {
                item = vectorElement as ScalarRhs
                result.append(item.value.toPlainString())
            } catch (ClassCastException ignored) {
                item = vectorElement as SequenceType
                result.append(sequence(item))
            }
            if (iterator.hasNext()) {
                result.append(",")
            }
        }
        result.append("]")
    }

    private void prefixToInfix(StringBuilder builder, List<MathsSymbol> stack) {
        if (stack.isEmpty()) {
            return;
        }
        MathsSymbol symbol=stack.pop()
        if (symbol instanceof OperatorSymbol) {
            OperatorSymbol operator=symbol as OperatorSymbol
            if (operator.type==OperatorSymbol.OperatorType.BINARY) {
                builder.append(operator.getOpening())
                if (operator.needsTermSeparation) {
                    builder.append("<mrow>")
                }
                prefixToInfix(builder,stack)
                if (operator.needsTermSeparation) {
                    builder.append("</mrow>")
                }
                builder.append(operator.getMapping())
                if (operator.needsTermSeparation) {
                    builder.append("<mrow>")
                }
                prefixToInfix(builder,stack)
                if (operator.needsTermSeparation) {
                    builder.append("</mrow>")
                }
                builder.append(operator.getClosing())
            }
            // Special case of root/square root, handled differently from other
            // operators.
            else if (operator.type==OperatorSymbol.OperatorType.ROOT) {
                StringBuilder operandBuilder=new StringBuilder()
                prefixToInfix(operandBuilder, stack)
                StringBuilder rootBuilder=new StringBuilder()
                prefixToInfix(rootBuilder, stack)
                boolean isSquareRoot=false
                try {
                    String rootValue=rootBuilder.toString().replace("<mi>","").replace("</mi>","")
                    double value = Double.parseDouble(rootValue)
                    if (value==2.0) {
                        isSquareRoot=true
                    }
                } catch(Exception notANumber) {}
                if (!isSquareRoot) {
                    builder.append("<mroot><mrow>")
                    builder.append(operandBuilder)
                    builder.append("</mrow><mrow>")
                    builder.append(rootBuilder)
                    builder.append("</mrow></mroot>")
                }
                else {
                    builder.append("<msqrt>")
                    builder.append(operandBuilder)
                    builder.append("</msqrt>")
                }
            }
            // Special case of power, handled differently from other
            // operators.
            else if (operator.type==OperatorSymbol.OperatorType.POWER) {
                StringBuilder operandBuilder=new StringBuilder()
                prefixToInfix(operandBuilder, stack)
                StringBuilder powerBuilder=new StringBuilder()
                prefixToInfix(powerBuilder, stack)
                boolean isSquareRoot=false
                builder.append("<msup><mrow>")
                builder.append(operandBuilder)
                builder.append("</mrow><mrow>")
                builder.append(powerBuilder)
                builder.append("</mrow></msup>")
            }
            else {
                builder.append(operator.getMapping())
                builder.append(operator.getOpening())
                prefixToInfix(builder,stack)
                builder.append(operator.getClosing())
            }
        } 
        else if (symbol instanceof FunctionSymbol) {
            FunctionSymbol function=symbol as FunctionSymbol
            builder.append(function.getMapping())
                builder.append(function.getOpening())
                for (int i=0; i<function.getArgCount(); i++) {
                    prefixToInfix(builder, stack)
                    if (i!=function.getArgCount()-1) {
                        builder.append(function.getArgSeparator())
                    }
                }
                builder.append(function.getClosing())
        }
        else if (symbol instanceof PiecewiseSymbol) {
            PiecewiseSymbol piecewise=symbol as PiecewiseSymbol
            builder.append(piecewise.getOpening())
            for (int i=0; i<piecewise.getPieceCount(); i++) {
                prefixToInfix(builder, stack)
            }
            builder.append(piecewise.getClosing())
        }
        else if (symbol instanceof PieceSymbol) {
            PieceSymbol piece=symbol as PieceSymbol
            builder.append(piece.getOpening())
            builder.append(piece.getTermStarter())
            prefixToInfix(builder, stack)
            builder.append(piece.getTermEnder())
            if (piece.type==PieceSymbol.ConditionType.OTHERWISE) {
                builder.append(piece.getOtherwiseText())
            }
            else {
                builder.append(piece.getIfText())
                builder.append(piece.getTermStarter())
                prefixToInfix(builder, stack)
                builder.append(piece.getTermEnder())
            }
            builder.append(piece.getClosing())
        }
        else {
            builder.append(symbol.getMapping())
        }
       // prefixToInfix(builder, stack)
    }

    private JAXBElement expandNestedSymbRefs(JAXBElement<SymbolRefType> symbRef,
            Map<String, Equation> transformations) {
        final EquationType TRANSF_EQ = resolveSymbolReference(symbRef.value, transformations)
        if (TRANSF_EQ) {
            final def FIRST_ELEM = TRANSF_EQ.scalarOrSymbRefOrBinop.first()
            final Class ELEM_CLASS = FIRST_ELEM.value.getClass()
            switch(ELEM_CLASS) {
                case BinopType:
                    break
                case UniopType:
                    break
                case SymbolRefType:
                    break
                case ConstantType:
                    break
                case FunctionCallType:
                    break
                case IdValueType:
                    break
                case StringValueType:
                    break
                case IntValueType:
                    break
                case RealValueType:
                    break
                default:
                    assert false, "Cannot have ${ELEM_CLASS.name} inside a transformation."
                    break
            }
            return FIRST_ELEM
        } else {
            return symbRef
        }
    }

    private JAXBElement expandNestedUniop(JAXBElement<UniopType> jaxbUniop,
            Map<String, Equation> transfMap) {
        UniopType uniop = jaxbUniop.value
        UniopType replacement
        if (uniop.symbRef) {
            final EquationType TRANSF_EQ = resolveSymbolReference(uniop.symbRef, transfMap)
            if (TRANSF_EQ) {
                final def FIRST_ELEM = TRANSF_EQ.scalarOrSymbRefOrBinop.first().value
                final Class ELEM_CLASS = FIRST_ELEM.getClass()
                replacement = new UniopType()
                replacement.op = uniop.op
                switch(ELEM_CLASS) {
                    case BinopType:
                        replacement.binop = FIRST_ELEM
                        break
                    case UniopType:
                        replacement.uniop = FIRST_ELEM
                        break
                    case SymbolRefType:
                        replacement.symbRef = FIRST_ELEM
                        break
                    case ConstantType:
                        replacement.constant = FIRST_ELEM
                        break
                    case FunctionCallType:
                        replacement.functionCall = FIRST_ELEM
                        break
                    case IdValueType:
                        replacement.scalar = FIRST_ELEM
                        break
                    case StringValueType:
                        break
                    case IntValueType:
                        replacement.scalar = FIRST_ELEM
                        break
                    case RealValueType:
                        replacement.scalar = FIRST_ELEM
                        break
                    default:
                        assert false, "Cannot have ${ELEM_CLASS.name} inside a unary operator."
                        replacement = null
                        break
                }
            }
        } else if (uniop.uniop) {
            def expanded = expandNestedUniop(wrapJaxb(uniop.uniop), transfMap)?.value
            if (expanded && !(expanded.equals(uniop.uniop))) {
                uniop.uniop = expanded
            }
        } else if (uniop.binop) {
            def expanded = expandNestedBinop(wrapJaxb(uniop.binop), transfMap)?.value
            if (expanded && !(expanded.equals(uniop.binop))) {
                uniop.binop = expanded
            }
        }
        if (replacement) {
            return wrapJaxb(replacement)
        }
        return jaxbUniop
    }

    private JAXBElement expandNestedBinop(JAXBElement<BinopType> jaxbBinop,
            Map<String, Equation> transfMap) {
        BinopType binop = jaxbBinop.value
        List<JAXBElement> terms = binop.content
        def expandedTerms = terms.collect { c ->
            switch (c.value) {
                case SymbolRefType:
                    return expandNestedSymbRefs(c, transfMap)
                    break
                case BinopType:
                    return expandNestedBinop(c, transfMap)
                    break
                case UniopType:
                    return expandNestedUniop(c, transfMap)
                    break
                default:
                    return c
                    break
            }
        }
        if (expandedTerms.equals(terms)) {
            return jaxbBinop
        }
        BinopType expanded = new BinopType()
        expanded.op = binop.op
        expanded.content = expandedTerms
        return wrapJaxb(expanded)
    }

    private EquationType expandEquation(EquationType equation, Map<String, Equation> transfMap) {
        List<JAXBElement> eqTerms = equation.scalarOrSymbRefOrBinop
        List<JAXBElement> expandedTerms = eqTerms.collect {
            switch(it.value) {
                case BinopType:
                    return expandNestedBinop(it, transfMap)
                    break
                case UniopType:
                    return expandNestedUniop(it, transfMap)
                    break
                case SymbolRefType:
                    return expandNestedSymbRefs(it, transfMap)
                    break
                default:
                    return it
                    break
            }
        }
        if (!eqTerms.equals(expandedTerms)) {
            def newEquation = new EquationType()
            newEquation.scalarOrSymbRefOrBinop = expandedTerms
            return newEquation
        }
        return equation
    }

    private void convertEquation(def equation, StringBuilder builder, Map<String, Equation> transfMap = [:]) {
        def equationToProcess
        if (!transfMap) {
            equationToProcess = equation
        } else if ((equation instanceof EquationType) || (equation instanceof Equation)) {
            equationToProcess = expandEquation(equation, transfMap)
        } else {
            equationToProcess = equation
        }
        List<MathsSymbol> symbols = MathsUtil.convertToSymbols(equationToProcess).reverse()
        List<String> stack = new LinkedList<String>()
        symbols.each {
           stack.push(it)
        }
        prefixToInfix(builder, stack)
    }

    private String convertToMathML(def equation, Map<String, Equation> transfMap = [:]) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        convertEquation(equation, builder, transfMap)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    private String convertToMathML(String lhs, def equation, Map<String, Equation> transfMap = [:]) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(lhs))
        builder.append(op("="))
        convertEquation(equation, builder, transfMap)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    //works with EquationType and Equation as well
    private String convertToMathML(EquationType lhs, EquationType rhs, Map<String, Equation> transfMap = [:]) {
        StringBuilder output = new StringBuilder("<div>")
        output.append("<math display='inline'><mstyle>")
        convertEquation(lhs, output)
        output.append(op("="))
        convertEquation(rhs, output, transfMap)
        output.append("</mstyle></math>")
        return output.append("</div>").toString()
    }

    private String convertToMathML(String lhs, ScalarRhs srhs, Map<String, Equation> transfMap = [:]) {
         if (srhs.equation) {
            return convertToMathML(lhs, srhs.equation, transfMap)
        }
        if (srhs.symbRef) {
            return convertToMathML(lhs, srhs.symbRef, transfMap)
        }
        StringBuilder result = new StringBuilder("<math display='inline'><mstyle>")
        result.append(oprand(lhs)).append(op("="))
        if (srhs.scalar) {
            result.append(oprand(scalar(srhs.scalar.value)))
        }
        return result.append("</mstyle></math>").toString()
    }

    private String convertToMathML(String lhs, Rhs rhs, Map<String, Equation> transfMap = [:]) {
        if (rhs.equation) {
            return convertToMathML(lhs, rhs.equation, transfMap)
        }
        if (rhs.symbRef) {
            return convertToMathML(lhs, rhs.symbRef, transfMap)
        }
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(lhs))
        builder.append(op("="))
        if (rhs.getScalar()) {
            builder.append(oprand(scalar(rhs.scalar.value)))
        }
        else if (rhs.getSequence()) {
            builder.append(sequenceAsMathML(rhs.sequence))
        }
        else if (rhs.getVector()) {
            builder.append(vectorAsMathML(rhs))
        }
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    private String convertToMathML(DerivativeVariableType derivative, def iv) {
        String independentVariable = derivative.independentVariable?.symbRef?.symbIdRef ?: (iv ?: "t")
        String derivTerm="d${derivative.symbId}<DIVIDEDBY>d${independentVariable}"
        return convertToMathML(derivTerm, derivative.getAssign())
    }

    private String convertToMathML(String lhs, List arguments, def equation) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(lhs))
        builder.append(op("("))
        for (int i=0; i<arguments.size(); i++) {
            builder.append(oprand(arguments.get(i).symbId))
            if (i<arguments.size()-1) {
                builder.append(op(","))
            }
        }
        builder.append(op(")"))
        builder.append(op("="))
        convertEquation(equation, builder)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    private void convertToMathML(String name, def matrix, List ipNames, StringBuilder output) {
        output.append("<div class='spaced'>")
        output.append("<span class='bold'>Correlation matrix for level <span class='italic'>")
        output.append(name).append("</span> and parameters: ")
        output.append(ipNames.join(", ")).append("</span></div>\n")
        output.append("<math display='inline'><mstyle>\n")
        output.append("<mrow>").append(op("(")).append("<mtable>\n")
        final int N = matrix.size()
        for (int i = 0; i < N; i++) {
            output.append("<mtr>")
            for (int j = 0; j < N; j++) {
                output.append("<mtd><mi>")
                output.append(matrix[i][j])
                output.append("</mi></mtd>\n")
            }
            output.append("</mtr>\n")
        }
        output.append("</mtable>\n").append(op(")")).append("</mrow>")
        output.append("</mstyle></math>")
    }

    private String op(String o) {
        return "<mo>${o}</mo>"
    }

    private String oprand(String o) {
        if (o.contains("<DIVIDEDBY>")) {
            String[] parts=o.split("<DIVIDEDBY>")
            return "<mfrac><mi>${parts[0]}</mi><mi>${parts[1]}</mi></mfrac>"
        }
        return "<mi>${o}</mi>"
    }

    private JAXBElement wrapJaxb(def elem) {
        return elem instanceof JAXBElement ? elem : new JAXBElement(new QName(""), elem.getClass(), elem)
    }

    private JAXBElement applyBinopToList(List elements, String operator) {
        if (elements.size() == 1) {
            // just return the element
            return wrapJaxb(elements.first())
        } else {
            def result = new BinopType()
            result.op = operator
            final int LAST = elements.size() - 1
            result.content = []
            result.content.add(wrapJaxb(elements.first()))
            result.content.add(applyBinopToList(elements[1..LAST], operator))
            return wrapJaxb(result)
        }
    }

    /*
     * Looks up a symbol reference in continuousCovariateTransformations map.
     * If the given @p ref contains both a blkIdRef and a symbIdRef, then it returns
     * the equation corresponding to that continuous covariate, or null if there is no
     * covariate with that @ref defined in the map.
     *
     * If @ref only has a symbIdRef, then it will return the first element from the map
     * that matches, or null if there were no matches.
     */
    private EquationType resolveSymbolReference(SymbolRefType ref, Map<String, Equation> transfMap) {
        EquationType transfEq
        if (ref.blkIdRef) {
            String transfRef = "${ref.blkIdRef}_${ref.symbIdRef}"
            transfEq = transfMap[transfRef]
        } else {
            String transfRef = ref.symbIdRef
            transfEq = transfMap.find{ it.key.contains("_${transfRef}")}?.value
        }
        return transfEq
    }

    private void populateRandomVariableMap(final String id, final String level, Map rv) {
        def currentRVs = rv[level]
        if (!currentRVs) {
            currentRVs = []
        }
        currentRVs.add(id)
        rv[level] = currentRVs
    }
}
