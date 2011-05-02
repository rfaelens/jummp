package net.biomodels.jummp.plugins.sbml

import javax.xml.stream.XMLStreamException
import net.biomodels.jummp.core.model.FileFormatService
import org.sbml.jsbml.SBMLDocument
import org.sbml.jsbml.SBMLError
import org.sbml.jsbml.SBMLReader

/**
 * Service class for handling Model files in the SBML format.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SbmlService implements FileFormatService {
    // TODO: the service might need a cache of the already parsed SBMLDocuments

    static transactional = true

    public boolean validate(final File model) {
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
}
