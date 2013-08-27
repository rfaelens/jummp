package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.commontypes.SequenceType
import eu.ddmore.libpharmml.dom.maths.ScalarType
import eu.ddmore.libpharmml.dom.maths.VarType
import eu.ddmore.libpharmml.dom.trialdesign.BolusType
import eu.ddmore.libpharmml.dom.trialdesign.InfusionType

class PharmMlTagLib {
    static namespace = "pharmml"

    def covariateParameters = { attrs ->
        if (!attrs.parameter) {
            out << "None"
            return
        }
        def s = new StringBuilder()
        attrs.parameter.each { p ->
            s.append(p.symbId).append(" ")
        }
        out << s.toString()
    }

    def covariates = { attrs ->
        if (!attrs.covariate) {
            out << "<td class=\"value\" colspan=\"4\">None</td>"
            return
        }

        attrs.covariate.each { c ->
            def thisCovariate = new StringBuilder()
            def type = c.continuous ? "continuous" : "categorical"
            thisCovariate.append("<td>").append(type).append("</td>")
            thisCovariate.append("<td>").append(c.symbId).append("</td>")
            String covName = c.name? c.name: "&nbsp;"
            thisCovariate.append("<td>").append(covName).append("</td>")
            String covTransf = c.transformation ? c.transformation : "&nbsp;"
            thisCovariate.append("<td>").append(covTransf).append("</td>")
            out << thisCovariate.toString()
        }
    }

    def symbolDefinitions = { attrs ->
        if (!attrs.symbolDefs || attrs.symbolDefs.size() == 0) {
            out << "No symbol definitions were found."
            return
        }
        def result = new StringBuilder("<table>\n\t")
        result.append("<thead>\n")
        result.append("<tr>\n<th>Identifier</th><th>Type</th></tr>\n")
        result.append("</thead>\n<tbody>\n")
        attrs.symbolDefs.each { d ->
            result.append("<tr><td class=\"value\">")
            result.append(d.symbId)
            result.append("</td><td class=\"value\">")
            result.append(d.symbolType.value())
            result.append("</td></tr>\n")
        }
        result.append("</tbody>\n</table>")
        out << result.toString()
    }

    def variabilityLevel = { attrs ->
        if (!attrs.level || attrs.level.size() == 0) {
            out << "No variability levels defined in the model."
            return
        }
        def result = new StringBuilder("<table>\n")
        result.append("<thead>\n<tr><th>Identifier</th><th>Name</th></tr>\n</thead>\n<tbody>\n")
        attrs.level.each { l ->
            result.append("<tr><td class=\"value\">")
            result.append(l.id)
            result.append("</td><td class=\"value\">")
            String levelName = l.name ? l.name : "&nbsp;"
            result.append(levelName)
            result.append("</td></tr>\n")
        }
        out << result.append("</tbody>\n</table>").toString()
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
            return "No distribution to render."
        }
        if ( !!(d.getNormal())) {
            return normalDistribution(d.normal)
        }
        if ( !!(d.getPDF())) {
            return pdfDistribution(d.pdf)
        }
        if (!!(d.getPoisson())) {
            return poissonDistribution(d.poisson)
        }
        if (!!(d.getStudentT())) {
            return studentTDistribution(d.studentT)
        }
        if (!!(d.getUniform())) {
            return uniformDistribution(d.uniform)
        }
        return "Cannot render this distribution because it is not defined in this version of PharmML.\n"
    }

    def normalDistribution = { d ->
        StringBuilder result = new StringBuilder("<p>This is a normal distribution.</p>")
        return result.toString()
    }

    def pdfDistribution = { d ->
        StringBuilder result = new StringBuilder("<p>This is a PDF distribution.</p>")
        return result.toString()
    }

    def poissonDistribution = { d ->
        StringBuilder result = new StringBuilder("<p>This is a Poisson distribution.</p>")
        return result.toString()
    }

    def studentTDistribution = { d ->
        StringBuilder result = new StringBuilder("<p>This is a StudentT distribution.</p>")
        return result.toString()
    }

    def uniformDistribution = { d ->
        StringBuilder result = new StringBuilder("<p>This is a uniform distribution.</p>")
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
        def amt = rhs(a.amount, new StringBuilder("<p>Dose amount:")).append("</p>")
        def d = variable(a.doseVar, new StringBuilder("<p>Dose variable:")).append("</p>")
        def t
        if (a.targetVar) {
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
            text.append(sequence(r))
        } else { // equation, vector, dataset, distribution or function call
            text.append(" cannot be extracted, sorry.")
        }
        return text
    }

    def constant = { c -> c.op }

    def variable(VarType v) {
        v.symbId
    }

    StringBuilder variable(VarType v, StringBuilder text) {
        text.append(v.symbId)
    }

    def scalar = { s -> s.value }

    def string = { s -> s.value }

    def sequence = { s ->
        new StringBuilder("[").append(a.amount.begin).append(":").append(a.amount.stepSize).append(":").append(a.amount.end).append("]")
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
        dt.sequenceOrScalar.each{ t ->
            def time
            try {
                time = t as ScalarType
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
        result.append("<th>Start</th><th>End</th><th>Occasions</th><th>Treatment</th></tr></thead>\n<tbody>\n<tr><td>")
        def td = new StringBuilder("</td><td>")
        attrs.epoch.each { e ->
            result.append(e.id).append(td).append(e.name ? e.name : "&nbsp;")
            result.append( e.start ? scalarRhs(e.start, td) : "&nbsp;</td><td>")
            result.append( e.end ? scalarRhs(e.end, td) : "&nbsp;</td><td>")
            result.append( e.occasion ? occasions(e.occasion, td) : "&nbsp;</td><td>")
            result.append(treatmentRef(e.treatmentRef, td)).append("</td></tr>")
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
        def td = new StringBuilder("</td><td>")
        def result = new StringBuilder("<table><thead>\n<tr><th>Identifier</th><th>Name</th>")
        result.append("<th>Treatment</th><th>Individuals</th></tr></thead>\n<tbody>\n<tr><td>")
        attrs.group.each { g ->
            result.append(g.id).append("</td><td>").append(g.name ? g.name : "&nbsp;")
            result.append(g.treatmentEpochRefOrWashout ? treatmentEpochRefs(g.treatmentEpochRefOrWashout, td) : "&nbsp;")
            result.append(g.individuals ? individuals(g.individuals) : "&nbsp;").append("</td></tr>")
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
        def result = new StringBuilder("[")
        i.each {
            StringBuilder sb = new StringBuilder("(symbolIdentifier:")
            sb.append(it.symbId).append(", levelIdentifier:").append(it.levelId)
            if (it.name) {
                sb.append(", name:").append(it.name)
            }
            if (it.constant) {
                sb.append(", constant: ").append(constant(it.constant))
            } else if (it.scalar) {
                sb.append(", scalar: ").append(scalar(it.scalar))
            } else if (it.string) {
                sb.append(", string: ").append(string(it.string))
            } else if (it.sequence) {
                sb.append(", sequence: ").append(sequence(it.sequence))
            } else if (it.var) {
                sb.append(", variable: ").append(variable(it.var))
            }
            result.append(sb.append(")"))
        }
        result.append("]")
    }
}
