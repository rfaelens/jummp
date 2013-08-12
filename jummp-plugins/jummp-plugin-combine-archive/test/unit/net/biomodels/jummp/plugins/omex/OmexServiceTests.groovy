package net.biomodels.jummp.plugins.omex

import grails.test.mixin.*
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import org.apache.commons.io.FileUtils
import org.junit.*

@TestFor(OmexService)
class OmexServiceTests {

    @Test
    void testValidation() {
        def omexService = new OmexService()
        assertFalse(omexService.validate(null))
        assertFalse(omexService.validate([]))
        assertFalse(omexService.validate([new File("inexistent")]))
        def randomFile = new File("target/misc.txt")
        FileUtils.touch(randomFile)
        randomFile.setText("Hello")
        assertTrue randomFile.exists()
        assertFalse omexService.validate([randomFile])
        def omexFile = new File("test/files/sample.omex")
        assertTrue omexService.validate([omexFile])
    }

    @Test
    void testExtractName() {
        def omexService = new OmexService()
        List<File> modelFiles = null
        assertEquals("", omexService.extractName(modelFiles))
        modelFiles = [new File("test/files/sample.omex")]
        assertEquals("", omexService.extractName(modelFiles))
    }

    @Test
    void testExtractDescription() {
        def omexService = new OmexService()
        List<File> modelFiles = null
        assertEquals("", omexService.extractDescription(modelFiles))
        modelFiles = [new File("test/files/sample.omex")]
        assertEquals("", omexService.extractDescription(modelFiles))
    }

    @Test
    void testExtractAnno() {
        def omexService = new OmexService()
        assertEquals([], omexService.getAllAnnotationURNs(null))
        def omexFormat = new ModelFormatTransportCommand(identifier: "OMEX",
                name: "Open Modelling Exchange Format")
        def file = new RepositoryFileTransportCommand(path: "test/files/sample.omex",
                mainFile: true, hidden: false, userSubmitted: true)
        def revision = new RevisionTransportCommand(format: omexFormat, files: [file])

        assertEquals([], omexService.getAllAnnotationURNs(revision))
    }

    @Test
    void testGetPublicationAnno() {
        def omexService = new OmexService()
        assertEquals([], omexService.getPubMedAnnotation(null))
        def omexFormat = new ModelFormatTransportCommand(identifier: "OMEX",
                name: "Open Modelling Exchange Format")
        def file = new RepositoryFileTransportCommand(path: "test/files/sample.omex",
                mainFile: true, hidden: false, userSubmitted: true)
        def revision = new RevisionTransportCommand(format: omexFormat, files: [file])

        assertEquals([], omexService.getPubMedAnnotation(revision))
    }
}
