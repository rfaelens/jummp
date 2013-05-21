package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import org.apache.commons.io.FileUtils

class FileSystemServiceTests {
    def fileSystemService
    def grailsApplication
    def parentLocation

    @Override
    @Before
    void setUp() {
        parentLocation = new File("target/workingDirectory")
        parentLocation.mkdir()
        fileSystemService.root = parentLocation
        fileSystemService.currentModelContainer = parentLocation.absolutePath+File.separator+"ttt"
        fileSystemService.maxContainerSize = 10
        println "root set to ${fileSystemService.root}"
        }

    @Override
    @After
    void tearDown() {
        //FileUtils.deleteQuietly(parentLocation)
    }

    @Test
    void testCreateParent() {
        assertTrue(parentLocation.exists())
    }

    @Test
    void testContainerPatterns() {
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttt"))
        mockModelFolders(11)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttu"))
        mockModelFolders(10)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttv"))
    }


    private void mockModelFolders(final int count) {
        final String SEP = File.separator
        count.times { it ->
            StringBuilder sb = new StringBuilder(fileSystemService.findCurrentModelContainer())
            sb.append(SEP).append("model")
            String f = "${sb.toString()}${String.format("%04d", it+1)}"
            File newModel = new File(f)
            if (newModel.exists()) {
                final int modelNumber = f.split("model")[1] as int
                f.replace(modelNumber as String, (modelNumber+1000) as String)
            }
            File m = new File(f)
            boolean result = m.mkdirs()
            println "${m.absolutePath} ${result}"
        }
    }
}
