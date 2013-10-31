/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


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

/*These tests are written to test the concurrent use of VcsManager.
They do not aim to test other concurrency aspects of Jummp, although
they may be extended to do so. The tests are based on the 
MultithreadedTestCase framework, and seek to test behaviour of the GitManager
when threads are accessing different models (which should occur concurrently)
and when threads are accessing the same model (which should result in sequential access)
*/


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

   /*Tests that multiple threads can access different models concurrently*/
    @Test
    void testConcurrentAccess() {
        TestFramework.setGlobalRunLimit(120)
        TestFramework.runOnce(new DontBlockWhenYouDontNeedTo())
    }
    
   /*Tests that multiple threads cant access the same model concurrently*/
    @Test
    void testConcurrentModelAccess() {
        TestFramework.setGlobalRunLimit(120)
        TestFramework.runOnce(new BlockWhenYouShould())
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
        assertEquals(lines[0],filetext)
    }
    
    private void testFileCorrectness(List<File> files, String filename, String filetext)
    {
        int fileIndex=-1
        files.eachWithIndex { file, i -> 
            if (file.name == filename) fileIndex=i
        };
        assertTrue(fileIndex>-1)
        List<String> lines = files.get(fileIndex).readLines()
        assertEquals(1, lines.size())
        assertEquals(filetext, lines[0])
    }
    
    
    class BlockWhenYouShould extends MultithreadedTestCase
    {
        /*This test compares the time taken to retrieve a 
	previous revision in two conditions: a reference
	case where there is no blocking, and the case where
	the thread should be blocked. Additionally, blocking
	is checked by verifying that the time taken to retrieve
	a small model is larger than the time taken to write a 
	large model, if the latter thread has acquired the
	thread first*/
        
        long timeWriteFinished=0;
        long timeReadFinished=1;
	long referenceReadTime=0;
        String modelIdentifier="target/vcs/git/blockmodel"
        final String commitMsg="I should write and block reads"
        final Model model;
        Repository repository;
        Set<String> delete = Collections.synchronizedSet(new HashSet<String>());
        
        public BlockWhenYouShould()
        {
            model = new Model(name: "bigmodel", vcsIdentifier: modelIdentifier)
        }
    
        public void initialize() {
            grailsApplication.config.jummp.plugins.sbml.validation=false
            repository=getModelRepository(modelIdentifier)
            
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
            authenticateAsAdmin()
            String testName="small_file_test"
            String testText="myTextIsEquallySmall"
            List<File> importMe=[smallModel(testName, testText), sbmlModel()]
	    // Write a small text file
            String rev=vcsService.importModel(model, importMe)
	    // update it
	    vcsService.vcsManager.updateModel(new File(model.vcsIdentifier),[smallModel(testName,"some other text")])
	    // retrieve the previous revision to establish baseline
	    long base=System.currentTimeMillis();
	    vcsService.vcsManager.retrieveModel(new File(modelIdentifier), rev);
	    referenceReadTime=System.currentTimeMillis() - base;
	    // block, and begin after 50 ms to ensure you make your read request after thread2's write request
            waitForTick(1);
            Thread.sleep(50)
            base=System.currentTimeMillis();
            List<File> files=vcsService.vcsManager.retrieveModel(new File(modelIdentifier), rev);
            timeReadFinished=System.currentTimeMillis() - base;
            // verify that you got the same two files you wrote in that revision
	    assertEquals(2,files.size())
	    testFileCorrectness(files,testName,testText)
            importMe.each {
                delete.add(it.getCanonicalPath())
            }
        }

        public void thread2() {
	    // create a large file
            File bigFile=File.createTempFile("bigfil_block_when_should", ".txt")
            RandomAccessFile f = new RandomAccessFile(bigFile, "rw")
            f.setLength(150 * 1024 * 1024);
            f.close()
            String testName="small_file_test"
            String finalText="myTextIsNotSoBig"
            authenticateAsAdmin()
	    // block, and begin immediately, updating the model with a large file
            waitForTick(1)
            long base=System.currentTimeMillis();
            List<File> importMe=[smallModel(testName, finalText), bigFile]
            String rev=vcsService.vcsManager.updateModel(new File(model.vcsIdentifier), importMe)
            timeWriteFinished=System.currentTimeMillis() - base
	    // test repository integrity
            testRepositoryCommit(repository, rev)
            importMe.each {
            	delete.add(it.getCanonicalPath())
            }
        }
       
        public void finish() {
	    // test that read time is greater than write time, because reader was blocked by writer
            assertTrue(timeReadFinished > timeWriteFinished)
	    // test that read time when blocked is greater than read time when not blocked
 	    assertTrue(timeReadFinished > referenceReadTime)
	    // test that you have three files in the repository at the end
            List<File> files=vcsService.vcsManager.retrieveModel(new File(modelIdentifier));
            assertEquals(3, files.size())
            String testName="small_file_test"
            String finalText="myTextIsNotSoBig"
            testFileCorrectness(files, testName, finalText)
            delete.each {
            	deleteFile(new File(it))
            }
        }
    
    }
    
    
    
    
    class DontBlockWhenYouDontNeedTo extends MultithreadedTestCase
    {
        /*This test is based around the idea that it should take
        less time to read a small file in one thread than it takes
        to write a huge file in another, assuming both start together
         and they have concurrent access to VcsManager. To make things
	even more interesting there is a third thread concurrently
	writing a smaller file to another model.*/
        
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
            authenticateAsAdmin()
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
            authenticateAsAdmin()
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
            authenticateAsAdmin()
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
    
    void deleteFile(File file) {
            try
            {
                FileUtils.forceDelete(file)
            }
            catch(Exception logMe) {
                log.error(logMe.getMessage())
            }
    } 
}
