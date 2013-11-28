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
import eu.ddmore.libpharmml.dom.maths.FunctionCallType
import eu.ddmore.libpharmml.dom.maths.EquationType
import eu.ddmore.libpharmml.dom.maths.UniopType
import eu.ddmore.libpharmml.dom.modeldefn.CategoryType
import eu.ddmore.libpharmml.dom.modeldefn.CovariateDefinitionType
import eu.ddmore.libpharmml.dom.modeldefn.GaussianObsError
import eu.ddmore.libpharmml.dom.modeldefn.GeneralObsError
import eu.ddmore.libpharmml.dom.modeldefn.IndividualParameterType
import eu.ddmore.libpharmml.dom.modeldefn.LhsTransformationType
import eu.ddmore.libpharmml.dom.modeldefn.ParameterRandomVariableType
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameterType
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

    private Map<String, String> modellingTabsMap

    StringBuilder simpleParams(List<SimpleParameterType> parameters) {
        def outcome = new StringBuilder()
        if (!parameters) {
            return outcome
        }
        outcome.append("<div class='spaced'>")
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
        return outcome.append("</div>")
    }

    StringBuilder randomVariables(List<ParameterRandomVariableType> rv) {
        def output = new StringBuilder()
        rv.inject(output) { o, i ->
            if (i.abstractContinuousUnivariateDistribution) {
                o.append("<div>")
                o.append(distributionAssignment(i.symbId, i.abstractContinuousUnivariateDistribution))
                o.append("&nbsp;&mdash;&nbsp;").append(i.variabilityReference.symbRef.symbIdRef)
                o.append("</div>\n")
            }
        }
        return output
    }

    StringBuilder individualParams(List<IndividualParameterType> parameters, List<ParameterRandomVariableType> rv,
                List<CovariateDefinitionType> covariates) {
        def output = new StringBuilder("<div class='spaced'>")
        parameters.each { p ->
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
                            SymbolRefType covEffectKey
                            c.fixedEffect.each { fe ->
                                if (fe.category) {
                                    def catIdSymbRef = new SymbolRefType()
                                    def trickReference = new StringBuilder("<msub><mi>")
                                    trickReference.append(c.symbRef.symbIdRef).append("</mi><mi>")
                                    trickReference.append(fe.category.catId).append("</mi></msub>")
                                    catIdSymbRef.symbIdRef = trickReference.toString()
                                    covEffectKey = catIdSymbRef
                                } else {
                                    covEffectKey = c.symbRef
                                }
                                fixedEffects << fe.symbRef
                            }
                            fixedEffectsCovMap[covEffectKey] = fixedEffects
                        }
                        def fixedEffectsTimesCovariateList = []
                        if (fixedEffectsCovMap) {
                            fixedEffectsCovMap.each{
                                def thisCov = []
                                thisCov.add(wrapJaxb(it.key))
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
        return output.append("</div>")
    }

    def functionDefinitions = { attrs ->
        if (!attrs.functionDefs) {
            return
        }
        def result = new StringBuilder("<h3>Function Definitions</h3>")
        attrs.functionDefs.each { d ->
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
        out << result.toString()
    }

    def structuralModel = { attrs ->
        if (!attrs.sm) {
            return
        }
        def result = new StringBuilder("<h3>Structural ")
        boolean multipleStructuralModels = attrs.sm.size() > 1
        if (!multipleStructuralModels) {
            result.append("Model ").append(attrs.sm[0].name?.value ?: attrs.sm[0].blkId)
        } else {
            result.append("Models")
        }
        result.append("</h3>\n")
        if (!multipleStructuralModels) {
            def model = attrs.sm[0]
            if (model.simpleParameter) {
                result.append("<p class=\"bold\">Parameters </p>")
                result.append(simpleParams(model.simpleParameter))
            }
            if (model.commonVariable) {
                result.append("<p class=\"bold\">Variable definitions</p>")
                result.append(["<div>", "</div>\n"].join(
                    commonVariables(model.commonVariable, attrs.iv).toString()))
            }
        } else {
            sm.each { s ->
                result.append("<h4>").append(s.name?.value ?: s.blkId).append("</h4>\n")
                //todo expand
            }
        }
        out << result.toString()
    }

    StringBuilder commonVariables(List<JAXBElement> vars, def indepVar) {
        def result = new StringBuilder()
        if (!vars) {
            return result
        }
        def initialConditions = [:]
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
        return result
    }

    def variabilityModel = { attrs ->
        if (!attrs.variabilityModel) {
            return
        }
        def result = new StringBuilder("<h3>Variability Model</h3>\n<table class='views-table cols-4'>\n<thead><tr>")
        result.append("<th>Identifier</th><th>Name</th><th>Level</th><th>Type</th></tr>")
        result.append("\n</thead>\n<tbody>\n")
        attrs.variabilityModel.each { m ->
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
    def parameterModel = { attrs ->
        if (!attrs.parameterModel) {
            return
        }
        out << "<h3>Parameter Model</h3>"

        def result = new StringBuilder()
        result.append("<span class=\"bold\">Parameters </span>")
        attrs.parameterModel.each { pm ->
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
               String randoms=randomVariables(rv)
               if (randoms) {
                       result.append(randoms)
               }
               String individuals = individualParams(individualParameters, rv, attrs.covariates)
               if (individuals) {
                       result.append(individuals)
               }
               result.append("</div>")
        }

        out << result.toString()
    }

    def covariates = { attrs ->
        if (!attrs.covariate) {
            return
        }
        out << "<h3>Covariate Model</h3>"
        def result = new StringBuilder()
        attrs.covariate.each { c ->
            result.append("<div>")
            if (c.simpleParameter) {
                result.append("<div><span class=\"bold\">Parameters</span></div>")
                result.append(simpleParams(c.simpleParameter))
            }
            if (c.covariate) {
                c.covariate.each {
                    result.append(
                        it.getCategorical() ? categCov(it.getCategorical(), it.symbId) :
                                contCov(it.getContinuous(), it.symbId))
                }
            }
            result.append("</div>")
        }
        out << result.toString()
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

    StringBuilder contCov = { c, symbId ->
        def result = new StringBuilder("<p>")
        result.append("<span class=\"bold\">Continuous covariate ${symbId}</span>\n</p>\n<p>")
        if (c.abstractContinuousUnivariateDistribution) {
            result.append(distributionAssignment(symbId, c.abstractContinuousUnivariateDistribution))
            result.append("</p><p>")
        }
        result.append(convertToMathML("Transformation",c.transformation.equation))
        return result.append("</p>")
    }

    def observations = { attrs ->
        if (!attrs.observations || attrs.observations.size() == 0) {
            return
        }

        StringBuilder result = new StringBuilder()
        result.append("<h3>Observation Model</h3>")
        attrs.observations.each { om ->
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
            result.append(simpleParams(simpleParameters))
            String randoms = randomVariables(rv)
            if (randoms) {
                result.append(randoms)
            }
            //result.append("\n<p>")
            if (obsErr.symbol?.value) {
                result.append(obsErr.symbol.value)
            }
            if (obsErr instanceof GaussianObsError) {
                result.append(gaussianObsErr(obsErr)).append(" ")
            } else { // can only be GeneralObsError
                result.append(generalObsErr(obsErr)).append(" ")
            }
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

    def treatment = { attrs ->
        if (!attrs.treatment) {
            out << "No treatments defined in the model."
            return
        }
        def result = new StringBuilder("<table>\n<thead>\n<tr><th>Identifier</th><th>Name</th><th>Dosing Regimen</th></tr></thead><tbody>\n")
        attrs.treatment.each { t ->
            result.append("<tr><td>")
            result.append(t.id).append("</td><td>").append(t.name? t.name : " ").append("</td><td>").append(dosingRegimen(t.dosingRegimen))
            result.append("</td>\n</tr>\n")
        }
        result.append("</tbody></table>")
        out << result.toString()
    }

    StringBuilder scalarRhs = { r, text ->
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
            def vectorElement = iterator.next()
            def item
            try {
                item = vectorElement as ScalarRhs
                result.append(oprand(item.value.toPlainString()))
            } catch (ClassCastException ignored) {
                item = vectorElement as SequenceType
                result.append(sequence(item))
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
    StringBuilder steadyState = { ss ->
        def result = new StringBuilder("<strong>Steady state</strong>")
        def i = ss.interval
        def end = ss.endTime
        result.append("<div>Interval: ").append(convertToMathML(i.symbRef.symbIdRef, i.assign))
        result.append("</div><div>")
        result.append("End time: ").append(convertToMathML(end.symbRef.symbIdRef, end.assign))
        result.append("</div>")
        return result
    }

    //todo - remove/reuse
    StringBuilder occasions = { occasions, text ->
        text.append("[")
        occasions.each { o ->
            StringBuilder sb = new StringBuilder("(level:")
            sb.append(o.levelId).append(", symbolIdentifier:").append(o.symbId).append(")")
            text.append(sb)
        }
        text.append("]")
    }

    // NEW TRIAL DESIGN
    def trialStructure = { attrs ->
        if (!attrs.structure) {
            return
        }
        def result = new StringBuilder()
        result.append("<h3>Structure overview</h3>\n")
        def tds = new TrialDesignStructure(attrs.structure.arm, attrs.structure.epoch,
                    attrs.structure.cell, attrs.structure.segment)
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

        /* segments and activities */
        List activities = attrs.structure.activity
        // avoid the need to increase the size of the map, because re-hashing is expensive
        def segmentActivitiesMap = new HashMap(activities.size(), 1.0)
        attrs.structure.segment.each { s ->
            segmentActivitiesMap[s.oid] = s.activityRef.collect{ a ->
                attrs.structure.activity.find{ a.oidRef.equals(it.oid) }
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
        /* epochs and occasions */
        if (attrs.structure.studyEvent) {
            ObservationEventsMap oem = new ObservationEventsMap(attrs.structure.studyEvent)
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
        out << result.toString()
    }

    def trialDosing = { attrs ->
        if (!attrs.dosing) {
            return
        }
        def result = new StringBuilder("<h4>Individual dosing</h4>\n")
        attrs.dosing.each { d ->
            if (d.dataSet) {
                dataSet(d.dataSet, null, result)
            }
        }
        out << result.toString()
    }

    def trialPopulation = { attrs ->
        if (!attrs.pop) {
            return
        }
        def result = new StringBuilder("<h4>Population</h4>\n")
        if (attrs.pop.variabilityReference) {
            result.append("<span><strong>Variability level: </strong>")
            result.append(attrs.pop.variabilityReference.symbRef.symbIdRef).append("</span>")
        }
        if (attrs.pop.dataSet) {
            dataSet(attrs.pop.dataSet, null, result)
        }
        out << result.toString()
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

    def decideModellingStepsTabs = { attrs ->
        if (!attrs.estimation && !attrs.simulation) {
            return
        }
        modellingTabsMap = new HashMap<String, String>(3, 1.0)
        if (attrs.estimation) {
            final String EST_TAB = "estimationSteps"
            modellingTabsMap["est"] = EST_TAB
            out << "<li><a href='#${EST_TAB}'>Estimation Steps</a></li>"
        }
        if (attrs.simulation) {
            final String SIM_TAB = "simulationSteps"
            modellingTabsMap["sim"] = SIM_TAB
            out << "<li><a href='#${SIM_TAB}'>Simulation Steps</a></li>"
        }
    }

    def handleModellingStepsTabs = { attrs ->
        if (!attrs.estimation && !attrs.simulation && !modellingTabsMap) {
            return
        }
        if (!attrs.independentVariable) {
            // the default independent variable is assumed to be time.
            attrs.independentVariable = "time"
        }

        def result = new StringBuilder()
        if (attrs.estimation) {
            result.append("<div id='${modellingTabsMap["est"]}'>")
            result.append(estimationSteps(attrs.estimation))
            //only consider step dependencies here when there are no simulation steps
            if (modellingTabsMap.size() == 1) {
                result.append(stepDeps(attrs.deps))
            }
            result.append("</div>")
        }
        if (attrs.simulation) {
            result.append("<div id='${modellingTabsMap["sim"]}'>")
            result.append(simulationSteps(attrs.simulation, attrs.independentVariable))
            result.append(stepDeps(attrs.deps))
            result.append("</div>")
        }
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
        def result  = new StringBuilder("<div><h5>Estimation parameters</h5>\n")
        def fixedParams = params.parameterEstimation.findAll{ it.initialEstimate.fixed }
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

    private void convertEquation(def equation, StringBuilder builder) {
        List<MathsSymbol> symbols = MathsUtil.convertToSymbols(equation).reverse()
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
        if (rhs.getSymbRef()) {
            return convertToMathML(lhs, rhs.getSymbRef())
        }
        StringBuilder builder=new StringBuilder("<math display='inline'><mstyle>")
        builder.append(oprand(lhs))
        builder.append(op("="))
        if (rhs.getScalar()) {
            builder.append(oprand(scalar(rhs.scalar.value)))
        }
        else if (r.getSequence()) {
            builder.append(sequenceAsMathML(r.sequence))
        }
        else if (r.getVector()) {
            builder.append(vectorAsMathML(r))
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
        return new JAXBElement(new QName(""), elem.getClass(), elem)
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
}
