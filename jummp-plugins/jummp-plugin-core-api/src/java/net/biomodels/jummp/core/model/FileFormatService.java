package net.biomodels.jummp.core.model;

import java.io.File;
import java.util.List;

/**
 * Service interface for handling a specific ModelFormat.
 * The interface needs to be implemented by a plugin providing support for a Model format
 * like SBML. The core application uses this interface to resolve the service which provides
 * the functionality to handle a specific format.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public interface FileFormatService {

    /**
     * Validate the @p model.
     * @param model File handle containing the Model to be validated.
     * @return @c true if the Model is valid, @c false otherwise
     */
    public boolean validate(final List<File> model);

    /**
     * Extracts the name from the @p model.
     * @param model File handle containing the Model whose name should be extracted.
     * @return The name of the Model, if possible, an empty String if not possible
     */
    public String extractName(final List<File> model);

    /**
     * Extracts the description from the @p model.
     * @param model File handle containing the Model whose name should be extracted.
     * @return The description of the Model, if possible, an empty String if not possible
     */
    public String extractDescription(final List<File> model);

    /**
     * Retrieves all annotation URNs in the model file referenced by @p revision.
     * @param revision The Revision identifying a model file
     * @return List of all URNs in the model file.
     */
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision);
    /**
     * Retrieves all pubmed annotations in the model file referenced by @p revision.
     * @param revision  The Revision identifying a model file
     * @return List of all pubmeds used in the Revision
     */
    public List<String> getPubMedAnnotation(RevisionTransportCommand revision);
    /*
     * Checks whether the files passed comprise a model of this format
     * @param files The files comprising a potential model of this format
     */
    public boolean areFilesThisFormat(final List<File> files);

    /**
     * Retrieves the version of a format in which revision @p revision is encoded.
     * @param revision the Revision of a model
     * @return the textual representation of the format's version - e.g. L3V2 for SBML.
     */
    public String getFormatVersion(RevisionTransportCommand revision);
}
