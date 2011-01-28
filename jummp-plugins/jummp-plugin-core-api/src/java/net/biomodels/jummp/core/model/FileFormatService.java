package net.biomodels.jummp.core.model;

import java.io.File;

/**
 * Service interface for handling a specific ModelFormat.
 * The interface needs to be implemented by a plugin providing support for a Model format
 * like SBML. The core application uses this interface to resolve the service which provides
 * the functionality to handle a specific format.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface FileFormatService {

    /**
     * Validate the @p model.
     * @param model File handle containing the Model to be validated.
     * @return @c true if the Model is valid, @c false otherwise
     */
    public boolean validate(final File model);

    /**
     * Extracts the name from the @p model.
     * @param model File handle containing the Model whose name should be extracted.
     * @return The name of the Model, if possible, an empty String if not possible
     */
    public String extractName(final File model);
}
