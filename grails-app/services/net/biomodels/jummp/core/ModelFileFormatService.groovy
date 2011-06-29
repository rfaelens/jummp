package net.biomodels.jummp.core

import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import org.codehaus.groovy.grails.commons.ApplicationHolder
import net.biomodels.jummp.model.Revision

/**
 * @short Service to handle Model files.
 *
 * This service provides methods to extract information from and about a Model file.
 * It does not provide own methods but delegates the calls to the concrete service for
 * the specific ModelFormat.
 *
 * Additionally the service provides methods to allow a plugin to register a new ModelFormat
 * and to tell the application which service is responsible for a format.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelFileFormatService {

    static transactional = true

    /**
     * The registered services to handle ModelFormats
     */
    private final Map<String, String> services = new HashMap<String, String>()

    /**
     * Registers a new ModelFormat in the application if it does not yet exist.
     * If the application already knows the ModelFormat identified by @p identifier
     * the existing ModelFormat is returned, otherwise a new ModelFormat is created
     * and stored in the database.
     * @param identifier The machine readable name of the ModelFormat, e.g. SBML
     * @param name A human readable name of the ModelFormat to be used in UIs.
     * @return Existing or new ModelFormat represented in a ModelFormatTransportCommand
     */
    ModelFormatTransportCommand registerModelFormat(String identifier, String name) {
        ModelFormat modelFormat = ModelFormat.findByIdentifier(identifier)
        if (modelFormat) {
            return modelFormat.toCommandObject()
        } else {
            modelFormat = new ModelFormat(identifier: identifier, name: name)
            modelFormat.save(flush: true)
            return modelFormat.toCommandObject()
        }
    }

    /**
     * Registers @p service to be responsible for ModelFormat identified by @p format.
     * This method can be used by a Plugin to register its service to be responsible for a
     * file format.
     * @param format The ModelFormat to be registered as a ModelFormatTransportCommand
     * @param service The name of the service which handles the ModelFormat.
     * @throws IllegalArgumentException if the @p format has not been registered yet
     */
    void handleModelFormat(ModelFormatTransportCommand format, String service) {
        ModelFormat modelFormat = ModelFormat.get(format.id)
        if (!modelFormat) {
            throw new IllegalArgumentException("ModelFormat ${format} not registered in database")
        }
        services.put(modelFormat.identifier, service)
    }

    /**
     * Validates the Model file in specified @p format.
     * @param model The Model file to validate.
     * @param format The format of the Model file
     * @return @c true, if the @p model is valid, @c false otherwise
     */
    boolean validate(final File model, final ModelFormat format) {
        FileFormatService service = serviceForFormat(format)
        if (service != null) {
            return service.validate(model)
        } else {
            return false
        }
    }

    /**
     * Extracts the name of the Model from the @p model in specified @p format.
     * @param model The Model file to use as a source
     * @param format The format of the Model file
     * @return The name of the Model, if possible, an empty String if not possible
     */
    String extractName(final File model, final ModelFormat format) {
        FileFormatService service = serviceForFormat(format)
        if (service != null) {
            return service.validate(model)
        } else {
            return ""
        }
    }

    /**
     * Retrieves all annotation URNs through the service responsible for the format used
     * by the @p revision.
     * @param rev The Revision for which all URNs should be retrieved
     * @return List of all URNs used in the Revision
     */
    List<String> getAllAnnotationURNs(Revision rev) {
        FileFormatService service = serviceForFormat(rev.format)
        if (service) {
            return service.getAllAnnotationURNs(rev.toCommandObject())
        } else {
            return []
        }
    }

    /**
     * Helper function to get the proper service for @p format.
     * @param format The ModelFormat for which the service should be returned.
     * @return The service which handles the format.
     */
    private FileFormatService serviceForFormat(final ModelFormat format) {
        println services
        if (format && services.containsKey(format.identifier)) {
            return ApplicationHolder.application.mainContext.getBean((String)services.getAt(format.identifier))
        } else {
            return null
        }
    }
}
