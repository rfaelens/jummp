package net.biomodels.jummp.plugins.pharmml

class PharmMlTagLib {
    static namespace = "pharmml"

    def covariateModelParameters = { attrs ->
        if (!attrs.parameter) {
            out << "No Covariate parameters defined."
            return
        }
        def s = new StringBuilder()
        attrs.parameter.each { p ->
            s.append(p.symbId).append(" ")
        }
        out << s.toString()
    }

    def covariateModelCovariate = { attrs ->
        if (!attrs.covariate) {
            out << "No covariates found."
            return
        }
        def covariates = new StringBuilder()
        def c = attrs.covariate
        def type = c.continuous ? "continuous" : "categorical"
        covariates.append("<p>Type:").append(type).append("</p>\n")
        covariates.append("<p>ID:").append(c[0].symbId).append("</p>\n")
        if (c[0].transformation) {
            covariates.append("<p>Transformation:").append(c[0].transformation).append("</p>\n")
        }

        out << covariates.toString()
    }

    def symbolDefinitions = { attrs ->
        if (!attrs.symbolDefs || attrs.symbolDefs.size() == 0) {
            out << "No symbol definitions were found."
            return
        }
        def result = new StringBuilder("<table>\n\t")
        attrs.symbolDefs.each { d ->
            result.append("<tr>\n\t<td class=\"key\">Symbol Id")
            result.append("</td>\n\t\t<td class=\"value\">")
            result.append(d.symbId)
            result.append("</td>\n\t</tr>\n")
            result.append("<tr>\n\t<td class=\"key\">Symbol Type")
            result.append("</td>\n\t\t<td class=\"value\">")
            result.append(d.symbolType.value())
            result.append("</td>\n\t</tr>\n")
        }
        result.append("</table>")
        out << result.toString()
    }

    def variabilityLevel = { attrs ->
        if (!attrs.level || attrs.level.size() == 0) {
            out << "No variability levels defined in the model."
            return
        }
        def result = new StringBuilder("<table>")
        attrs.level.each { l ->
            result.append("\n\t<tr>\n\t\t<td class=\"key\">Id</td>\n")
            result.append("\t\t<td class=\"value\">")
            result.append(l.id)
            result.append("</td>\n\t</tr>")
            if (l.name) {
                result.append("\n\t<tr>\n\t\t<td class=\"key\">Name</td>\n")
                result.append("\t\t<td class=\"value\">")
                result.append(l.name)
                result.append("</td>\n\t</tr>")
            }
        }
        out << result.append("\n</table>").toString()
    }

}
