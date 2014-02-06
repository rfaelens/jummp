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

import eu.ddmore.libpharmml.dom.IndependentVariableType
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
import eu.ddmore.libpharmml.dom.modeldefn.ParameterRandomVariableType
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameterType
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModelType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityLevelDefnType
import eu.ddmore.libpharmml.dom.modellingsteps.DatasetMappingType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.OperationPropertyType
import eu.ddmore.libpharmml.dom.modellingsteps.ParameterEstimateType
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.StepDependencyType
import eu.ddmore.libpharmml.dom.modellingsteps.ToEstimateType
import eu.ddmore.libpharmml.dom.modellingsteps.VariableMappingType
import eu.ddmore.libpharmml.dom.trialdesign.BolusType
import eu.ddmore.libpharmml.dom.trialdesign.InfusionType
import eu.ddmore.libpharmml.dom.uncertml.NormalDistribution
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import net.biomodels.jummp.plugins.pharmml.ObservationEventsMap
import net.biomodels.jummp.plugins.pharmml.TrialDesignStructure
import net.biomodels.jummp.plugins.pharmml.maths.FunctionSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsUtil
import net.biomodels.jummp.plugins.pharmml.maths.OperatorSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PieceSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PiecewiseSymbol

class PharmMlTagLib {
    static namespace = "pharmml"

    // resolve references to covariates from the parameter model
    private Map<String, Equation> continuousCovariateTransformations = [:]
    // helps us decide the size of each correlation matrix in the parameter model
    private Map<String, List<String>> paramRandomVariableMap = [:]
    // pairs("${level}|${randomVar1}|${randomVar2}", covarianceOrCorrelationCoefficient)
    private Map<String, String> paramCorrelations = [:]
    private List<String> individualParametersInParameterModel = []
    // pairs (variabilityLevel, correlationMatrix), must not entangle with observation model params
    private Map<String, String[][]> paramCorrelationMatrixMap = [:]
    // helps us decide the size of each correlation matrix in the parameter model
    private Map<String, List<String>> obsRandomVariableMap = [:]
    // pairs("${level}|${randomVar1}|${randomVar2}", covarianceOrCorrelationCoefficient)
    private Map<String, String> obsCorrelations = [:]
    private List<String> individualParametersInObservationModel = []
    // pairs (variabilityLevel, correlationMatrix)
    private Map<String, String[][]> obsCorrelationMatrixMap = [:]
    // holds information about which PharmML-specific tabs should be shown
    private Map<String, String> tabsMap

    def decideTabs = { attrs ->
        boolean haveTabsToDisplay = true
        def topics = ["iv", "fd", "md", "td", "est", "sim"]
        topics.inject(haveTabsToDisplay) { display, t ->
            display && attrs."${t}"
        }
        if (!haveTabsToDisplay) {
            return
        }
        tabsMap = new HashMap<String, String>(topics.size(), 1.0)
        if (attrs.iv || attrs.fd || attrs.md) {
            final String MDEF_TAB = "modelDefinition"
            tabsMap["mdef"] = MDEF_TAB
            out << "<li><a href='#${MDEF_TAB}'>Model Definition</a></li>\n"
        }
        if (attrs.td) {
            final String TD_TAB = "trialDesign"
            tabsMap["td"] = TD_TAB
            out << "<li><a href='#${TD_TAB}'>Trial Design</a></li>\n"
        }
        if (attrs.est) {
            final String EST_TAB = "estimationSteps"
            tabsMap["est"] = EST_TAB
            out << "<li><a href='#${EST_TAB}'>Estimation Steps</a></li>\n"
        }
        if (attrs.sim) {
            final String SIM_TAB = "simulationSteps"
            tabsMap["sim"] = SIM_TAB
            out << "<li><a href='#${SIM_TAB}'>Simulation Steps</a></li>\n"
        }
    }

    def handleModelDefinitionTab = { attrs ->
        if (!tabsMap["mdef"]) {
            return
        }
        out << "<div id='${tabsMap["mdef"]}'>"
        if (attrs.iv) {
            out << "\n<p><strong>Independent variable</strong>&nbsp;${attrs.iv}</p>\n"
        }
        if (attrs.fd) {
            functionDefinitions(attrs.fd)
        }
        if (attrs.sm) {
            structuralModel(attrs.sm, attrs.iv)
        }
        if (attrs.vm) {
            variabilityModel(attrs.vm)
        }
        if (attrs.cm) {
            covariates(attrs.cm)
        }
        if (attrs.pm) {
            parameterModel(attrs.pm, attrs.cm)
        }
        if (attrs.om) {
            observations(attrs.om, attrs.cm)
        }
        out << "</div>\n"
    }

    def handleTrialDesignTab = { attrs ->
        if (!tabsMap["td"]) {
            return
        }
        out << "<div id='${tabsMap["td"]}'>"
        if (attrs.ts) {
            trialStructure(attrs.ts)
        }
        if (attrs.td) {
            trialDosing(attrs.td)
        }
        if (attrs.tp) {
            trialPopulation(attrs.tp)
        }
        out << "</div>\n"
    }

    def handleModellingStepsTabs = { attrs ->
        if (!attrs.estimation && !attrs.simulation && !tabsMap) {
            return
        }
        if (!attrs.independentVariable) {
            // the default independent variable is assumed to be time.
            attrs.independentVariable = "time"
        }

        def result = new StringBuilder()
        if (attrs.estimation) {
            result.append("<div id='${tabsMap["est"]}'>")
            result.append(estimationSteps(attrs.estimation))
            //only consider step dependencies here when there are no simulation steps
            if (!tabsMap["sim"]) {
                result.append(stepDeps(attrs.deps))
            }
            result.append("</div>")
        }
        if (attrs.simulation) {
            result.append("<div id='${tabsMap["sim"]}'>")
            result.append(simulationSteps(attrs.simulation, attrs.independentVariable))
            result.append(stepDeps(attrs.deps))
            result.append("</div>")
        }
        out << result.toString()
    }

    StringBuilder simpleParams(List<SimpleParameterType> parameters) {
        def outcome = new StringBuilder()
        if (!parameters) {
            return outcome
        }
        outcome.append("<div class='spaced'>")
        try {
            parameters.inject(outcome) { o, p ->
                String thisParam
                if (p.assign) {
                    thisParam = convertToMathML(p.symbId, p.assign)
                } else {
                    thisParam = ["<math display='inline'><mstyle>", "</mstyle></math>"].join(op(p.symbId))
                }
                o.append("<span>")
                o.append(thisParam).append(";&nbsp;")
                o.append("</span>\n")
            }
        } catch(Exception e) {
            outcome.append("Cannot display simple parameters.")
            log.error("Error encountered while rendering simple params ${parameters.inspect()}: ${e.message}")
        }
        return outcome.append("</div>")
    }

    StringBuilder randomVariables(List<ParameterRandomVariableType> rv, Map rvMap) {
        def output = new StringBuilder()
        try {
            rv.inject(output) { o, i ->
                populateRandomVariableMap(i.symbId, i.variabilityReference.symbRef.symbIdRef, rvMap)
                if (i.abstractContinuousUnivariateDistribution) {
                    o.append("<div>")
                    o.append(distributionAssignment(i.symbId, i.abstractContinuousUnivariateDistribution))
                    o.append("&nbsp;&mdash;&nbsp;").append(i.variabilityReference.symbRef.symbIdRef)
                    o.append("</div>\n")
                }
            }
        } catch(Exception e) {
            output.append("Cannot display random variables.")
            log.error("Error encountered while rendering random variables ${rv.inspect()}: ${e.message}")
        }
        return output
    }

    StringBuilder individualParams(List<IndividualParameterType> parameters,
                List<ParameterRandomVariableType> rv, List<CovariateDefinitionType> covariates,
                List<String> indivParamNameList) {
        def output = new StringBuilder("<div class='spaced'>")
        try {
            parameters.each { p ->
                if (!indivParamNameList.contains(p.symbId)) {
                    indivParamNameList.add(p.symbId)
                }
                if (p.assign) {
                    String converted = convertToMathML(p.symbId, p.assign)
                    output.append("<div>")
                    output.append(converted)
                    output.append("</div>")
                }
                if (p.gaussianModel) {
                    output.append("\n")
                    def gaussianModel = p.gaussianModel
                    //RANDOM EFFECTS
                    def randomEffects = []
                    if (gaussianModel.randomEffects) {
                        gaussianModel.randomEffects.symbRef.each { re ->
                            def randomEffectSymbol = new SymbolRefType()
                            randomEffectSymbol.symbIdRef = re.symbIdRef
                            randomEffects << wrapJaxb(randomEffectSymbol)
                        }
                    }
                    if (gaussianModel.linearCovariate) {
                        def linearCovariate = gaussianModel.linearCovariate
                        if (gaussianModel.transformation) {
                            final String TRANSFORMATION = gaussianModel.transformation.value()
                            //LHS
                            UniopType indivParam = new UniopType()
                            indivParam.op = TRANSFORMATION
                            def paramSymbRef = new SymbolRefType()
                            paramSymbRef.symbIdRef = p.symbId
                            indivParam.symbRef = paramSymbRef
                            def lhsEquation = new Equation()
                            lhsEquation.scalarOrSymbRefOrBinop.add(wrapJaxb(indivParam))
                            //POPULATION
                            def popParam
                            if (linearCovariate.populationParameter.assign.symbRef) {
                                popParam = new UniopType()
                                popParam.op = TRANSFORMATION
                                popParam.symbRef = linearCovariate.populationParameter.assign.symbRef
                            }
                            def fixedEffectsCovMap = [:]
                            linearCovariate.covariate.each { c ->
                                //fixed effects
                                if (!c.fixedEffect) {
                                    return
                                }
                                def fixedEffects = []
                                def covEffectKey
                                c.fixedEffect.each { fe ->
                                    if (fe.category) {
                                        def catIdSymbRef = new SymbolRefType()
                                        def trickReference = new StringBuilder("<msub><mi>")
                                        trickReference.append(c.symbRef.symbIdRef).append("</mi><mi>")
                                        trickReference.append(fe.category.catId).append("</mi></msub>")
                                        catIdSymbRef.symbIdRef = trickReference.toString()
                                        covEffectKey = catIdSymbRef
                                    } else {
                                        //RESOLVE REFERENCE TO CONT COV TRANSF
                                        final EquationType transfEq = resolveSymbolReference(c.symbRef)
                                        if (transfEq) {
                                            covEffectKey = transfEq
                                        } else {
                                            covEffectKey = c.symbRef
                                        }
                                    }
                                    fixedEffects << fe.symbRef
                                }
                                fixedEffectsCovMap[covEffectKey] = fixedEffects
                            }
                            def fixedEffectsTimesCovariateList = []
                            if (fixedEffectsCovMap) {
                                fixedEffectsCovMap.each{
                                    def thisCov = []
                                    def key = it.key
                                    if ( key instanceof Equation) {
                                        thisCov.addAll(key.scalarOrSymbRefOrBinop)
                                    } else {
                                        thisCov.add(wrapJaxb(key))
                                    }
                                    it.value.collect{ v -> thisCov.add(wrapJaxb(v)) }
                                    fixedEffectsTimesCovariateList.add(applyBinopToList(thisCov, "times"))
                                }
                            }
                            def sumElements = []
                            sumElements.add(wrapJaxb(popParam))
                            if (fixedEffectsTimesCovariateList) {
                                sumElements.addAll(fixedEffectsTimesCovariateList)
                            }
                            sumElements.addAll(randomEffects)

                            Equation rhsEquation = new Equation()
                            rhsEquation.scalarOrSymbRefOrBinop.add(applyBinopToList(sumElements, "plus"))
                            output.append(convertToMathML(lhsEquation, rhsEquation))
                            output.append("\n")
                        }
                    } else if (gaussianModel.generalCovariate) {
                        def rhsEquation
                        def covModel
                        def covariateModelAssign = gaussianModel.generalCovariate.assign
                        assert covariateModelAssign != null
                        if (!randomEffects) {
                            rhsEquation = covariateModelAssign
                        } else {
                            if (covariateModelAssign.equation) {
                                covModel = covariateModelAssign.equation.scalarOrSymbRefOrBinop.first()
                            } else if (covariateModelAssign.scalar) {
                                covModel = wrapJaxb(covariateModelAssign.scalar)
                            } else if (covariateModelAssign.symbRef) {
                                covModel = wrapJaxb(covariateModelAssign.symbRef)
                            }
                        }
                        def cm_re = []
                        cm_re.add(covModel)
                        cm_re.addAll(randomEffects)
                        rhsEquation = new Equation()
                        rhsEquation.scalarOrSymbRefOrBinop.add(applyBinopToList(cm_re, "plus"))
                        String converted = convertToMathML(p.symbId, rhsEquation)
                        output.append("<div>")
                        output.append(converted)
                        output.append("</div>")
                    }
                }
            }
        } catch(Exception e) {
            output.append("Cannot display individual parameters.")
            log.error("Error encountered while rendering individual parameters ${parameters.inspect()} using random variables ${rv.inspect()} and covariates ${covariates.inspect()}: ${e.message}")
        }
        return output.append("</div>")
    }

    def functionDefinitions = { functionDefs ->
        if (!functionDefs) {
            return
        }
        def result = new StringBuilder("<h3>Function Definitions</h3>")

        try {
            functionDefs.each { d ->
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
                result.append("<div>${convertToMathML(d.symbId, d.getFunctionArgument(), rightHandSide)}</div>")
            }
        } catch(Exception e) {
            log.error("Error while rendering function definitions ${functionDefs.inspect()}: ${e.message}")
            result.append("Sorry, cannot render the function definitions.")
        }
        out << result.toString()
    }

    def structuralModel = { sm, iv ->
        if (!sm) {
            return
        }

        def result
        try {
            result = new StringBuilder()
            boolean multipleStructuralModels = sm.size() > 1
            if (!multipleStructuralModels) {
                displayStructuralModel(sm[0], iv, result)
            } else {
                sm.each { s ->
                    displayStructuralModel(s, iv, result)
                }
            }
        } catch(Exception e) {
            log.error("Error while rendering structural model ${sm.inspect()} ${sm.properties}:${e.message}")
            out << "Sorry, something went wrong while displaying the structural model."
            return
        }
        out << result.toString()
    }

    void displayStructuralModel(StructuralModelType model, String iv, StringBuilder result) {
        result.append("<h3>Structural Model <span class='italic'>")
        result.append(model.name?.value ?: model.blkId).append("</span></h3>\n")
        if (model.simpleParameter) {
            result.append("<p class=\"bold\">Parameters </p>")
            result.append(simpleParams(model.simpleParameter))
        }
        if (model.commonVariable) {
            result.append("<p class=\"bold\">Variable definitions</p>")
            result.append(["<div>", "</div>\n"].join(
                commonVariables(model.commonVariable, iv).toString()))
        }
    }

    StringBuilder commonVariables(List<JAXBElement> vars, def indepVar) {
        def result = new StringBuilder()
        if (!vars) {
            return result
        }
        def initialConditions = [:]
        try {
            vars.each { v ->
                result.append("<div>")
                switch(v.value) {
                    case DerivativeVariableType:
                        if (v.value.initialCondition) {
                            initialConditions << [(v.value.symbId) : v.value.initialCondition]
                        }
                        result.append(convertToMathML(v.value, indepVar))
                        break
                    case VariableDefinitionType:
                        if (v.value.assign) {
                            result.append(convertToMathML(v.value.symbId, v.value.assign))
                        } else {
                            result.append("<math display='inline'><mstyle>").append(op(v.value.symbId)).append(
                                    "</mstyle></math>")
                        }
                        break
                    case FunctionDefinitionType:
                        def fd = v.value
                        result.append(convertToMathML(fd.symbId, fd.functionArgument, fd))
                        break
                    case FuncParameterDefinitionType:
                        result.append(v.value.symbId)
                        break
                    default:
                        result.append(v.value.symbId)
                        break
                }
                result.append("</div>")
            }
            if (initialConditions) {
                result.append("\n<p class='bold'>Initial conditions</p>\n")
                initialConditions.keySet().each { s ->
                    result.append("<div>").append(convertToMathML(s, initialConditions[s].assign)).append("</div>\n")
                }
            }
        } catch(Exception e) {
            log.error("Error while displaying common variables - arguments ${vars.properties} ${indepVar.inspect()}: ${e.message} ")
            return new StringBuilder("Sorry, ran into issues while trying to display variable definitions.")
        }
        return result
    }

    def variabilityModel = { variabilityModel ->
        if (!variabilityModel) {
            return
        }
        def result = new StringBuilder("<h3>Variability Model</h3>\n<table class='views-table cols-4'>\n<thead><tr>")
        result.append("<th>Identifier</th><th>Name</th><th>Level</th><th>Type</th></tr>")
        result.append("\n</thead>\n<tbody>\n")
        variabilityModel.each { m ->
            result.append("<tr><td class=\"value\">")
            result.append(m.blkId)
            result.append("</td><td class=\"value\">")
            String modelName = m.name ? m.name : " "
            result.append(modelName)
            result.append("</td><td class=\"value\">")
            result.append(variabilityLevel(m.level))
            result.append("</td><td class=\"value\">")
            result.append(m.type.value())
            result.append("</td></tr>\n")
        }
        out << result.append("</tbody>\n</table>").toString()
    }

    StringBuilder variabilityLevel(List variabilityLevels) {
        def result = new StringBuilder()
        if (!variabilityLevels) {
            return result.append(" ")
        }
        variabilityLevels.each { l ->
            result.append("<p class=\"default\">")
            if (l.name) {
                result.append(l.name.value)
            } else {
                result.append(l.symbId)
            }
            if (l.parentLevel) {
                result.append(", ")
                result.append("parent level:").append(l.parentLevel.symbRef.symbIdRef)
            }
            result.append("</p>")
        }
        return result
    }

    /**
     * Expects as arguments the parameter model as well as the covariate model.
     * The latter is necessary to display the transformations that are defined
     * for each individual parameter.
     */
    def parameterModel = { parameterModel, covariates ->
        if (!parameterModel) {
            return
        }
        out << "<h3>Parameter Model</h3>"

        def result = new StringBuilder()
        result.append("<span class=\"bold\">Parameters </span>")
        try {
            parameterModel.each { pm ->
                result.append("<div class='spaced'>")
                def simpleParameters = pm.commonParameterElement.value.findAll {
                       it instanceof SimpleParameterType
                }
                def rv = pm.commonParameterElement.value.findAll {
                       it instanceof ParameterRandomVariableType
                }
                def individualParameters = pm.commonParameterElement.value.findAll {
                       it instanceof IndividualParameterType
                }
                result.append(simpleParams(simpleParameters))
                String randoms = randomVariables(rv, paramRandomVariableMap)
                if (randoms) {
                   result.append(randoms)
                }
                StringBuilder individuals = individualParams(individualParameters, rv, covariates,
                            individualParametersInParameterModel)
                if (individuals) {
                   result.append(individuals)
                }
                if (pm.correlation) {
                    handleCorrelations(pm.correlation, paramCorrelations,
                                paramRandomVariableMap, paramCorrelationMatrixMap,
                                individualParametersInParameterModel, result)
                }
                result.append("</div>")
            }
        } catch(Exception e) {
            log.error("Error rendering the parameter model for ${parameterModel.inspect()} ${parameterModel.properties}: ${e.message}\nStacktrace:\n")
            out << "Sorry, something went wrong while rendering the parameter model."
        }
        out << result.toString()
    }

    /*
     * Processes and displays correlations between covariates.
     * @param corList:     correlations to be displayed.
     * @param corMap:      covariate pair mapped to correlation coefficient or covariance
     * @param rvMap:       random variables grouped by variability level
     * @param matrix:      correlation matrices grouped by variability level
     * @param indivParams: the names of the individual parameters.
     * @param result:      StringBuilder where the output is accumulated.
     */
    private void handleCorrelations(List<CorrelationType> corList, Map corMap, Map rvMap,
                Map matrix, List indivParams, StringBuilder result) {
        corList.each { cor ->
           buildCorrelationMap(cor, corMap)
        }
        processCorrelations(corMap, rvMap,
                    matrix)
        if (matrix) {
            displayCorrelationMatrices(matrix,
                        indivParams, result)
        }
    }

    private void buildCorrelationMap(CorrelationType c, Map correlationsMap) {
        final ScalarRhs VALUE = c.covariance ?: c.correlationCoefficient

        String var = c.variabilityReference.symbRef?.symbIdRef ?:
                        c.variabilityReference.symbRef?.blkIdRef ?: "undefined"
        String r1 = c.randomVariable1.symbRef?.symbIdRef
        String r2 = c.randomVariable2.symbRef?.symbIdRef
        final String KEY = "$var|$r1|$r2"
        correlationsMap[KEY] = VALUE.symbRef.symbIdRef
        final String KEY_REV = "$var|$r2|$r1"
        correlationsMap[KEY_REV] = VALUE.symbRef.symbIdRef
    }


    private void processCorrelations(Map<String, String> c, Map<String, List<String>> rv,
                Map<String, String[][]> corrMatrixMap) {
        rv.entrySet().each {
            final String LVL = it.key
            if (!corrMatrixMap[LVL]) {
                final int MATRIX_SIZE = it.value.size()
                String[][] corrMatrix = new String[MATRIX_SIZE][MATRIX_SIZE]
                for (int i = 0; i < MATRIX_SIZE; i++) {
                    for (int j = 0; j < MATRIX_SIZE; j++) {
                        final String R1 = it.value[i]
                        final String R2 = it.value[j]
                        final String KEY = "$LVL|$R1|$R2"
                        final String KEY_REV = "$LVL|$R2|$R1"
                        final String RHO = c[KEY]
                        final String RHO_REV = c[KEY_REV]
                        if (i == j) {
                            corrMatrix[i][j] = "1"
                        } else if (RHO) {
                            corrMatrix[i][j] = RHO
                        } else if (RHO_REV) {
                            corrMatrix[i][j] = RHO_REV
                        }else {
                            corrMatrix[i][j] = "0"
                        }
                    }
                }
                corrMatrixMap[LVL] = corrMatrix
            }
        }
    }

    private void displayCorrelationMatrices(Map<String, String[][]> matrices,
                List<String> paramNames, StringBuilder output) {
        matrices.entrySet().each {
            convertToMathML(it.key, it.value, paramNames, output)
        }
    }

    def covariates = { covariate ->
        if (!covariate) {
            return
        }
        out << "<h3>Covariate Model</h3>"
        def result = new StringBuilder()
        try {
            covariate.each { c ->
                result.append("<div>")
                if (c.simpleParameter) {
                    result.append("<div><span class=\"bold\">Parameters</span></div>")
                    result.append(simpleParams(c.simpleParameter))
                }
                if (c.covariate) {
                    c.covariate.each {
                        result.append(
                            it.getCategorical() ? categCov(it.getCategorical(), it.symbId) :
                                    contCov(it.symbId, c.blkId, it.getContinuous()))
                    }
                }
                result.append("</div>")
            }
        } catch(Exception e) {
            log.error("Error rendering the covariates ${covariate.inspect()} ${covariate.properties}: ${e.message}")
            result.append("Sorry, something went wrong while rendering the covariates.")
        } finally {
            out << result.toString()
        }
    }

    StringBuilder categCov = { c, symbId ->
        def result = new StringBuilder("<p>\n")
        result.append("<span class=\"bold\">Categorical covariate ${symbId}</span><p>\n")
        c.category.inject(result) { r, categ ->
            if (categ.probability) {
                r.append(distributionAssignment(symbId,categ.probability))
            }
        }
        result.append("</p>\n")
        result.append("<p>Categories:")
        assert c.category instanceof List
        c.category.inject(result) { StringBuilder sb, CategoryType cat ->
            sb =  sb ? sb : new StringBuilder()
            sb.append(cat.catId)
            if (cat.name) {
                sb.append("(").append(cat.name.value).append(")")
            }
            sb.append(" ")
        }
        result.append("</p>\n")
        return result
    }

    StringBuilder contCov(String symbId, String blkId, ContinuousCovariateType c) {
        def result = new StringBuilder("<p>")
        result.append("<span class=\"bold\">Continuous covariate ${symbId}</span>\n</p>\n<p>")
        if (c.abstractContinuousUnivariateDistribution) {
            result.append(distributionAssignment(symbId, c.abstractContinuousUnivariateDistribution))
            result.append("</p><p>")
        }

        final String COV_KEY = "${blkId}_${symbId}"
        // there is no need to expand the symbRef here, so temporarily pop it from the map
        final EquationType TRANSF_REF = continuousCovariateTransformations.remove(COV_KEY)
        final EquationType TRANSF_EQ =  TRANSF_REF ?: c.transformation.equation
        result.append(convertToMathML("Transformation", TRANSF_EQ))
        assert !(continuousCovariateTransformations[COV_KEY])
        continuousCovariateTransformations[COV_KEY] = TRANSF_EQ

        return result.append("</p>")
    }

    def observations = { observations, covariates ->
        if (!observations || observations.size() == 0) {
            return
        }

        StringBuilder result = new StringBuilder()
        result.append("<h3>Observation Model</h3>")
        try {
            observations.each { om ->
                result.append("<h4>Observation <span class='italic'>")
                // the API returns a JAXBElement, not ObservationErrorType
                def obsErr = om.observationError.value
                result.append(obsErr.symbId).append("</span></h4>\n")
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
                String randoms = randomVariables(rv, obsRandomVariableMap)
                if (randoms) {
                    result.append(randoms)
                }
                StringBuilder individuals = individualParams(individualParameters, rv, covariates,
                            individualParametersInObservationModel)
                if (individuals) {
                   result.append(individuals)
                }
                if (om.correlation) {
                    handleCorrelations(om.correlation, obsCorrelations,
                                obsRandomVariableMap, obsCorrelationMatrixMap,
                                individualParametersInObservationModel, result)
                }
                if (obsErr.symbol?.value) {
                    result.append(obsErr.symbol.value)
                }
                if (obsErr instanceof GaussianObsError) {
                    result.append(gaussianObsErr(obsErr)).append(" ")
                } else { // can only be GeneralObsError
                    result.append(generalObsErr(obsErr)).append(" ")
                }
            }
        } catch(Exception e) {
            log.error("Error rendering the observations ${observations.inspect()}: ${e.message}")
            out << "Sorry, something went wrong while rendering the observations."
        }
        out << result.toString()
    }

    StringBuilder gaussianObsErr(GaussianObsError e) {
        def result = new StringBuilder("<div class='spaced'>")

        // could be an Equation or just a String
        def lhs
        def lhsSymb = new SymbolRefType()
        lhsSymb.symbIdRef = e.symbId
        def prediction
        def predictionSymb = e.output.symbRef
        def residualErrorSymb = e.residualError.symbRef

        if (e.transformation) {
            final String tr = e.transformation.value()
            def lhsUniop = new UniopType()
            lhsUniop.op = tr
            lhsUniop.symbRef = lhsSymb
            lhs = new Equation()
            lhs.scalarOrSymbRefOrBinop.add(wrapJaxb(lhsUniop))
            def predUniop = new UniopType()
            predUniop.op = tr
            predUniop.symbRef = predictionSymb
            prediction = wrapJaxb(predUniop)
        } else {
            lhs = lhsSymb.symbIdRef
            prediction = wrapJaxb(predictionSymb)
        }

        def errModelAssign = e.errorModel.assign
        assert errModelAssign != null
        def errModel
        def errModelTimesResidualErr
        def rhsEquation
        if (errModelAssign.equation) {
            errModel = errModelAssign.equation.scalarOrSymbRefOrBinop.first()
        } else if (errModelAssign.scalar) {
            errModel = wrapJaxb(errModelAssign.scalar)
        } else if (errModelAssign.symbRef) {
            errModel = wrapJaxb(errModelAssign.symbRef)
        }
        def em_re = new BinopType()
        em_re.op = "times"
        em_re.content.add(errModel)
        em_re.content.add(wrapJaxb(residualErrorSymb))
        errModelTimesResidualErr = wrapJaxb(em_re)
        def sum = new BinopType()
        sum.op = "plus"
        sum.content.add(prediction)
        sum.content.add(errModelTimesResidualErr)
        rhsEquation = new Equation()
        rhsEquation.scalarOrSymbRefOrBinop.add(wrapJaxb(sum))
        return result.append(convertToMathML(lhs, rhsEquation)).append("</div>")
    }

    StringBuilder generalObsErr(GeneralObsError e) {
        def result = new StringBuilder()
        if (!e) {
            return result
        }
        if (e.assign) {
            result.append("<p>").append(convertToMathML(e.symbId, e.assign)).append("</p>")
        }
        return result
    }

    def randomEffect = { re ->
        if (!re) {
            return ""
        }
        return distribution(re.distribution[0])
    }

    def distributionAssignment = { l, d->
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(l))
        builder.append(op("&sim;"))
        builder.append(distribution(d))
        builder.append("</mstyle></math>")
    }

    def distribution = { d ->
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

    def normalDistribution = { dist ->
        StringBuilder result = new StringBuilder()
        NormalDistribution d= dist.value
        String mean = d.mean.var?.varId ? d.mean.var.varId : d.mean.rVal
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

    StringBuilder scalarRhs = { r ->
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

    StringBuilder rhs = { r, text ->
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

    String scalar = { s ->
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

    def sequenceAsMathML = {
        new StringBuilder(op("[")).append(oprand(s.begin)).append(op(":")).
                   append(oprand(s.stepSize)).append(op(":")).
                   append(oprand(s.end)).append(op("]"))
    }

    StringBuilder vectorAsMathML = { v ->
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

    StringBuilder vector = { v ->
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

    /* TRIAL DESIGN */
    def trialStructure = { structure ->
        if (!structure) {
            return
        }
        def result = new StringBuilder()
        TrialDesignStructure tds
        def segmentActivitiesMap
        result.append("<h3>Structure overview</h3>\n")
        try {
            tds = new TrialDesignStructure(structure.arm, structure.epoch,
                        structure.cell, structure.segment)
            def armRefs     = new ArrayList(tds.getArmRefs())
            def epochRefs   = new ArrayList(tds.getEpochRefs())
            /* arm-epoch matrix*/
            result.append("<table><thead><tr><th class='bold'>Arm/Epoch</th>")
            for (String e: epochRefs) {
                result.append("<th class='bold'>").append(e).append("</th>")
            }
            result.append("</tr></thead><tbody>\n")
            for (String a: armRefs) {
                result.append("<tr><th class='bold'>").append(a).append("</th>")
                tds.findSegmentRefsByArm(a).each { s ->
                    result.append("<td>").append(s).append("</td>")
                }
                result.append("</tr>\n")
            }
            result.append("</tbody></table>\n")
        } catch(Exception e) {
            result.append("Cannot display the arm-epoch matrix.")
            def errMsg = new StringBuilder("Error encountered while rendering the arm-epoch matrix of")
            errMsg.append("trial design structure ${structure.properties} ")
            errMsg.append("using helper ${tds.trialDesignStructure.inspect()}: ")
            log.error(errMsg, e)
        }
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
            result.append("<h4>Segment-Activity definition</h4>\n")
            result.append("<table style='margin-bottom:0px;'><thead><tr><th class='bold'>Segment</th><th class='bold'>Activity</th>")
            result.append("<th class='bold'>Treatment</th><th class='bold'>Dose time</th>")
            result.append("<th class='bold'>Dose size</th><th class='bold'>Target variable</th></tr></thead><tbody>")
            if (segmentActivitiesMap) {
                segmentActivitiesMap.entrySet().each {
                    def activityList = it.value
                    if (!activityList) {
                        result.append("<tr><td colspan='6'></td></tr>")
                    } else {
                        final int ACTIVITY_COUNT = activityList.size()
                        result.append("<tr><td")
                        if (ACTIVITY_COUNT > 1) {
                            result.append(" rowspan='").append(ACTIVITY_COUNT).append("'")
                        }
                        result.append(">").append(it.key).append("</td><td>")
                        def first = activityList[0]
                        result.append(first.oid).append("</td>")
                        if (first.washout) {
                            result.append("<td>washout</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td>")
                        } else {
                            /* dosingRegimen is a JAXBElement */
                            def regimen = first.dosingRegimen.value
                            switch(regimen) {
                                case BolusType:
                                    result.append("<td>bolus</td>")
                                    //fall through
                                case InfusionType:
                                    if (regimen instanceof InfusionType) {
                                        result.append("<td>infusion</td>")
                                    }
                                    if (regimen.dosingTimes) {
                                        rhs(regimen.dosingTimes.assign, result.append("<td>"))
                                        result.append("</td>")
                                    } else if (regimen.steadyState) {
                                        result.append("<td>")
                                        result.append(steadyState(regimen.steadyState))
                                        result.append("</td>")
                                    } else {
                                        result.append("<td>*</td>")
                                        showDosingFootnote = true
                                    }
                                    def amt = regimen.doseAmount
                                    if (amt.assign) {
                                        rhs(amt.assign, result.append("<td>")).append("</td>")
                                    } else {
                                        result.append("<td>*</td>")
                                        showDosingFootnote = true
                                    }
                                    result.append("<td>").append(amt.symbRef.symbIdRef).append("</td>")
                                    break
                               default:
                                    result.append("<td colspan='4'>Unknown</td>")
                                    break
                            }
                        }
                        result.append("</tr>\n")
                    }
                }
                result.append("</tbody></table>\n")
            }
            if (showDosingFootnote) {
                result.append("<span>* &ndash; Element defined in the Individual dosing section.</span>")
            }
        } catch(Exception e) {
            result.append("Cannot display the segment-activity overview.")
            def errMsg = new StringBuilder("Cannot display the segment-activity overview for structure ")
            errMsg.append(structure.properties).append(" using helper map ")
            errMsg.append(segmentActivitiesMap.inspect())
            log.error(errMsg, e)
        }
        ObservationEventsMap oem
        /* epochs and occasions */
        try {
            if (structure.studyEvent) {
                oem = new ObservationEventsMap(structure.studyEvent)
                def arms = oem.getArms()
                def epochs = oem.getEpochs()
                result.append("\n<h4>Epoch-Occasion definition</h4>\n")
                result.append("<table><thead><tr><th class='bold'>Arm/Epoch</th>")
                for (String e: epochs) {
                    result.append("<th class='bold'>").append(e).append("</th>")
                }
                result.append("</tr></thead><tbody>\n")
                arms.each { a ->
                    result.append("<tr><th class='bold'>").append(a).append("</th>")
                    def occ = oem.findOccasionsByArm(a)
                    occ.each {
                        def o = it.firstEntry()
                        result.append("<td>")
                        result.append("<div>").append(o.key).append("</div><div><span class='bold'>")
                        result.append(o.value).append("</span> variability</div></td>")
                    }
                    result.append("</tr>")
                }
                result.append("</tbody></table>\n")
            }
        } catch(Exception e) {
            result.append("<p>Cannot display the epoch-occasion overview.</p>")
            def errMsg = new StringBuilder("Cannot display the epoch-occasion overview for structure ")
            errMsg.append(structure.properties).append(" using helper map ")
            errMsg.append(oem.inspect())
            log.error(errMsg, e)
        }

        out << result.toString()
    }

    def trialDosing = { dosing ->
        if (!dosing) {
            return
        }
        def result = new StringBuilder()
        try {
            result.append("<h4>Individual dosing</h4>\n")
            dosing.each { d ->
                if (d.dataSet) {
                    dataSet(d.dataSet, null, result)
                }
            }
        } catch(Exception e) {
            result.append("Cannot display the trial dosing.")
            def errMsg = new StringBuilder("Cannot display the trial dosing ")
            errMsg.append(d.properties)
            log.error(errMsg, e)
        }

        out << result.toString()
    }

    def trialPopulation = { pop ->
        if (!pop) {
            return
        }
        def result = new StringBuilder("<h4>Population</h4>\n")
        if (pop.variabilityReference) {
            result.append("<span><strong>Variability level: </strong>")
            result.append(pop.variabilityReference.symbRef.symbIdRef).append("</span>")
        }
        try {
            if (pop.dataSet) {
                dataSet(pop.dataSet, null, result)
            }
        } catch (Exception e) {
            result.append("Cannot display population data set.")
            def errMsg = new StringBuilder()
            errMsg.append("Cannot display population data set ")
            errMsg.append(pop.dataSet.properties).append( "for population ")
            errMsg.append(pop.properties)
            log.error(errMsg, e)
        } finally {
            out << result.toString()
        }
    }

     StringBuilder steadyState = { ss ->
        def result = new StringBuilder()
        try {
            result.append("<strong>Steady state</strong>")
            def i = ss.interval
            def end = ss.endTime
            result.append("<div>Interval: ").append(convertToMathML(i.symbRef.symbIdRef, i.assign))
            result.append("</div><div>")
            result.append("End time: ").append(convertToMathML(end.symbRef.symbIdRef, end.assign))
            result.append("</div>")
        } catch(Exception e) {
            result.append("Cannot display steady state.")
            log.error("Cannot display steady state ${ss.properties}", e)
        }
        return result
    }

    StringBuilder occasions = { occasions, text ->
        text.append("[")
        occasions.each { o ->
            StringBuilder sb = new StringBuilder("(level:")
            sb.append(o.levelId).append(", symbolIdentifier:").append(o.symbId).append(")")
            text.append(sb)
        }
        text.append("]")
    }

   /* MODELLING STEPS */
    def variableDefs = { attrs ->
        if (!attrs.variables) {
            // there may not be any variables in the modelling steps
            return
        }

        def result = new StringBuilder("<h3>Variables</h3>")
        result.append("<table><thead><tr>")
        result.append("<th>Identifier</th><th>Independent Variable</th><th>Symbol Type</th><th>Value</th>")
        result.append("</tr></thead><tbody>")
        attrs.variables.each { v ->
            result.append("<tr><td>").append(v?.symbId).append("</td><td>")
            result.append(v.independentVar ? v.independentVar : "&nbsp;").append("</td><td>")
            result.append(v.symbolType.value()).append("</td><td>")
            if (v.dataSet) {
                result.append(" ")
            }
            if (v.scalar) {
                result.append(scalar(v.scalar))
            }
            result.append("</td></tr>")
        }
        result.append("</tbody></table>")
        out << result.toString()
    }
    StringBuilder simulationSteps = { List<SimulationStepType> steps, String iv ->
        if (!steps) {
            return
        }
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

    StringBuilder jaxbVector(VectorType vector) {
        if (!vector) {
            return new StringBuilder()
        }
        def result = new StringBuilder("")
        def values = []
        vector.sequenceOrScalar.inject(result) { r, ss ->
            switch(ss.value) {
               case SequenceType:
                    values << sequence(ss.value)
                    break
                default:
                    values << scalar(ss.value)
            }
        }
        result.append(values.size() > 1 ? "[${values.join(', ')}]" : values.first())
        return result
    }

    StringBuilder variableAssignments(List<VariableAssignmentType> assignments, String heading) {
        def result = new StringBuilder("\n${heading}\n")
        assignments.inject(result){r,v ->
            r.append("<p>").append(convertToMathML(v.symbRef.symbIdRef, v.assign)).append("</p>")
        }
        return result
    }

    StringBuilder estimationSteps(List<EstimationStepType> steps) {
        if (!steps) {
            return
        }
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
                result.append(estimationOps(s.operation))
            }
            if (s.objectiveDataSet) {
                result.append(objectiveDataSetMapping(s.objectiveDataSet))
            }
        }
        return result
    }

    StringBuilder objectiveDataSetMapping(List<DatasetMappingType> mappings) {
        def result = new StringBuilder("<h5>Dataset mapping")
        if (mappings.size() > 1) {
            result.append("s")
        }
        result.append("</h5>\n")
        def variableMap = [:]

        mappings.each { dsm ->
            if (dsm.variableAssignment) {
                result.append(variableAssignments(dsm.variableAssignment,
                            "<span class=\"bold\">Variable assignments</span>"))
            }
            dsm.mapping.each {
                //keep track of variableMappings so that we know how to name the columns
                //deal with JAXBElement
                if (it.value instanceof VariableMappingType) {
                    //TODO handle nested  ColumnRefs recursively
                    variableMap << [ (it.value.columnRef.columnIdRef) : (it.value.symbRef.symbIdRef)]
                }
            }
            if (dsm.dataSet) {
                dataSet(dsm.dataSet, variableMap, result)
            }
        }
        return result
    }

    StringBuilder dataSet(DataSetType dataSet, Map variableMap, StringBuilder sb) {
        def columnOrder = [:]
        List tables = dataSet.definition.columnOrTable
        tables.each {
            if (it instanceof ColumnDefnType) {
                columnOrder << [ (it.columnNum) : (it.columnId) ]
            } else if (it instanceof DataSetTableDefnType) {
                columnOrder << [ (it.columnNum) : (it.tableId) ]
            }
        }
        sb.append("\n<table><thead><tr>")

        tables.inject(sb) { txt, d ->
            def key = columnOrder[d.columnNum]
            if (key && variableMap && variableMap[key]) {
                txt.append(["<th>", "</th>"].join(variableMap[key]))
            } else if (d instanceof ColumnDefnType) {
                txt.append(["<th>", "</th>"].join(d.columnId))
            } else if (d instanceof DataSetTableDefnType) {
                txt.append(["<th>", "</th>"].join(d.tableId))
            }
        }
        sb.append("</tr></thead><tbody>")
        dataSet.table.row.each { i ->
            sb.append("\n<tr>")
            i.scalarOrTable.each { td ->
                if (td.value instanceof DataSetTableType) {
                    def content = new StringBuilder("<table class='default'>")
                    td.value.row.inject(content) { cont, r ->
                        cont.append("<tr class='default'>")
                        r.scalarOrTable.inject(cont) { s, val ->
                            s.append("<td class='default'>")
                            if (val instanceof DataSetTableType) {
                                s.append("*")
                            } else {
                                s.append(scalar(val.value))
                            }
                            s.append("</td>")
                        }
                        cont.append("</tr>")
                    }
                    String ready = content.append("</table>").toString()
                    sb.append(["<td class='default'>", "</td>"].join(ready))
                } else {
                    sb.append(["<td class='default'>", "</td>"].join(scalar(td.value)))
                }
            }
            sb.append("</tr>")
        }
        return sb.append("</tbody></table>\n")
    }

    StringBuilder paramsToEstimate(ToEstimateType params) {
        def result = new StringBuilder("<div><h5>Estimation parameters</h5>\n")
        def fixedParams = params.parameterEstimation.findAll{ it.initialEstimate?.fixed }
        if (fixedParams) {
            result.append(estimParamsWithInitialEstimate(fixedParams, "Fixed parameters"))
        }
        def estimatedParams = params.parameterEstimation - fixedParams
        if (estimatedParams) {
            result.append(estimParamsWithInitialEstimate(estimatedParams, "Initial estimates for non-fixed parameters"))
        }
        return result.append("</div>")
    }

    StringBuilder estimParamsWithInitialEstimate(List<ParameterEstimateType> params, String heading) {
        def result = new StringBuilder("<p class=\"bold\">${heading}</p>\n")
        if (params.size() > 1) {
            result.append("<ul>")
            params.inject(result) { r, p ->
                r.append("<li>")
                //how do we display lowerBound and upperBound?
                r.append(convertToMathML(p.symbRef.symbIdRef, p.initialEstimate))
                r.append("</li>\n")
            }
            result.append("</ul>\n")
        } else {
            result.append("&nbsp;<span>")
            result.append(convertToMathML(params[0].symbRef.symbIdRef, params[0].initialEstimate))
            result.append("</span>\n")
        }
        return result
    }

    StringBuilder estimationOps = { operations ->
        if (!operations) {
            return new StringBuilder()
        }
        def result = new StringBuilder("<h5>Estimation operations</h5>\n")
        // It is nicer to display the long description than the enum value.
        def operationMeaningMap = [
                "estFIM"   : "Calculate the Fisher Information Matrix",
                "estIndiv" : "Estimate the individual parameters",
                "estPop"   : "Estimate the population parameters"
        ]
        operations.each { o ->
            result.append("<div><span class=\"bold\">")
            result.append(o.order).append(") ")
            result.append(o.name ? o.name.value : operationMeaningMap[o.opType.value()])
            result.append("</span>\n")
            if (o.description || o.algorithm || o.property) {
                result.append("<div>")
                if (o.description) {
                    result.append(o.description.value)
                }
                result.append("</div>")
                if (o.property || o.algorithm) {
                    result.append("\n<ul>")
                    o.property.inject(result) { r, p ->
                        r.append("<li>").append(operationProperty(p)).append("</li>")
                    }
                    result.append("</ul>\n")
                    if (o.algorithm) {
                        result.append("\n<span>Algorithm ").append(o.algorithm.name ? o.algorithm.name.value :
                                (o.algorithm.definition ? o.algorithm.definition : "")).append("</span>\n")
                        if (o.algorithm.property) {
                            result.append("<ul>")
                            o.algorithm.property.inject(result) { r, p ->
                                r.append("<li>").append(operationProperty(p)).append("</li>")
                            }
                            result.append("</ul>")
                        }
                    }
                }
            }
            result.append("</div>")
        }
        return result
    }

    StringBuilder operationProperty(OperationPropertyType prop) {
        return new StringBuilder().append(convertToMathML(prop.name, prop.assign))
    }

    StringBuilder stepDeps(StepDependencyType deps) {
        StringBuilder result = new StringBuilder()
        if (!deps || !deps.step) {
            return result
        }
        result.append("<h3>Step Dependencies</h3>")
        result.append("\n<ul>")
        deps.step.inject(result) { r, s ->
            StringBuilder dep = new StringBuilder(s.oidRef.oidRef)
            if (s.dependents) {
                dep.append(": ").append(transitiveStepDeps(s.dependents))
            }
            r.append(["<li>", "</li>\n"].join(dep.toString()))
        }
        return result.append("</ul>")
    }

    StringBuilder transitiveStepDeps = { ds ->
        StringBuilder result = new StringBuilder()
        if (!ds) {
            return result
        }
        def deps = []
        ds.each { deps << it.oidRef }

        result.append(deps.join(", "))
        return deps.size() == 1 ? result :
                new StringBuilder("[").append(result).append("]")
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
            	}
            	catch(Exception notANumber) {
            	}
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

    private def expandNestedSymbRefs = { JAXBElement<SymbolRefType> symbRef ->
        final EquationType TRANSF_EQ = resolveSymbolReference(symbRef.value)
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
    }.memoizeAtMost(10)

    private def expandNestedUniop = { JAXBElement<UniopType> jaxbUniop ->
        UniopType uniop = jaxbUniop.value
        UniopType replacement
        if (uniop.symbRef) {
            final EquationType TRANSF_EQ = resolveSymbolReference(uniop.symbRef)
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
            def expanded = expandNestedUniop.call(wrapJaxb(uniop.uniop))?.value
            if (expanded && !(expanded.equals(uniop.uniop))) {
                uniop.uniop = expanded
            }
        } else if (uniop.binop) {
            def expanded = expandNestedBinop.call(wrapJaxb(uniop.binop))?.value
            if (expanded && !(expanded.equals(uniop.binop))) {
                uniop.binop = expanded
            }
        }
        if (replacement) {
            return wrapJaxb(replacement)
        }
        return jaxbUniop
    }.memoizeAtMost(10)

    private def expandNestedBinop = { JAXBElement<BinopType> jaxbBinop ->
        BinopType binop = jaxbBinop.value
        List<JAXBElement> terms = binop.content
        def expandedTerms = terms.collect { c ->
            switch (c.value) {
                case SymbolRefType:
                    return expandNestedSymbRefs.call(c)
                    break
                case BinopType:
                    return expandNestedBinop.call(c)
                    break
                case UniopType:
                    return expandNestedUniop.call(c)
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
    }.memoizeAtMost(10)

    private EquationType expandEquation(EquationType equation) {
        List<JAXBElement> eqTerms = equation.scalarOrSymbRefOrBinop
        List<JAXBElement> expandedTerms = eqTerms.collect {
            switch(it.value) {
                case BinopType:
                    return expandNestedBinop.call(it)
                    break
                case UniopType:
                    return expandNestedUniop.call(it)
                    break
                case SymbolRefType:
                    return expandNestedSymbRefs.call(it)
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

    private void convertEquation(final def equation, StringBuilder builder) {
        def equationToProcess
        if ((equation instanceof EquationType) || (equation instanceof Equation)) {
            equationToProcess = expandEquation(equation)
        } else {
            equationToProcess = equation
        }
        List<MathsSymbol> symbols = MathsUtil.convertToSymbols(equationToProcess).reverse()
        List<String> stack=new LinkedList<String>()
        symbols.each {
           stack.push(it)
        }
        prefixToInfix(builder, stack)
    }

    private String convertToMathML(def equation) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        convertEquation(equation, builder)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    private String convertToMathML(String lhs, def equation) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(lhs))
        builder.append(op("="))
        convertEquation(equation, builder)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    //works with EquationType and Equation as well
    private String convertToMathML(EquationType lhs, EquationType rhs) {
        StringBuilder output = new StringBuilder("<div>")
        output.append("<math display='inline'><mstyle>")
        convertEquation(lhs, output)
        output.append(op("="))
        convertEquation(rhs, output)
        output.append("</mstyle></math>")
        return output.append("</div>").toString()
    }

    private String convertToMathML(String lhs, ScalarRhs srhs) {
         if (srhs.equation) {
            return convertToMathML(lhs, srhs.equation)
        }
        if (srhs.symbRef) {
            return convertToMathML(lhs, srhs.symbRef)
        }
        StringBuilder result = new StringBuilder("<math display='inline'><mstyle>")
        result.append(oprand(lhs)).append(op("="))
        if (srhs.scalar) {
            result.append(oprand(scalar(srhs.scalar.value)))
        }
        return result.append("</mstyle></math>").toString()
    }

    private String convertToMathML(String lhs, Rhs rhs) {
        if (rhs.equation) {
            return convertToMathML(lhs, rhs.equation)
        }
        if (rhs.symbRef) {
            return convertToMathML(lhs, rhs.symbRef)
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
    private EquationType resolveSymbolReference(SymbolRefType ref) {
        EquationType transfEq
        if (ref.blkIdRef) {
            String transfRef = "${ref.blkIdRef}_${ref.symbIdRef}"
            transfEq = continuousCovariateTransformations[transfRef]
        } else {
            String transfRef = ref.symbIdRef
            transfEq = continuousCovariateTransformations.find{ it.key.contains("_${transfRef}")}?.value
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
