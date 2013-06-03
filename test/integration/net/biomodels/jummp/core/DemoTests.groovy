package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.model.Model
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand


class DemoTests extends JummpIntegrationTest{
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

    @Test
    void testBatchImport() {
        final File target = new File("/home/mglont/jummp/models/curated")

        authenticateAsAdmin()
        target.eachFile {
            ModelTransportCommand meta = new ModelTransportCommand(comment:
                "model import test", name: it.name, format: new ModelFormatTransportCommand(identifier: "SBML"))
            File tempFile = File.createTempFile("metadata", ".txt")
            tempFile.setText("Metadata for ${it.name}\n")
            modelService.uploadModelAsList([it,tempFile], meta)
        }
    }
}
