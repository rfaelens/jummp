package net.biomodels.jummp.core.vcs;

import java.io.File;

/**
* @short Interface for a manager of a version control system (VCS).
*
* This interface defines the common methods a manager for a concrete VCS has to provide.
* A VcsManager operates a local working copy of the remote (either truly remote or local) VCS.
* The VcsManager provides methods to import, update and retrieve files to and from the VCS.
* Due to the nature of a working copy of a VCS the manager does not provide direct access to
* the files in the working copy. All files are copied into the working copy for import/update
* operations and copied to an exchange location for retrieval operations. This is required due to
* the fact that another update operation might overwrite a retrieved file and the File handle
* returned previously would not point to the retrieved revision anymore.
*
* To use a VcsManager the init method has to be invoked.
*
* Due to the nature of a working copy all methods can alter the state of the working copy. This
* implies that all methods are neither reentrant nor thread-safe. It is recommended to the
* implementation of this interface to use pessimistic locking. If a concrete implementation is
* thread safe it should state this in it's documentation. For users of the VcsManager it is
* recommended to expect the worst and ensure locking itself, if possible. As a matter of fact
* it is also recommended to not modify the working copy from outside the application.
* @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
**/
public interface VcsManager {
    /**
    * Initialises a working copy of the VCS.
    * @param workingCopy The directory where the working copy should be cloned to
    * @param exchangeDirectory The exchange directory for retrieved files
    * @throws VcsAlreadyInitedException If init has been called before
    **/
    public void init(File workingCopy, File exchangeDirectory) throws VcsAlreadyInitedException;

    /**
    * Imports a file into the VCS.
    * Copies @p file into the working copy of the VCS and performs an initial import
    * to the remote location of the VCS.
    * Use this method if the file did not exist previously.
    * @param file The file to import
    * @param name The name of the to be used in the VCS. If @c null the file's name is used.
    * @param commitMessage The commit message to be used for the import.
    * @return Revision number of inserted file
    * @throws FileAlreadyVersionedException If the file already exists in the working copy
    * @see updateFile
    **/
    public String importFile(File file, String name, String commitMessage) throws FileAlreadyVersionedException;

    /**
     * Overloaded method for convenience using a default commit message.
     * @param file The file to import
     * @param name The name of the to be used in the VCS. If @c null the file's name is used.
     * @return Revision number of inserted file
     * @throws FileAlreadyVersionedException If the file already exists in the working copy
     * @see importFile(File file, String name, String commitMessage)
     */
    public String importFile(File file, String name) throws FileAlreadyVersionedException;

    /**
    * Updates a file previously imported to the VCS.
    * Copies @p file into the working copy of the VCS and updates the existing file in the
    * VCS and the remote location of the VCS.
    * Use this method if the file had been imported previously.
    * @param file The file to update
    * @param name The name of the file in the VCS. If @c null the file's name is used.
    * @param commitMessage The commit message to be used for the update.
    * @return Revision number of updated file
    * @throws FileNotVersionedException If the file does not exist in the working copy
    * @see importFile
    **/
    public String updateFile(File file, String name, String commitMessage) throws FileNotVersionedException;

    /**
     * Overloaded method for convenience using a default commit message.
     * @param file The file to update
     * @param name The name of the file in the VCS. If @c null the file's name is used.
     * @return Revision number of updated file
     * @throws FileNotVersionedException If the file does not exist in the working copy
     * @see updateFile(File file, String name, String commitMessage)
     */
    public String updateFile(File file, String name) throws FileNotVersionedException;

    /**
    * Retrieves the @p file of given @p revision.
    * Copies the @p file from the working copy to the exchange directory. If the version is the
    * current head this results in a single copy operation. If a previous revision is required
    * a VCS depending operation is performed to retrieve the revision from the remote VCS.
    * It is the responsibility of the caller to delete the file in the exchange directory, when
    * it is not required any more.
    * @param file The name of the file in the working copy
    * @param revision The revision of the file. If @c null HEAD will be used.
    * @return The file in the exchange location
    * @throws FileNotVersionedException If the file does not exist in the working copy
    */
    public File retrieveFile(String file, String revision) throws FileNotVersionedException;

    /**
     * Overloaded method for convenience passing null as revision
     * @param file The name of the file in the working copy
     * @return The file in the exchange location
     * @throws FileNotVersionedException If the file does not exist in the working copy
     * @see retrieveFile(String file, String revision)
     */
    public File retrieveFile(String file) throws FileNotVersionedException;

    /**
    * Updates the working copy to the latest remote HEAD.
    */
    public void updateWorkingCopy();
}