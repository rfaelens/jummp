package net.biomodels.jummp.plugins.git

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import net.biomodels.jummp.core.vcs.*
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.AddCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

/**
 * @short GitManager provides the interface to a local git clone.
 *
 * This class is the concrete implementation of the VcsManager for git. It manages importing files,
 * updating files and retrieving files. The clone needs to be checkout before using this manager.
 * It uses the high level API of JGit for managing the clone.
 *
 * GitManager uses a ReentrantLock in all of its method to make it thread safe. Nevertheless this does not ensure
 * that the clone is kept in a correct state. If two GitManager access the same clone, the lock cannot
 * protect and GitManager is also not able to detect whether the clone has been changed outside the class.
 * It is important to let the instance of the GitManager be the only resource accessing the clone!
 * GitManager should be able to handle situations that the remote repository changes. Before any change to the
 * clone is done, a pull from remote repository is performed to ensure that the later push won't fail.
 * Of course there is still the chance of race conditions. The best way is to not interfer at all with the
 * repository managed by the GitManger.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class GitManager implements VcsManager {
    private static final ReentrantLock lock = new ReentrantLock()
    private static final AtomicInteger uid = new AtomicInteger(0)
    private final File clone
    private final File exchangeDirectory
    private boolean inited = false
    private Repository repository
    private Git git
    private boolean hasRemote

    public GitManager() {

    }

    public void init(File clone, File exchangeDirectory) {
        lock.lock()
        try {
            if (inited) {
                throw new VcsAlreadyInitedException()
            }
            if (!clone.isDirectory() || !clone.exists()) {
                throw new VcsException("Local clone directory " + clone.toString() + " is either not a directory or does not exist")
            }
            if (!exchangeDirectory.isDirectory() || !exchangeDirectory.exists()) {
                throw new VcsException("Exchange directory " + exchangeDirectory.toString() + " is either not a directory or does not exist")
            }
            this.clone = clone
            this.exchangeDirectory = exchangeDirectory
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
            repository = builder.setWorkTree(clone)
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir() // scan up the file system tree
            .build()
            git = new Git(repository)

            String branchName
            String fullBranch = repository.getFullBranch()
            branchName = fullBranch.substring(Constants.R_HEADS.length())
            Config repoConfig = repository.getConfig()
            final String remote = repoConfig.getString(
                 ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                  ConfigConstants.CONFIG_KEY_REMOTE)
            hasRemote = (remote != null)
            inited = true
        } finally {
            lock.unlock()
        }
    }

    public String importFile(File file, String name, String commitMessage) {
        String revision = null
        lock.lock()
        try {
            if (!inited) {
                throw new VcsNotInitedException()
            }
            if (!file.exists()) {
                throw new VcsException(file.toString() + " does not exists and cannot be imported into SVN")
            }
            if (!file.isFile()) {
                throw new VcsException(file.toString() + " is not a file and cannot be imported into SVN")
            }
            if (clone.list().toList().contains(name)) {
                throw new FileAlreadyVersionedException(name)
            }
            revision = handleAddition(file, name, commitMessage)
        } finally {
            lock.unlock()
        }
        return revision
    }

    public String importFile(File file, String name) {
        return importFile(file, name, "Import of ${name}")
    }

    public String updateFile(File file, String name, String commitMessage) {
        String revision = null
        lock.lock()
        try {
            if (!inited) {
                throw new VcsNotInitedException()
            }
            if (!file.exists()) {
                throw new VcsException(file.toString() + " does not exists and cannot be imported into SVN")
            }
            if (!file.isFile()) {
                throw new VcsException(file.toString() + " is not a file and cannot be imported into SVN")
            }
            if (!clone.list().toList().contains(name)) {
                throw new FileNotVersionedException(name)
            }
            revision = handleAddition(file, name, commitMessage)
        } finally {
            lock.unlock()
        }
        return revision
    }

    public String updateFile(File file, String name) {
        return updateFile(file, name, "Update of ${name}")
    }

    public File retrieveFile(String file, String revision) {
        File destinationFile = null
        lock.lock()
        try {
            if (!inited) {
                throw new VcsNotInitedException()
            }
            if (!clone.list().toList().contains(file)) {
                throw new FileNotVersionedException(file)
            }
            File sourceFile = new File(clone.absolutePath + System.getProperty("file.separator") + file)
            destinationFile = new File(exchangeDirectory.absolutePath + System.getProperty("file.separator") + "svn_${uid.getAndIncrement()}_" + file)
            if (revision == null) {
                // return current HEAD revision
                FileUtils.copyFile(sourceFile, destinationFile)
            } else {
                try {
                    // need to checkout in a temporary branch
                    String branchName = "temp${uid.getAndIncrement()}"
                    git.checkout().setName(branchName).setCreateBranch(true).setStartPoint(revision).call()
                    FileUtils.copyFile(sourceFile, destinationFile)
                    git.checkout().setName("master").call()
                    git.branchDelete().setBranchNames(branchName).call()
                } catch (Exception e) {
                    throw new VcsException("Checking out file from git failed", e)
                }
            }
        } finally {
            lock.unlock()
        }
        return destinationFile
    }

    public File retrieveFile(String file) {
        return retrieveFile(file, null)
    }

    public void updateWorkingCopy() {
        if (!inited) {
            throw new VcsNotInitedException()
        }
        if (hasRemote) {
            git.pull().call()
        }
    }

    /**
    * Internal implementation for git add/git commit.
    *
    * In git there is no difference between initial import and update of a file.
    * This method contains the merged implementation for both import and update.
    * It first performs a git pull, copies the file, does git add, git commit and finally a push
    * @param source The file to copy into the clone
    * @param destination The location inside the clone
    * @param commitMessage The commit message 
    */
    private String handleAddition(File source, String destination, String commitMessage) {
        String revision
        try {
            updateWorkingCopy()
            FileUtils.copyFile(source, new File(clone.absolutePath + File.separator + destination))
            AddCommand add = git.add()
            add = add.addFilepattern(destination)
            add.call()
            RevCommit commit = git.commit().setMessage(commitMessage).call()
            revision = commit.getId().getName()
            if (hasRemote) {
                git.push().call()
            }
        } catch (Exception e) {
            throw new VcsException("Git command could not be executed", e)
        }
        return revision
    }
}
