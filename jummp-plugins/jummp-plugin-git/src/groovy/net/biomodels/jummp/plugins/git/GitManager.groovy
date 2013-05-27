package net.biomodels.jummp.plugins.git

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import net.biomodels.jummp.core.vcs.*
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.AddCommand
import org.eclipse.jgit.api.Git
import java.util.List;
import org.eclipse.jgit.api.InitCommand
import java.util.LinkedHashMap;
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
    private LruCache<File, Git>  initedRepositories=new LruCache<File, Git>(20);
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
    
    public void init(File exchangeDirectory)
    {
        this.exchangeDirectory=exchangeDirectory;
    }
    
    private void initRepository(File modelDirectory) {
        lock.lock()
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
            .findGitDir(modelDirectory) // scan up the file system tree
            .build()
            Git git = new Git(repository)

            String branchName
            String fullBranch = repository.getFullBranch()
            if (!fullBranch) {
                
                /*try
                {
                    createGitRepo(modelDirectory)
                }
                catch(Exception e)
                {
                    throw new VcsException(e.toString());
                }*/
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
            lock.unlock()
        }
    }

    private Git createGitRepo(File directory) {
        InitCommand initCommand = Git.init();
        initCommand.setDirectory(directory);
        return initCommand.call();
    }
    
    
    public String updateModel(File modelDirectory, List<File> files, String commitMessage) {
        String revision = null
        lock.lock()
        try {
            if (!initedRepositories.containsKey(modelDirectory)) {
                if (exchangeDirectory==null) throw new VcsException("init error: exchange directory cannot be null")
                initRepository(modelDirectory);
            }
            revision = handleAddition(modelDirectory, files, commitMessage)
        } finally {
            lock.unlock()
        }
        return revision
    }

    public String updateModel(File modelDirectory, List<File> files) {
        return updateFile(modelDirectory, files, "Update of ${name}")
    }
    
    private void downloadFiles(File modelDirectory, List<File> addHere)
    {
         File[] repFiles=modelDirectory.listFiles();
         repFiles.each
         {
             File destinationFile = new File(exchangeDirectory.absolutePath + System.getProperty("file.separator") + it.getName())
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
    
    
    public List<File> retrieveModel(File modelDirectory, String revision)
    {
        List<File> returnedFiles = new LinkedList<File>()
        lock.lock()
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
            lock.unlock()
        }
        return returnedFiles
    }
    
    
    
    public List<String> getRevisions(File modelDirectory)
    {
        Iterator<RevCommit> log=initedRepositories.get(modelDirectory).log().call().iterator();
        List<String> myList=new LinkedList<String>();
        log.each
        {
            myList.add(it.getName())
        }
        return myList
    }
    

    public void updateWorkingCopy(File modelDirectory) {
        if (!initedRepositories.containsKey(modelDirectory)) {
            if (exchangeDirectory==null) throw new IOException("not inited")
            initRepository(modelDirectory);
        }
        if (hasRemote) {
            initedRepositories.get(modelDirectory).pull().call()
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