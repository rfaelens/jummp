package net.biomodels.jummp.core.vcs;

/**
 * Exception indicating that a file is tried to import a file to the VCS although it is already under version control.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class FileAlreadyVersionedException extends VcsException {
    private String fileName;

    /**
     * Constructs the exception
     * @param fileName The name of the file tried to import
     */
    public FileAlreadyVersionedException(String fileName) {
        super("The File " + fileName + " is already under version control.");
        this.fileName = fileName;
    }

    /**
     * @return The name of the file tried to import
     */
    public String getFileName() {
        return this.fileName;
    }
}
