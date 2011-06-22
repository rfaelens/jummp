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
     * Renders a list of SBML parameters - either global or reaction parameters - for the overview.
     * @attr parameters List of Parameters
     * @attr title REQUIRED The Title for the parameters
     */
    def renderParametersOverview = { attrs ->
        out << renderParameterTitle(title: attrs.title, size: attrs.parameters.size())
        attrs.parameters.each { param ->
            out << renderParameterOverview(parameter: param)
        }
    }

    /**
     * Renders the HTML code for one SBML parameter for the overview
     * @attr parameter REQUIRED The Map describing one parameter
     */
    def renderParameterOverview = { attrs ->
        Map param = attrs.parameter
        String name = param.name ? param.name : param.id
        String metaLink = g.createLink(controller: 'sbml', action: 'parameterMetaOverview', params: [id: params.id, parameterId: param.id, revision: params.revision])
        out << render(template: "/templates/sbml/parameter", model: [title: name, metaLink: metaLink])
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
     * Renders a list of SBML reactions for the overview.
     * @attr reactions REQUIRED List of Reactions
     */
    def renderReactionsOverview = { attrs ->
        out << renderParameterTitle(title: g.message(code: "sbml.reactions.title"), size: attrs.reactions.size())
        attrs.reactions.each {
            out << renderReactionOverview(reaction: it)
        }
    }

    /**
     * Renders one reaction for the overview.
     * @attr reaction REQUIRED Map describing the Reaction
     */
    def renderReactionOverview = { attrs ->
        Map reaction = attrs.reaction
        String metaLink = g.createLink(controller: 'sbml', action: 'reactionMetaOverview', params: [id: params.id, reactionId: reaction.id, revision: params.revision, reversible: reaction.reversible, products: reaction.products, modifiers: reaction.modifiers, reactants: reaction.reactants])
        String name = reaction.name ? reaction.name : reaction.id
        out << render(template: "/templates/sbml/reactionOverview", model: [title: name, metaLink: metaLink])
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
     * Renders a table row with the given initial amount.
     * The primary use for this tag is inside of the tooltips for various SBML elements.
     * The tag expects an attribute notes which is the notes xhtml markup as a string to be rendered.
     * In case the string is empty the table row is not rendered.
     * @attr notes REQUIRED The initialAmount string
     */
    def initialAmountTableRow = { attrs ->
        if (attrs.initialAmount == null) {
            return
        }
        out << render(template: "/templates/sbml/initialAmountTableRow", model: [initialAmount: attrs.initialAmount])
    }

    /**
     * Renders a table row with the given initial concentration.
     * The primary use for this tag is inside of the tooltips for various SBML elements.
     * The tag expects an attribute notes which is the notes xhtml markup as a string to be rendered.
     * In case the string is empty the table row is not rendered.
     * @attr notes REQUIRED The initialConcentration string
     */
    def initialConcentrationTableRow = { attrs ->
        if (attrs.initialConcentration == null) {
            return
        }
        out << render(template: "/templates/sbml/initialConcentrationTableRow", model: [initialConcentration: attrs.initialConcentration])
    }

    /**
     * Renders a table row with the given substance units.
     * The primary use for this tag is inside of the tooltips for various SBML elements.
     * The tag expects an attribute notes which is the notes xhtml markup as a string to be rendered.
     * In case the string is empty the table row is not rendered.
     * @attr notes REQUIRED The substanceUnits string
     */
    def substanceUnitsTableRow = { attrs ->
        if (!attrs.substanceUnits || attrs.substanceUnits == "") {
            return
        }
        out << render(template: "/templates/sbml/substanceUnitsTableRow", model: [substanceUnits: attrs.substanceUnits])
    }

    /**
     * Renders a table row with the given substance units.
     * The primary use for this tag is inside of the tooltips for various SBML elements.
     * The tag expects an attribute notes which is the notes xhtml markup as a string to be rendered.
     * In case the string is empty the table row is not rendered.
     * @attr notes REQUIRED The substanceUnits string
     */
    def speciesTitleTableRow = { attrs ->
        if (!attrs.title || attrs.title == "") {
            return
        }
        out << render(template: "/templates/sbml/speciesTitleTableRow", model: [title: attrs.title])
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
     * Renders list of SBML Rules.
     * @attr rules REQUIRED The list of rules
     */
    def renderRulesOverview = { attrs ->
        if (!attrs.rules) {
            return
        }
        out << renderParameterTitle(title: g.message(code: "sbml.rules.title"), size: attrs.rules.size())
        attrs.rules.each { rule ->
            switch (rule.type) {
            case "rate":
                String variableName = rule.variableName
                if (!variableName || variableName == "") {
                    variableName = rule.variableId
                }
                out << render(template: "/templates/sbml/rateRuleRowOverview", model: [variable: variableName])
                break
            case "assignment":
                String variableName = rule.variableName
                if (!variableName || variableName == "") {
                    variableName = rule.variableId
                }
                out << render(template: "/templates/sbml/assignmentRuleRowOverview", model: [variable: variableName])
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
     * Modifies the assignment MathML to contain the "= d(variable)/dt".
     * Expects a jummp:contentMathML as body.
     * @attr variable REQUIRED The name of the variable
     */
    def rateRuleMath = { attrs, body ->
        String math = body()
        def root = new XmlSlurper(true, false).parseText(math)
        root.mrow.appendNode {
            mo("=")
            mfrac {
                mrow {
                    mi("d")
                    mfenced(open: "(", close: ")") {
                        mi(attrs.variable)
                    }
                }
                mrow {
                    mi("d")
                    mkp.yieldUnescaped("<mo>&it;</mo>")
                    mi("t")
                }
            }
        }
        def outputBuilder = new StreamingMarkupBuilder()
        String result = outputBuilder.bindNode(root)
        out << result
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

    /**
     * Modifies the algebraic MathML to contain the equals 0.
     * Expects a jummp:contentMathML as body.
     */
    def algebraicRuleMath = { attrs, body ->
        String math = body()
        def root = new XmlSlurper(true, false).parseText(math)
        root.mrow.appendNode {
            mo("=")
            mn(0)
        }
        def outputBuilder = new StreamingMarkupBuilder()
        String result = outputBuilder.bind{ mkp.yield root }
        out << result
    }

    /**
     * Renders a list of Function Definitions.
     * @attr functions REQUIRED The list of function definitions
     */
    def renderFunctionDefinitions = { attrs ->
        if (!attrs.functions) {
            return
        }
        out << renderParameterTitle(title: g.message(code: "sbml.functionDefinitions.title"), size: attrs.functions.size())
        attrs.functions.each {
            out << renderFunctionDefinition(function: it)
        }
    }

    /**
     * Renders one Function Definition.
     * @attr function REQUIRED Map describing the Function Definition
     */
    def renderFunctionDefinition = { attrs ->
        Map function = attrs.function
        String metaLink = g.createLink(controller: 'sbml', action: 'functionDefinitionMeta', params: [id: params.id, functionDefinitionId: function.id, revision: params.revision])
        String name = (function.name && function.name != "") ? function.name : function.id
        out << render(template: "/templates/sbml/functionDefinition", model: [title: name, metaLink: metaLink, math: function.math])
    }

    /**
     * Renders a list of SBML compartments.
     * @attr compartments REQUIRED List of Compartments
     */
    def renderCompartments = { attrs ->
        if(!attrs.compartments) {
            return
        }
        out << renderParameterTitle(title: g.message(code: "sbml.compartments.title"), size: attrs.compartments.size())
        attrs.compartments.each {
            out << renderCompartment(compartment: it)
        }
    }

    /**
     * Renders one compartment.
     * @attr compartment REQUIRED Map describing the Compartment
     */
    def renderCompartment = { attrs ->
        Map compartment = attrs.compartment
        String metaLink = g.createLink(controller: 'sbml', action: 'compartmentMeta', params: [id: params.id, compartmentId: compartment.id, revision: params.revision])
        String name = compartment.name ? compartment.name : compartment.id
        out << render(template: "/templates/sbml/compartment", model:[title: name, metaLink: metaLink, size: compartment.size, spatialDimensions: compartment.spatialDimensions, units: compartment.units, notes: compartment.notes, allSpecies: compartment.allSpecies])
    }

    /**
     * Renders one Species
     * @attr assignment REQUIRED Map describing the Species
     */
    def renderSpecies = { attrs ->
        Map species = attrs.species
        String metaLink = g.createLink(controller: 'sbml', action: 'speciesMeta', params: [id: params.id, speciesId: species.id, revision: params.revision])
        out << render(template: "/templates/sbml/species", model: [title: species.id, initialAmount: species.initialAmount, initialConcentration: species.initialConcentration, substanceUnits: species.substanceUnits, metaLink: metaLink])
    }

    /**
     * Renders one Species for the overview.
     * @attr assignment REQUIRED Map describing the Species
     */
    def renderSpeciesOverview = { attrs ->
        Map species = attrs.species
        out << render(template: "/templates/sbml/speciesOverview", model: [title: species.id, initialAmount: species.initialAmount, initialConcentration: species.initialConcentration, substanceUnits: species.substanceUnits, sboName: species.sboName, sboTerm: species.sboTerm, annotation: species.annotation])
    }

    /**
     * Renders a list of SBML compartments for the overview.
     * @attr compartments REQUIRED List of Compartments
     */
    def renderCompartmentsOverview = { attrs ->
        if(!attrs.compartments) {
            return
        }
        out << renderParameterTitle(title: g.message(code: "sbml.compartments.title"), size: attrs.compartments.size())
        attrs.compartments.each {
            out << renderCompartmentOverview(compartment: it)
        }
    }

    /**
     * Renders one compartment for the overview.
     * @attr compartment REQUIRED Map describing the Compartment
     */
    def renderCompartmentOverview = { attrs ->
        Map compartment = attrs.compartment
        String metaLink = g.createLink(controller: 'sbml', action: 'compartmentMetaOverview', params: [id: params.id, compartmentId: compartment.id, revision: params.revision, size: compartment.size, spatialDimensions: compartment.spatialDimensions, units: compartment.units, notes: compartment.notes, allSpecies: compartment.allSpecies])
        String name = compartment.name ? compartment.name : compartment.id
        out << render(template: "/templates/sbml/compartmentOverview", model:[title: name, metaLink: metaLink, allSpecies: compartment.allSpecies])
    }
}
