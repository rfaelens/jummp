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

class OmexServiceTests extends JummpIntegrationTest {

    def fileSystemService
    def modelService
    def omexService
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
        fileSystemService.root = new File("target/omex/git/").getCanonicalFile()
        fileSystemService.currentModelContainer = fileSystemService.root.absolutePath + "/aaa/"
   }

    @After
    void tearDown() {
        fileSystemService.root = defaultRootModelDir
        grailsApplication.config.jummp.vcs.workingDirectory  = defaultWorkingDir
        grailsApplication.config.jummp.vcs.exchangeDirectory = defaultExchangeDir
        modelService.vcsService.vcsManager = defaultVcsManager
        FileUtils.deleteQuietly(new File("target/omex/"))
    }

    @Test
    void testAreFilesThisFormat() {
        assertFalse omexService.areFilesThisFormat([null])
        def modelFile = new File("target/omex/exchange/unknown.txt")
        FileUtils.touch modelFile
        modelFile.setText("I am the main entry of this model submission.")
        assertFalse omexService.areFilesThisFormat([modelFile])
        modelFile = new File("jummp-plugins/jummp-plugin-combine-archive/test/files/sample.omex")
        assertTrue omexService.areFilesThisFormat([modelFile])
    }

    @Test
    void testGetVersionFormat() {
        def modelFile = new File("jummp-plugins/jummp-plugin-combine-archive/test/files/sample.omex")
        authenticateAsTestUser()
        def rf = new RepositoryFileTransportCommand(path: modelFile.absolutePath,
                    description: "A very interesting model.", mainFile: true)
        def defaultFormat = new ModelFormatTransportCommand(identifier: "OMEX")
        def modelCommand = new ModelTransportCommand(format: defaultFormat, comment: "First commit", name: "Foo")
        Model model = modelService.uploadModelAsFile(rf, modelCommand)
        def revision = modelService.getLatestRevision(model).toCommandObject()
        assertEquals "0.1", omexService.getFormatVersion(revision)
    }

    private void setupVcs() {
        // setup VCS
        File clone = new File("target/omex/git/")
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
        grailsApplication.config.jummp.vcs.workingDirectory = "target/omex/git/"
        File exchangeDir = new File("target/omex/exchange/")
        exchangeDir.mkdirs()
        grailsApplication.config.jummp.vcs.exchangeDirectory = exchangeDir.path
        modelService.vcsService.vcsManager = gitService.getInstance()
    }
}
