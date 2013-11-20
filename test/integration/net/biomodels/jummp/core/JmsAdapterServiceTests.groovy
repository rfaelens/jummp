/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
* JGit, Apache Commons, Spring Framework, JUnit, Spring Security 
* (or a modified version of that library), containing parts
* covered by the terms of Common Public License, Apache License v2.0, 
* Eclipse Distribution License v1.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JGit, Apache Commons, Spring Framework,
* JUnit, Spring Security used as well as that of the covered work.}
**/





package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.domain.BasePermission

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import org.springframework.security.core.AuthenticationException

import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.user.JummpAuthentication
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import java.util.concurrent.Callable

@TestMixin(IntegrationTestMixin)
class JmsAdapterServiceTests extends JummpIntegrationTest {
    def aclUtilService
    def modelService
    def applicationJmsAdapterService
    def modelJmsAdapterService
    def grailsApplication
    /**
     * Dependency injection for ExecutorService to run threads
     */
    def executorService
    @Before
    void setUp() {
        createUserAndRoles()
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        modelService.vcsService.vcsManager = null
    }

    @Ignore
    @Test
    void testAuthenticate() {
        // test wrong parameter
        def illegalArgumentException = send2("authenticate", "test")
        assertNotNull(illegalArgumentException)
        assertTrue(illegalArgumentException instanceof IllegalArgumentException)
        // test an invalid authentication
        def exception = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "wrongpassword"))
        assertNotNull(exception)
        assertTrue(exception instanceof AuthenticationException)
        assertTrue(exception instanceof BadCredentialsException)
        // test not existing user
        def exception2 = send2("authenticate", new UsernamePasswordAuthenticationToken("foo", ""))
        assertNotNull(exception2)
        assertTrue(exception2 instanceof AuthenticationException)
        assertTrue(exception2 instanceof BadCredentialsException)
        // test a valid authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
    }

    @Ignore
    @Test
    void testGetAllModels() {
        // invalid parameter should end in IllegalArgumentException
        assertTrue(send("getAllModels", "test") instanceof IllegalArgumentException)
        // use an anonymous authentication
        def auth = "anonymous"
        modelAdminUser(false)
        def returnList = send("getAllModels", [auth])
        assertTrue(returnList instanceof List)
        assertTrue(returnList.isEmpty())
        // create a model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // grant read right to testuser
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        // anonymous user should not get any models
        returnList = send("getAllModels", [auth])
        assertTrue(returnList instanceof List)
        assertTrue(returnList.isEmpty())
        // authenticate as testuser
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        // testuser should see the model
        returnList = send("getAllModels", [auth2.getAuthenticationHash()])
        assertTrue(returnList instanceof List)
        assertFalse(returnList.isEmpty())
        assertTrue(returnList[0] instanceof ModelTransportCommand)
        assertEquals(model.id, returnList[0].id)
        // let's try the different variants of getAllModels
        returnList = send("getAllModels", [auth2.getAuthenticationHash(), 0, 10])
        assertTrue(returnList instanceof List)
        assertFalse(returnList.isEmpty())
        assertTrue(returnList[0] instanceof ModelTransportCommand)
        assertEquals(model.id, returnList[0].id)
        returnList = send("getAllModels", [auth2.getAuthenticationHash(), 0, 10, false])
        assertTrue(returnList instanceof List)
        assertFalse(returnList.isEmpty())
        assertTrue(returnList[0] instanceof ModelTransportCommand)
        assertEquals(model.id, returnList[0].id)
        // switch to user
        def auth3 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth3)
        assertTrue(auth3 instanceof JummpAuthentication)
        // should not see the model
        returnList = send("getAllModels", [auth3.getAuthenticationHash()])
        assertTrue(returnList instanceof List)
        assertTrue(returnList.isEmpty())
        // test other variants of IllegalArgumentExceptions
        assertTrue(send("getAllModels", ["test"]) instanceof AuthenticationHashNotFoundException)
        assertTrue(send("getAllModels", [0]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3.getAuthenticationHash(), "test", "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3.getAuthenticationHash(), 0]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3.getAuthenticationHash(), 0, 0, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3.getAuthenticationHash(), 0, 0, true, "test"]) instanceof IllegalArgumentException)
    }

    @Ignore
    @Test
    void testModelCount() {
        // first test without authentication
        assertTrue(send("getModelCount", ["test"]) instanceof IllegalArgumentException)
        assertTrue(send("getModelCount", "test") instanceof AuthenticationHashNotFoundException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        def result = send("getModelCount", auth.getAuthenticationHash())
        assertTrue(result instanceof Integer)
        assertEquals(0, result)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // it's still 0
        result = send("getModelCount", auth.getAuthenticationHash())
        assertTrue(result instanceof Integer)
        assertEquals(0, result)
        // grant read right to testuser
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        // now testuser should have 1
        result = send("getModelCount", auth.getAuthenticationHash())
        assertTrue(result instanceof Integer)
        assertEquals(1, result)
        auth = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        result = send("getModelCount", auth.getAuthenticationHash())
        assertTrue(result instanceof Integer)
        assertEquals(0, result)
    }

    @Ignore
    @Test
    void testGetLatestRevision() {
        // first test a completely invalid variant
        assertTrue(send("getLatestRevision", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testuser should get an AccessDeniedException
        assertTrue(send("getLatestRevision", [auth.getAuthenticationHash(), model.id]) instanceof AccessDeniedException)
        // add permission to Revision
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        // now the testuser should see the revision
        def result = send("getLatestRevision", [auth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof RevisionTransportCommand)
        assertEquals(revision.id, result.id)
        // some IllegalArgumentExceptions
        assertTrue(send("getLatestRevision", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("getLatestRevision", [auth.getAuthenticationHash(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getLatestRevision", [auth.getAuthenticationHash(), model.id, "test"]) instanceof IllegalArgumentException)
        // user should get an AccessDeniedException
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        assertTrue(send("getLatestRevision", [auth2.getAuthenticationHash(), model.id]) instanceof AccessDeniedException)
    }

    @Ignore
    @Test
    void testGetAllRevisions() {
        // first test a completely invalid variant
        assertTrue(send("getAllRevisions", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testuser should get an empty list
        def result = send("getAllRevisions", [auth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof List)
        assertTrue(result.isEmpty())
        // add permission to Revision
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        result = send("getAllRevisions", [auth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof List)
        assertFalse(result.isEmpty())
        assertEquals(1, result.size())
        assertEquals(revision.id, result[0].id)
        // some invalid argument exceptions
        assertTrue(send("getAllRevisions", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("getAllRevisions", [auth.getAuthenticationHash(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllRevisions", [auth.getAuthenticationHash(), model.id, "test"]) instanceof IllegalArgumentException)
        // user should get an empty List
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        result = send("getAllRevisions", [auth2.getAuthenticationHash(), model.id])
        assertTrue(result instanceof List)
        assertTrue(result.isEmpty())
    }

    @Ignore
    @Test
    void testUploadModel() {
        // first test a completely invalid variant
        assertTrue(send("uploadModel", "test") instanceof IllegalArgumentException)
        // setup VCS
        setupVcs()
        // try uploading a valid model
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
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
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "upload", format: new ModelFormatTransportCommand(identifier: "SBML"))
        def result = send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes, meta])
        assertTrue(result instanceof ModelTransportCommand)
        // uploading the same again should render a ModelException
        assertTrue(send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes, meta]) instanceof ModelException)
        // test illegal argument exceptions
        assertTrue(send("uploadModel", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth.getAuthenticationHash(), "test", meta]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes, "Test"]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes, meta, "test"]) instanceof IllegalArgumentException)
        // test anonymous authentication - should end in AccessDeniedException
        def anonAuth = "anonymous"
        assertTrue(send("uploadModel", [anonAuth, modelSource.bytes, meta]) instanceof AccessDeniedException)
        
        // need to delete the ACL or following tests will fail
        modelAdminUser(true)
        authenticateAsAdmin()
        aclUtilService.deletePermission(Model, result.id, "testuser", BasePermission.READ)
        def revision = send("getLatestRevision", [auth.getAuthenticationHash(), result.id])
        assertTrue(revision instanceof RevisionTransportCommand)
        aclUtilService.deletePermission(Revision, revision.id, "testuser", BasePermission.READ)
        authenticateAnonymous()
        modelAdminUser(false)
        // cleanup
        assertTrue(send("deleteModel", [auth.getAuthenticationHash(), result.id as Long]))
    }

    @Ignore
    @Test
    void testAddRevision() {
        // first test a completely invalid variant
        assertTrue(send("addRevision", "test") instanceof IllegalArgumentException)
        // setup VCS
        setupVcs()
        // try uploading a valid model
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
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
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "addRevision", format: new ModelFormatTransportCommand(identifier: "SBML"))
        def model = send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes, meta])
        assertTrue(model instanceof ModelTransportCommand)
        // test uploading a new Revision
        def result = send("addRevision", [auth.getAuthenticationHash(), model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment"])
        assertTrue(result instanceof RevisionTransportCommand)
        // other user should get an AccessDeniedException
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        assertTrue(send("addRevision", [auth2.getAuthenticationHash(), model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment"]) instanceof AccessDeniedException)
        // a deleted model has to end in ModelException
        send("deleteModel", [auth.getAuthenticationHash(), model.id])
        assertTrue(send("addRevision", [auth.getAuthenticationHash(), model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment"]) instanceof ModelException)
        // illegal arguments exceptions
        assertTrue(send("addRevision", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth.getAuthenticationHash(), model.id]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth.getAuthenticationHash(), model.id, modelSource.bytes]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth.getAuthenticationHash(), model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML")]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth.getAuthenticationHash(), model.id, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment", "Test"]) instanceof IllegalArgumentException)

        // need to delete the ACL or following tests will fail
        modelAdminUser(true)
        authenticateAsAdmin()
        aclUtilService.deletePermission(Model, model.id, "testuser", BasePermission.READ)
        authenticateAnonymous()
        modelAdminUser(false)
    }

    @Ignore
    @Test
    void testRetrieveModelFiles() {
        // first test a completely invalid variant
        assertTrue(send("retrieveModelFiles", "test") instanceof IllegalArgumentException)
        // setup VCS
        setupVcs()
        // try uploading a valid model
        String modelSource = '''<?xml version="1.0" encoding="UTF-8"?>
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
      </reaction>
    </listOfReactions>
  </model>
</sbml>'''
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // upload the model
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "retrieveFile", format: new ModelFormatTransportCommand(identifier: "SBML"))
        def model = send("uploadModel", [auth.getAuthenticationHash(), modelSource.bytes, meta])
        assertTrue(model instanceof ModelTransportCommand)
        // get the latest revision
        def revision = send("getLatestRevision", [auth.getAuthenticationHash(), model.id])
        assertTrue(revision instanceof RevisionTransportCommand)
        // get the file
        def result = send("retrieveModelFiles", [auth.getAuthenticationHash(), revision])
        assertTrue(result instanceof byte[])
        assertEquals(modelSource.bytes.toString(), result.toString())
        // other user should get an AccessDeniedException
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        assertTrue(send("retrieveModelFiles", [auth2.getAuthenticationHash(), revision]) instanceof AccessDeniedException)

        // test illegal argument exceptions
        assertTrue(send("retrieveModelFiles", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("retrieveModelFiles", [auth.getAuthenticationHash(), revision, "Test"]) instanceof IllegalArgumentException)

        // need to delete the ACL or following tests will fail
        modelAdminUser(true)
        authenticateAsAdmin()
        aclUtilService.deletePermission(Model, model.id, "testuser", BasePermission.READ)
        aclUtilService.deletePermission(Revision, revision.id, "testuser", BasePermission.READ)
        authenticateAnonymous()
        modelAdminUser(false)

        // cleanup
        assertTrue(send("deleteModel", [auth.getAuthenticationHash(), model.id as Long]))
    }

    @Ignore
    @Test
    void testGrantReadAccess() {
        // first test a completely invalid variant
        assertTrue(send("grantReadAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testuser does not have access to any models
        assertEquals(0, send("getModelCount", auth.getAuthenticationHash()))
        // create an admin Authentication
        def adminAuth = send2("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertNotNull(adminAuth)
        assertTrue(adminAuth instanceof JummpAuthentication)
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        def result = send("grantReadAccess", [adminAuth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // testuser does have access to the model
        assertEquals(1, send("getModelCount", auth.getAuthenticationHash()))
        // test some illegal argument  exceptions
        assertTrue(send("grantReadAccess", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("grantReadAccess", [auth.getAuthenticationHash(), model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("grantReadAccess", [auth.getAuthenticationHash(), model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("grantReadAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // test access denied exception
        assertTrue(send("grantReadAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("username")]) instanceof AccessDeniedException)
        // user should not have access to it
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        assertEquals(0, send("getModelCount", auth2.getAuthenticationHash()))
    }

    @Ignore
    @Test
    void testGrantWriteAccess() {
        // first test a completely invalid variant
        assertTrue(send("grantWriteAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // create an admin Authentication
        def adminAuth = send2("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertNotNull(adminAuth)
        assertTrue(adminAuth instanceof JummpAuthentication)
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // grant the right
        def result = send("grantWriteAccess", [adminAuth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // testuser should now have the write
        // TODO: do an actual test by trying to upload
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // user should not be allowed to grant right
        assertTrue(send("grantWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("username")]) instanceof AccessDeniedException)
        // test some illegal argument  exceptions
        assertTrue(send("grantWriteAccess", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("grantWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("grantWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("grantWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // user should not have access to it
        def auth2 = send2("authenticate", new UsernamePasswordAuthenticationToken("username", "verysecret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof JummpAuthentication)
        result = send("addRevision", [auth2.getAuthenticationHash(), model.id, new byte[0], new ModelFormatTransportCommand(identifier: "UNKNOWN"), "no test"])
        assertTrue(result instanceof AccessDeniedException)
    }

    @Ignore
    @Test
    void testRevokeReadAccess() {
        // first test a completely invalid variant
        assertTrue(send("revokeReadAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // grant read right to testuser
        authenticateAsUser()
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        assertEquals(1, send("getModelCount", auth.getAuthenticationHash()))
        // create an admin Authentication
        def adminAuth = send2("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertNotNull(adminAuth)
        assertTrue(adminAuth instanceof JummpAuthentication)
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // revoke the right
        def result = send("revokeReadAccess", [adminAuth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // user should not see the model any more
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertTrue(send("revokeReadAccess", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("revokeReadAccess", [auth.getAuthenticationHash(), model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("revokeReadAccess", [auth.getAuthenticationHash(), model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("revokeReadAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // revoking read access is not allowed for testuser
        assertTrue(send("revokeReadAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("username")]) instanceof AccessDeniedException)
    }

    @Ignore
    @Test
    void testRevokeWriteAccess() {
        // first test a completely invalid variant
        assertTrue(send("revokeWriteAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // grant read right to testuser
        authenticateAsUser()
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        aclUtilService.addPermission(model, "testuser", BasePermission.WRITE)
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // create an admin Authentication
        def adminAuth = send2("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertNotNull(adminAuth)
        assertTrue(adminAuth instanceof JummpAuthentication)
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // revoke the right
        def result = send("revokeWriteAccess", [adminAuth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // user should not have write access any more
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        assertTrue(send("revokeWriteAccess", [auth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("revokeWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("revokeWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("revokeWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // revoking write access is not allowed for testuser
        assertTrue(send("revokeWriteAccess", [auth.getAuthenticationHash(), model.toCommandObject(), User.findByUsername("username")]) instanceof AccessDeniedException)
    }

    @Ignore
    @Test
    void testDeleteRestoreModel() {
        // first test a completely invalid variant
        assertTrue(send("deleteModel", "test") instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", "test") instanceof IllegalArgumentException)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // create admin authentication
        def adminAuth = send2("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertNotNull(adminAuth)
        assertTrue(adminAuth instanceof JummpAuthentication)
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // delete the model
        def result = send("deleteModel", [adminAuth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        assertEquals(ModelState.DELETED, model.refresh().state)
        // delete again should not work
        result = send("deleteModel", [adminAuth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof Boolean)
        assertFalse(result)
        // restore
        result = send("restoreModel", [adminAuth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        assertEquals(ModelState.UNPUBLISHED, model.refresh().state)
        // restore again should not work
        result = send("restoreModel", [adminAuth.getAuthenticationHash(), model.id])
        assertTrue(result instanceof Boolean)
        assertFalse(result)
        modelAdminUser(false)
        // create an authentication
        def auth = send2("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof JummpAuthentication)
        // user should not be able to delete/restore Model
        assertTrue(send("deleteModel", [auth.getAuthenticationHash(), model.id]) instanceof AccessDeniedException)
        assertTrue(send("restoreModel", [auth.getAuthenticationHash(), model.id]) instanceof AccessDeniedException)
        // try illegal arguments
        assertTrue(send("deleteModel", [adminAuth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("deleteModel", [adminAuth.getAuthenticationHash(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("deleteModel", [adminAuth.getAuthenticationHash(), model.id, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", [adminAuth.getAuthenticationHash()]) instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", [adminAuth.getAuthenticationHash(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", [adminAuth.getAuthenticationHash(), model.id, "test"]) instanceof IllegalArgumentException)

    }

    private def send2(String method, def message) {
        def future =  executorService.submit(new Callable() {
            def call() {
            return applicationJmsAdapterService."${method}"(message)
        }})
        return future.get()
    }

    private def send(String method, def message) {
        def future = executorService.submit(new Callable() {
            def call() {
            return modelJmsAdapterService."${method}"(message)
        }})
        return future.get()
    }

    private void setupVcs() {
        File clone = new File("target/vcs/git")
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
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        modelService.vcsService.vcsManager = gitService.getInstance()
        assertTrue(modelService.vcsService.isValid())
    }
}
