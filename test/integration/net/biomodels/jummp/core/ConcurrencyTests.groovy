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
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand


class ConcurrencyTests extends JummpIntegrationTest {

    def modelService
    def modelFileFormatService
    def fileSystemService
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
        modelService.vcsService.vcsManager = gitService.getInstance()
        assertTrue(modelService.vcsService.isValid())
    }

    @After
    void tearDown() {
        /*FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        vcsService.vcsManager = null
        appCtx.getBean("gitManagerFactory").git = null*/
    }

    @Test
    void testConcurrentAccess() {
        TestFramework.runOnce(new DontBlockWhenYouDontNeedTo())
    }
    
    class DontBlockWhenYouDontNeedTo extends MultithreadedTestCase
    {
        /*This test is based around the idea that it should take
        less time to read a small file in one thread than it takes
        to write a huge file in another, assuming both start together
         and they have concurrent access to VcsManager*/
        
        long timeWriteFinished=0;
        long timeReadFinished=1;
        long base;
        
        public void initialize() {
            grailsApplication.config.jummp.plugins.sbml.validation=false
            base=System.currentTimeMillis()
        }
    
        public void thread1() {
            ModelTransportCommand meta = new ModelTransportCommand(comment:
                "model import test", name: "mybigfile", format: new ModelFormatTransportCommand(identifier: "UNKNOWN"))
            RandomAccessFile f = new RandomAccessFile(File.createTempFile("bigfil", ".txt"), "rw");
            f.setLength(1024 * 1024 * 1024);
            authenticateAsAdmin()
            System.out.println("Thread 1 stopping at"+System.currentTimeMillis())
            waitForTick(1)
            System.out.println("Thread 2 starting at"+System.currentTimeMillis())
            modelService.uploadModelAsList([f], meta)
            timeWriteFinished=System.currentTimeMillis()
        }

        public void thread2() {       
            ModelTransportCommand meta = new ModelTransportCommand(comment:
                "model import test", name: "mysmallfile", format: new ModelFormatTransportCommand(identifier: "UNKNOWN"))
            File smallFile = File.createTempFile("smallfile",".txt")
            smallFile.setText("ThisIsIt")
            authenticateAsAdmin()
            Model model= modelService.uploadModelAsList([smallFile], meta)
            System.out.println("Thread 2 stopping at"+System.currentTimeMillis())
            waitForTick(1);
            System.out.println("Thread 2 starting at"+System.currentTimeMillis())
            retrieveModelFiles(model);
            timeReadFinished=System.currentTimeMillis()
       }
       
        public void finish() {
            assertTrue(timeReadFinished < timeWriteFinished);
            timeReadFinished-=base;
            timeWriteFinished-=base;
            System.out.println("${timeReadFinished}\t${timeWriteFinished}")
        }
    
    }
    
}
