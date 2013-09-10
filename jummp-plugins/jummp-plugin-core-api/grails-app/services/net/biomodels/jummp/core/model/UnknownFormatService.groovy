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
    public final boolean validate(final List<File> model) {
        return areFilesThisFormat(model)
    }

    /**
     * Extracts the name from the @p model.
     * @param model File handle containing the Model whose name should be extracted.
     * @return The name of the Model, if possible, an empty String if not possible
     */
    public final String extractName(final List<File> model) {
        return ""
    }

    /**
     * Implementation of {@link net.biomodels.jummp.core.model.FileFormatService}
     * Does not make any attempt extract the version of an unknown model as it is undefined.
     * @param revision a revision of a model in a format that has not been recognised.
     * @return an empty String.
     */
    public final String getFormatVersion(RevisionTransportCommand revision) {
        return ""
    }
    /**
     * Extracts the descrition from the @p model.
     */
    public final String extractDescription(final List<File> model) {
        return ""
    }
    /**
     * Retrieves all annotation URNs in the model file referenced by @p revision.
     * @param revision The Revision identifying a model file
     * @return List of all URNs in the model file.
     */
    public final List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        return new LinkedList<String>()
    }
    /**
     * Retrieves all pubmed annotations in the model file referenced by @p revision.
     * @param revision  The Revision identifying a model file
     * @return List of all pubmeds used in the Revision
     */
    public final List<String> getPubMedAnnotation(RevisionTransportCommand revision) {
        return new LinkedList<String>()
    }
    /*
     * Checks whether the files passed comprise a model of this format
     * @param files The files comprising a potential model of this format
     */
    public final boolean areFilesThisFormat(final List<File> files) {
        if (files && !files.isEmpty()) {
            return true
        } 
        return false
    }
    
    public String getSearchIndexingContent(RevisionTransportCommand revision) {
    	    return ""
    }
}

