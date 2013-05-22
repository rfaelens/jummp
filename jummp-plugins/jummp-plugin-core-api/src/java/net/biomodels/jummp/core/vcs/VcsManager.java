package net.biomodels.jummp.core.vcs;

import java.io.File;
import java.util.List;

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
* 
* 
* 
* This interface has been modified from a 'file oriented' view to 'model oriented'. Here a folder 
* is associated with a model, with files added and retrieved through specified
* revisions of the model folder. The interface has been considerably modified including
* the following behaviours: the initRepository no longer throws an exception if a
* repository is already inited. Conversely, the functions that previously used to
* throw an exception when the repository was not inited no longer do so, instead 
* the repository should be inited. This is because repository folders are now to be
* generated dynamically, which will make initing manually as part of the system 
* setup infeasible.
**/
public interface VcsManager {
    /**
    * Initialises a working copy of the VCS.
    * @param modelDirectory The directory where the model repository is created
    * @param exchangeDirectory The exchange directory for retrieved files
    * @throws VcsException if something goes wrong (needs to be made more specific to new usage)
    **/
    public void initRepository(File modelDirectory, File exchangeDirectory) throws VcsException;

    /**
    * Imports files into the model repository
    * Copies @p files into the model directory of the VCS, with a single commit
    * @param modelDirectory the model directory
    * @param files the files to import
    * @param commitMessage The commit message to be used for the import.
    * @return Revision corresponding to the commit
    * @throws VcsException
    **/
    public String updateModel(File modelDirectory, List<File> files, String commitMessage) throws VcsException;

    /**
    * Overloaded method for convenience using a default commit message.
    * Copies @p files into the model directory of the VCS, with a single commit
    * @param modelDirectory the model directory
    * @param files the files to import
    * @return Revision corresponding to the commit
    * @see updateModel
    * @throws VcsException
     */
    public String updateModel(File modelDirectory, List<File> files) throws VcsException;

    /**
    * Retrieves files at the @p modelDirectory  of given @p revision.
    * Copies the files from @p modelDirectory to the exchange directory. If the version is the
    * current head (specified by passing null as revision) this results in copying the 
    * current model directory to the exchange directory. If a previous revision is required
    * a VCS depending operation is performed to retrieve the revision from VCS.
    * It is the responsibility of the caller to delete the files in the exchange directory, when
    * they is not required any more.
    * @param modelDirectory the model directory
    * @param revision The revision of the file. If @c null HEAD will be used.
    * @return The list of files in the exchange location
    * @throws VcsException if the revision is not found
    */
    public List<File> retrieveModel(File modelDirectory, String revision) throws VcsException;

    /**
     * Overloaded method for convenience passing null as revision
     * @param modelDirectory the model directory
     * @return The list of files in the exchange location
     * @throws VcsException if an error occurs (needs to be made more specific
     * @see retrieveFile(String file, String revision)
     */
    public List<File> retrieveModel(File modelDirectory) throws VcsException;

    
    /**
    * Retrieves the revision ids from @p modelDirectory
    * Returns the revision ids associated with the modelDirectory in the repository
    * @param modelDirectory the model directory
    */
    public List<String> getRevisions(File modelDirectory);
    
    
    /**
    * Updates the working copy to the latest remote HEAD.
    */
    public void updateWorkingCopy(File modelDirectory);


}