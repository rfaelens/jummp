package net.biomodels.jummp.core

import static org.junit.Assert.*
import java.util.UUID
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
        }

    @Override
    @After
    void tearDown() {
        FileUtils.deleteQuietly(parentLocation)
    }

    @Test
    void testCreateParent() {
        assertTrue(parentLocation.exists())
    }

    @Test
    void testContainerPatterns() {
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttt"))
        mockModelFolders(1)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttt"))
        mockModelFolders(10)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttu"))
        mockModelFolders(9)
        assertTrue(fileSystemService.findCurrentModelContainer().endsWith("ttv"))
    }


    private void mockModelFolders(final int count) {
        String modelSuffix
        count.times { it ->
            StringBuilder sb = new StringBuilder(fileSystemService.findCurrentModelContainer())
            sb.append(File.separator).append(UUID.randomUUID().toString())
            File m = new File(sb.toString())
            boolean result = m.mkdirs()
        }
    }
}
