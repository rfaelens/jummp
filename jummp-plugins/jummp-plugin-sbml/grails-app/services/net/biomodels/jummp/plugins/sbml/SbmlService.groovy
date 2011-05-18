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

    public String extractName(final File model) {
        return ""
    }

    public String getMetaId(RevisionTransportCommand revision) {
        return getFromCache(revision).model.metaId
    }

    public long getVersion(RevisionTransportCommand revision) {
        return getFromCache(revision).version
    }

    public long getLevel(RevisionTransportCommand revision) {
        return getFromCache(revision).level
    }

    public String getNotes(RevisionTransportCommand revision) {
        // JSBML may return null - see https://sourceforge.net/tracker/?func=detail&aid=3300490&group_id=279608&atid=1186776
        String notesString = getFromCache(revision).model.notesString
        if (!notesString) {
            return ""
        } else {
            return notesString
        }
    }

    public List<Map> getAnnotations(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        return convertCVTerms(model.annotation)
    }

    public List<Map> getParameters(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        ListOf<Parameter> parameters = model.getListOfParameters()
        List<Map> list = []
        parameters.each { parameter ->
            list << parameterToMap(parameter)
        }
        return list
    }

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

    public List<Map> getReactions(RevisionTransportCommand revision) {
        Model model = getFromCache(revision).model
        List<Map> reactions = []
        model.listOfReactions.each { reaction ->
            reactions << [
                    id: reaction.id,
                    metaId: reaction.metaId,
                    name: reaction.name,
                    reversible: reaction.reversible,
                    sboTerm: reaction.getSBOTerm() != -1 ? reaction.getSBOTerm() : null,
                    reactants: convertSpeciesReferences(reaction.listOfReactants),
                    products: convertSpeciesReferences(reaction.listOfProducts),
                    modifiers: convertSpeciesReferences(reaction.listOfModifiers)
            ]
        }
        return reactions
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
                sboTerm: parameter.getSBOTerm() != -1 ? parameter.getSBOTerm() : null,
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
}
