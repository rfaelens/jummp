package net.biomodels.jummp.core

/**
 * @short Interface describing a class that detects the format of a model.
 *
 * Plugins providing support for a particular model format are expected
 * to implement this interface.
 * @see net.biomodels.jummp.core.model.FileFormatService#areFilesThisFormat(Lis)
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
public interface RunnableModelFormatDetector extends Runnable {
    /**
     * Detects whether a certain @p modelFile conforms to a given model format.
     * @param modelFile the file that should be examined.
     * @return true if there was a match, false otherwise.
     */
    public boolean isRecognisedFormat(final File modelFile);
}
