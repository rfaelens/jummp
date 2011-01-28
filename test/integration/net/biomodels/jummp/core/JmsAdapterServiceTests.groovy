package net.biomodels.jummp.core

import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitService
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.GrantedAuthorityImpl
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand

class JmsAdapterServiceTests extends JummpIntegrationTestCase {
    def aclUtilService
    def modelService
    def jmsSynchronousService
    protected void setUp() {
        super.setUp()
        createUserAndRoles()
        mockLogging(ModelService)
        mockLogging(JmsAdapterService)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        modelService.vcsService.vcsManager = null
    }

    void testAuthenticate() {
        // test wrong parameter
        def illegalArgumentException = send("authenticate", "test")
        assertNotNull(illegalArgumentException)
        assertTrue(illegalArgumentException instanceof IllegalArgumentException)
        // test an invalid authentication
        def exception = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "wrongpassword"))
        assertNotNull(exception)
        assertTrue(exception instanceof AuthenticationException)
        assertTrue(exception instanceof BadCredentialsException)
        // test not existing user
        def exception2 = send("authenticate", new UsernamePasswordAuthenticationToken("foo", ""))
        assertNotNull(exception2)
        assertTrue(exception2 instanceof AuthenticationException)
        assertTrue(exception2 instanceof BadCredentialsException)
        // test a valid authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth)
        assertTrue(auth instanceof Authentication)
        assertTrue(auth.isAuthenticated())
    }

    void testGetAllModels() {
        // invalid parameter should end in IllegalArgumentException
        assertTrue(send("getAllModels", "test") instanceof IllegalArgumentException)
        // use an anonymous authentication
        def auth = new AnonymousAuthenticationToken("test", "Anonymous", [ new GrantedAuthorityImpl("ROLE_ANONYMOUS")])
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
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        assertNotNull(auth2)
        assertTrue(auth2 instanceof Authentication)
        assertTrue(auth2.isAuthenticated())
        // testuser should see the model
        returnList = send("getAllModels", [auth2])
        assertTrue(returnList instanceof List)
        assertFalse(returnList.isEmpty())
        assertTrue(returnList[0] instanceof ModelTransportCommand)
        assertEquals(model.id, returnList[0].id)
        // let's try the different variants of getAllModels
        returnList = send("getAllModels", [auth2, 0, 10])
        assertTrue(returnList instanceof List)
        assertFalse(returnList.isEmpty())
        assertTrue(returnList[0] instanceof ModelTransportCommand)
        assertEquals(model.id, returnList[0].id)
        returnList = send("getAllModels", [auth2, 0, 10, false])
        assertTrue(returnList instanceof List)
        assertFalse(returnList.isEmpty())
        assertTrue(returnList[0] instanceof ModelTransportCommand)
        assertEquals(model.id, returnList[0].id)
        // switch to user
        def auth3 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        assertNotNull(auth3)
        assertTrue(auth3 instanceof Authentication)
        assertTrue(auth3.isAuthenticated())
        // should not see the model
        returnList = send("getAllModels", [auth3])
        assertTrue(returnList instanceof List)
        assertTrue(returnList.isEmpty())
        // test other variants of IllegalArgumentExceptions
        assertTrue(send("getAllModels", ["test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3, "test", "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3, 0]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3, 0, 0, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllModels", [auth3, 0, 0, true, "test"]) instanceof IllegalArgumentException)
    }

    void testModelCount() {
        // first test without authentication
        assertTrue(send("getModelCount", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        def result = send("getModelCount", auth)
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
        result = send("getModelCount", auth)
        assertTrue(result instanceof Integer)
        assertEquals(0, result)
        // grant read right to testuser
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        // now testuser should have 1
        result = send("getModelCount", auth)
        assertTrue(result instanceof Integer)
        assertEquals(1, result)
        auth = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        result = send("getModelCount", auth)
        assertTrue(result instanceof Integer)
        assertEquals(0, result)
    }

    void testGetLatestRevision() {
        // first test a completely invalid variant
        assertTrue(send("getLatestRevision", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testuser should get an AccessDeniedException
        assertTrue(send("getLatestRevision", [auth, model.toCommandObject()]) instanceof AccessDeniedException)
        // add permission to Revision
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        // now the testuser should see the revision
        def result = send("getLatestRevision", [auth, model.toCommandObject()])
        assertTrue(result instanceof RevisionTransportCommand)
        assertEquals(revision.id, result.id)
        // some IllegalArgumentExceptions
        assertTrue(send("getLatestRevision", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("getLatestRevision", [auth, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getLatestRevision", [auth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        // user should get an AccessDeniedException
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        assertTrue(send("getLatestRevision", [auth2, model.toCommandObject()]) instanceof AccessDeniedException)
    }

    void testGetAllRevisions() {
        // first test a completely invalid variant
        assertTrue(send("getAllRevisions", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testuser should get an empty list
        def result = send("getAllRevisions", [auth, model.toCommandObject()])
        assertTrue(result instanceof List)
        assertTrue(result.isEmpty())
        // add permission to Revision
        authenticateAsUser()
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        authenticateAnonymous()
        result = send("getAllRevisions", [auth, model.toCommandObject()])
        assertTrue(result instanceof List)
        assertFalse(result.isEmpty())
        assertEquals(1, result.size())
        assertEquals(revision.id, result[0].id)
        // some invalid argument exceptions
        assertTrue(send("getAllRevisions", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("getAllRevisions", [auth, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("getAllRevisions", [auth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        // user should get an empty List
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        result = send("getAllRevisions", [auth2, model.toCommandObject()])
        assertTrue(result instanceof List)
        assertTrue(result.isEmpty())
    }

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
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "upload", format: new ModelFormatTransportCommand(identifier: "SBML"))
        def result = send("uploadModel", [auth, modelSource.bytes, meta])
        assertTrue(result instanceof ModelTransportCommand)
        // uploading the same again should render a ModelException
        assertTrue(send("uploadModel", [auth, modelSource.bytes, meta]) instanceof ModelException)
        // test illegal argument exceptions
        assertTrue(send("uploadModel", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth, modelSource.bytes]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth, "test", meta]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth, modelSource.bytes, "Test"]) instanceof IllegalArgumentException)
        assertTrue(send("uploadModel", [auth, modelSource.bytes, meta, "test"]) instanceof IllegalArgumentException)
        // test anonymous authentication - should end in AccessDeniedException
        def anonAuth = new AnonymousAuthenticationToken("test", "Anonymous", [ new GrantedAuthorityImpl("ROLE_ANONYMOUS")])
        assertTrue(send("uploadModel", [anonAuth, modelSource.bytes, meta]) instanceof AccessDeniedException)
        
        // need to delete the ACL or following tests will fail
        modelAdminUser(true)
        authenticateAsAdmin()
        aclUtilService.deletePermission(Model, result.id, "testuser", BasePermission.READ)
        def revision = send("getLatestRevision", [auth, result])
        assertTrue(revision instanceof RevisionTransportCommand)
        aclUtilService.deletePermission(Revision, revision.id, "testuser", BasePermission.READ)
        authenticateAnonymous()
        modelAdminUser(false)
    }

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
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "addRevision", format: new ModelFormatTransportCommand(identifier: "SBML"))
        def model = send("uploadModel", [auth, modelSource.bytes, meta])
        assertTrue(model instanceof ModelTransportCommand)
        // test uploading a new Revision
        def result = send("addRevision", [auth, model, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment"])
        assertTrue(result instanceof RevisionTransportCommand)
        // other user should get an AccessDeniedException
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        assertTrue(send("addRevision", [auth2, model, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment"]) instanceof AccessDeniedException)
        // a deleted model has to end in ModelException
        send("deleteModel", [auth, model])
        assertTrue(send("addRevision", [auth, model, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment"]) instanceof ModelException)
        // illegal arguments exceptions
        assertTrue(send("addRevision", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth, model]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth, model, modelSource.bytes]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth, model, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML")]) instanceof IllegalArgumentException)
        assertTrue(send("addRevision", [auth, model, modelSource.bytes, new ModelFormatTransportCommand(identifier: "SBML"), "Comment", "Test"]) instanceof IllegalArgumentException)

        // need to delete the ACL or following tests will fail
        modelAdminUser(true)
        authenticateAsAdmin()
        aclUtilService.deletePermission(Model, model.id, "testuser", BasePermission.READ)
        authenticateAnonymous()
        modelAdminUser(false)
    }

    void testRetrieveModelFile() {
        // first test a completely invalid variant
        assertTrue(send("retrieveModelFile", "test") instanceof IllegalArgumentException)
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
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        // upload the model
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "retrieveFile", format: new ModelFormatTransportCommand(identifier: "SBML"))
        def model = send("uploadModel", [auth, modelSource.bytes, meta])
        assertTrue(model instanceof ModelTransportCommand)
        // get the latest revision
        def revision = send("getLatestRevision", [auth, model])
        assertTrue(revision instanceof RevisionTransportCommand)
        // get the file
        def result = send("retrieveModelFile", [auth, revision])
        assertTrue(result instanceof byte[])
        assertEquals(modelSource.bytes, result)
        // other user should get an AccessDeniedException
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        assertTrue(send("retrieveModelFile", [auth2, revision]) instanceof AccessDeniedException)
        // create a random revision
        Revision rev = new Revision(model: Model.get(model.id), vcsId: "2", revisionNumber: 2, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        Model.get(model.id).refresh().addToRevisions(rev)
        Model.get(model.id).save(flush: true)
        aclUtilService.addPermission(rev, "testuser", BasePermission.READ)
        assertTrue(send("retrieveModelFile", [auth, rev.toCommandObject()]) instanceof ModelException)

        // test illegal argument exceptions
        assertTrue(send("retrieveModelFile", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("retrieveModelFile", [auth, model]) instanceof IllegalArgumentException)
        assertTrue(send("retrieveModelFile", [auth, revision, "Test"]) instanceof IllegalArgumentException)

        // need to delete the ACL or following tests will fail
        modelAdminUser(true)
        authenticateAsAdmin()
        aclUtilService.deletePermission(Model, model.id, "testuser", BasePermission.READ)
        aclUtilService.deletePermission(Revision, revision.id, "testuser", BasePermission.READ)
        authenticateAnonymous()
        modelAdminUser(false)
    }

    void testGrantReadAccess() {
        // first test a completely invalid variant
        assertTrue(send("grantReadAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testuser does not have access to any models
        assertEquals(0, send("getModelCount", auth))
        // create an admin Authentication
        def adminAuth = send("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        def result = send("grantReadAccess", [adminAuth, model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // testuser does have access to the model
        assertEquals(1, send("getModelCount", auth))
        // test some illegal argument  exceptions
        assertTrue(send("grantReadAccess", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("grantReadAccess", [auth, model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("grantReadAccess", [auth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("grantReadAccess", [auth, model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // test access denied exception
        assertTrue(send("grantReadAccess", [auth, model.toCommandObject(), User.findByUsername("user")]) instanceof AccessDeniedException)
        // user should not have access to it
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        assertEquals(0, send("getModelCount", auth2))
    }

    void testGrantWriteAccess() {
        // first test a completely invalid variant
        assertTrue(send("grantWriteAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        modelAdminUser(false)
        // create a Model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // create an admin Authentication
        def adminAuth = send("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // grant the right
        def result = send("grantWriteAccess", [adminAuth, model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // testuser should now have the write
        // TODO: do an actual test by trying to upload
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // user should not be allowed to grant right
        assertTrue(send("grantWriteAccess", [auth, model.toCommandObject(), User.findByUsername("user")]) instanceof AccessDeniedException)
        // test some illegal argument  exceptions
        assertTrue(send("grantWriteAccess", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("grantWriteAccess", [auth, model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("grantWriteAccess", [auth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("grantWriteAccess", [auth, model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // user should not have access to it
        def auth2 = send("authenticate", new UsernamePasswordAuthenticationToken("user", "verysecret"))
        result = send("addRevision", [auth2, model.toCommandObject(), new byte[0], new ModelFormatTransportCommand(identifier: "UNKNOWN"), "no test"])
        assertTrue(result instanceof AccessDeniedException)
    }

    void testRevokeReadAccess() {
        // first test a completely invalid variant
        assertTrue(send("revokeReadAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
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
        assertEquals(1, send("getModelCount", auth))
        // create an admin Authentication
        def adminAuth = send("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // revoke the right
        def result = send("revokeReadAccess", [adminAuth, model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // user should not see the model any more
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertTrue(send("revokeReadAccess", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("revokeReadAccess", [auth, model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("revokeReadAccess", [auth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("revokeReadAccess", [auth, model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // revoking read access is not allowed for testuser
        assertTrue(send("revokeReadAccess", [auth, model.toCommandObject(), User.findByUsername("user")]) instanceof AccessDeniedException)
    }

    void testRevokeWriteAccess() {
        // first test a completely invalid variant
        assertTrue(send("revokeWriteAccess", "test") instanceof IllegalArgumentException)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
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
        def adminAuth = send("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // revoke the right
        def result = send("revokeWriteAccess", [adminAuth, model.toCommandObject(), User.findByUsername("testuser")])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        modelAdminUser(false)
        // user should not have write access any more
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        assertTrue(send("revokeWriteAccess", [auth]) instanceof IllegalArgumentException)
        assertTrue(send("revokeWriteAccess", [auth, model.toCommandObject()]) instanceof IllegalArgumentException)
        assertTrue(send("revokeWriteAccess", [auth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("revokeWriteAccess", [auth, model.toCommandObject(), User.findByUsername("testuser"), "test"]) instanceof IllegalArgumentException)
        // revoking write access is not allowed for testuser
        assertTrue(send("revokeWriteAccess", [auth, model.toCommandObject(), User.findByUsername("user")]) instanceof AccessDeniedException)
    }

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
        def adminAuth = send("authenticate", new UsernamePasswordAuthenticationToken("admin", "1234"))
        assertTrue(adminAuth.isAuthenticated())
        modelAdminUser(true)
        // delete the model
        def result = send("deleteModel", [adminAuth, model.toCommandObject()])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        assertEquals(ModelState.DELETED, model.refresh().state)
        // delete again should not work
        result = send("deleteModel", [adminAuth, model.toCommandObject()])
        assertTrue(result instanceof Boolean)
        assertFalse(result)
        // restore
        result = send("restoreModel", [adminAuth, model.toCommandObject()])
        assertTrue(result instanceof Boolean)
        assertTrue(result)
        assertEquals(ModelState.UNPUBLISHED, model.refresh().state)
        // restore again should not work
        result = send("restoreModel", [adminAuth, model.toCommandObject()])
        assertTrue(result instanceof Boolean)
        assertFalse(result)
        modelAdminUser(false)
        // create an authentication
        def auth = send("authenticate", new UsernamePasswordAuthenticationToken("testuser", "secret"))
        // user should not be able to delete/restore Model
        assertTrue(send("deleteModel", [auth, model.toCommandObject()]) instanceof AccessDeniedException)
        assertTrue(send("restoreModel", [auth, model.toCommandObject()]) instanceof AccessDeniedException)
        // try illegal arguments
        assertTrue(send("deleteModel", [adminAuth]) instanceof IllegalArgumentException)
        assertTrue(send("deleteModel", [adminAuth, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("deleteModel", [adminAuth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", [adminAuth]) instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", [adminAuth, "test"]) instanceof IllegalArgumentException)
        assertTrue(send("restoreModel", [adminAuth, model.toCommandObject(), "test"]) instanceof IllegalArgumentException)

    }

    private def send(String method, def message) {
        return jmsSynchronousService.send([service: "jmsAdapter", method: method],message, [service: "jmsAdapter", method: "${method}.response"])
    }

    private void setupVcs() {
        File clone = new File("target/vcs/git")
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()
        GitService gitService = new GitService()
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
            ''')
        gitService.afterPropertiesSet()
        assertTrue(gitService.isValid())
        modelService.vcsService.vcsManager = gitService.vcsManager()
        assertTrue(modelService.vcsService.isValid())
    }
}
