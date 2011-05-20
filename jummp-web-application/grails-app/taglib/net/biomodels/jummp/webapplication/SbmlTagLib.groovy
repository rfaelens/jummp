package net.biomodels.jummp.webapplication

import groovy.xml.StreamingMarkupBuilder

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

    /**
     * Renders a list of SBML reactions.
     * @attr reactions REQUIRED List of Reactions
     */
    def renderReactions = { attrs ->
        out << renderParameterTitle(title: g.message(code: "sbml.reactions.title"), size: attrs.reactions.size())
        attrs.reactions.each {
            out << renderReaction(reaction: it)
        }
    }

    /**
     * Renders one reaction.
     * @attr reaction REQUIRED Map describing the Reaction
     */
    def renderReaction = { attrs ->
        Map reaction = attrs.reaction
        String metaLink = g.createLink(controller: 'sbml', action: 'reactionMeta', params: [id: params.id, reactionId: reaction.id, revision: params.revision])
        String name = reaction.name ? reaction.name : reaction.id
        out << render(template: "/templates/sbml/reaction", model: [title: name, metaLink: metaLink, reversible: reaction.reversible, products: reaction.products, modifiers: reaction.modifiers, reactants: reaction.reactants])
    }

    /**
     * Renders a list of SBML events.
     * @attr events REQUIRED List of Events
     */
    def renderEvents = { attrs ->
        out << renderParameterTitle(title: "Events", size: attrs.events.size())
        attrs.events.each {
            out << renderEvent(event: it)
        }
    }

    /**
     * Renders one event.
     * @attr event REQUIRED Map describing the Event
     */
    def renderEvent = { attrs ->
        Map event = attrs.event
        String metaLink = g.createLink(controller: 'sbml', action: 'eventMeta', params: [id: params.id, eventId: event.id, revision: params.revision])
        String name = (event.name && event.name != "") ? event.name : event.id
        out << render(template: "/templates/sbml/event", model: [title: name, metaLink: metaLink, assignments: event.assignments])
    }

    /**
     * Renders one event assignment.
     * @attr assignment REQUIRED Map describing the Event Assignment.
     */
    def renderEventAssignment = { attrs ->
        Map assignment = attrs.assignment
        String name = (assignment.variableName && assignment.variableName != "") ? assignment.variableName : assignment.variableId
        out << render(template: "/templates/sbml/eventAssignment", model: [variable: name, math: assignment.math, type: assignment.variableType])
    }

    /**
     * Renders a table row with the given notes.
     * The primary use for this tag is inside of the tooltips for various SBML elements.
     * The tag expects an attribute notes which is the notes xhtml markup as a string to be rendered.
     * In case the string is empty the table row is not rendered.
     * @attr notes REQUIRED The notes string
     */
    def notesTableRow = { attrs ->
        if (!attrs.notes || attrs.notes == "") {
            return
        }
        out << render(template: "/templates/sbml/notesTableRow", model: [notes: attrs.notes])
    }

    /**
     * Renders list of SBML Rules.
     * @attr rules REQUIRED The list of rules
     */
    def renderRules = { attrs ->
        if (!attrs.rules) {
            return
        }
        out << renderParameterTitle(title: g.message(code: "sbml.rules.title"), size: attrs.rules.size())
        attrs.rules.each { rule ->
            switch (rule.type) {
            case "rate":
                out << renderRateRule(rule: rule)
                break
            case "assignment":
                out << renderAssignmentRule(rule: rule)
                break
            case "algebraic":
                out << renderAlgebraicRule(rule: rule)
                break
            default:
                // no real rule - nothing to render
                break
            }
        }
    }

    /**
     * Renders one SBML Rate Rule.
     * @attr rule REQUIRED Map describing the Rate Rule
     */
    def renderRateRule = { attrs ->
        if (!attrs.rule) {
            return
        }
        String variableName = attrs.rule.variableName
        if (!variableName || variableName == "") {
            variableName = attrs.rule.variableId
        }
        out << render(template: "/templates/sbml/rateRuleRow", model: [variable: variableName, math: attrs.rule.math])
    }

    /**
     * Renders one SBML Assignment Rule.
     * @attr rule REQUIRED Map describing the Assignment Rule
     */
    def renderAssignmentRule = { attrs ->
        if (!attrs.rule) {
            return
        }
        String variableName = attrs.rule.variableName
        if (!variableName || variableName == "") {
            variableName = attrs.rule.variableId
        }
        out << render(template: "/templates/sbml/assignmentRuleRow", model: [variable: variableName, math: attrs.rule.math])
    }

    /**
     * Modifies the assignment MathML to contain the equals variable.
     * Expects a jummp:contentMathML as body.
     * @attr variable REQUIRED The name of the variable
     */
    def assignmentRuleMath = { attrs, body ->
        String math = body()
        def root = new XmlSlurper(true, false).parseText(math)
        root.mrow.appendNode {
            mo("=")
            mi(attrs.variable)
        }
        def outputBuilder = new StreamingMarkupBuilder()
        String result = outputBuilder.bind{ mkp.yield root }
        out << result
    }

    /**
     * Renders one SBML Algebraic Rule.
     * @attr rule REQUIRED Map describing the Algebraic Rule
     */
    def renderAlgebraicRule = { attrs ->
        if (!attrs.rule) {
            return
        }
        out << render(template: "/templates/sbml/algebraicRuleRow", model: [math: attrs.rule.math])
    }
}
