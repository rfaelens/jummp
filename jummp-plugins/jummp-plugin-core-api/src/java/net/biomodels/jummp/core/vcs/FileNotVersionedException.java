package net.biomodels.jummp.core.vcs;

/**
 * Exception indicating that a VCS operation was tried to perform on a file that is not under version control.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class FileNotVersionedException extends VcsException {
    private String fileName;

    /**
     * Constructs the exception
     * @param fileName The name of the file tried to access
     */
    public FileNotVersionedException(String fileName) {
        super("The File " + fileName + " is not under version control.");
        this.fileName = fileName;
    }

    /**
     * @return The name of the file tried to access
     */
    public String getFileName() {
        return this.fileName;
    }
}
