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
import org.sbml.jsbml.Compartment
import org.sbml.jsbml.Species
import org.sbml.jsbml.SBase
import org.sbfc.converter.sbml2dot.SBML2Dot
import org.sbfc.converter.sbml2octave.SBML2Octave
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.InitializingBean
import grails.util.Environment
import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.sbml.jsbml.CVTerm
import org.sbfc.converter.models.SBMLModel
import org.sbfc.converter.models.OctaveModel
import org.sbml.jsbml.SBMLWriter
import org.sbfc.converter.sbml2biopax.SBML2BioPAX_l3
import org.sbfc.converter.models.BioPaxModel
import com.thoughtworks.xstream.converters.ConversionException

/**
 * Service class for handling Model files in the SBML format.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SbmlService implements FileFormatService, ISbmlService, InitializingBean {

    static transactional = true

    /**
     * Dependency Injection of MiriamService
     */
    def miriamService
    def grailsApplication
    def config

    /**
     * Keep one of each SBML2* converters around as it takes quite some time to load the converters.
     * These are defs because we don't want to call the static initialization code immediately.
     */
    private def dotConverter = null
    private def octaveConverter = null
    private def biopaxConverter = null

    // TODO: move initialization into afterPropertiesSet and make it configuration dependent
    SbmlCache<RevisionTransportCommand, SBMLDocument> cache = new SbmlCache(100)

    public void afterPropertiesSet() {
        if (Environment.current == Environment.PRODUCTION) {
            // only initialize the SBML2* Converters during startup in production mode
            sbml2dotConverter()
            sbml2OctaveConverter()
            sbml2BioPaxConverter()
        }
    }

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
        if (!config.jummp.plugins.sbml.validate) {
            log.info("Validation for ${model.name} skipped due to configuration option")
            println("Validation for ${model.name} skipped due to configuration option")
            return true
        }
        // TODO: WARNING: checkConsistency uses an online validator. This might render timeouts during model upload
        try {
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
        } catch (ConversionException e) {
            log.error(e.getMessage(), e)
            return false
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
        eventMap.put("sbo", sboName(event))
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
        functionMap.put("sbo", sboName(function))
        return functionMap
    }

    @Profiled(tag="SbmlService.getCompartments")
    public List<Map> getCompartments(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> compartments = []
        model.listOfCompartments.each { compartment ->
             compartments << compartmentToMap(compartment)
        }
        return compartments
    }

    @Profiled(tag="SbmlService.getCompartment")
    public Map getCompartment(RevisionTransportCommand revision, String id) {
        Model model = getFromCache(revision).model
        Compartment compartment = model.getCompartment(id)
        if(!compartment) {
            return [:]
        }
        Map compartmentMap = compartmentToMap(compartment)
        compartmentMap.put("annotation", convertCVTerms(compartment.annotation))
        compartmentMap.put("notes", compartment.notesString)
        return compartmentMap
    }

    @Profiled(tag="SbmlService.getAllSpecies")
    public List<Map> getAllSpecies(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> allSpecies = []
        model.listOfSpecies.each { species ->
            allSpecies << speciesToMap(species)
        }
        return allSpecies
    }

    @Profiled(tag="SbmlService.getAllCompartmentSpecies")
    private List<Map> getAllCompartmentSpecies(Compartment compartment) {
        Model model = compartment.model
        List<Map> allSpecies = []
        model.listOfSpecies.each { species ->
            if (species.compartmentInstance == compartment) {
                allSpecies << speciesToMap(species)
            }
        }
        return allSpecies
    }

     @Profiled(tag="SbmlService.getSpecies")
     public Map getSpecies(RevisionTransportCommand revision, String id) {
         Model model =getFromCache(revision).model
         Species species = model.getSpecies(id)
         if(!species) {
             return [:]
         }
         Map speciesMap = speciesToMap(species)
         speciesMap.put("annotation", convertCVTerms(species.annotation))
         speciesMap.put("notes", species.notesString)
         return speciesMap
     }

    @Profiled(tag="SbmlService.generateSvg")
    public byte[] generateSvg(RevisionTransportCommand revision) {
        File dotFile = File.createTempFile("jummp", "dot")
        PrintWriter writer = new PrintWriter(dotFile)
        sbml2dotConverter().dotExport(getFromCache(revision), writer)
        File svgFile = File.createTempFile("jummp", "svg")
        def process = "dot -Tsvg -o ${svgFile.absolutePath} ${dotFile.absolutePath}".execute()
        process.waitFor()
        FileUtils.deleteQuietly(dotFile)
        if (process.exitValue()) {
            FileUtils.deleteQuietly(svgFile)
            return new byte[0]
        }
        byte[] bytes = svgFile.readBytes()
        FileUtils.deleteQuietly(svgFile)
        return bytes
    }

    @Profiled(tag="SbmlService.generateOctave")
    public String generateOctave(RevisionTransportCommand revision) {
        SBMLModel sbmlModel = resolveSbmlModel(revision)
        OctaveModel octaveModel = sbml2OctaveConverter().octaveExport(sbmlModel)
        return octaveModel.modelToString()
    }

    @Profiled(tag="SbmlService.generateBioPax")
    public String generateBioPax(RevisionTransportCommand revision) {
        SBMLModel sbmlModel = resolveSbmlModel(revision)
        BioPaxModel bioPaxModel = sbml2BioPaxConverter().biopaxexport(sbmlModel)
        return bioPaxModel.modelToString()
    }

    @Profiled(tag="SbmlService.getAllAnnotationURNs")
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        SBMLDocument document = getFromCache(revision)
        List<String> urns = []
        List<SBase> sbases = []
        sbases.addAll(document.model.listOfCompartments)
        sbases.addAll(document.model.listOfConstraints)
        sbases.addAll(document.model.listOfEvents)
        sbases.addAll(document.model.listOfFunctionDefinitions)
        sbases.addAll(document.model.listOfInitialAssignments)
        sbases.addAll(document.model.listOfParameters)
        sbases.addAll(document.model.listOfReactions)
        sbases.addAll(document.model.listOfRules)
        sbases.addAll(document.model.listOfSpecies)
        sbases.addAll(document.model.listOfUnitDefinitions)
        sbases << document.model
        sbases.each { sbase ->
            sbase.annotation.listOfCVTerms.each { cvTerm ->
                cvTerm.resources.each {
                    urns << it
                }
            }
        }
        return urns
    }

    @Profiled(tag="SbmlService.getPubMedAnnotation")
    public List<List<String>> getPubMedAnnotation(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        Annotation annotation = model.annotation
        if(!annotation) {
            return null
        }
        List<CVTerm> filters = annotation.filterCVTerms(CVTerm.Qualifier.BQM_IS_DESCRIBED_BY)
        List<List<String>> pubMedAnnotation = []
        filters.each { filter ->
            CVTerm cvTerm = new CVTerm(filter)
            pubMedAnnotation.add(cvTerm.filterResources("pubmed"))
        }
        return pubMedAnnotation
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
        byte[] bytes = grailsApplication.mainContext.getBean("modelDelegateService").retrieveModelFile(revision)
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
                sbo: sboName(parameter),
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
                    resources: cvTerm.resources.collect {
                        Map data = miriamService.miriamData(it)
                        data.put("urn", it)
                        data
                    }
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
                sbo: sboName(reaction),
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

    private Map compartmentToMap(Compartment compartment) {
        return [
                metaId: compartment.metaId,
                id: compartment.id,
                name: compartment.name,
                size: compartment.size,
                spatialDimensions: compartment.getSpatialDimensions(),
                units: compartment.units,
                sbo: sboName(compartment),
                allSpecies: getAllCompartmentSpecies(compartment)
        ]
    }

    private Map speciesToMap(Species species) {
        def initialAmount
        def initialConcentration
        if (species.isSetInitialAmount()) {
            initialAmount = species.initialAmount
        } else {
            initialAmount = null
        }
        if (species.isSetInitialConcentration()) {
            initialConcentration = species.initialConcentration
        } else {
            initialConcentration = null
        }
        return [
                metaid: species.metaId,
                id: species.id,
                compartment: species.compartment,
                initialAmount: initialAmount,
                initialConcentration: initialConcentration,
                substanceUnits: species.substanceUnits,
                sbo: sboName(species)
        ]
    }

    /**
     * Retrieves the name of the SBOTerm in the given @p sbase.
     * @param sbase The sbase from which to extract the SBO Term name.
     * @return The name or an empty String if there is no name
     */
    private Map sboName(SBase sbase) {
        try {
            String name = SBO.getTerm(sbase.getSBOTerm()).name
            Map map = miriamService.miriamData("urn:miriam:obo.sbo:${URLCodec.encode(sbase.getSBOTermID())}")
            map.put("name", name)
            return map
        } catch (NoSuchElementException e) {
            return [:]
        }
    }

    private SBML2Dot sbml2dotConverter() {
        if (!dotConverter) {
            dotConverter = new SBML2Dot()
        }
        return dotConverter
    }

    private SBML2Octave sbml2OctaveConverter() {
        if (!octaveConverter) {
            octaveConverter = new SBML2Octave()
        }
        return octaveConverter
    }

    private SBML2BioPAX_l3 sbml2BioPaxConverter() {
        if (!biopaxConverter) {
            biopaxConverter = new SBML2BioPAX_l3()
        }
        return biopaxConverter
    }

    /**
     * Resolves the SBMLModel from the given @p revision.
     * @param revision The RevisionTransportCommand from which to extract the SBMLModel.
     * @return The SBMLModel to be found or an empty array if the model could not be found.
     */
    private SBMLModel resolveSbmlModel(RevisionTransportCommand revision) {
        try {
        Model model = getFromCache(revision).model
        SBMLWriter sbmlWriter = new SBMLWriter()
        String sbmlString = sbmlWriter.writeSBMLToString(model.getSBMLDocument())
        SBMLModel sbmlModel = new SBMLModel()
        sbmlModel.setModelFromString(sbmlString)
        return sbmlModel
        } catch (NoSuchElementException e) {
            return [:]
        }
    }

    public String triggerSubmodelGeneration(RevisionTransportCommand revision, String subModelId, String metaId, List<String> compartmentIds, List<String> speciesIds, List<String> reactionIds, List<String> ruleIds, List<String> eventIds) {
        Model model = getFromCache(revision).model
        return new SubmodelGenerator().generateSubModel(model, subModelId, metaId, compartmentIds, speciesIds, reactionIds, ruleIds, eventIds)
    }
}
