package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariableType
import eu.ddmore.libpharmml.dom.commontypes.FalseBooleanType
import eu.ddmore.libpharmml.dom.commontypes.IntValueType
import eu.ddmore.libpharmml.dom.commontypes.RealValueType
import eu.ddmore.libpharmml.dom.commontypes.Rhs
import eu.ddmore.libpharmml.dom.commontypes.ScalarRhs
import eu.ddmore.libpharmml.dom.commontypes.SequenceType
import eu.ddmore.libpharmml.dom.commontypes.StringValueType
import eu.ddmore.libpharmml.dom.commontypes.SymbolRefType
import eu.ddmore.libpharmml.dom.commontypes.TrueBooleanType
import eu.ddmore.libpharmml.dom.commontypes.VariableAssignmentType
import eu.ddmore.libpharmml.dom.commontypes.VectorType
import eu.ddmore.libpharmml.dom.maths.Equation
import eu.ddmore.libpharmml.dom.maths.EquationType
import eu.ddmore.libpharmml.dom.modeldefn.CategoryType
import eu.ddmore.libpharmml.dom.modeldefn.GaussianObsError
import eu.ddmore.libpharmml.dom.modeldefn.GeneralObsError
import eu.ddmore.libpharmml.dom.modeldefn.ParameterRandomVariableType
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameterType
import eu.ddmore.libpharmml.dom.modeldefn.VariabilityLevelDefnType
import eu.ddmore.libpharmml.dom.modellingsteps.DatasetMappingType
import eu.ddmore.libpharmml.dom.modellingsteps.EstimationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.SimulationStepType
import eu.ddmore.libpharmml.dom.modellingsteps.OperationPropertyType
import eu.ddmore.libpharmml.dom.trialdesign.BolusType
import eu.ddmore.libpharmml.dom.trialdesign.InfusionType
import eu.ddmore.libpharmml.dom.uncertml.NormalDistribution
import javax.xml.bind.JAXBElement
import net.biomodels.jummp.plugins.pharmml.maths.FunctionSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsUtil
import net.biomodels.jummp.plugins.pharmml.maths.OperatorSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PieceSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PiecewiseSymbol

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
            } else {
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
    
    private String convertToMathML(DerivativeVariableType derivative) {
    	    String derivTerm="d${derivative.symbId}<DIVIDEDBY>d${derivative.independentVariable.symbRef.symbId}"
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

    StringBuilder simpleParams(List<SimpleParameterType> parameters) {
        def outcome = new StringBuilder()
        if (!parameters) {
            return outcome
        }
        boolean first=true;
        parameters.inject(outcome) { sb, p ->
            if (!first) {
                sb.append(", ")
            }
            else {
                first=false;
            }
            if (p.assign) {
                sb.append(convertToMathML(p.symbId, p.assign))
            }
            else {
                sb.append(p.symbId)
            }
        }
        return outcome
    }

    StringBuilder randomVariables(List<ParameterRandomVariableType> rv) {
        def output = new StringBuilder()
        boolean first=true;
        rv.inject(output) { o, i ->
            if (i.abstractContinuousUnivariateDistribution) {
                if (!first) {
                    o.append(", ")
                } else {
                    first=false;
                }
                o.append("[")
                o.append(distributionAssignment(i.symbId, i.abstractContinuousUnivariateDistribution))
                o.append(", variability: ").append(i.variabilityReference.symbRef.symbIdRef)
                o.append("]")
            }
        }
        return output
    }

    def functionDefinitions = { attrs ->
        if (!attrs.functionDefs) {
            out << "No function definitions were found."
            return
        }
        def result = new StringBuilder()
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
            result.append("<p>${convertToMathML(d.symbId, d.getFunctionArgument(), rightHandSide)}</p>")
        }
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

    def covariates = { attrs ->
        if (!attrs.covariate) {
            return
        }
        out << "<h3>Covariate Model</h3>"
        def result = new StringBuilder()
        attrs.covariate.each { c ->
            result.append("<div>")
            if (c.simpleParameter) {
                result.append("<p><span class=\"bold\">Parameters:</span></p> <p>")
                result.append(simpleParams(c.simpleParameter))
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
            result.append("<h4>Observation error ")
            // the API returns a JAXBElement, not ObservationErrorType
            def obsErr = om.observationError.value
            result.append(obsErr.symbId).append("</h4>\n<p>")
            result.append("<span class=\"bold\">Parameters: </span>")
            def simpleParameters = om.commonParameterElement.value.findAll {
                it instanceof SimpleParameterType
            }
            def rv = om.commonParameterElement.value.findAll {
                it instanceof ParameterRandomVariableType
            }
            result.append(simpleParams(simpleParameters))
            String randoms=randomVariables(rv)
            if (randoms) {
            	    result.append(", ")
            	    result.append(randoms)
            }
            result.append("\n<p>")
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
        def result = new StringBuilder()
        if (e.transformation) {
            result.append("<p> <span class=\"bold\">Transformation:</span>")
            result.append(e.transformation.value()).append("</p>")
        }
        result.append("<p>")
        result.append(convertToMathML(e.output.symbRef.symbIdRef, e.errorModel.assign.equation)).append("</p>")
        result.append("<p><span class=\"bold\">Residual error:</span>")
        return result.append(e.residualError.symbRef.symbIdRef).append("</p>")
    }

    StringBuilder generalObsErr(GeneralObsError e) {
        def result = new StringBuilder()
        if (!e) {
            return result
        }
        if (e.assign) {
            result.append(convertToMathML(e.symbId, e.assign))
        }
        return result
    }

    def randomEffect = { re ->
        if (!re) {
            return "No random effect defined in the observation model.\n"
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
        def amt = new StringBuilder("<p>").append(convertToMathML("Dose Amount", a?.amount)).append("</p>")
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
                return s.value as String
                break
            case TrueBooleanType:
                return "true"
            case FalseBooleanType:
                return "false"
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
        return result.append(interval).append(" ").append(endTime)
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
            result.append("<tr><td>").append(e.id).append(td).append(e.name ? e.name : " ")
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
            text.append(" ")
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

    def modellingSteps = { attrs ->
        if (!attrs.steps) {
            return
        }
        /*
        * Check which kind of step we are dealing with. Estimation and simulation steps cannot
        * be mixed, hence only look at the first one to decide.
        */
        def step = attrs.steps.first()
        def result = new StringBuilder()
        if (step instanceof EstimationStepType) {
            result.append(estimationSteps(attrs.steps))
        } else {
            result.append(simulationSteps(attrs.steps))
        }
        out << result.toString()
    }

    StringBuilder simulationSteps = { List<SimulationStepType> steps ->
        if (!steps) {
            return
        }
        def result = new StringBuilder("<h3>Simulation Steps</h3>\n")
        steps.each { s ->
            result.append("<h4>Simulation step ${s.oid}")
            if (s.variableAssignment) {
                result.append(variableAssignments(s.variableAssignment))
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
                        result.append("<p><span class=\"bold\">Timepoints:</span>\n")
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

    StringBuilder variableAssignments(List<VariableAssignmentType> assignments) {
        def result = new StringBuilder("\n<h5>Variable assignments</h5>\n")
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
                result.append(variableAssignments(s.variableAssignment))
            }
            if (s.objectiveDataSet) {
                result.append(objectiveDataSet(s.objectiveDataSet))
            }
            if (s.operation) {
                result.append(estimationOps(s.operation))
            }
            if (s.parametersToEstimate) {
                result.append(" ")
            }
        }
        return result
    }

    StringBuilder objectiveDataSet(DatasetMappingType dataSet) {
        return new StringBuilder("<h5>Dataset mapping</h5>\n")
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
            println o.opType.value()
            result.append(o.name ? o.name.value : operationMeaningMap[o.opType.value()])
            result.append("</span>\n")
            if (o.description || o.algorithm || o.property) {
                result.append("<p>")
                if (o.description) {
                    result.append(o.description.value)
                }
                if (o.property || o.algorithm) {
                    result.append("\n<ul>")
                    o.property.inject(result) { r, p ->
                        r.append("<li>").append(operationProperty(p)).append("</li>")
                    }
                    result.append("</ul>\n</p>")
                    if (o.algorithm) {
                        result.append("\n<p>Algorithm ").append(o.algorithm.name ? o.algorithm.name.value :
                                (o.algorithm.definition ? o.algorithm.definition : ""))
                        if (o.algorithm.property) {
                            result.append("<ul>")
                            o.algorithm.property.inject(result) { r, p ->
                                r.append("<li>").append(operationProperty(p)).append("</li>")
                            }
                        }
                        result.append("</ul>")
                    }
                    result.append("</p>")
                }
            }
            result.append("</div>")
        }
        return result
    }

    StringBuilder operationProperty(OperationPropertyType prop) {
        return new StringBuilder().append(convertToMathML(prop.name, prop.assign))
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
            result.append(s.oidRef)
            if (s.dependantStep) {
                StringBuilder dependenciesOfThisStep = transitiveStepDeps(s.dependantStep)
                result.append(", ").append(dependenciesOfThisStep)
            }

            if (iterator.hasNext()) {
                result.append(", ")
            }
        }
        out << result.append("]").toString()
    }

    StringBuilder transitiveStepDeps = { ds ->
        StringBuilder result = new StringBuilder()
        if (!ds) {
            return result
        }
        result.append(ds.join(", "))
    }
}
