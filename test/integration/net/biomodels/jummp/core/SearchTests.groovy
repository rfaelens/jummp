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
 * Apache Commons, JUnit (or a modified version of that library), containing parts
 * covered by the terms of Common Public License, Apache License v2.0, the licensors of this
 * Program grant you additional permission to convey the resulting work.
 * {Corresponding Source for a non-source form of such a combination shall
 * include the source code for the parts of Apache Commons, JUnit used as well as
 * that of the covered work.}
 **/

package net.biomodels.jummp.core

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import net.biomodels.jummp.annotationstore.ElementAnnotation
import net.biomodels.jummp.annotationstore.Qualifier
import net.biomodels.jummp.annotationstore.ResourceReference
import net.biomodels.jummp.annotationstore.Statement
import net.biomodels.jummp.core.adapters.DomainAdapter
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.ValidationState
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.model.RepositoryFile
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.junit.*
import static org.junit.Assert.*

@TestMixin(IntegrationTestMixin)
class SearchTests extends JummpIntegrationTest {
    def searchService
    def modelService
    def fileSystemService
    def grailsApplication
    def solrServerHolder

    @Test
    void testGetLatestRevision() {
        // generate unique ids for the name and description
        String nameTag = "testModel"
        String descriptionTag = "test description"
        authenticateAsTestUser()
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.plugins.sbml.validation = false
        modelService.vcsService.vcsManager = gitService.getInstance()
        modelService.vcsService.vcsManager.exchangeDirectory = new File('target/vcs/exchange')
        // upload the model
        def rf = new RepositoryFileTransportCommand(path:
                smallModel("importModel.xml", nameTag, descriptionTag).absolutePath,
                mainFile: true, description: "")
        Model upped = modelService.uploadModelAsFile(rf, new ModelTransportCommand(format:
                new ModelFormatTransportCommand(identifier: "PharmML"), comment: "test", name: "Test"))
        //wait a bit for the model to be indexed
        Thread.sleep(25000)
        // Search for the model using the name and description, and ensure it's the same we uploaded
        ModelTransportCommand result = searchForModel(nameTag)
        assertNotNull result
        assertEquals(upped.id, result.id)
        result = searchForModel(descriptionTag)
        assertNotNull result
        assertEquals(upped.id, result.id)
        result = searchForModel(upped.submissionId)
        assertNotNull result
        assertEquals(upped.id, result.id)
        result = searchForModel("submissionId:${upped.submissionId}")
        assertNotNull result
        assertEquals(upped.id, result.id)
        result = searchForModel("pharmml")
        assertNotNull result
        assertEquals(upped.id, result.id)
    }

    @Test
    void testIndexRegenerationResetsTheDatabaseAnnotations() {
        // given a model revision with one annotation
        def model = new Model(submissionId: 'MODEL123', vcsIdentifier: 'aaa/model1')
        def rev = new Revision(name: 'model v1', description: 'sample model snapshot', comment:
                'please import me', uploadDate: new Date(), validated: true, owner: User.first(),
                revisionNumber: 1, validationState: ValidationState.APPROVE, format:
                ModelFormat.first(), minorRevision: false, vcsId: 'sha256hash', model: model)
        def rf = new RepositoryFile(path: '/nowhere', mainFile: true, description: 'not found',
                mimeType: 'text/plain', hidden: false, userSubmitted: true)
        rev.addToRepoFiles rf
        model.addToRevisions(rev)
        assertNotNull model.save()
        assertFalse model.hasErrors()

        def p = new Qualifier(accession: 'hasName', uri: 'http://example.com/hasName',
                qualifierType: 'UNKNOWN')
        assertNotNull p.save()
        assertFalse p.hasErrors()

        def o = new ResourceReference(datatype: 'foo', collectionName: 'bar', name: 'baz', uri:
                'http://ddg.gg/bar/baz', accession: 'baz', shortName: 'baz')
        assertNotNull o.save()
        assertFalse o.hasErrors()

        def stmt = new Statement(subjectId: 'someone', qualifier: p, object: o, )
        def ea   = new ElementAnnotation(statement: stmt, creatorId: 'notMe', revision: rev)
        assertNotNull ea.save()
        assertFalse ea.hasErrors()
        assertEquals 1, Statement.count()
        assertEquals 1, ElementAnnotation.count()
        assertEquals 1, ResourceReference.count()
        assertEquals 1, Qualifier.count()

        // when we delete all element annotations and statements before re-indexing
        searchService.clearAnnotationStatementsFromDatabase()

        // we start with a clean database (the qualifiers and xrefs are preserved)
        assertEquals 0, ElementAnnotation.count()
        assertEquals 0, Statement.count()
        assertEquals 1, Qualifier.count()
        assertEquals 1, ResourceReference.count()
    }

    @Before
    void setUp() {
        def container = new File("target/vcs/git/ggg/")
        container.mkdirs()
        new File("target/vcs/exchange/").mkdirs()
        fileSystemService.currentModelContainer = container.getCanonicalPath()
        fileSystemService.root = container.getParentFile()
        modelService.vcsService.modelContainerRoot = fileSystemService.root
        createUserAndRoles()
        assertNotNull solrServerHolder.server
    }

    @After
     void tearDown() {
        try {
            FileUtils.deleteDirectory(new File("target/vcs/git"))
            FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        } catch(Exception ignore) {
        }
        modelService.vcsService.vcsManager = null
        solrServerHolder.server.deleteByQuery("*:*")
        solrServerHolder.server.commit()
    }

    ModelTransportCommand searchForModel(String query) {
        Collection<ModelTransportCommand> mods = searchService.searchModels(query)
        if (mods.isEmpty()) {
            return null
        }
        return mods.first()
    }

    // Convenience functions follow..
    private File smallModel(String filename, String id, String desc) {
        return new File("test/files/test.xml")
    }

    private File getFileForTest(String filename, String text) {
        def tempDir = FileUtils.getTempDirectory()
        def testFile = new File(tempDir.absolutePath + File.separator + filename)
        if (text) {
            testFile.setText(text)
        }
        return testFile
    }
}
