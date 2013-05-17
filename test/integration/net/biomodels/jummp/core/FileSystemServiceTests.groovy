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
}
