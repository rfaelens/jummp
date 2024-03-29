/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Commons, Spring Framework, Perf4j, Grails (or a modified version of that library), 
* containing parts covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, Spring Framework, Perf4j, Grails used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import java.io.FilenameFilter
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.perf4j.aop.Profiled
import org.springframework.beans.factory.InitializingBean

/**
 * Provides an implementation of IFileSystemService. VcsManager implementations should use it
 * to fetch the location where a new repository should be created.
 *
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 */
class FileSystemService implements IFileSystemService, InitializingBean {
    static transactional = false
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
    private static final Log log = LogFactory.getLog(this.getClass())

    /**
     * Override org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * Sets the location of the root and of the current model container.
     */
    void afterPropertiesSet() throws Exception {
        root = findRoot()
        if (root) {
            File[] containers = getFolders(root)
            if (containers.length == 0) {
                StringBuffer currentModelContainerPath = new StringBuffer(root.absolutePath)
                currentModelContainerPath.append(File.separator).append(CONTAINER_PATTERN_SEED)
                currentModelContainer = currentModelContainerPath.toString()
                ensureFolderExists(currentModelContainer)
            } else {
                /*
                 * String comparator that looks first at the length of the string
                 * and then at the contents. Therefore aaaa would be greater than zzz.
                 */
                def cmp = [compare: { a, b ->
                    if (a.equals(b)) {
                        return 0
                    } else {
                        if (a.length() < b.length()) {
                            return -1
                        }
                        if (a.length() > b.length()) {
                            return 1
                        }
                        if (a.length() == b.length()) {
                            return a.compareTo(b)
                        }
                    }
                }] as Comparator
                String lastContainerName = Collections.max(containers.collect{ it.name }, cmp)
                File lastContainer = new File(root, lastContainerName)
                currentModelContainer = lastContainer.absolutePath
                findCurrentModelContainer()
            }
            log.debug("New model to be deposited in $currentModelContainer")
        }
        else {
            log.error("Root for FileSystemService was not configured!")
        }
    }

    /**
     * Returns the folder where the model will be stored.
     *
     * This can be either the current container, or a new one,
     * depending on the number of models we already have.
     */
    @Profiled(tag = "fileSystemService.findCurrentModelContainer")
    public String findCurrentModelContainer() {
        final int MODEL_COUNT
        File[] dirs = getFolders(new File(currentModelContainer))
        if (dirs == null) {
            MODEL_COUNT = 0
        } else {
            MODEL_COUNT = dirs.length
        }
        if (MODEL_COUNT == maxContainerSize) {
            currentModelContainer = incrementModelContainer(currentModelContainer)
            ensureFolderExists(currentModelContainer)
        }
        return currentModelContainer
    }

    /*
     * Updates the model container name.
     *
     * aaa becomes aab, aaz becomes aba, zzz becomes aaaa.
     * @param current the string to be incremented.
     * @return the updated model container name.
     */
    String incrementModelContainer(String current) {
        current = new File(current).name
        ArrayDeque<Character> resultStack = new ArrayDeque()
        boolean mustIncrementNext = true
        for (int i = current.length() - 1; i >= 0; i--) {
            char c = current.charAt(i)
            if (c < 'a' || c > 'z') {
                String msg = "Unexpected character $c within model container name $current"
                throw new IllegalArgumentException(msg)
            }
            if (mustIncrementNext) {
                if (c == 'z') {
                    resultStack.addFirst('a')
                } else {
                    resultStack.addFirst(c.next())
                    mustIncrementNext = false
                }
            } else {
                resultStack.addFirst(c)
            }
        }
        if (mustIncrementNext) { // have reached maximum capacity - e.g. 'zzz'
            resultStack.addFirst('a')
        }
        int len = root.absolutePath.length() + 1 + resultStack.size()
        StringBuilder result = new StringBuilder(len)
        result.append(root.absolutePath).append(File.separator)
        resultStack.inject(result) { r, c -> r.append(c) }
        return result.toString()
    }

    /*
     * Finds the subfolders from a given parent.
     * @param parent    the location where to look for model folders
     * @return          an array of model folders
     */
    private File[] getFolders(File parent) {
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
            if (rootLocation) {
                root = new File(rootLocation).getCanonicalFile()
            }
       } catch(IOException ex) {
            log.error(ex.message, ex)
        } catch(SecurityException e) {
            log.error(e.message, e)
        }
        if (root && !root.exists()) {
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
