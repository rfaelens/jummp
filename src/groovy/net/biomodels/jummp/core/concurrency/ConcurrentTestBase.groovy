package net.biomodels.jummp.core.concurrency
import edu.umd.cs.mtc.MultithreadedTestCase
import edu.umd.cs.mtc.TestFramework
import net.biomodels.jummp.plugins.git.GitManagerFactory
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.util.concurrent.atomic.AtomicInteger
import static org.junit.Assert.*;
import net.biomodels.jummp.core.JummpIntegrationTest


public class ConcurrentTestBase extends MultithreadedTestCase {

    protected File smallModel(String name, String text) {

        File nonModel=new File("target/vcs/exchange/"+name)
        nonModel.setText(text)
        return nonModel
    }
    
    protected File sbmlModel()
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

    protected void testRepositoryCommit(Repository repository, String rev)
    {
        ObjectId commit = repository.resolve(Constants.HEAD)
        RevWalk revWalk = new RevWalk(repository)
        RevCommit revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
    }
        
    protected void testFileCorrectness(String modelDirectory, String filename, String filetext)
    {
        File testFile=new File(modelDirectory + "/" + filename);
        List<String> lines = testFile.readLines()
        assertEquals(1, lines.size())
        assertEquals(lines[0],filetext)
    }
    
    protected void testFileCorrectness(List<File> files, String filename, String filetext)
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
        
    protected void deleteFile(File file) {
            try
            {
                FileUtils.forceDelete(file)
            }
            catch(Exception logMe) {
                log.error(logMe.getMessage())
            }
    } 
}
