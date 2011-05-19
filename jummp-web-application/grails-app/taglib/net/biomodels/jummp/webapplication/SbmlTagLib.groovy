package net.biomodels.jummp.webapplication

class SbmlTagLib {
    static namespace = "sbml"

    /**
     * Renders an SBML notes string.
     * @attr notes REQUIRED The notes string
     */
    def notes = { attrs ->
        if (!attrs.notes || attrs.notes == "") {
            return
        }
        // the notesString returned by the core is wrapped in a <notes><body></body></notes>
        // we parse the String and build a new XML String from the GPath representation
        // at the same time we also drop the dangerous script tag
        // for reconstructing the HTML we use a closure
        def rootNode = new XmlSlurper(false, false).parseText(attrs.notes)
        String html = ""
        def closure = { element, closure ->
            if (!(element instanceof groovy.util.slurpersupport.Node)) {
                return element
            }
            String text = ""
            if (element.name.toLowerCase() == "script") {
                return ""
            }
            text += "<${element.name}"
            element.attributes.each { name, value ->
                // TODO: the attributes may contain JavaScript, this should be stripped
                text += " ${name}=\"${value}\""
            }
            if (element.children.isEmpty()) {
                text += "/>"
                return text
            }
            text += ">"
            element.children.each {
                text += closure(it, closure)
            }
            text += "</${element.name}>"
            return text
        }
        rootNode.body.childNodes().each { child ->
            html += closure(child, closure)
        }
        // not all notes are wrapped in a body
        rootNode.childNodes().each { child ->
            // body already handled above
            if (child.name.toLowerCase() != "body") {
                html += closure(child, closure)
            }
        }
        out << html
    }

    /**
     * Renders a list of SBML parameters - either global or reaction parameters.
     * @attr parameters List of Parameters
     * @attr title REQUIRED The Title for the parameters
     */
    def renderParameters = { attrs ->
        out << renderParameterTitle(title: attrs.title, size: attrs.parameters.size())
        attrs.parameters.each { param ->
            out << renderParameter(parameter: param)
        }
    }

    /**
     * Renders the HTML code for one SBML parameter
     * @attr parameter REQUIRED The Map describing one parameter
     */
    def renderParameter = { attrs ->
        Map param = attrs.parameter
        String name = param.name ? param.name : param.id
        String metaLink = g.createLink(controller: 'sbml', action: 'parameterMeta', params: [id: params.id, parameterId: param.id, revision: params.revision])
        out << render(template: "/templates/sbml/parameter", model: [title: name, metaLink: metaLink, value: param.value, unit: param.unit, constant: param.constant])
    }

    /**
     * Renders the title row of one SBML parameter section.
     * @attr title REQUIRED The title for the following parameter section
     * @attr size REQUIRED The number of parameters in the following section
     */
    def renderParameterTitle = { attrs ->
        out << render(template: "/templates/sbml/parameterTitle", model: [title: attrs.title, size: attrs.size])
    }
}