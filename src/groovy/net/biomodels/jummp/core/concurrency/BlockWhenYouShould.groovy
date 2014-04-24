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
* JGit, MultiThreadedTestCase (or a modified version of that library), containing parts
* covered by the terms of the modified BSD license and the Eclipse Distribution License, 
* the licensors of this Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JGit and MultithreadedTC used as well as
* that of the covered work.}
**/



package net.biomodels.jummp.core.concurrency
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.plugins.security.User
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import edu.umd.cs.mtc.MultithreadedTestCase
import edu.umd.cs.mtc.TestFramework
import java.util.concurrent.atomic.AtomicInteger
import grails.util.Holders
import net.biomodels.jummp.core.JummpIntegrationTest




/*This test compares the time taken to retrieve a 
	previous revision in two conditions: a reference
	case where there is no blocking, and the case where
	the thread should be blocked. Additionally, blocking
	is checked by verifying that the time taken to retrieve
	a small model is larger than the time taken to write a 
	large model, if the latter thread has acquired the
	thread first*/
class BlockWhenYouShould extends ConcurrentTestBase
    {
    	def vcsService=Holders.applicationContext.getBean("vcsService")
    	def grailsApplication=Holders.grailsApplication
        long timeWriteFinished=0;
        long timeReadFinished=1;
        long referenceReadTime=0;
        String modelIdentifier="target/vcs/git/blockmodel"
        final String commitMsg="I should write and block reads"
        final Model model;
        Repository repository;
        Set<String> delete = Collections.synchronizedSet(new HashSet<String>());
        JummpIntegrationTest jit=new JummpIntegrationTest()
        
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
            jit.authenticateAsAdmin()
            String testName="small_file_test"
            String testText="myTextIsEquallySmall"
            List<File> importMe=[smallModel(testName, testText), sbmlModel()]
	    // Write a small text file
            String rev=vcsService.importModel(model, importMe)
	    // update it
	    vcsService.vcsManager.updateModel(new File(model.vcsIdentifier),[smallModel(testName,"some other text")], null, "Test update")
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
            jit.authenticateAsAdmin()
	    // block, and begin immediately, updating the model with a large file
            waitForTick(1)
            long base=System.currentTimeMillis();
            List<File> importMe=[smallModel(testName, finalText), bigFile]
            String rev=vcsService.vcsManager.updateModel(new File(model.vcsIdentifier), importMe, null)
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
    
    
