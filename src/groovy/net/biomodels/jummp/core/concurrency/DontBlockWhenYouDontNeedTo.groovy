package net.biomodels.jummp.core.concurrency
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import edu.umd.cs.mtc.MultithreadedTestCase
import edu.umd.cs.mtc.TestFramework
import java.util.concurrent.atomic.AtomicInteger
import static org.junit.Assert.*;
import grails.util.Holders
import net.biomodels.jummp.core.JummpIntegrationTest

        /*This test is based around the idea that it should take
        less time to read a small file in one thread than it takes
        to write a huge file in another, assuming both start together
         and they have concurrent access to VcsManager. To make things
	even more interesting there is a third thread concurrently
	writing a smaller file to another model.*/


class DontBlockWhenYouDontNeedTo extends ConcurrentTestBase
    {
        def vcsService=Holders.applicationContext.getBean("vcsService")
        def grailsApplication=Holders.grailsApplication
        
        long timeWriteFinished=0;
        long timeReadFinished=1;
        long timeSecondWrite=1;
        long base;
        JummpIntegrationTest jit=new JummpIntegrationTest()
        
        public void initialize() {
            grailsApplication.config.jummp.plugins.sbml.validation=false
            base=System.currentTimeMillis()
        }
        
        private Repository getModelRepository(String vcsId)
        {
            File directory=new File(vcsId)
            directory.mkdirs()
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
            Repository repository = builder.setWorkTree(directory)
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir(directory) // scan up the file system tree
            .build()
            return repository
        }
        
        public void thread1() {
	    //Create the big model data structure and file
            String modelIdentifier="target/vcs/git/bigmodel"
            Model model = new Model(name: "bigmodel", vcsIdentifier: modelIdentifier)
            Repository repository=getModelRepository(modelIdentifier)
            String testName="big_file_test"
            String testText="myTextIsNotSoBig"
            File bigFile=File.createTempFile("bigfil_dont_block", ".txt")
            RandomAccessFile f = new RandomAccessFile(bigFile, "rw")
            f.setLength(150 * 1024 * 1024);
            f.close()
            jit.authenticateAsAdmin()
	    // Block, and start writing immediately
            waitForTick(1)
            long base=System.currentTimeMillis();
            List<File> importMe=[smallModel(testName, testText), bigFile]
            String rev=vcsService.importModel(model, importMe)
            timeWriteFinished=System.currentTimeMillis() - base
            testRepositoryCommit(repository, rev)
            testFileCorrectness(modelIdentifier, testName, testText)
            importMe.each {
            	deleteFile(it)
            }
        }

        public void thread2() {       
	    //Create the small model data structure and file
            String modelIdentifier="target/vcs/git/smallmodel"
            Model model = new Model(name: "smallmodel", vcsIdentifier: modelIdentifier)
            Repository rep=getModelRepository(modelIdentifier)
            String testName="small_file_test"
            String testText="myTextIsEquallySmall"
            jit.authenticateAsAdmin()
	    //Save small model to the repository
            List<File> importMe=[smallModel(testName, testText), sbmlModel()]
            String rev=vcsService.importModel(model, importMe)
	    //Block, wait 200ms, then read the model. Your read time should be smaller than the write time
	    //of thread1 indicating concurrent use of the VcsManager.
            waitForTick(1);
            Thread.sleep(200)
            long base=System.currentTimeMillis();
            List<File> files=vcsService.vcsManager.retrieveModel(new File(modelIdentifier));
            timeReadFinished=System.currentTimeMillis() - base;
            assertEquals(2,files.size())
            testFileCorrectness(modelIdentifier,testName,testText)
            importMe.each {
            	deleteFile(it)
            }

       }
       
        public void thread3() {
	    //Create the medium model data structure and file
            String modelIdentifier="target/vcs/git/mediummodel"
            Model model = new Model(name: "mediummodel", vcsIdentifier: modelIdentifier)
            Repository repository=getModelRepository(modelIdentifier)
            String testName="medium_file_test"
            String testText="myTextIsNotMedium"
            File bigFile=File.createTempFile("medfil", ".txt")
            RandomAccessFile f = new RandomAccessFile(bigFile, "rw")
            f.setLength(10 * 1024 * 1024);
            f.close()
            jit.authenticateAsAdmin()
	    //block, and write the medium sized file after a very small delay.
	    //Your time should be smaller than the large file writing thread
            waitForTick(1)
	    Thread.sleep(50)
            List<File> importMe=[smallModel(testName, testText), bigFile]
            long base=System.currentTimeMillis();
            String rev=vcsService.importModel(model, importMe)
            timeSecondWrite=System.currentTimeMillis() - base
            testRepositoryCommit(repository, rev)
            testFileCorrectness(modelIdentifier, testName, testText)
            importMe.each {
            	deleteFile(it)
            }

        }

        public void finish() {
	    //It should take longer to write a big file than it does to read a small one, even if read started slightly later
            assertTrue(timeReadFinished < timeWriteFinished);
	    //It should take longer to write a big file than it does to write a medium one, even if the latter started later
	    assertTrue(timeSecondWrite < timeWriteFinished);
	}
    
    }

