package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.commontypes.ScalarRhs
import eu.ddmore.libpharmml.dom.commontypes.SequenceType
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinitionType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityLevelDefnType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.trialdesign.BolusType
import eu.ddmore.libpharmml.dom.trialdesign.InfusionType
import eu.ddmore.libpharmml.dom.uncertml.NormalDistribution
import net.biomodels.jummp.plugins.pharmml.maths.MathsSymbol
import net.biomodels.jummp.plugins.pharmml.maths.OperatorSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsUtil
import eu.ddmore.libpharmml.dom.maths.EquationType


class PharmMlTagLib {
    static namespace = "pharmml"

    private void prefixToInfix(StringBuilder builder, List<MathsSymbol> stack) {
        if (stack.isEmpty()) {
            return;
        }
        MathsSymbol symbol=stack.pop()
        if (symbol instanceof OperatorSymbol) {
            OperatorSymbol operator=symbol as OperatorSymbol
            if (operator.type==OperatorSymbol.OperatorType.BINARY) {
                builder.append(operator.getOpening())
                prefixToInfix(builder,stack)
                builder.append(operator.getMapping())
                prefixToInfix(builder,stack)
                builder.append(operator.getClosing())
            } else {
                builder.append(operator.getMapping())
                builder.append(operator.getOpening())
                prefixToInfix(builder,stack)
                builder.append(operator.getClosing())
            }
            return;
        } else {
            builder.append(symbol.getMapping())
            return;
        }
        prefixToInfix(builder, stack)
    }

    private String convertToMathML(def equation) {
        List<MathsSymbol> symbols = MathsUtil.convertToSymbols(equation).reverse()
        StringBuilder builder=new StringBuilder("")
        List<String> stack=new LinkedList<String>()
        symbols.each {
               stack.push(it)
        }
        prefixToInfix(builder, stack)
        return builder.toString()
    }

    def simpleParams = { attrs ->
        if (!attrs.parameter) {
            return
        }
        def s = attrs.parameter.inject(new StringBuilder()) { s, p ->
            s.append(p.symbId)
            return (p.assign ? rhs(p.assign, s.append("=")) : s).append("&nbsp;")
        }
        out << s.toString()
    }

    def functionDefinitions = { attrs ->
        if (!attrs.functionDefs) {
            out << "No function definitions were found."
            return
        }
        def result = new StringBuilder("<table>\n\t")
        result.append("<thead>\n")
        result.append("<tr>\n<th>Identifier</th><th>Type</th></tr>\n")
        result.append("</thead>\n<tbody>\n")
        attrs.functionDefs.each { d ->
            result.append("<tr><td class=\"value\">")
            result.append(d.symbId)
            result.append("</td><td class=\"value\">")
            result.append(d.symbolType.value())
            result.append("</td></tr>\n")
        }
        result.append("</tbody>\n</table>")
        out << result.toString()
    }

    def variabilityModel = { attrs ->
        if (!attrs.variabilityModel) {
            return
        }
        def result = new StringBuilder("<h3>Variability Model</h3>\n<table>\n<thead><tr>")
        result.append("<th>Identifier</th><th>Name</th><th>Levels</th><th>Type</th></tr>")
        result.append("\n</thead>\n<tbody>\n")
        attrs.variabilityModel.each { m ->
            result.append("<tr><td class=\"value\">")
            result.append(m.blkId)
            result.append("</td><td class=\"value\">")
            String modelName = m.name ? m.name : "&nbsp;"
            result.append(modelName)
            result.append("</td><td class=\"value\">")
            result.append(variabilityLevel(m.level))
            result.append("</td><td class=\"value\">")
            result.append(m.type.value())
            result.append("</td></tr>\n")
        }
        out << result.append("</tbody>\n</table>").toString()
    }

    StringBuilder variabilityLevel(VariabilityLevelDefnType variabilityLevels) {
        def result = new StringBuilder()
        if (!variabilityLevels) {
            return result.append("&nbsp;")
        }
        variabilityLevels.each { l ->
            result.append("<p class=\"default\">")
            if (l.name) {
                result.append(l.name.value)
            } else {
                result.append(l.symbId)
            }
            if (l.parentLevel) {
                result.append(",&nbsp;")
                result.append("parent level:").append(l.parentLevel.symbRef)
            }
            result.append("</p>")
        }
        return result
    }

    def covariates = { attrs ->
        if (!attrs.covariate) {
            return
        }
        out.println("<h3>Covariate Model</h3>")
        def result = new StringBuilder()
        attrs.covariate.each { c ->
            result.append("<div>")
            if (c.simpleParameter) {
                result.append("<p><span class=\"bold\">Simple parameters:</span>&nbsp;")
                c.simpleParameter.inject(result) { r, p ->
                    r.append(p.symbId)
                    return (p.assign ? rhs(p.assign, r.append("=")) : r).append("&nbsp;")
                }
                result.append("</p>")
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
        def result = new StringBuilder("<p>")
        result.append("<span class=\"bold\"Type: Categorical</span>").append("&nbsp;")
        result.append(symbId).append("~")
        result.append(distribution(c.probability))
        return result.append("</p>")
    }

    StringBuilder contCov = { c, symbId ->
        def result = new StringBuilder("<p>")
        result.append("<span class=\"bold\">Type:</span>").append("&nbsp;Continuous</p>\n<p>")
        result.append(symbId)
        if (c.abstractContinuousUnivariateDistribution) {
            result.append(distribution(c.abstractContinuousUnivariateDistribution))
            result.append("</p><p>")
        }
        result.append("<span class=\"bold\">Transformation:</span>")
        result.append(convertToMathML(c.transformation.equation))
        return result.append("</p>")
    }

    def observations = { attrs ->
        if (!attrs.observations || attrs.observations.size() == 0) {
            out << "No observations defined in the model."
        }

        StringBuilder result = new StringBuilder()
        // skip parameters to avoid rendering complex maths
        if (attrs.observations.parameter) {
            out << "<strong>Observation parameters have not been displayed. We apologise for the inconvenience.</strong>\n"
        }
        if (attrs.observations.continuous) {
            attrs.observations.continuous.each { c ->
                result.append(randomEffect(c.randomEffect))
            }
        }
        out << result.toString()
    }

    def randomEffect = { re ->
        if (!re) {
            return "No random effect defined in the observation model.\n"
        }
        return distribution(re.distribution[0])
    }

    def distribution = { d ->
        if (!d) {
            return
        }
        def StringBuilder result = new StringBuilder("&nbsp;~&nbsp;N(")
        def distributionType = d.value
        if (distributionType instanceof NormalDistribution) {
            return result.append(normalDistribution(d))
        }
    }

    def normalDistribution = { d ->
        StringBuilder result = new StringBuilder()
        NormalDistribution distrib = d.value
        String mean = distrib.mean.var.varId
        result.append(mean).append(",&nbsp;")
        String stdDev = distrib.stddev?.var?.varId
        if (stdDev) {
            result.append(stdDev).append(",&nbsp;")
        }
        String variance = distrib.variance?.var?.varId
        if (variance) {
            result.append(variance)
        }
        result.append(")&nbsp;")

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
            result.append(t.id).append("</td><td>").append(t.name? t.name : "&nbsp;").append("</td><td>").append(dosingRegimen(t.dosingRegimen))
            result.append("</td>\n</tr>\n")
        }
        result.append("</tbody></table>")
        out << result.toString()
    }

    StringBuilder dosingRegimen = { dr ->
        def result = new StringBuilder("<table>\n<thead><tr><th>Type</th><th>Description</th></tr></thead>\n<tbody>\n<tr><td>")
        // JAXB returns an Object, so we need to guess the type
        def regimen
        boolean isInfusion = false
        // could be either Bolus or Infusion - no way of telling without casting
        try {
            regimen = dr[0].bolusOrInfusion[0] as InfusionType
            isInfusion = true
        } catch (ClassCastException e) {
            regimen = dr[0].bolusOrInfusion[0] as BolusType
        }

        result.append(isInfusion ? "Infusion" : "Bolus").append("</td><td>")
        result.append(doseAmount(regimen.doseAmount)).append("<p>")
        if (regimen.dosingTimes) {
            result.append(dosingTimes(regimen.dosingTimes))
        } else if (regimen.steadyState) {
            result.append(steadyState(regimen.steadyState))
        }
        result.append("</p>")
        if (isInfusion) {
            result.append("<p>Duration: ").append(regimenDuration(regimen.duration)).append("</p>")
        }
        result.append("</td></tr></tbody></table>")
        return result
    }

    StringBuilder doseAmount = { a ->
        def amt = rhs(a?.amount, new StringBuilder("<p>Dose amount:")).append("</p>")
        def d = variable(a?.doseVar, new StringBuilder("<p>Dose variable:")).append("</p>")
        def t
        if (a?.targetVar) {
            t = variable(a.targetVar, new StringBuilder("<p>Target variable:")).append("</p>")
        }
        return  t ? amt.append(d).append(t) : amt.append(d)
    }

    StringBuilder scalarRhs = { r, text ->
        if (r.getScalar()) {
            text.append(scalar(r))
        } else if (r.getString()) {
            text.append(string(r))
        } else if (r.getVar()) {
            text.append(variable(r))
        }
        return text
    }

    StringBuilder rhs = { r, text ->
        if (r.getConstant()) {
            text.append(constant(r))
        } else if (r.getScalar()) {
            text.append(scalar(r))
        } else if (r.getString()) {
            text.append(string(r))
        } else if (r.getVar()) {
            text.append(variable(r))
        } else if (r.getSequence()) {
            text.append(sequence(r.sequence))
        } else if (r.getVector()) {
            text.append(vector(r))
        }else { // equation, dataset, distribution or function call
            text.append(" cannot be extracted, sorry. ${r.inspect()}")
        }
        return text
    }

    def constant = { c -> c.op }

    def variable(VariableDefinitionType v) {
        v?.symbId
    }

    //TODO HANDLE VariableAssignmentType too
    StringBuilder variable(VariableDefinitionType v, StringBuilder text) {
        text.append(v?.symbId ? v.symbId : " undefined.")
    }

    def scalar = { s -> s.value }

    def string = { s -> s.value }

    def sequence = { s ->
        new StringBuilder("[").append(s.begin).append(":").append(s.stepSize).append(":").append(s.end).
                append("]")
    }

    StringBuilder vector = { v ->
        def result = new StringBuilder()
        if (!v) {
            return result.append("&nbsp;")
        }
        result.append("[")
        def iterator = v.vector.sequenceOrScalar.iterator()
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

    def regimenDuration = { d ->
        if (d.equation || d.functionCall) {
            return "Regimen duration cannot be extracted, sorry."
        }
        if (d.scalar) {
            return scalar(d.scalar)
        }
        if (d.string) {
            return string(d.string)
        }
        if (d.var) {
            return variable(d.var)
        }
    }

    StringBuilder dosingTimes = { dt ->
        def result = new StringBuilder("Dosing Times:")
        dt.sequenceOrScalar.each { t ->
            def time
            try {
                time = t as ScalarRhs
                result.append(scalar(time))
            } catch(ClassCastException ignored) {
                time = t as SequenceType
                result.append(sequence(time))
            }
        }
        return result
    }

    StringBuilder steadyState = { ss ->
        def result = new StringBuilder("Steady state:")
        def interval = scalarRhs(ss.interval, new StringBuilder("Interval:"))
        def endTime = scalarRhs(ss.endTime, new StringBuilder("End time:"))
        return result.append(interval).append("&nbsp;").append(endTime)
    }

    def treatmentEpoch = { attrs ->
        if (!attrs.epoch) {
            out << "No treatment timeframe defined in the model - epoch fail!"
            return
        }
        def result = new StringBuilder("<table><thead>\n<tr><th>Identifier</th><th>Name</th>")
        result.append("<th>Start</th><th>End</th><th>Occasions</th><th>Treatment</th></tr></thead>\n<tbody>\n")
        def td = new StringBuilder("</td><td>")
        attrs.epoch.each { e ->
            result.append("<tr><td>").append(e.id).append(td).append(e.name ? e.name : "&nbsp;")
            result.append( e.start ? scalarRhs(e.start, new StringBuilder("</td><td>")) : "&nbsp;</td><td>")
            result.append( e.end ? scalarRhs(e.end, new StringBuilder("</td><td>")) : "&nbsp;</td><td>")
            result.append( e.occasion ? occasions(e.occasion, new StringBuilder("</td><td>")) : "&nbsp;</td><td>")
            result.append(treatmentRef(e.treatmentRef, new StringBuilder("</td><td>"))).append("</td></tr>")
        }
        out << result.append("\n</tbody></table>").toString()
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

    StringBuilder treatmentRef = { t, text ->
        t.each {
            text.append(it.idRef)
        }
        text
    }

    def group = { attrs ->
        if (!attrs.group) {
            out << "No treatment groups defined in the model."
            return
        }
        new StringBuilder("</td><td>")
        def result = new StringBuilder("<table><thead>\n<tr><th>Identifier</th><th>Name</th>")
        result.append("<th>Treatment</th><th>Individuals</th><th>Variability</th></tr>")
        result.append("</thead>\n<tbody>\n")

        attrs.group.each { g ->
            result.append("<tr><td>").append(g.id).append("</td><td>").append(g.name ? g.name : "&nbsp;")
            result.append(
                    g.treatmentEpochRefOrWashout ?
                        treatmentEpochRefs(g.treatmentEpochRefOrWashout, new StringBuilder("</td><td>")) :
                        "</td><td>&nbsp;")
            result.append("</td><td>").append(g.individuals ? individuals(g.individuals) :
                        "&nbsp;</td><td>&nbsp;")
            result.append("</td></tr>")
        }
        out << result.append("\n</tbody></table>").toString()
    }

    StringBuilder treatmentEpochRefs = { refs, text ->
        refs.each {
            if (it.idRef) {
                // code repetition is faster than another method call
                text.append(it.idRef)
            } else {
                text.append("Washout")
            }
            text.append("&nbsp;")
        }
        text
    }

    StringBuilder individuals = { i ->
        List<String> indivCounts = []
        def indivVars = []
        i.each {
            indivVars << it.levelId
            def individualCount = it.scalar ? it.scalar.value.toPlainString() : "undefined"
            indivCounts << individualCount
        }

        def result = new StringBuilder()
        if (indivCounts.size() == 1) {
            return result.append(indivCounts[0]).append("</td><td>").append(indivVars[0])
        } else {
            def indivCountsIterator = indivCounts.iterator()
            while (indivCountsIterator.hasNext()) {
                result.append(indivCountsIterator.next())
                if (indivCountsIterator.hasNext()) {
                    result.append("<br/>")
                }
            }
            result.append("</td><td>")
            def indivVarsIterator = indivVars.iterator()
            while (indivVarsIterator.hasNext()) {
                result.append(indivVarsIterator.next())
                if (indivVarsIterator.hasNext()) {
                    result.append("<br/>")
                }
            }
        }
        result
    }

    def checkModellingSteps = { attrs ->
        if (!attrs.steps) {
            out << "No modelling steps defined in the model."
            return
        }
    }

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
                result.append("&nbsp;")
            }
            if (v.scalar) {
                result.append(scalar(v.scalar))
            }
            result.append("</td></tr>")
        }
        result.append("</tbody></table>")
        out << result.toString()
    }

    def estSimSteps = { attrs ->
        if (!attrs.steps) {
            return
        }
        /*
        * Check which kind of step we are dealing with.
        * Estimation and simulation steps cannot be mixed, hence only look at the first one to decide.
        */
        boolean areSimulations = true
        def step = attrs.steps.first()
        if (step instanceof EstimationStepType) {
            areSimulations = false
        }
        areSimulations ? simulationSteps(attrs.steps) : estimationSteps(attrs.steps)
    }

    def simulationSteps = { steps ->
        if (!steps) {
            return
        }
        def result = new StringBuilder("<h3>Simulation Steps</h3>\n")
        result.append("<table><thead><tr>").append("<th>Identifier</th>")
        result.append("<th>Replicates</th><th>Initial Values</th>")
        result.append("<th>Output variable</th><th>Observation times</th></tr>")
        result.append("</thead><tbody>")
        steps.each { s ->
            result.append("\n<tr><td>").append(s.id).append("</td><td>")
            result.append(s.replicates.scalar.value).append("</td><td>")
            result.append(initialValues(s.initialValue)).append("</td><td>")
            result.append(simulationObservations(s.observations)).append("</td></tr>")
        }
        out << result.append("</tbody></table>").toString()
    }

    StringBuilder initialValues = {
        def result  = new StringBuilder()
        if (!it) {
            return result.append("&nbsp;")
        }
        def iValues = it.iterator()
        while (iValues.hasNext()) {
            def v = iValues.next()
            if (v.block) {
                result.append(v.block).append(".")
            }
            result.append(v.symbId).append("=")
            if (v.scalar) {
                result.append(v.scalar.value.toPlainString())
            }
            if (iValues.hasNext()) {
                result.append(",<br/>")
            }
        }
        result
    }

    StringBuilder simulationObservations = {
        if (!it) {
            return new StringBuilder("None.</td><td>&nbsp;")
        }
        def timepointList = []
        def outputList = []
        it.each {
            outputList << it.output
            timepointList << it.timepoints
        }
        def result = new StringBuilder()
        def iterator = outputList.iterator()
        while (iterator.hasNext()) {
            def o = iterator.next()
            //each observation has variables and timepoints
            if (o.var) {
                o.var.each { v ->
                    if (v.block) {
                        result.append(v.block).append(".")
                    }
                    result.append(v.symbId)
                    if (iterator.hasNext()) {
                        result.append("<br/>")
                    }
                }
            } else {
                return new StringBuilder("None.")
            }
        }

        result.append("</td><td>")
        if (!timepointList) {
            return result.append("None.")
        } else {
            result.append("[")
        }
        iterator = timepointList.iterator()
        while(iterator.hasNext()) {
            def t = iterator.next()
            result.append(rhs(t, new StringBuilder()))
            if (iterator.hasNext()) {
                result.append(",<br/>")
            }
        }
        result.append("]")
    }

    def estimationSteps = { steps ->
        if (!steps) {
            return
        }
        def result = new StringBuilder("<h3>Estimation Steps</h3>")
        result.append("\n<table><thead><tr><th>Identifier</th>")
        result.append("<th>Initial Values</th><th>Estimation Operations</th></tr></thead><tbody>")
        steps.each { s ->
            result.append("<tr><td>").append(s.id).append("</td><td>")
            if (s.initialValue) {
                result.append(initialValues(s.initialValue)).append("</td><td>")
            } else {
                result.append("&nbsp;</td><td>")
            }
            if (s.estimationOperation) {
                result.append(estimationOps(s.estimationOperation))
            } else {
                result.append("&nbsp;")
            }
            result.append("</td></tr>")
        }
        out << result.append("</tbody></table>").toString()
    }

    StringBuilder estimationOps = { operations ->
        def result = new StringBuilder()
        if (!operations) {
            return result
        }
        if ( operations.size() == 1 ) {
            return result.append(operations[0].opType)
        } else {
            result.append("[")
            def iOperations = operations.iterator()
            while (iOperations.hasNext()) {
                result.append(iOperations.next().opType)
                if (iOperations.hasNext()){
                    result.append(",&nbsp;")
                }
            }
            return result.append("]")
        }
    }

    def stepDeps = { attrs ->
        StringBuilder result = new StringBuilder("<h3>Step Dependencies</h3>")
        if (!attrs.deps || !attrs.deps.step) {
            return result.append("<p>There are no step dependencies defined in the model.</p>")
        }
        result.append("[")
        def iterator = attrs.deps.step.iterator()
        while (iterator.hasNext()) {
            def s = iterator.next()
            // each step has a list of dependencies, need to fetch those as well.
            result.append(s.idRef)
            if (s.dependantStep) {
                StringBuilder dependenciesOfThisStep = transitiveStepDeps(s.dependantStep)
                result.append(",&nbsp;").append(dependenciesOfThisStep)
            }

            if (iterator.hasNext()) {
                result.append(",&nbsp;")
            }
        }
        out << result.append("]").toString()
    }

    StringBuilder transitiveStepDeps = { ds ->
        StringBuilder result = new StringBuilder()
        if (!ds) {
            return result
        }
        result.append(ds.join(",&nbsp;"))
    }
}
