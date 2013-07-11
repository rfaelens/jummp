package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.util.interprocess.InterJummpSync
import net.biomodels.jummp.util.interprocess.UpdateAndTest
import net.biomodels.jummp.plugins.git.GitManager
import org.apache.commons.io.FileUtils

/*
* These tests ensure consistent multi-threaded and multi-process access to
* the GitManager implementation. A separate process is launched and three
* threads are created, all writing random numbers to the same file and
* updating the repository, subsequently verifying that the value for the
* file in the revision id returned by GitManager is correct.
*/
class MultiProcessRepositoryTests  {
    GitManager manager
    File exchange
    File modelFolder
    
    @Before
    void setUp() {
        // Setup exchange/model folders
        exchange=new File("target/vcs/exchange")
        modelFolder=new File("target/vcs/modelfolder")
        modelFolder.mkdirs()
        exchange.mkdirs()
        manager=new GitManager()
        manager.init(exchange)
    }

    @After
    void tearDown() {
        // Delete exchange/model folders
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        FileUtils.deleteDirectory(new File("target/vcs/modelfolder"))
    }

    @Test
    void testConcurrent() {
    	//start up sync server
        InterJummpSync sync=new InterJummpSync()
        sync.start(false, 50000)
        
        //launch concurrent tester grails script and wait for it to be ready
        String[] command=new String[3]
        command[0]="grails"
        command[1]="test"
        command[2]="ConcurrentRepositoryTester"
        try
        {
            Process process = Runtime.getRuntime().exec(command,null);
        }
        catch(Exception e) {
            e.printStackTrace()
        }
        
        sync.waitForMessage("DoneStartup")
        
        //update the model
        manager.updateModel(modelFolder, [bigModel()])
        
        //launch three update and test class threads
        UpdateAndTest test1=new UpdateAndTest(manager, modelFolder, exchange, 1)
        Thread thread1=new Thread(test1)
        thread1.start()
        UpdateAndTest test2=new UpdateAndTest(manager, modelFolder, exchange, 2)
        Thread thread2=new Thread(test2)
        thread2.start()
        UpdateAndTest test3=new UpdateAndTest(manager, modelFolder, exchange, 3)
        Thread thread3=new Thread(test3)
        thread3.start()
        // wait for threads to terminate
        thread1.join()
        thread2.join()
        thread3.join()
        
        // ensure results for each thread are okay
        assertTrue(test1.dotestsPass())
        assertTrue(test2.dotestsPass())
        assertTrue(test3.dotestsPass())
        // wait for grails script to report its done, and test its result message
        sync.waitForMessage("DoneTesting")
        assertTrue(sync.getMessages().contains("TestResult: true"))
        System.out.println("Messages: "+sync.getMessages())
        // terminate server
        sync.terminate()
    }
    
    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }
}
