package net.biomodels.jummp.plugins.git

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.ConcurrentHashMap
import net.biomodels.jummp.core.vcs.*
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.AddCommand
import org.eclipse.jgit.api.Git
import java.util.List
import org.eclipse.jgit.api.InitCommand
import java.util.LinkedHashMap;
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.util.UUID

/**
 * @short GitManager provides the interface to a local git clone.
 *
 * This class is the concrete implementation of the VcsManager for git. It manages importing files,
 * updating files and retrieving files. Each model is stored as a separate directory with its own repository.
 * If a model repository does not exist, it is created. It uses the high level 
 * API of JGit for managing the clone.
 *
 * GitManager uses ReentrantLocks in all of its method to make it thread safe
 * These locks are at the level of model repositories. 
 * Nevertheless this does not ensure that the clone is kept in a correct state. 
 * If two GitManager access the same model, the lock cannot protect and 
 * GitManager is also not able to detect whether the model has been changed 
 * outside the class. It is important to let the instance of the GitManager be 
 * the only resource accessing the model repositories! 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class GitManager implements VcsManager {

    private static final AtomicInteger uid = new AtomicInteger(0)
    private final ConcurrentHashMap<String, ReentrantLock> locks= new ConcurrentHashMap<String, ReentrantLock>();
    private final Map<File, Git>  initedRepositories=Collections.synchronizedMap(new LruCache<File, Git>(20));
    private File exchangeDirectory
    private boolean hasRemote

    class LruCache<A, B> extends LinkedHashMap<A, B> {
        private final int maxEntries;

        public LruCache(final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;
        }

        /**
         * Returns <tt>true</tt> if this <code>LruCache</code> has more entries than the maximum specified when it was
         * created.
         *
         * <p>
         * This method <em>does not</em> modify the underlying <code>Map</code>; it relies on the implementation of
         * <code>LinkedHashMap</code> to do that, but that behavior is documented in the JavaDoc for
         * <code>LinkedHashMap</code>.
         * </p>
         *
         * @param eldest
         *            the <code>Entry</code> in question; this implementation doesn't care what it is, since the
         *            implementation is only dependent on the size of the cache
         * @return <tt>true</tt> if the oldest
         * @see java.util.LinkedHashMap#removeEldestEntry(Map.Entry)
         */
        @Override
        protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
            return super.size() > maxEntries;
        }
    }

    public void init(File exchangeDirectory) {
        this.exchangeDirectory = exchangeDirectory
    }

    public String createModel(File modelDirectory, List<File> modelFiles, String commit) {
        //FIXME this is meant to do Git-specific initialisation of the repository
        //then execute the same logic as importModel(modelDirectory, modelFiles, commit)
        return ""
    }
    
    private void lockModelRepository(File modelDirectory)
    {
        if (!locks.containsKey(modelDirectory.name))
        {
            ReentrantLock lock = new ReentrantLock()
            locks.put(modelDirectory.name, lock)
        }
        locks.get(modelDirectory.name).lock();
    }
    
    private void unlockModelRepository(File modelDirectory)
    {
        ReentrantLock lock=locks.get(modelDirectory.name)
        if (!lock.hasQueuedThreads()) locks.remove(modelDirectory);
        lock.unlock()
    }
    

    private void initRepository(File modelDirectory) {
        lockModelRepository(modelDirectory)
        try {
            if (initedRepositories.containsKey(modelDirectory)) {
                //throw new VcsAlreadyInitedException()
                return;
            }
            if (exchangeDirectory==null)
            {
                throw new VcsException("Exchange directory cannot be null!");
            }
            if (!modelDirectory.isDirectory() || !modelDirectory.exists()) {
                throw new VcsException("Local model directory " + modelDirectory.toString() + " is either not a directory or does not exist")
            }
            if (!exchangeDirectory.isDirectory() || !exchangeDirectory.exists()) {
                throw new VcsException("Exchange directory " + exchangeDirectory.toString() + " is either not a directory or does not exist")
            }
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
            Repository repository = builder.setWorkTree(modelDirectory)
            .readEnvironment() // scan environment GIT_* variables
            .setGitDir(modelDirectory) // use the current directory for the repository
            .build()
            Git git = new Git(repository)

            String branchName
            String fullBranch = repository.getFullBranch()
            
            if (!fullBranch)  {
                
                git=createGitRepo(modelDirectory)
                repository=git.getRepository();
                fullBranch=repository.getFullBranch();

            }
            branchName = fullBranch.substring(Constants.R_HEADS.length())
            Config repoConfig = repository.getConfig()
            final String remote = repoConfig.getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                ConfigConstants.CONFIG_KEY_REMOTE)
            hasRemote = (remote != null)
            initedRepositories.put(modelDirectory,git);
            
        } finally {
            unlockModelRepository(modelDirectory)
        }
    }

    private Git createGitRepo(File directory) {
        Git git=null;
        InitCommand initCommand = Git.init();
        initCommand.setDirectory(directory);
        git=initCommand.call();
        return git
    }

    public String updateModel(File modelDirectory, List<File> files, String commitMessage) {
        String revision = null
        lockModelRepository(modelDirectory)
        try {
            if (!initedRepositories.containsKey(modelDirectory)) {
                if (exchangeDirectory==null) throw new VcsException("init error: exchange directory cannot be null")
                initRepository(modelDirectory);
            }
            revision = handleAddition(modelDirectory, files, commitMessage)
        } finally {
            unlockModelRepository(modelDirectory)
        }
        return revision
    }

    public String updateModel(File modelDirectory, List<File> files) {
        return updateModel(modelDirectory, files, "Update of ${modelDirectory.name}")
    }
    
    
    private void downloadFiles(File modelDirectory, List<File> addHere)
    {
        File[] repFiles=modelDirectory.listFiles();
        File tempDir = new File (exchangeDirectory.absolutePath + System.getProperty("file.separator") + UUID.randomUUID().toString() );
        tempDir.mkdir();
        repFiles.each
        {
            File destinationFile = new File(tempDir.absolutePath + System.getProperty("file.separator") + it.getName())
            if (!it.isDirectory())
            {
                FileUtils.copyFile(it, destinationFile)
                addHere.add(destinationFile)
            }
        }
        if (addHere.isEmpty()) throw new VcsException("Model directory is empty!");

    }


    public List<File> retrieveModel(File modelDirectory)
    {
        return retrieveModel(modelDirectory, null);
    }


    public List<File> retrieveModel(File modelDirectory, String revision) {
        List<File> returnedFiles = new LinkedList<File>()
        lockModelRepository(modelDirectory)
        try {
            if (!initedRepositories.containsKey(modelDirectory)) {
                if (exchangeDirectory==null) throw new VcsException("init error: exchange directory cannot be null")
                initRepository(modelDirectory);
            }
            if (revision == null) {
                // return current HEAD revision
                downloadFiles(modelDirectory, returnedFiles);
            } else {
                if (!getRevisions(modelDirectory).contains(revision))
                throw new VcsException("Revision '$revision' not found in model directory '$modelDirectory' !");
                try {
                    // need to checkout in a temporary branch
                    String branchName = "tempa${uid.getAndIncrement()}"
                    initedRepositories.get(modelDirectory).checkout().setName(branchName).setCreateBranch(true).setStartPoint(revision).call()
                    downloadFiles(modelDirectory, returnedFiles);
                    initedRepositories.get(modelDirectory).checkout().setName("master").call()
                    initedRepositories.get(modelDirectory).branchDelete().setBranchNames(branchName).call()
                } catch (Exception e) {
                    throw new VcsException("Checking out file from git failed: ", e)
                }
            }
        } finally {
            unlockModelRepository(modelDirectory)
        }
        return returnedFiles
    }

    public List<String> getRevisions(File modelDirectory)
    {
        List<String> myList=new LinkedList<String>();
        lockModelRepository(modelDirectory)
        try {
            Iterator<RevCommit> log=initedRepositories.get(modelDirectory).log().call().iterator();
            log.each
            {
                myList.add(it.getName())
            }
        }
        finally {
            unlockModelRepository(modelDirectory)
        }
        return myList
    }

    public void updateWorkingCopy(File modelDirectory) {
        lockModelRepository(modelDirectory)
        try {
            if (!initedRepositories.containsKey(modelDirectory)) {
                if (exchangeDirectory==null) throw new IOException("not inited")
                initRepository(modelDirectory);
            }
            if (hasRemote) {
                initedRepositories.get(modelDirectory).pull().call()
            }
        }
        finally {
            unlockModelRepository(modelDirectory)
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
    private String handleAddition(File modelDirectory, List<File> files, String commitMessage) {
        String revision
        try {
            updateWorkingCopy(modelDirectory)
            Git git = initedRepositories.get(modelDirectory);
            AddCommand add = git.add()
            files.each
            {
                FileUtils.copyFile(it, new File(modelDirectory.absolutePath + File.separator + it.getName()))
                add = add.addFilepattern(it.getName())
            }
            add.call()
            RevCommit commit = git.commit().setMessage(commitMessage).call()
            revision = commit.getId().getName()
            if (hasRemote) {
                git.push().call()
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Git command could not be executed", e)
        }
        return revision
    }
}
