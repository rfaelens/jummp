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
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariableType
import eu.ddmore.libpharmml.dom.commontypes.FalseBooleanValue
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
import eu.ddmore.libpharmml.dom.commontypes.TrueBooleanValue
import eu.ddmore.libpharmml.dom.commontypes.VariableAssignmentType
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinitionType
import eu.ddmore.libpharmml.dom.commontypes.VectorType
import eu.ddmore.libpharmml.dom.dataset.ColumnDefinition
import eu.ddmore.libpharmml.dom.dataset.DataSetTableDefnType
import eu.ddmore.libpharmml.dom.dataset.DataSetTableType
import eu.ddmore.libpharmml.dom.dataset.DataSetType
import eu.ddmore.libpharmml.dom.maths.Binop
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
import eu.ddmore.libpharmml.dom.modeldefn.CovariateModelType
import eu.ddmore.libpharmml.dom.modeldefn.GaussianObsError
import eu.ddmore.libpharmml.dom.modeldefn.GeneralObsError
import eu.ddmore.libpharmml.dom.modeldefn.IndividualParameterType
import eu.ddmore.libpharmml.dom.modeldefn.LhsTransformationType
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition
import eu.ddmore.libpharmml.dom.modeldefn.ObservationModelType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterRandomVariableType
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameterType
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityDefnBlock
import eu.ddmore.libpharmml.dom.modellingsteps.CommonModellingStepType
import eu.ddmore.libpharmml.dom.modellingsteps.DatasetMappingType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps
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
import eu.ddmore.libpharmml.dom.trialdesign.TrialDesign
import eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType
import eu.ddmore.libpharmml.dom.uncertml.NormalDistribution
import grails.gsp.PageRenderer
import grails.util.Holders
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import net.biomodels.jummp.plugins.pharmml.maths.FunctionSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsUtil
import net.biomodels.jummp.plugins.pharmml.maths.OperatorSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PieceSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PiecewiseSymbol
import net.biomodels.jummp.plugins.pharmml.util.correlation.CorrelationMatrix
import net.biomodels.jummp.plugins.pharmml.util.correlation.PharmMl0_2AwareCorrelationProcessor
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled

class PharmMl0_2AwareRenderer extends AbstractPharmMlRenderer {
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
     * @param modelDefinition an instance of {@link eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderModelDefinition")
    String renderModelDefinition(ModelDefinition modelDefinition) {}

    /**
     * @param covModel a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.CovariateModelType}s.
     * @param transfMap the transformations for continuous covariates.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderCovariateModel")
    String renderCovariateModel(List<CovariateModelType> covModel, Map transfMap) {
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

    /**
     * @param cov a a list of {@link eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType}s.
     * @param blkId the block identifier of the covariate model where @p cov are defined.
     * @param transfMap the transformations for continuous covariates.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderCovariates")
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
     * Expects as arguments the parameter model as well as the covariate model.
     * The latter is necessary to display the transformations that are defined
     * for each individual parameter.
     *
     * @param parameterModel a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.ParameterModelType}s.
     * @param covariates a list of covariate models.
     * @param transfMap the transformations for the continuous covariates.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderParameterModel")
    String renderParameterModel(List<ParameterModelType> parameterModel,
            List<CovariateModelType> covariates, Map transfMap) {

        def result = new StringBuilder()
        result.append("<h3>Parameter Model</h3>")
        result.append("<span class=\"bold\">Parameters </span>")
        try {
            parameterModel.each { pm ->
                result.append("<div class='spaced-top-bottom'>")
                def simpleParameters = pm.commonParameterElement.value.findAll {
                       it instanceof SimpleParameterType
                }
                def rv = pm.commonParameterElement.value.findAll {
                       it instanceof ParameterRandomVariableType
                }
                def individualParameters = pm.commonParameterElement.value.findAll {
                       it instanceof IndividualParameterType
                }
                result.append(simpleParams(simpleParameters, transfMap))

                Map<String, List<String>> paramRandomVariableMap = [:]
                String randoms = randomVariables(rv, paramRandomVariableMap)
                if (randoms) {
                   result.append(randoms)
                }
                StringBuilder individuals = individualParams(individualParameters, rv, covariates,
                                                transfMap)
                if (individuals) {
                   result.append(individuals)
                }
                if (pm.correlation) {
                    def processor = new PharmMl0_2AwareCorrelationProcessor()
                    List<CorrelationMatrix> matrices = processor.convertToStringMatrix(
                                pm.correlation, paramRandomVariableMap)
                    if (matrices) {
                        displayCorrelationMatrices(matrices, result)
                    }

                }
                result.append("</div>")
            }
        } catch(Exception e) {
            log.error("Error rendering the parameter model for ${parameterModel.inspect()} ${parameterModel.properties}: ${e.message}", e)
            result.append("Sorry, something went wrong while rendering the parameter model.")
        } finally {
            return result.toString()
        }
    }

    /**
     * @param structuralModels a list of
     * {@link eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType}s.
     * @param iv the independent variable of the model
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderStructuralModel")
    String renderStructuralModel(List<StructuralModelType> structuralModels, String iv) {
        def model = [:]
        model["version"] = "0.2.1"
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
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderObservationModel")
    String renderObservationModel(List<ObservationModelType> observations,
                List<CovariateModelType> covariates) {
        StringBuilder result = new StringBuilder()
        result.append("<h3>Observation Model</h3>")
        try {
            observations.each { om ->
                result.append("<h4>Observation")
                // the API returns a JAXBElement, not ObservationErrorType
                def obsErr = om.observationError?.value
                if (obsErr) {
                    result.append(" <span class='italic'>")append(obsErr.symbId).append("</span>")
                }
                result.append("</h4>\n")
                result.append("<span class=\"bold\">Parameters </span>")
                def simpleParameters = om.commonParameterElement.value.findAll {
                    it instanceof SimpleParameterType
                }
                def rv = om.commonParameterElement.value.findAll {
                    it instanceof ParameterRandomVariableType
                }
                def individualParameters = om.commonParameterElement.value.findAll {
                       it instanceof IndividualParameterType
                }
                result.append(simpleParams(simpleParameters))

                Map<String, List<String>> obsRandomVariableMap = [:]

                String randoms = randomVariables(rv, obsRandomVariableMap)
                if (randoms) {
                    result.append(randoms)
                }
                StringBuilder individuals = individualParams(individualParameters, rv, covariates,
                            [:])
                if (individuals) {
                   result.append(individuals)
                }
                if (om.correlation) {
                    def processor = new PharmMl0_2AwareCorrelationProcessor()
                    List<CorrelationMatrix> matrices = processor.convertToStringMatrix(
                                om.correlation, obsRandomVariableMap)
                    if (matrices) {
                        displayCorrelationMatrices(matrices, result)
                    }
                }
                if (obsErr) {
                    if (obsErr.symbol?.value) {
                        result.append(obsErr.symbol.value)
                    }
                    if (obsErr instanceof GaussianObsError) {
                        result.append(gaussianObsErr(obsErr)).append(" ")
                    } else { // can only be GeneralObsError
                        result.append(generalObsErr(obsErr)).append(" ")
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error rendering the observations ${observations.inspect()}: ${e.message}")
            result.append "Sorry, something went wrong while rendering the observations."
        } finally {
            return result.toString()
        }
    }

    /**
     * @param trialDesign an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.TrialDesign}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderTrialDesign")
    String renderTrialDesign(TrialDesign trialDesign) {}

    /**
     * @param structure - an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.TrialStructureType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderTrialDesignStructure")
    String renderTrialDesignStructure(TrialStructureType structure) {
        def result = new StringBuilder()
        TrialDesignStructure tds
        def segmentActivitiesMap
        result.append("<h3>Structure overview</h3>\n")
        StringBuilder structureBuilder=new StringBuilder();
        try {
            tds = new TrialDesignStructure(structure.arm, structure.epoch,
                        structure.cell, structure.segment)
            def armRefs     = new ArrayList(tds.getArmRefs())
            def epochRefs   = new ArrayList(tds.getEpochRefs())
            /* arm-epoch matrix*/
            structureBuilder.append("<table><thead><tr><th class='bold'>Arm/Epoch</th>")
            for (String e: epochRefs) {
                structureBuilder.append("<th class='bold'>").append(e).append("</th>")
            }
            structureBuilder.append("</tr></thead><tbody>\n")
            for (String a: armRefs) {
                structureBuilder.append("<tr><th class='bold'>").append(a).append("</th>")
                tds.findSegmentRefsByArm(a).each { s ->
                    structureBuilder.append("<td>").append(s).append("</td>")
                }
                structureBuilder.append("</tr>\n")
            }
            structureBuilder.append("</tbody></table>\n")
            result.append(structureBuilder.toString());
        } catch(Exception e) {
            result.append("Cannot display the arm-epoch matrix.")
            def errMsg = new StringBuilder("Error encountered while rendering the arm-epoch matrix of")
            errMsg.append("trial design structure ${structure.properties} ")
            errMsg.append("using helper ${tds.trialDesignStructure.inspect()}: ")
            log.error(errMsg, e)
        }
        StringBuilder segActBuilder=new StringBuilder();
        try {
            /* segments and activities */
            List activities = structure.activity
            // avoid the need to increase the size of the map, because re-hashing is expensive
            segmentActivitiesMap = new HashMap(activities.size() + 1, 1.0)
            structure.segment.each { s ->
                segmentActivitiesMap[s.oid] = s.activityRef.collect{ a ->
                    structure.activity.find{ a.oidRef.equals(it.oid) }
                }
            }
            boolean showDosingFootnote = false
            segActBuilder.append("<h4>Segment-Activity definition</h4>\n")
            segActBuilder.append("<table style='margin-bottom:0px;'><thead><tr><th class='bold'>Segment</th><th class='bold'>Activity</th>")
            segActBuilder.append("<th class='bold'>Treatment</th><th class='bold'>Dose time</th>")
            segActBuilder.append("<th class='bold'>Dose size</th><th class='bold'>Target variable</th></tr></thead><tbody>")
            if (segmentActivitiesMap) {
                segmentActivitiesMap.entrySet().each {
                    def activityList = it.value
                    if (!activityList) {
                        segActBuilder.append("<tr><td colspan='6'></td></tr>")
                    } else {
                        final int ACTIVITY_COUNT = activityList.size()
                        segActBuilder.append("<tr><td")
                        if (ACTIVITY_COUNT > 1) {
                            segActBuilder.append(" rowspan='").append(ACTIVITY_COUNT).append("'")
                        }
                        segActBuilder.append(">").append(it.key).append("</td>")
                        boolean toShowFootnote = activity(activityList[0], true, segActBuilder)
                        if ((!showDosingFootnote) && toShowFootnote) {
                            showDosingFootnote = toShowFootnote
                        }
                        if (ACTIVITY_COUNT > 1) {
                            for (int i = 1; i < ACTIVITY_COUNT; i++) {
                                toShowFootnote = activity(activityList[i], false, segActBuilder)
                                if (!showDosingFootnote && toShowFootnote) {
                                    showDosingFootnote = toShowFootnote
                                }
                            }
                        }
                    }
                }
                segActBuilder.append("</tbody></table>\n")
            }
            if (showDosingFootnote) {
                segActBuilder.append("<span>* &ndash; Element defined in the Individual dosing section.</span>")
            }
            result.append(segActBuilder.toString());
        } catch(Exception e) {
            result.append("Cannot display the segment-activity overview.")
            def errMsg = new StringBuilder("Cannot display the segment-activity overview for structure ")
            errMsg.append(structure.properties).append(" using helper map ")
            errMsg.append(segmentActivitiesMap.inspect())
            log.error(errMsg, e)
        }
        ObservationEventsMap oem
        StringBuilder epOccBuilder=new StringBuilder();
        /* epochs and occasions */
        try {
            if (structure.studyEvent) {
                oem = new ObservationEventsMap(structure.studyEvent)
                def arms = oem.getArms()
                def epochs = oem.getEpochs()
                epOccBuilder.append("\n<h4>Epoch-Occasion definition</h4>\n")
                epOccBuilder.append("<table><thead><tr><th class='bold'>Arm/Epoch</th>")
                for (String e: epochs) {
                    epOccBuilder.append("<th class='bold'>").append(e).append("</th>")
                }
                epOccBuilder.append("</tr></thead><tbody>\n")
                arms.each { a ->
                    epOccBuilder.append("<tr><th class='bold'>").append(a).append("</th>")
                    def occ = oem.findOccasionsByArm(a)
                    occ.each {
                        //TODO OCCASIONS CAN HAVE MANY ENTRIES
                        def o = it.firstEntry()
                        epOccBuilder.append("<td>")
                        epOccBuilder.append("<div>").append(o.key).append("</div><div><span class='bold'>")
                        epOccBuilder.append(o.value).append("</span> variability</div></td>")
                    }
                    epOccBuilder.append("</tr>")
                }
                epOccBuilder.append("</tbody></table>\n")
            }
            result.append(epOccBuilder.toString());
        } catch(Exception e) {
            result.append("<p>Cannot display the epoch-occasion overview.</p>")
            def errMsg = new StringBuilder("Cannot display the epoch-occasion overview for structure ")
            errMsg.append(structure.properties).append(" using helper map ")
            errMsg.append(oem.inspect())
            log.error(errMsg, e)
        } finally {
            return result.toString()
        }
    }

    /**
     * @param dosing - a list of {@link eu.ddmore.libpharmml.dom.trialdesign.IndividualDosingType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderIndividualDosing")
    String renderIndividualDosing(List<IndividualDosingType> dosing, RevisionTransportCommand rev, String downloadLink) {
        def result = new StringBuilder()
        try {
            result.append("<h4>Individual dosing</h4>\n")
            dosing.each { d ->
                if (d.dataSet) {
                    dataSet(d.dataSet, null, result, rev, downloadLink)
                }
            }
        } catch(Exception e) {
            result.append("Cannot display the trial dosing.")
            def errMsg = new StringBuilder("Cannot display the trial dosing ")
            errMsg.append(d.properties)
            log.error(errMsg, e)
        } finally {
            return result.toString()
        }
    }

    /**
     * @param population an instance of {@link eu.ddmore.libpharmml.dom.trialdesign.PopulationType}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderPopulation")
    String renderPopulation(PopulationType pop, RevisionTransportCommand rev, String downloadLink) {
        def result = new StringBuilder("<h4>Population</h4>\n")
        if (pop.variabilityReference) {
            result.append("<span><strong>Variability level: </strong>")
            result.append(pop.variabilityReference.symbRef.symbIdRef).append("</span>")
        }
        try {
            if (pop.dataSet) {
                dataSet(pop.dataSet, null, result, rev, downloadLink)
            }
        } catch (Exception e) {
            result.append("Cannot display population data set.")
            def errMsg = new StringBuilder()
            errMsg.append("Cannot display population data set ")
            errMsg.append(pop.dataSet.properties).append( "for population ")
            errMsg.append(pop.properties)
            log.error(errMsg, e)
        } finally {
            return result.toString()
        }
    }

    /**
     * @param steps an instance of {@link eu.ddmore.libpharmml.dom.modellingsteps.ModellingSteps}
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderModellingSteps")
    String renderModellingSteps(ModellingSteps steps) {}

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.CommonModellingStepType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderCommonModellingSteps")
    String renderCommonModellingSteps(List<CommonModellingStepType> steps) {}

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderSimulationSteps")
    String renderSimulationSteps(List<SimulationStepType> steps, String iv) {
        def result = new StringBuilder("<h3>Simulation Steps</h3>\n")
        steps.each { s ->
            result.append("<h4>Simulation step <span class='italic'>${s.oid}</span></h4>")
            if (s.variableAssignment) {
                result.append(variableAssignments(s.variableAssignment, "<h5>Variable assignments</h5>\n"))
            }
            if (s.observations) {
                s.observations.each { o ->
                    result.append("\n<h5>Observation</h5>\n")
                    if (o.continuous) {
                        result.append("<p><span class=\"bold\">Type:</span>Continuous</p>\n")
                        if (o.continuous.symbRef) {
                            result.append("<p><span class=\"bold\">Variables:</span>")
                            //check for symbIdRef clashes and display the blkIdRef accordingly
                            List vars = o.continuous.symbRef
                            List allVars = []
                            List uniqueVars = []
                            vars.each {
                                uniqueVars << it.symbIdRef
                                allVars << [it.blkIdRef, it.symbIdRef].join('.')
                            }
                            boolean needBlk = allVars.size() != uniqueVars.unique().size()
                            vars.inject(result) { r, v ->
                                r.append("${needBlk ? v.blkIdRef + '.': ''}${v.symbIdRef} ")
                            }
                            result.append("</p>\n")
                        }
                    }
                    if (o.timepoints) {
                        result.append("<p><span class=\"bold\">Independent variable")
                        if (iv) {
                            result.append("(").append(iv).append(")")
                        }
                        result.append(":</span>\n")
                        // put all timepoints here, output them separated by commas
                        List<String> observationTimepoints = []
                        o.timepoints.arrays.each { a ->
                            //JAXBElement parameterised with SequenceType or VectorType
                            if (a.value instanceof VectorType) {
                                observationTimepoints << jaxbVector(a.value).toString()
                            } else if (a.value instanceof SequenceType) {
                                observationTimepoints << sequence(a.value).toString()
                            }
                        }
                        result.append(
                            observationTimepoints.size() == 1 ? observationTimepoints[0] :
                                    "[${observationTimepoints.join(', ')}]")
                        result.append("</p>\n")
                    }
                }
            }
        }
        return result
    }

    /**
     * @param steps a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderEstimationSteps")
    String renderEstimationSteps(List<EstimationStepType> steps, RevisionTransportCommand rev, String downloadLink) {
        def result = new StringBuilder("<h3>Estimation Steps</h3>\n")
        steps.each { s ->
            result.append("<h4>Estimation Step ${s.oid}</h4>\n")
            if (s.variableAssignment) {
                result.append(variableAssignments(s.variableAssignment, "<h5>Variable assignments</h5>"))
            }
            if (s.parametersToEstimate) {
                result.append(paramsToEstimate(s.parametersToEstimate))
            }
            if (s.operation) {
                result.append(formatOperations(s.operation))
            }
            if (s.objectiveDataSet) {
                result.append(objectiveDataSetMapping(s.objectiveDataSet, rev, downloadLink))
            }
        }
        return result.toString()
    }

    /**
     * @param dependencies a list of {@link eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType}s.
     */
    @Profiled(tag = "pharmMl0_2AwareRenderer.renderStepDependencies")
    String renderStepDependencies(StepDependencyType dependencies) {
        StringBuilder result = new StringBuilder()
        if (!dependencies || !dependencies.step) {
            return result
        }
        result.append("<h3>Step Dependencies</h3>")
        result.append("\n<ul>")
        dependencies.step.inject(result) { r, s ->
            StringBuilder dep = new StringBuilder(s.oidRef.oidRef)
            if (s.dependents) {
                dep.append(": ").append(transitiveStepDeps(s.dependents))
            }
            r.append(["<li>", "</li>\n"].join(dep.toString()))
        }
        return result.append("</ul>").toString()
    }
}
