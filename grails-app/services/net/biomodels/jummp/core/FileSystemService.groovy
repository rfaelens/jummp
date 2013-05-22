package net.biomodels.jummp.core

import grails.util.Holders
import java.io.FilenameFilter
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.beans.factory.InitializingBean
/**
 * Provides an implementation of IFileSystemService. VcsManager implementations should use it
 * to fetch the location where a new repository should be created.
 *
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 * @date 20130521
 */
class FileSystemService implements IFileSystemService, InitializingBean {
    static transactional = true
    /*
     * Dependency Injection for Grails Application
     */
    def grailsApplication
    /**
     * The location of the parent folder where all repositories reside.
     */
    File root
    /**
     * The number of characters that container names have.
     */
    static final int CONTAINER_PATTERN_LENGTH = 3
    /**
     * The name of the first container.
     */
    final String CONTAINER_PATTERN_SEED = "a" * CONTAINER_PATTERN_LENGTH
    /**
     * Ideally, this should be a symlink that just changes its target as needed.
     * The path of the current container is absolute.
     */
    String currentModelContainer
    /**
     * The maximum number of repositories stored in the current container before a new one is
     * created.
     */
    int maxContainerSize = 1000
    /**
     * This class' log
     */
    private static final Log log = LogFactory.getLog(this)

    /**
     * Override org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * Sets the location of the root and of the current model container.
     */
    void afterPropertiesSet() throws Exception {
        root = findRoot()
        StringBuffer currentModelContainerPath = new StringBuffer(root.absolutePath)
        currentModelContainerPath.append(File.separator).append(CONTAINER_PATTERN_SEED)
        currentModelContainer = currentModelContainerPath.toString()
        ensureFolderExists(currentModelContainer)
        log.debug("New model to be deposited in $currentModelContainer")
    }

    /**
     * Returns the folder where the model will be stored.
     *
     * This can be either the current container, or a new one, depending on the number of models we already have.
     */
    public String findCurrentModelContainer() {
        final int MODEL_COUNT
        File[] dirs = getModelFolders(new File(currentModelContainer))
        if (dirs == null) {
            MODEL_COUNT = 0
        } else {
            MODEL_COUNT = dirs.length
        }
        if (MODEL_COUNT == maxContainerSize) {
            currentModelContainer = currentModelContainer.next()
            ensureFolderExists(currentModelContainer)
        }
        return currentModelContainer
    }

    /*
     * Finds the folders from the current model container.
     * @param parent    the location where to look for model folders
     * @return          an array of model folders
     */
    private File[] getModelFolders(File parent) {
        def existingContainers = parent.listFiles( new FilenameFilter() {
            boolean accept(File root, String name) {
                StringBuffer currentLocation = new StringBuffer(root.absolutePath)
                currentLocation.append(File.separator).append(name)
                final File currentEntry = new File(currentLocation.toString())
                return (currentEntry.exists() && currentEntry.isDirectory())
            }
        })
        return existingContainers
    }

    /*
     * Locates the folder where all models should reside based on user's settings.
     */
    private File findRoot() {
        String rootLocation
        def conf = grailsApplication.config
        if (conf.jummp.vcs.plugin == "git") {
            rootLocation = conf.jummp.vcs.workingDirectory
        }
        else if (conf.jummp.vcs.plugin == "subversion") {
            rootLocation = conf.jummp.plugins.subversion.localRepository
        }
        log.debug("Root folder for model repositories set to ${rootLocation}")
        try {
            root = new File(rootLocation).getCanonicalFile()
        } catch(NullPointerException e) {
            log.error("Cannot find the location of the root folder", e)
        } catch(IOException ex) {
            log.error(ex.message, ex)
        } catch(SecurityException e) {
            log.error(e.message, e)
        }
        if (!root.exists()) {
            log.error("Root folder ${root.absolutePath} does not exist.")
        }
        return root
    }

    /*
     * Creates a folder with a given path. Does not overwrite existing files or folders.
     * @param   path the absolute where the folder should be created.
     * @return  true if the folder has been created or if it already existed, false otherwise.
     */
    private boolean ensureFolderExists(String path) {
        if (path == null || path.isEmpty()) {
            return false
        }
        File nextContainer
        try {
            nextContainer = new File(path).getCanonicalFile()
        } catch(IOException ex) {
            log.error("Cannot construct canonical path for file ${path}", ex)
            return false
        } catch(SecurityException ex) {
            log.error("Cannot gain access to file ${path} due to security issues", ex)
            return false
        }
        if (!nextContainer.exists()) {
            boolean success = nextContainer.mkdirs()
            if (!success) {
                log.error("Cannot create directory ${nextContainer.absolutePath}. I don't know why.")
                return false
            }
            return true
        }
        // if nextContainer already exists
        return true
    }
}
