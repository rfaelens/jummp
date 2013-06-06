package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import edu.umd.cs.mtc.MultithreadedTestCase
import edu.umd.cs.mtc.TestFramework
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.mock.web.MockServletContext
import org.springframework.core.io.FileSystemResourceLoader
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.User
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.lib.Constants
import net.biomodels.jummp.model.ModelFormat


class ConcurrencyTests extends JummpIntegrationTest {

    def vcsService
    def grailsApplication
    
    
    @Before
    void setUp() {
       createUserAndRoles()
       setUpVcs()
    }
    
    void setUpVcs() {
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        vcsService.vcsManager = gitService.getInstance()
        File exchange=new File("target/vcs/exchange")
        exchange.mkdirs()
        assertTrue(vcsService.isValid())
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        vcsService.vcsManager = null
    }

    @Test
    void testConcurrentAccess() {
        TestFramework.setGlobalRunLimit(120)
        TestFramework.runOnce(new DontBlockWhenYouDontNeedTo())
    }
    
    private File smallModel(String name, String text) {

        File nonModel=new File("target/vcs/exchange/"+name)
        nonModel.setText(text)
        return nonModel
    }
    
    private File sbmlModel()
    {
        File sbmlModel=File.createTempFile("model", ".xml")
        sbmlModel.setText('''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
Add a comment to this line
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')
        return sbmlModel
    }

    private void testRepositoryCommit(Repository repository, String rev)
    {
        ObjectId commit = repository.resolve(Constants.HEAD)
        RevWalk revWalk = new RevWalk(repository)
        RevCommit revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
    }
        
    private void testFileCorrectness(String modelDirectory, String filename, String filetext)
    {
        File testFile=new File(modelDirectory + "/" + filename);
        List<String> lines = testFile.readLines()
        assertEquals(1, lines.size())
        assertEquals(filetext, lines[0])
    }
    
    
    
    class DontBlockWhenYouDontNeedTo extends MultithreadedTestCase
    {
        /*This test is based around the idea that it should take
        less time to read a small file in one thread than it takes
        to write a huge file in another, assuming both start together
         and they have concurrent access to VcsManager*/
        
        long timeWriteFinished=0;
        long timeReadFinished=1;
        long timeSecondWrite=1;
        long base;
        
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
            String modelIdentifier="target/vcs/git/bigmodel"
            Model model = new Model(name: "bigmodel", vcsIdentifier: modelIdentifier)
            Repository repository=getModelRepository(modelIdentifier)
            String testName="big_file_test"
            String testText="myTextIsNotSoBig"
            File bigFile=File.createTempFile("bigfil", ".txt")
            RandomAccessFile f = new RandomAccessFile(bigFile, "rw")
            f.setLength(100 * 1024 * 1024);
            f.close()
            authenticateAsAdmin()
            System.out.println("Thread 1 stopping at"+System.currentTimeMillis())
            waitForTick(1)
            long base=System.currentTimeMillis();
            System.out.println("Thread 1 starting at"+base)
            String rev=vcsService.importModel(model, [smallModel(testName, testText), bigFile])
            timeWriteFinished=System.currentTimeMillis() - base
            testRepositoryCommit(repository, rev)
            testFileCorrectness(modelIdentifier, testName, testText)
        }

        public void thread2() {       
            String modelIdentifier="target/vcs/git/smallmodel"
            Model model = new Model(name: "smallmodel", vcsIdentifier: modelIdentifier)
            Repository rep=getModelRepository(modelIdentifier)
            String testName="small_file_test"
            String testText="myTextIsEquallySmall"
            authenticateAsAdmin()
            String rev=vcsService.importModel(model, [smallModel(testName, testText), sbmlModel()])
            System.out.println("Thread 2 stopping at"+System.currentTimeMillis())
            waitForTick(1);
            Thread.sleep(200)
            long base=System.currentTimeMillis();
            System.out.println("Thread 2 starting at"+base)
            List<File> files=vcsService.vcsManager.retrieveModel(new File(modelIdentifier));
            timeReadFinished=System.currentTimeMillis() - base;
            assertEquals(2,files.size())
            testFileCorrectness(modelIdentifier,testName,testText)
       }
       
        public void thread3() {
            String modelIdentifier="target/vcs/git/mediummodel"
            Model model = new Model(name: "mediummodel", vcsIdentifier: modelIdentifier)
            Repository repository=getModelRepository(modelIdentifier)
            String testName="medium_file_test"
            String testText="myTextIsNotMedium"
            File bigFile=File.createTempFile("medfil", ".txt")
            RandomAccessFile f = new RandomAccessFile(bigFile, "rw")
            f.setLength(20 * 1024 * 1024);
            f.close()
            authenticateAsAdmin()
            System.out.println("Thread 3 stopping at"+System.currentTimeMillis())
            waitForTick(1)
            long base=System.currentTimeMillis();
            System.out.println("Thread 3 starting at"+base)
            String rev=vcsService.importModel(model, [smallModel(testName, testText), bigFile])
            timeSecondWrite=System.currentTimeMillis() - base
            testRepositoryCommit(repository, rev)
            testFileCorrectness(modelIdentifier, testName, testText)
        }

        
       
        public void finish() {
            assertTrue(timeReadFinished < timeWriteFinished);
            System.out.println("${timeReadFinished}\t${timeWriteFinished}\t${timeSecondWrite}")
        }
    
    }
    
}
