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
import eu.ddmore.libpharmml.dom.modeldefn.CovariateModelType
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

abstract class AbstractPharmMlRenderer implements IPharmMlRenderer {
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    private static final String IS_DEBUG_ENABLED = log.isDebugEnabled()
    private static final String IS_INFO_ENABLED = log.isInfoEnabled()

    /*
     * Parses an activity and writes it to a StringBuilder.
     * Returns whether to display a dosing footnote or not.
     */
    protected boolean activity(ActivityType activity, boolean isFirst, StringBuilder result) {
        boolean showDosingFootnote = false
        if (!isFirst) {
            result.append("<tr>")
        }
        result.append("<td>")
        result.append(activity.oid).append("</td>")
        if (activity.washout) {
            result.append("<td>washout</td><td>&mdash;</td><td>&mdash;</td><td>&mdash;</td>")
        } else {
            /* dosingRegimen is a JAXBElement */
            def regimen = activity.dosingRegimen.value
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
        return showDosingFootnote
    }

    protected StringBuilder transitiveStepDeps(def ds) {
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

    protected StringBuilder randomVariables(List<ParameterRandomVariableType> rv, Map rvMap) {
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
            output = new StringBuilder()
            output.append("Cannot display random variables.")
            log.error("Error encountered while rendering random variables ${rv.inspect()}: ${e.message}")
        }
        return output
    }

    protected StringBuilder individualParams(List<IndividualParameterType> parameters,
                List<ParameterRandomVariableType> rv, List<CovariateDefinitionType> covariates,
                List<String> indivParamNameList, Map<String, Equation> transfMap) {
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
                                        final EquationType transfEq = resolveSymbolReference(c.symbRef, transfMap)
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
            output = new StringBuilder("<div class='spaced'>")
            output.append("Cannot display individual parameters.")
            log.error("Error encountered while rendering individual parameters ${parameters.inspect()} using random variables ${rv.inspect()} and covariates ${covariates.inspect()}: ${e.message}")
        }
        return output.append("</div>")
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
    protected void handleCorrelations(List<CorrelationType> corList, Map corMap, Map rvMap,
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

    protected void buildCorrelationMap(CorrelationType c, Map correlationsMap) {
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

    protected void processCorrelations(Map<String, String> c, Map<String, List<String>> rv,
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

    protected void displayCorrelationMatrices(Map<String, String[][]> matrices,
                List<String> paramNames, StringBuilder output) {
        matrices.entrySet().each {
            convertToMathML(it.key, it.value, paramNames, output)
        }
    }

    protected StringBuilder gaussianObsErr(GaussianObsError e) {
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

    protected StringBuilder generalObsErr(GeneralObsError e) {
        def result = new StringBuilder()
        if (!e) {
            return result
        }
        if (e.assign) {
            result.append("<p>").append(convertToMathML(e.symbId, e.assign)).append("</p>")
        }
        return result
    }

    //TODO REMOVE THIS
    protected StringBuilder simpleParams(List<SimpleParameterType> parameters,
                Map<String, Equation> transfMap = [:]) {
        def outcome = new StringBuilder()
        if (!parameters) {
            return outcome
        }
        outcome.append("<div class='spaced'>")
        try {
            parameters.inject(outcome) { o, p ->
                String thisParam
                if (p.assign) {
                    thisParam = convertToMathML(p.symbId, p.assign, transfMap)
                } else {
                    thisParam = ["<math display='inline'><mstyle>", "</mstyle></math>"].join(op(p.symbId))
                }
                o.append("<span>")
                o.append(thisParam).append(";&nbsp;")
                o.append("</span>\n")
            }
        } catch(Exception e) {
            outcome.append("<p>Cannot display simple parameters.<p>")
            log.error("Error encountered while rendering simple params ${parameters.inspect()}: ${e.message}")
        } finally {
            return outcome.append("</div>")
        }
    }

    protected StringBuilder jaxbVector(VectorType vector) {
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

    protected StringBuilder variableAssignments(List<VariableAssignmentType> assignments, String heading) {
        def result = new StringBuilder("\n${heading}\n")
        assignments.inject(result){r,v ->
            r.append("<p>").append(convertToMathML(v.symbRef.symbIdRef, v.assign)).append("</p>")
        }
        return result
    }

    protected StringBuilder paramsToEstimate(ToEstimateType params) {
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

    protected StringBuilder estimParamsWithInitialEstimate(List<ParameterEstimateType> params,
                String heading) {
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

    protected StringBuilder estimationOps(def operations) {
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
                        result.append("\n<span>Algorithm ")
                        result.append(o.algorithm.name ? o.algorithm.name.value :
                                (o.algorithm.definition ? o.algorithm.definition : ""))
                        result.append("</span>\n")
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
                    variableMap << [ (it.value.columnRef.columnIdRef) : (it.value.symbRef.symbIdRef)]
                }
            }
            if (dsm.dataSet) {
                dataSet(dsm.dataSet, variableMap, result)
            }
        }
        return result
    }

    protected StringBuilder operationProperty(OperationPropertyType prop) {
        return new StringBuilder().append(convertToMathML(prop.name, prop.assign))
    }

    protected StringBuilder dataSet(DataSetType dataSet, Map variableMap, StringBuilder sb) {
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

     protected StringBuilder steadyState(def ss) {
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
            result = new StringBuilder()
            result.append("Cannot display steady state.")
            log.error("Cannot display steady state ${ss.properties}", e)
        }
        return result
    }

    protected StringBuilder occasions(def occasions, StringBuilder text) {
        text.append("[")
        occasions.each { o ->
            StringBuilder sb = new StringBuilder("(level:")
            sb.append(o.levelId).append(", symbolIdentifier:").append(o.symbId).append(")")
            text.append(sb)
        }
        return text.append("]")
    }

    protected StringBuilder distributionAssignment(String l, def d) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(l))
        builder.append(op("&sim;"))
        builder.append(distribution(d))
        builder.append("</mstyle></math>")
        return builder
    }

    protected StringBuilder distribution(def d) {
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

    protected String normalDistribution(def dist) {
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

    protected StringBuilder scalarRhs(def r) {
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

    protected StringBuilder rhs(Rhs r, StringBuilder text) {
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

    protected String scalar(def s) {
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

    protected String sequence(SequenceType s) {
        return [s.begin, s.stepSize, s.end].collect{rhs(it, new StringBuilder())}.join(":")
    }

    protected StringBuilder sequenceAsMathML(def s) {
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

    protected void prefixToInfix(StringBuilder builder, List<MathsSymbol> stack) {
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

    protected JAXBElement expandNestedSymbRefs(JAXBElement<SymbolRefType> symbRef,
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

    protected JAXBElement expandNestedUniop(JAXBElement<UniopType> jaxbUniop,
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

    protected JAXBElement expandNestedBinop(JAXBElement<BinopType> jaxbBinop,
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

    protected EquationType expandEquation(EquationType equation, Map<String, Equation> transfMap) {
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

    protected void convertEquation(def equation, StringBuilder builder, Map<String, Equation> transfMap = [:]) {
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

    protected String convertToMathML(def equation, Map<String, Equation> transfMap = [:]) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        convertEquation(equation, builder, transfMap)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    protected String convertToMathML(String lhs, def equation, Map<String, Equation> transfMap = [:]) {
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(lhs))
        builder.append(op("="))
        convertEquation(equation, builder, transfMap)
        builder.append("</mstyle></math>")
        return builder.toString()
    }

    //works with EquationType and Equation as well
    protected String convertToMathML(EquationType lhs, EquationType rhs, Map<String, Equation> transfMap = [:]) {
        StringBuilder output = new StringBuilder("<div>")
        output.append("<math display='inline'><mstyle>")
        convertEquation(lhs, output)
        output.append(op("="))
        convertEquation(rhs, output, transfMap)
        output.append("</mstyle></math>")
        return output.append("</div>").toString()
    }

    protected String convertToMathML(String lhs, ScalarRhs srhs, Map<String, Equation> transfMap = [:]) {
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

    protected String convertToMathML(String lhs, Rhs rhs, Map<String, Equation> transfMap = [:]) {
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

    protected String convertToMathML(DerivativeVariableType derivative, def iv) {
        String independentVariable = derivative.independentVariable?.symbRef?.symbIdRef ?: (iv ?: "t")
        String derivTerm="d${derivative.symbId}<DIVIDEDBY>d${independentVariable}"
        return convertToMathML(derivTerm, derivative.getAssign())
    }

    protected String convertToMathML(String lhs, List arguments, def equation) {
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

    protected void convertToMathML(String name, def matrix, List ipNames, StringBuilder output) {
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

    protected String op(String o) {
        return "<mo>${o}</mo>"
    }

    protected String oprand(String o) {
        if (o.contains("<DIVIDEDBY>")) {
            String[] parts=o.split("<DIVIDEDBY>")
            return "<mfrac><mi>${parts[0]}</mi><mi>${parts[1]}</mi></mfrac>"
        }
        return "<mi>${o}</mi>"
    }

    protected JAXBElement wrapJaxb(def elem) {
        return elem instanceof JAXBElement ? elem : new JAXBElement(new QName(""), elem.getClass(), elem)
    }

    protected JAXBElement applyBinopToList(List elements, String operator) {
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
    protected EquationType resolveSymbolReference(SymbolRefType ref, Map<String, Equation> transfMap) {
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

    protected void populateRandomVariableMap(final String id, final String level, Map rv) {
        def currentRVs = rv[level]
        if (!currentRVs) {
            currentRVs = []
        }
        currentRVs.add(id)
        rv[level] = currentRVs
    }

}
