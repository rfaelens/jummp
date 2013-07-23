package net.biomodels.jummp.core

import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.model.Revision
import java.util.List
import org.perf4j.aop.Profiled

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
     * Dependency Injection of grails application
     */
    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication

    /**
     * The registered services to handle ModelFormats
     */
    private final Map<String, String> services = new HashMap<String, String>()

    @Profiled(tag = "modelFileFormatService.inferModelFormat")
    ModelFormatTransportCommand inferModelFormat(List<File> modelFiles) {
        if (!modelFiles) {
            return null
        }
        String match=services.keySet().find {
            if (it == "UNKNOWN") return false
            FileFormatService ffs=grailsApplication.mainContext.getBean((String)services.getAt(it))
            return ffs.areFilesThisFormat(modelFiles)
        }
        if (!match) {
            match="UNKNOWN"
        }
        return ModelFormat.findByIdentifier(match).toCommandObject()
    }
    
    
    /**
     * Registers a new ModelFormat in the application if it does not yet exist.
     * If the application already knows the ModelFormat identified by @p identifier
     * the existing ModelFormat is returned, otherwise a new ModelFormat is created
     * and stored in the database.
     * @param identifier The machine readable name of the ModelFormat, e.g. SBML
     * @param name A human readable name of the ModelFormat to be used in UIs.
     * @return Existing or new ModelFormat represented in a ModelFormatTransportCommand
     */
    @Profiled(tag = "modelFileFormatService.registerModelFormat")
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
    @Profiled(tag = "modelFileFormatService.handleModelFormat")
    void handleModelFormat(ModelFormatTransportCommand format, String service) {
        ModelFormat modelFormat = ModelFormat.get(format.id)
        if (!modelFormat) {
            throw new IllegalArgumentException("ModelFormat ${format} not registered in database")
        }
        services.put(modelFormat.identifier, service)
    }
    

    boolean validate(final List<File> model, String formatId) {
        return validate(model, ModelFormat.findByIdentifier(formatId))
    }

    /**
     * Validates the Model file in specified @p format.
     * @param model The Model file to validate.
     * @param format The format of the Model file
     * @return @c true, if the @p model is valid, @c false otherwise
     */
    boolean validate(final List<File> model, final ModelFormat format) {
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
    String extractName(final List<File> model, final ModelFormat format) {
        FileFormatService service = serviceForFormat(format)
        if (service != null) {
            return service.extractName(model)
        } else {
            return ""
        }
    }
    
    
    /**
     * Extracts the description of the Model from the @p model in specified @p format.
     * @param model The Model file to use as a source
     * @param format The format of the Model file
     * @return The description of the Model, if possible, an empty String if not possible
     */
    String extractDescription(final List<File> model, final ModelFormat format) {
        FileFormatService service = serviceForFormat(format)
        if (service != null) {
            return service.extractDescription(model)
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
     * Retrieves all pubmed annotations through the service responsible for the format used
     * by the @p revision.
     * @param rev The Revision for which all pubmed annotations should be retrieved
     * @return List of all pubmeds used in the Revision
     */
    List<String> getPubMedAnnotation(Revision rev) {
        FileFormatService service = serviceForFormat(rev.format)
        if (service) {
            return service.getPubMedAnnotation(rev.toCommandObject())
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
        if (format && services.containsKey(format.identifier)) {
            return grailsApplication.mainContext.getBean((String)services.getAt(format.identifier))
        } else {
            return null
        }
    }
}
