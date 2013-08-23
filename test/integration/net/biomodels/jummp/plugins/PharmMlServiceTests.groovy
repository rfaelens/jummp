package net.biomodels.jummp.plugins

import net.biomodels.jummp.core.JummpIntegrationTest
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitManagerFactory
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.*
import static org.junit.Assert.*

class PharmMlServiceTests extends JummpIntegrationTest {

    def fileSystemService
    def modelService
    def pharmMlService
    def grailsApplication

    private File defaultRootModelDir
    private String defaultWorkingDir
    private String defaultExchangeDir
    private def defaultVcsManager

    @Before
    void setUp() {
        defaultRootModelDir = fileSystemService.root
        defaultWorkingDir   = grailsApplication.config.jummp.vcs.workingDirectory
        defaultExchangeDir  = grailsApplication.config.jummp.vcs.exchangeDirectory
        defaultVcsManager   = modelService.vcsService.vcsManager
        createUserAndRoles()
        setupVcs()
        fileSystemService.root = new File("target/pharmml/git/").getCanonicalFile()
        fileSystemService.currentModelContainer = fileSystemService.root.absolutePath + "/aaa/"
   }

    @After
    void tearDown() {
        fileSystemService.root = defaultRootModelDir
        grailsApplication.config.jummp.vcs.workingDirectory  = defaultWorkingDir
        grailsApplication.config.jummp.vcs.exchangeDirectory = defaultExchangeDir
        modelService.vcsService.vcsManager = defaultVcsManager
        FileUtils.deleteDirectory(new File("target/pharmml/"))
    }

    @Test
    void testAreFilesThisFormat() {
        assertFalse pharmMlService.areFilesThisFormat([null])
        def modelFile = new File("target/pharmml/exchange/unknown.txt")
        FileUtils.touch modelFile
        modelFile.setText("I am the main entry of this model submission.")
        assertFalse pharmMlService.areFilesThisFormat([modelFile])
        modelFile = new File("jummp-plugins/jummp-plugin-pharmml/test/files/example2.xml")
        assertTrue pharmMlService.areFilesThisFormat([modelFile])
        modelFile = new File("test/files/BIOMD0000000272.xml")
        assertFalse pharmMlService.areFilesThisFormat([modelFile])
    }

    @Test
    void testGetVersionFormat() {
        def modelFile = new File("jummp-plugins/jummp-plugin-pharmml/test/files/example2.xml")
        authenticateAsTestUser()
        def rf = new RepositoryFileTransportCommand(path: modelFile.absolutePath,
                    description: "A very interesting model.", mainFile: true)
        def defaultFormat = new ModelFormatTransportCommand(identifier: "PharmML")
        def modelCommand = new ModelTransportCommand(format: defaultFormat, comment: "First commit", name: "Foo")
        Model model = modelService.uploadModelAsFile(rf, modelCommand)
        def revision = modelService.getLatestRevision(model).toCommandObject()
        assertEquals "0.1", pharmMlService.getFormatVersion(revision)
    }

    @Test
    void testValidate() {
        def modelFile = new File("jummp-plugins/jummp-plugin-pharmml/test/files/example6.xml")
        authenticateAsTestUser()
        def rf = new RepositoryFileTransportCommand(path: modelFile.absolutePath,
                    description: "A very interesting model.", mainFile: true)
        def defaultFormat = new ModelFormatTransportCommand(identifier: "PharmML")
        def modelCommand = new ModelTransportCommand(format: defaultFormat, comment: "First commit", name: "Foo")
        Model model = modelService.uploadModelAsFile(rf, modelCommand)
        def revision = modelService.getLatestRevision(model).toCommandObject()
        List<String> locations = []
        revision.files?.findAll{it.mainFile}.each { locations << it.path}
        List<File> files = []
        locations.each { l ->
            File f = new File(l)
            if (f && f.exists() && f.canRead()) {
                files << f
            }
        }
        assertTrue !!files
        assertTrue pharmMlService.validate(files)
    }

    private void setupVcs() {
        // setup VCS
        File clone = new File("target/pharmml/git/")
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.workingDirectory = "target/pharmml/git/"
        File exchangeDir = new File("target/pharmml/exchange/")
        exchangeDir.mkdirs()
        grailsApplication.config.jummp.vcs.exchangeDirectory = exchangeDir.path
        modelService.vcsService.vcsManager = gitService.getInstance()
    }
}
