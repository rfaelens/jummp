package net.biomodels.jummp.core;

/**
 * Interface for a service handling the structure of the folder where all models are stored.
 *
 * VcsManager implementations should rely on it 
 *
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 */
public interface IFileSystemService {
    //public createModelFolderStructure(File parent, long id);
    /**
     * Returns the path of the subfolder where models are currently created.
     *
     * Most filesystems struggle to handle more than a few thousand entries in a single folder.
     * Therefore, in the interest of scalability, we should divide the main folder for storing 
     * models into sub-directories called containers.
     *
     * Implementations of this service are free to choose the naming convention for containers,
     * as well as the maximum number of entries that should be available in 
     *
     * Given the following folder structure for the models
     *      /
     *          container1
     *              model1, model2, model3 ...
     *          container2
     *              model1001, model1002, model1003 ...
     *          container3
     *              model2001
     * this method would return the absolute path to container3
     */
    public String getCurrentModelContainer();
}
