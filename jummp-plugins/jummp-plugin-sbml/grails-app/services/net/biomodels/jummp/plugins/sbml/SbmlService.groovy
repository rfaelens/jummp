package net.biomodels.jummp.plugins.sbml

import javax.xml.stream.XMLStreamException
import net.biomodels.jummp.core.model.FileFormatService
import org.sbml.jsbml.SBMLDocument
import org.sbml.jsbml.SBMLError
import org.sbml.jsbml.SBMLReader
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Service class for handling Model files in the SBML format.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SbmlService implements FileFormatService {

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

    /**
     * Retrieves the metaId on the Model level. Does not retrieve the metaId of child elements
     * of model such as compartment.
     * @param revision
     * @return MetaId on Model level
     */
    public String getMetaId(RevisionTransportCommand revision) {
        return getFromCache(revision).model.metaId
    }

    /**
     *
     * @param revision
     * @return Version of the SBML file
     */
    public long getVersion(RevisionTransportCommand revision) {
        return getFromCache(revision).version
    }

    /**
     *
     * @param revision
     * @return Level of the SBML file
     */
    public long getLevel(RevisionTransportCommand revision) {
        return getFromCache(revision).level
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
}
