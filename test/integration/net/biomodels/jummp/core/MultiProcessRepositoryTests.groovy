package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.util.InterJummpSync
import net.biomodels.jummp.plugins.git.GitManager

class MultiProcessRepositoryTests  {
    GitManager manager
    File exchange
    File modelFolder
    
    @Before
    void setUp() {
        // Setup logic here
        exchange=new File("target/vcs/exchange")
        modelFolder=new File("target/vcs/modelfolder")
        modelFolder.mkdirs()
        manager=new GitManager()
        manager.init(exchange)
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    
    
    @Test
    void testConcurrent() {
        InterJummpSync sync=new InterJummpSync()
        sync.start(false, 50000)
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
        
        manager.updateModel(modelFolder, [bigModel()])

        
        sync.terminate()
    }
    
    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }
}
