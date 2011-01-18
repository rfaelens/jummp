package net.biomodels.jummp.core

import net.biomodels.jummp.core.model.ModelFormat
import net.biomodels.jummp.core.model.FileFormatService
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.beans.factory.NoSuchBeanDefinitionException

/**
 * @short Service to handle Model files.
 *
 * This service provides methods to extract information from and about a Model file.
 * It does not provide own methods but delegates the calls to the concrete service for
 * the specific ModelFormat.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelFileFormatService {

    static transactional = true

    def sbmlService

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
     * Helper function to get the proper service for @p format.
     * @param format The ModelFormat for which the service should be returned.
     * @return The service which handles the format.
     */
    private FileFormatService serviceForFormat(final ModelFormat format) {
        String serviceName
        switch (format) {
        case ModelFormat.SBML:
            return sbmlService
        case ModelFormat.UNKNOWN: // fall through
        default:
            // do not want to return a proper service for unknown file formats.
            return null
        }
        return null
    }
}
