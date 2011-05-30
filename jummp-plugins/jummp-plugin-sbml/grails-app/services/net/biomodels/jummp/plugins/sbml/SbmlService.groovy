package net.biomodels.jummp.plugins.sbml

import javax.xml.stream.XMLStreamException
import net.biomodels.jummp.core.model.FileFormatService
import org.sbml.jsbml.SBMLDocument
import org.sbml.jsbml.SBMLError
import org.sbml.jsbml.SBMLReader
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.sbml.jsbml.Model
import net.biomodels.jummp.core.ISbmlService
import org.sbml.jsbml.ListOf
import org.sbml.jsbml.Parameter
import org.sbml.jsbml.Annotation
import org.sbml.jsbml.QuantityWithUnit
import org.sbml.jsbml.SpeciesReference
import org.sbml.jsbml.SimpleSpeciesReference
import org.sbml.jsbml.Reaction
import org.sbml.jsbml.Event
import org.sbml.jsbml.EventAssignment
import org.sbml.jsbml.Symbol
import org.sbml.jsbml.Rule
import org.sbml.jsbml.RateRule
import org.sbml.jsbml.AlgebraicRule
import org.sbml.jsbml.AssignmentRule
import org.sbml.jsbml.ExplicitRule
import org.sbml.jsbml.Variable
import org.perf4j.aop.Profiled
import org.sbml.jsbml.FunctionDefinition
import org.sbml.jsbml.SBO

/**
 * Service class for handling Model files in the SBML format.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SbmlService implements FileFormatService, ISbmlService {

    static transactional = true

    /**
     * Dependency Injection for ModelDelegateService allowing to access models
     */
    def modelDelegateService

    // TODO: move initialization into afterPropertiesSet and make it configuration dependent
    SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache(100)

    @Profiled(tag="SbmlService.validate")
    public boolean validate(final File model) {
        // TODO: we should insert the parsed model into the cache
        SBMLDocument doc
        SBMLReader reader = new SBMLReader()
        try {
            doc = reader.readSBML(model)
        } catch (XMLStreamException e) {
            log.error("SBMLDocument could not be read from ${model.name}")
            return false
        }
        if (doc == null) {
            // although the API documentation states that an Exception is thrown for incorrect files, it seems that null is returned
            log.error("SBMLDocuement is not valid for file ${model.name}")
            return false
        }
        // TODO: WARNING: checkConsistency uses an online validator. This might render timeouts during model upload
        if (doc.checkConsistency() > 0) {
            boolean valid = true
            // search for an error
            for (SBMLError error in doc.getListOfErrors().validationErrors) {
                if (error.isFatal() || error.isInternal() || error.isSystem() || error.isXML() || error.isError()) {
                    log.debug(error.getMessage())
                    valid = false
                    break
                }
            }
            return valid
        } else {
            return true
        }
    }

    @Profiled(tag="SbmlService.extractName")
    public String extractName(final File model) {
        return ""
    }

    @Profiled(tag="SbmlService.getMetaId")
    public String getMetaId(RevisionTransportCommand revision) {
        return getFromCache(revision).model.metaId
    }

    @Profiled(tag="SbmlService.getVersion")
    public long getVersion(RevisionTransportCommand revision) {
        return getFromCache(revision).version
    }

    @Profiled(tag="SbmlService.getLevel")
    public long getLevel(RevisionTransportCommand revision) {
        return getFromCache(revision).level
    }

    @Profiled(tag="SbmlService.getNotes")
    public String getNotes(RevisionTransportCommand revision) {
        // JSBML may return null - see https://sourceforge.net/tracker/?func=detail&aid=3300490&group_id=279608&atid=1186776
        String notesString = getFromCache(revision).model.notesString
        if (!notesString) {
            return ""
        } else {
            return notesString
        }
    }

    @Profiled(tag="SbmlService.getAnnotations")
    public List<Map> getAnnotations(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        return convertCVTerms(model.annotation)
    }

    @Profiled(tag="SbmlService.getParameters")
    public List<Map> getParameters(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        ListOf<Parameter> parameters = model.getListOfParameters()
        List<Map> list = []
        parameters.each { parameter ->
            list << parameterToMap(parameter)
        }
        return list
    }

    @Profiled(tag="SbmlService.getParameter")
    public Map getParameter(RevisionTransportCommand revision, String id) {
        Model model = getFromCache(revision).model
        QuantityWithUnit param = model.getParameter(id)
        if (!param) {
            param = (QuantityWithUnit)model.findLocalParameters(id).find { it.id == id }
        }
        if (!param) {
            return [:]
        }
        Map map = parameterToMap(param)
        map.put("notes", param.getNotesString())
        map.put("annotation", convertCVTerms(param.annotation))
        return map
    }

    @Profiled(tag="SbmlService.getLocalParameters")
    public List<Map> getLocalParameters(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> reactions = []
        model.listOfReactions.each { reaction ->
            List<Map> localParameters = []
            reaction.kineticLaw?.getListOfLocalParameters()?.each { parameter ->
                Map map = parameterToMap(parameter)
                localParameters << map
            }
            reactions << [id: reaction.id, name: reaction.name, parameters: localParameters]
        }
        return reactions
    }

    @Profiled(tag="SbmlService.getReactions")
    public List<Map> getReactions(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> reactions = []
        model.listOfReactions.each { reaction ->
            reactions << reactionToMap(reaction)
        }
        return reactions
    }

    @Profiled(tag="SbmlService.getReaction")
    public Map getReaction(RevisionTransportCommand revision, String id) {
        Model model = getFromCache(revision).model
        Reaction reaction = model.getReaction(id)
        if (!reaction) {
            return [:]
        }
        Map reactionMap = reactionToMap(reaction)
        reactionMap.put("annotation", convertCVTerms(reaction.annotation))
        reactionMap.put("math", reaction.kineticLaw ? reaction.kineticLaw.mathMLString : "")
        reactionMap.put("notes", reaction.notesString)
        return reactionMap
    }

    @Profiled(tag="SbmlService.getEvents")
    public List<Map> getEvents(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> events = []
        model.listOfEvents.each { event ->
            events << eventToMap(event)
        }
        return events
    }

    @Profiled(tag="SbmlService.getEvent")
    public Map getEvent(RevisionTransportCommand revision, String id) {
        Model model = getFromCache(revision).model
        Event event = model.getEvent(id)
        Map eventMap = eventToMap(event)
        eventMap.put("annotation", convertCVTerms(event.annotation))
        eventMap.put("notes", event.notesString)
        eventMap.put("sboTerm", event.getSBOTermID())
        eventMap.put("sboName", SBO.getTerm(event.getSBOTerm()).name)
        eventMap.put("trigger", event.trigger ? event.trigger.mathMLString : "")
        eventMap.put("delay", event.delay ? event.delay.mathMLString : "")
        return eventMap
    }

    @Profiled(tag="SbmlService.getRules")
    public List<Map> getRules(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> rules = []
        model.listOfRules.each { rule ->
            rules << ruleToMap(rule)
        }
        return rules
    }

    @Profiled(tag="SbmlService.getRule")
    public Map getRule(RevisionTransportCommand revision, String variable) {
        Model model = getFromCache(revision).model
        ExplicitRule rule = model.getRule(variable)
        if (!rule) {
            return [:]
        }
        Map ruleMap = ruleToMap(rule)
        ruleMap.put("annotation", convertCVTerms(rule.annotation))
        ruleMap.put("notes", rule.notesString)
        return ruleMap
    }

    public List<Map> getFunctionDefinitions(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> functions = []
        model.listOfFunctionDefinitions.each { function ->
            functions << functionDefinitionToMap(function)
        }
        return functions
    }

    public Map getFunctionDefinition(RevisionTransportCommand revision, String id) {
        Model model = getFromCache(revision).model
        FunctionDefinition function = model.getFunctionDefinition(id)
        if (!function) {
            return [:]
        }
        Map functionMap = functionDefinitionToMap(function)
        functionMap.put("annotation", convertCVTerms(function.annotation))
        functionMap.put("notes", function.notesString)
        functionMap.put("sboTerm", function.getSBOTermID())
        functionMap.put("sboName", SBO.getTerm(function.getSBOTerm()).name)
        return functionMap
    }

    /**
     * Returns the SBMLDocument for the @p revision from the cache.
     * If the cache does not contain the SBMLDocument, the model file is
     * retrieved, parsed and inserted into the Cache.
     * @param revision The revision for which the SBMLDocument needs to be retrieved
     * @return The parsed SBMLDocument
     */
    private SBMLDocument getFromCache(RevisionTransportCommand revision) throws XMLStreamException {
        SBMLDocument document = cache.get(revision)
        if (document) {
            return document
        }
        // we do not have a document, so retrieve first the file
        byte[] bytes = modelDelegateService.retrieveModelFile(revision)
        document = (new SBMLReader()).readSBMLFromStream(new ByteArrayInputStream(bytes))
        cache.put(revision, document)
        return document
    }

    private Map parameterToMap(QuantityWithUnit parameter) {
        return [
                id: parameter.id,
                name: parameter.name,
                metaId: parameter.metaId,
                constant: (parameter instanceof Parameter) ? parameter.constant : true,
                value: parameter.isSetValue() ? parameter.value : null,
                sboTerm: parameter.getSBOTermID(),
                sboName: SBO.getTerm(parameter.getSBOTerm()).name,
                unit: parameter.units
        ]
    }

    private List<Map> convertCVTerms(Annotation annotation) {
        List<Map> list = []
        annotation.listOfCVTerms.each { cvTerm ->
            list << [
                    qualifier: cvTerm.biologicalQualifier ? cvTerm.biologicalQualifierType.toString() : (cvTerm.modelQualifier ? cvTerm.modelQualifierType.toString() : ""),
                    biologicalQualifier: cvTerm.biologicalQualifier,
                    modelQualifier: cvTerm.modelQualifier,
                    resources: cvTerm.resources
            ]
        }
        return list
    }

    private List<Map> convertSpeciesReferences(List<SimpleSpeciesReference> list) {
        List<Map> species = []
        list.each {
            if (it instanceof SpeciesReference) {
                species << speciesReferenceToMap(it)
            } else {
                species << [species: it.species, speciesName: it.model.getSpecies(it.species).name]
            }
        }
        return species
    }

    private Map speciesReferenceToMap(SpeciesReference reference) {
        return [
                species: reference.species,
                speciesName: reference.model.getSpecies(reference.species).name,
                constant: reference.constant,
                stoichiometry: reference.stoichiometry
        ]
    }

    private Map reactionToMap(Reaction reaction) {
        return [
                id: reaction.id,
                metaId: reaction.metaId,
                name: reaction.name,
                reversible: reaction.reversible,
                sboTerm: reaction.getSBOTermID(),
                sboName: SBO.getTerm(reaction.getSBOTerm()).name,
                reactants: convertSpeciesReferences(reaction.listOfReactants),
                products: convertSpeciesReferences(reaction.listOfProducts),
                modifiers: convertSpeciesReferences(reaction.listOfModifiers)
        ]
    }

    private Map eventToMap(Event event) {
        return [
                id: event.id,
                metaId: event.metaId,
                name: event.name,
                assignments: eventAssignmentsToList(event.listOfEventAssignments)
        ]
    }

    private List<Map> eventAssignmentsToList(List<EventAssignment> assignments) {
        List<Map> eventAssignments = []
        assignments.each { assignment ->
            Symbol symbol = assignment.model.findSymbol(assignment.variable)
            eventAssignments << [
                    meataId: assignment.metaId,
                    math: assignment.mathMLString,
                    variableId: assignment.variable,
                    variableName: symbol ? symbol.name : "",
                    variableType: symbol ? symbol.elementName : ""
            ]
        }
        return eventAssignments
    }

    private Map ruleToMap(Rule rule) {
        String type = null
        Variable symbol = null
        if (rule instanceof RateRule) {
            type = "rate"
            symbol = rule.model.findSymbol(rule.variable)
        } else if (rule instanceof AssignmentRule) {
            type = "assignment"
            symbol = rule.model.findSymbol(rule.variable)
        } else if (rule instanceof AlgebraicRule) {
            type = "algebraic"
            symbol = rule.derivedVariable
        }

        return [
                metaId: rule.metaId,
                math: rule.getMathMLString(),
                variableId : symbol ? symbol.id : null,
                variableName: symbol ? symbol.name : null,
                variableType: symbol ? symbol.elementName : null,
                type: type
        ]
    }

    private Map functionDefinitionToMap(FunctionDefinition function) {
        return [
                id: function.id,
                name: function.name,
                metaId: function.metaId,
                math: function.mathMLString
        ]
    }
}
