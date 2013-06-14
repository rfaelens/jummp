package net.biomodels.jummp.core.model
/**
 * Possibly the most permissive implementation possible of the file format service
 * interface. Everything is a valid unknown format.
 * @author raza
 */
class UnknownFormatService implements FileFormatService {
    /**
     * Validate the @p model.
     * @param model File handle containing the Model to be validated.
     * @return @c true if the Model is valid, @c false otherwise
     */
    public boolean validate(final List<File> model) {
        return true;
    }

    /**
     * Extracts the name from the @p model.
     * @param model File handle containing the Model whose name should be extracted.
     * @return The name of the Model, if possible, an empty String if not possible
     */
    public String extractName(final List<File> model) {
        return ""
    }
    /**
     * Retrieves all annotation URNs in the model file referenced by @p revision.
     * @param revision The Revision identifying a model file
     * @return List of all URNs in the model file.
     */
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        return new LinkedList<String>()
    }
    /**
     * Retrieves all pubmed annotations in the model file referenced by @p revision.
     * @param revision  The Revision identifying a model file
     * @return List of all pubmeds used in the Revision
     */
    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) {
        return new LinkedList<String>()
    }
    /*
     * Checks whether the files passed comprise a model of this format
     * @param files The files comprising a potential model of this format
     */
    public boolean areFilesThisFormat(List<File> files) {
        return true
    }
}

