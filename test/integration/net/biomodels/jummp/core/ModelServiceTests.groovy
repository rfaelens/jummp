package net.biomodels.jummp.core

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.model.Revision
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository
import org.apache.commons.io.FileUtils
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.plugins.git.GitService
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Constants
import net.biomodels.jummp.core.model.ModelFormat
import net.biomodels.jummp.core.model.ModelTransportCommand

class ModelServiceTests extends JummpIntegrationTestCase {
    def aclUtilService
    def modelService
    def origMethod

    protected void setUp() {
        super.setUp()
        mockLogging(VcsService, true)
        createUserAndRoles()
        origMethod = modelService.modelFileFormatService.metaClass.methods.findAll { it.name == "validate" }.first()
        println origMethod
        modelService.modelFileFormatService.metaClass.validate = { File file, ModelFormat format ->
            if (format == ModelFormat.UNKNOWN) {
                // for unknown format we model true to make all tests pass
                return true
            } else {
                return modelService.modelFileFormatService.sbmlService.validate(file)
            }
        }
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        modelService.vcsService.vcsManager = null
        modelService.modelFileFormatService.metaClass.validate = origMethod
    }

    void testGetAllModelsSecurity() {
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testUser should not see the model
        authenticateAsTestUser()
        assertTrue(modelService.getAllModels().isEmpty())
        assertEquals(0, modelService.getModelCount())
        // admin should see one model
        authenticateAsAdmin()
        assertFalse(modelService.getAllModels().isEmpty())
        assertSame(model, modelService.getAllModels().first())
        assertEquals(1, modelService.getModelCount())
        // user 2 should not see the model
        authenticateAsUser()
        assertTrue(modelService.getAllModels().isEmpty())
        assertEquals(0, modelService.getModelCount())
        // adding an acl for user 2
        aclUtilService.addPermission(revision, "user", BasePermission.READ)
        // now user 2 should see the model
        assertFalse(modelService.getAllModels().isEmpty())
        assertSame(model, modelService.getAllModels().first())
        assertEquals(1, modelService.getModelCount())
    }

    void testModelCount() {
        // no models, should return 0 for all users
        authenticateAsTestUser()
        assertEquals(0, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(0, modelService.getModelCount())
        authenticateAsAdmin()
        assertEquals(0, modelService.getModelCount())
        // create one model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // no auth is set, only admin should see it
        authenticateAsTestUser()
        assertEquals(0, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(0, modelService.getModelCount())
        authenticateAsAdmin()
        assertEquals(1, modelService.getModelCount())
        // adding read permission for all users should render one model for all
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        aclUtilService.addPermission(revision, "user", BasePermission.READ)
        aclUtilService.addPermission(revision, "admin", BasePermission.READ)
        authenticateAsTestUser()
        assertEquals(1, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(1, modelService.getModelCount())
        authenticateAsAdmin()
        assertEquals(1, modelService.getModelCount())
        // admin user should also see it if he is not modelled as admin
        modelAdminUser(false)
        authenticate("admin", "1234")
        assertEquals(1, modelService.getModelCount())
        // verify adding another revision with no read permission
        Revision revision2 = new Revision(model: model, vcsId: "2", revisionNumber: 2, owner: User.findByUsername("user"), minorRevision: true, comment: "", uploadDate: new Date())
        assertTrue(revision2.validate())
        model.addToRevisions(revision2)
        assertTrue(model.validate())
        model.save()
        authenticateAsTestUser()
        assertEquals(1, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(1, modelService.getModelCount())
        authenticateAsAdmin()
        assertEquals(1, modelService.getModelCount())
        // admin user should also see it if he is not modelled as admin
        modelAdminUser(false)
        authenticate("admin", "1234")
        assertEquals(1, modelService.getModelCount())
        // create another ten models
        for (int i=0; i<10; i++) {
            Model m = new Model(name: "${i}", vcsIdentifier: "test${i}.xml")
            Revision r = new Revision(model: m, vcsId: "rev${i}", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
            assertTrue(r.validate())
            m.addToRevisions(r)
            assertTrue(m.validate())
            m.save()
        }
        // admin should see all 11 models
        authenticateAsAdmin()
        assertEquals(11, modelService.getModelCount())
        // there are no permissions set yet - users should only see one model
        authenticateAsTestUser()
        assertEquals(1, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(1, modelService.getModelCount())
        // user 1 gets first three models, user 2 gets next three and both get next four models, last not assigned
        aclUtilService.addPermission(Revision, revision2.id + 1, "testuser", BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 2, "testuser", BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 3, "testuser", BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 4, "user",     BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 5, "user",     BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 6, "user",     BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 7, "ROLE_USER",     BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 8, "ROLE_USER",     BasePermission.READ)
        aclUtilService.addPermission(Revision, revision2.id + 9, "ROLE_USER",     BasePermission.READ)
        // admin should see all 11 models
        authenticateAsAdmin()
        assertEquals(11, modelService.getModelCount())
        // both users should get seven models
        authenticateAsTestUser()
        assertEquals(7, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(7, modelService.getModelCount())
        // let's delete the very first model
        authenticateAsAdmin()
        assertTrue(modelService.deleteModel(model))
        assertEquals(10, modelService.getModelCount())
        // both users should get six models
        authenticateAsTestUser()
        assertEquals(6, modelService.getModelCount())
        authenticateAsUser()
        assertEquals(6, modelService.getModelCount())
    }

    void testGetLatestRevision() {
        // create Model with one revision, without ACL
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // only admin should see the revision
        authenticateAsTestUser()
        assertNull(modelService.getLatestRevision(model))
        authenticateAsUser()
        assertNull(modelService.getLatestRevision(model))
        authenticateAsAdmin()
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        // adding permission for the users
        aclUtilService.addPermission(revision, "user", BasePermission.READ)
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        // now our users should see the revision
        authenticateAsTestUser()
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        authenticateAsUser()
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        authenticateAsAdmin()
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        // add some more revisions
        Revision rev2 = new Revision(model: model, vcsId: "2", revisionNumber: 2, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev3 = new Revision(model: model, vcsId: "3", revisionNumber: 3, owner: User.findByUsername("user"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev4 = new Revision(model: model, vcsId: "4", revisionNumber: 4, owner: User.findByUsername("user"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev5 = new Revision(model: model, vcsId: "5", revisionNumber: 5, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev6 = new Revision(model: model, vcsId: "6", revisionNumber: 6, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev2.validate())
        assertTrue(rev3.validate())
        assertTrue(rev4.validate())
        assertTrue(rev5.validate())
        assertTrue(rev6.validate())
        model.addToRevisions(rev2)
        model.addToRevisions(rev3)
        model.addToRevisions(rev4)
        model.addToRevisions(rev5)
        model.addToRevisions(rev6)
        assertTrue(model.validate())
        model.save()
        // no acl set for these new revisions, user should still see previous revision, admin should see rev6
        authenticateAsTestUser()
        assertSame(revision, modelService.getLatestRevision(model))
        authenticateAsUser()
        assertSame(revision, modelService.getLatestRevision(model))
        authenticateAsAdmin()
        assertSame(rev6, modelService.getLatestRevision(model))
        // let's add some ACL
        aclUtilService.addPermission(rev3, "testuser", BasePermission.READ)
        aclUtilService.addPermission(rev6, "user", BasePermission.READ)
        authenticateAsTestUser()
        assertSame(rev3, modelService.getLatestRevision(model))
        authenticateAsUser()
        assertSame(rev6, modelService.getLatestRevision(model))
        // allow rev5 for all users
        aclUtilService.addPermission(rev5, "ROLE_USER", BasePermission.READ)
        authenticateAsTestUser()
        assertSame(rev5, modelService.getLatestRevision(model))
        authenticateAsUser()
        assertSame(rev6, modelService.getLatestRevision(model))
        authenticateAsAdmin()
        assertSame(rev6, modelService.getLatestRevision(model))
        // let's delete the model
        authenticateAsAdmin()
        assertTrue(modelService.deleteModel(model))
        assertNull(modelService.getLatestRevision(model))
        // both users should get six models
        authenticateAsTestUser()
        assertNull(modelService.getLatestRevision(model))
        authenticateAsUser()
        assertNull(modelService.getLatestRevision(model))
    }

    @SuppressWarnings('UnusedVariable')
    void testGetAllModels() {
        for (int i=0; i<30; i++) {
            // create thirty models
            Model m = new Model(name: "${i}", vcsIdentifier: "test${i}.xml")
            Revision r = new Revision(model: m, vcsId: "rev${i}", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
            assertTrue(r.validate())
            m.addToRevisions(r)
            assertTrue(m.validate())
            m.save()
        }
        // change to admin
        authenticateAsAdmin()
        // just using getAllModels without parameters should return first ten elements
        List<Model> testElements = modelService.getAllModels()
        assertEquals(10, testElements.size())
        assertSame(Model.findByName("0"), testElements.first())
        // use a loop on all elements
        for (int i=0; i<10; i++) {
            assertSame(Model.findByName("${i}"), testElements[i])
        }
        // get five elements starting from 15
        testElements = modelService.getAllModels(15, 5)
        assertEquals(5, testElements.size())
        for (int i=0; i<5; i++) {
            assertSame(Model.findByName("${15+i}"), testElements[i])
        }
        // try inverted ordering offset 5 and 6 elements
        testElements = modelService.getAllModels(5, 6, false)
        assertEquals(6, testElements.size())
        for (int i=0; i<6; i++) {
            assertSame(Model.findByName("${24-i}"), testElements[i])
        }
        // add some permissions - each second becomes visible to users
        for (int i=0; i<15; i++) {
            aclUtilService.addPermission(Revision.findByVcsId("rev${i*2}"), "ROLE_USER", BasePermission.READ)
        }
        authenticateAsTestUser()
        // get first ten elements
        testElements = modelService.getAllModels()
        assertEquals(10, testElements.size())
        assertSame(Model.findByName("0"), testElements.first())
        // use a loop on all elements
        for (int i=0; i<10; i++) {
            assertSame(Model.findByName("${i*2}"), testElements[i])
        }
        // get five elements starting from 5
        testElements = modelService.getAllModels(5, 5)
        assertEquals(5, testElements.size())
        for (int i=0; i<5; i++) {
            assertSame(Model.findByName("${10+i*2}"), testElements[i])
        }
        // try inverted ordering offset 5 and 6 elements
        testElements = modelService.getAllModels(5, 6, false)
        assertEquals(6, testElements.size())
        for (int i=0; i<6; i++) {
            assertSame(Model.findByName("${18-i*2}"), testElements[i])
        }
        // some border case tests
        // 0 elements
        assertEquals(0, modelService.getAllModels(0, 0).size())
        // index out of bounds
        assertEquals(0, modelService.getAllModels(30, 10).size())
        // negative offset and count
        assertEquals(0, modelService.getAllModels(-2, -2).size())
        // with offset and count expanding boundaries
        testElements = modelService.getAllModels(13, 5)
        assertEquals(2, testElements.size())
        assertSame(Model.findByName("${13*2}"), testElements.first())
        assertSame(Model.findByName("${14*2}"), testElements.last())
        // same as admin user
        authenticateAsAdmin()
        testElements = modelService.getAllModels(28, 5)
        assertEquals(2, testElements.size())
        assertSame(Model.findByName("${28}"), testElements.first())
        assertSame(Model.findByName("${29}"), testElements.last())
        // delete the very first model
        modelService.deleteModel(Model.findByName("0"))
        testElements = modelService.getAllModels()
        assertEquals(10, testElements.size())
        assertSame(Model.findByName("1"), testElements.first())
        authenticateAsTestUser()
        testElements = modelService.getAllModels()
        assertEquals(10, testElements.size())
        assertSame(Model.findByName("2"), testElements.first())
    }

    @SuppressWarnings('UnusedVariable')
    void testGetAllRevisions() {
        // create one model with one revision and no acl
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // only admin should see the revision
        authenticateAsTestUser()
        assertEquals(0, modelService.getAllRevisions(model).size())
        authenticateAsUser()
        assertEquals(0, modelService.getAllRevisions(model).size())
        authenticateAsAdmin()
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        // adding permission should user make see it
        aclUtilService.addPermission(revision, "ROLE_USER", BasePermission.READ)
        authenticateAsTestUser()
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        authenticateAsUser()
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        authenticateAsAdmin()
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        // add some more revisions without ACL
        Revision rev1 = new Revision(model: model, vcsId: "2", revisionNumber: 2, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev2 = new Revision(model: model, vcsId: "3", revisionNumber: 3, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev3 = new Revision(model: model, vcsId: "4", revisionNumber: 4, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev4 = new Revision(model: model, vcsId: "5", revisionNumber: 5, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev5 = new Revision(model: model, vcsId: "6", revisionNumber: 6, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev1.validate())
        assertTrue(rev2.validate())
        assertTrue(rev3.validate())
        assertTrue(rev4.validate())
        assertTrue(rev5.validate())
        model.addToRevisions(rev1)
        model.addToRevisions(rev2)
        model.addToRevisions(rev3)
        model.addToRevisions(rev4)
        model.addToRevisions(rev5)
        assertTrue(model.validate())
        model.save()
        // admin should see all revisions, users the previous ones
        authenticateAsTestUser()
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        authenticateAsUser()
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        authenticateAsAdmin()
        List<Revision> testResults = modelService.getAllRevisions(model)
        assertEquals(6, testResults.size())
        for (int i=0; i<6; i++) {
            assertSame(Revision.findByRevisionNumber(i+1), testResults[i])
        }
        // add some permissions to the revisions
        aclUtilService.addPermission(rev1, "testuser", BasePermission.READ)
        aclUtilService.addPermission(rev3, "testuser", BasePermission.READ)
        aclUtilService.addPermission(rev1, "user", BasePermission.READ)
        aclUtilService.addPermission(rev2, "user", BasePermission.READ)
        aclUtilService.addPermission(rev4, "user", BasePermission.READ)
        aclUtilService.addPermission(rev5, "ROLE_USER", BasePermission.READ)
        // verify that users see the revision
        authenticateAsTestUser()
        testResults = modelService.getAllRevisions(model)
        assertEquals(4, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev5, testResults[3])
        authenticateAsUser()
        testResults = modelService.getAllRevisions(model)
        assertEquals(5, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev2, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])
        // unchanged for admin
        authenticateAsAdmin()
        testResults = modelService.getAllRevisions(model)
        assertEquals(6, testResults.size())
        for (int i=0; i<6; i++) {
            assertSame(Revision.findByRevisionNumber(i+1), testResults[i])
        }
        // add another model
        Model model2 = new Model(name: "test12", vcsIdentifier: "test12.xml")
        Revision revision2 = new Revision(model: model2, vcsId: "12", revisionNumber: 1, owner: User.findByUsername("user"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision2.validate())
        model2.addToRevisions(revision2)
        assertTrue(model2.validate())
        model2.save()
        aclUtilService.addPermission(revision2, "ROLE_USER", BasePermission.READ)
        // nothing should have changed...
        authenticateAsTestUser()
        testResults = modelService.getAllRevisions(model)
        assertEquals(4, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev5, testResults[3])
        authenticateAsUser()
        testResults = modelService.getAllRevisions(model)
        assertEquals(5, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev2, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])
        // unchanged for admin
        authenticateAsAdmin()
        testResults = modelService.getAllRevisions(model)
        assertEquals(6, testResults.size())
        for (int i=0; i<6; i++) {
            assertSame(Revision.findByRevisionNumber(i+1), testResults[i])
        }
        // lets' see if we all get the revision for model2
        authenticateAsTestUser()
        testResults = modelService.getAllRevisions(model2)
        assertEquals(1, testResults.size())
        assertSame(revision2, testResults[0])
        authenticateAsUser()
        testResults = modelService.getAllRevisions(model2)
        assertEquals(1, testResults.size())
        assertSame(revision2, testResults[0])
        authenticateAsAdmin()
        testResults = modelService.getAllRevisions(model2)
        assertEquals(1, testResults.size())
        assertSame(revision2, testResults[0])
        // let's delete Model 2
        modelService.deleteModel(model2)
        assertTrue(modelService.getAllRevisions(model2).isEmpty())
        authenticateAsTestUser()
        assertTrue(modelService.getAllRevisions(model2).isEmpty())
        authenticateAsUser()
        assertTrue(modelService.getAllRevisions(model2).isEmpty())
    }

    void testAddRevision() {
        // create one model with one revision and no acl
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // authenticate as testuser, as there is no ACL he is not allowed to add a revision
        def auth = authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.addRevision(model, new File("target/test"), ModelFormat.UNKNOWN, null)
        }
        // give user the right to write to the model
        authenticateAsAdmin()
        modelService.grantReadAccess(model, User.findByUsername("testuser"))
        modelService.grantWriteAccess(model, User.findByUsername("testuser"))
        // model may not be null - test as admin as otherwise will throw AccessDeniedException
        shouldFail(ModelException) {
            modelService.addRevision(null, new File("target/test"), ModelFormat.UNKNOWN, "")
        }
        // vcs is not yet setup, adding a revision should fail
        authenticateAsTestUser()
        // model may not be null - as a user this throws an AccessDeniedException
        shouldFail(AccessDeniedException) {
            modelService.addRevision(null, new File("target/test"), ModelFormat.UNKNOWN, "")
        }
        // file may not be null
        shouldFail(ModelException) {
            modelService.addRevision(model, null, ModelFormat.UNKNOWN, "")
        }
        // comment may not be null
        shouldFail(ModelException) {
            modelService.addRevision(model, new File("target/test"), ModelFormat.UNKNOWN, null)
        }
        // file must exist
        shouldFail(ModelException) {
            modelService.addRevision(model, new File("target/test"), ModelFormat.UNKNOWN, "")
        }
        // file may not be a directory
        File exchangeDirectory = new File("target/vcs/exchange")
        exchangeDirectory.mkdirs()
        shouldFail(ModelException) {
            modelService.addRevision(model, exchangeDirectory, ModelFormat.UNKNOWN, "")
        }
        File importFile = new File("target/vcs/exchange/test.xml")
        FileUtils.touch(importFile)
        shouldFail(ModelException) {
            modelService.addRevision(model, importFile, ModelFormat.UNKNOWN, "")
        }
        // setup VCS
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
        // import a file to the git repository, to make future updates possible
        gitService.vcsManager().importFile(importFile, "test.xml")
        // test the real update
        File updateFile = new File("target/vcs/exchange/update.xml")
        updateFile.append("Test\n")
        FileUtils.touch(updateFile)
        Revision rev = modelService.addRevision(model, updateFile, ModelFormat.UNKNOWN, "")
        assertEquals(2, rev.revisionNumber)
        assertTrue(aclUtilService.hasPermission(auth, rev, BasePermission.ADMINISTRATION))
        assertTrue(aclUtilService.hasPermission(auth, rev, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, rev, BasePermission.DELETE))
        File gitFile = new File("target/vcs/git/test.xml")
        List<String> lines = gitFile.readLines()
        assertEquals(1, lines.size())
        assertEquals("Test", lines[0])
        // user should not have received a read right on the revision
        def auth2 = authenticateAsUser()
        assertFalse(aclUtilService.hasPermission(auth2, rev, BasePermission.READ))
        // grant read access to the user - he should than get right to read the next revision
        authenticateAsAdmin()
        modelService.grantReadAccess(model, User.findByUsername("user"))
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        authenticateAsTestUser()
        updateFile.append("Further Test\n")
        Revision rev2 = modelService.addRevision(model, updateFile, ModelFormat.UNKNOWN, "")
        assertEquals(3, rev2.revisionNumber)
        assertFalse(rev2.vcsId == rev.vcsId)
        assertTrue(aclUtilService.hasPermission(auth, rev2, BasePermission.ADMINISTRATION))
        assertTrue(aclUtilService.hasPermission(auth, rev2, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, rev2, BasePermission.DELETE))
        assertTrue(aclUtilService.hasPermission(auth2, rev2, BasePermission.READ))
        lines = gitFile.readLines()
        assertEquals(2, lines.size())
        assertEquals("Test", lines[0])
        assertEquals("Further Test", lines[1])
        // admin should also be able to import updates
        def adminAuth = authenticateAsAdmin()
        updateFile.append("Admin Test\n")
        Revision rev3 = modelService.addRevision(model, updateFile, ModelFormat.UNKNOWN, "")
        assertEquals(4, rev3.revisionNumber)
        assertFalse(rev3.vcsId == rev2.vcsId)
        assertTrue(aclUtilService.hasPermission(adminAuth, rev3, BasePermission.ADMINISTRATION))
        assertTrue(aclUtilService.hasPermission(adminAuth, rev3, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(adminAuth, rev3, BasePermission.DELETE))
        assertTrue(aclUtilService.hasPermission(auth, rev3, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth2, rev3, BasePermission.READ))
        lines = gitFile.readLines()
        assertEquals(3, lines.size())
        assertEquals("Test", lines[0])
        assertEquals("Further Test", lines[1])
        assertEquals("Admin Test", lines[2])
        // try adding a revision with invalid sbml file - should not be possible
        File sbmlFile = new File("target/sbml/addRevisionSbmlFile")
        FileUtils.deleteQuietly(sbmlFile)
        FileUtils.touch(sbmlFile)
        shouldFail(ModelException) {
            modelService.addRevision(model, sbmlFile, ModelFormat.SBML, "")
        }
        // with a valid sbml file it should be possible
        sbmlFile = new File("target/sbml/addRevisionValidSbmlFile")
        FileUtils.deleteQuietly(sbmlFile)
        FileUtils.touch(sbmlFile)
        sbmlFile.append('''<?xml version="1.0" encoding="UTF-8"?>
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
</sbml>''')
        Revision rev4 = modelService.addRevision(model, sbmlFile, ModelFormat.SBML, "")
        assertEquals(5, rev4.revisionNumber)
        assertEquals(ModelFormat.SBML, rev4.format)
        // delete the Model - any further updates should end in a ModelException
        modelService.deleteModel(model)
        shouldFail(ModelException) {
            modelService.addRevision(model, updateFile, ModelFormat.UNKNOWN, "")
        }
    }

    void testDeleteRestoreModel() {
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision rev1 = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev1.validate())
        model.addToRevisions(rev1)
        assertTrue(model.validate())
        model.save()
        // Model is not yet deleted
        assertEquals(ModelState.UNPUBLISHED, model.state)
        // try to delete as anonymous
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            modelService.deleteModel(model)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            modelService.deleteModel(model)
        }
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.deleteModel(model)
        }
        // admin is allowed to delete
        authenticateAsAdmin()
        assertTrue(modelService.deleteModel(model))
        assertEquals(ModelState.DELETED, model.state)
        // no user is allowed to restore
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            modelService.restoreModel(model)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            modelService.restoreModel(model)
        }
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.restoreModel(model)
        }
        // admin can restore the model
        authenticateAsAdmin()
        assertTrue(modelService.restoreModel(model))
        assertEquals(ModelState.UNPUBLISHED, model.state)
        // let's try to restore again
        assertFalse(modelService.restoreModel(model))
        assertEquals(ModelState.UNPUBLISHED, model.state)
        // let's grant some rights on the model
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        aclUtilService.addPermission(model, "testuser", BasePermission.WRITE)
        aclUtilService.addPermission(model, "testuser", BasePermission.DELETE)
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        // user get's all rights except delete
        aclUtilService.addPermission(model, "user", BasePermission.READ)
        aclUtilService.addPermission(model, "user", BasePermission.WRITE)
        aclUtilService.addPermission(model, "user", BasePermission.ADMINISTRATION)
        // user is still not allowed to delete
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            modelService.deleteModel(model)
        }
        // testuser is allowed to delete
        authenticateAsTestUser()
        assertTrue(modelService.deleteModel(model))
        assertEquals(ModelState.DELETED, model.state)
        // further delete should not work
        assertFalse(modelService.deleteModel(model))
        assertEquals(ModelState.DELETED, model.state)
        // testuser is not allowed to restore
        shouldFail(AccessDeniedException) {
            modelService.restoreModel(model)
        }
        // also user is not allowed to restore
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            modelService.restoreModel(model)
        }
        // admin is allowed to restore
        authenticateAsAdmin()
        assertTrue(modelService.restoreModel(model))
        assertEquals(ModelState.UNPUBLISHED, model.state)
        // let's change the model state
        model.state = ModelState.UNDER_CURATION
        assertFalse(modelService.deleteModel(model))
        assertEquals(ModelState.UNDER_CURATION, model.state)
        assertFalse(modelService.restoreModel(model))
        assertEquals(ModelState.UNDER_CURATION, model.state)
        model.state = ModelState.PUBLISHED
        assertFalse(modelService.deleteModel(model))
        assertEquals(ModelState.PUBLISHED, model.state)
        assertFalse(modelService.restoreModel(model))
        assertEquals(ModelState.PUBLISHED, model.state)
        model.state = ModelState.RELEASED
        assertFalse(modelService.deleteModel(model))
        assertEquals(ModelState.RELEASED, model.state)
        assertFalse(modelService.restoreModel(model))
        assertEquals(ModelState.RELEASED, model.state)
    }

    void testUploadModel() {
        // anonymous user is not allowed to invoke method
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            modelService.uploadModel(null, null)
        }
        // try importing with null file - should fail
        def auth = authenticateAsTestUser()
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "test", format: ModelFormat.UNKNOWN)
        shouldFail(ModelException) {
            modelService.uploadModel(null, meta)
        }
        File importFile = new File("target/vcs/exchange/import.xml")
        // file does not yet exists = it should fail
        shouldFail(ModelException) {
            modelService.uploadModel(importFile, meta)
        }
        FileUtils.touch(importFile)
        importFile.append("Test\n")
        // also for directory it should fail
        shouldFail(ModelException) {
            modelService.uploadModel(new File("target/vcs/exchange/"), meta)
        }
        // VCS system should not be valid - so it should fail
        shouldFail(ModelException) {
            modelService.uploadModel(importFile, meta)
        }
        // now let's create the VCS
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
        // import should work now
        Model model = modelService.uploadModel(importFile, meta)
        assertTrue(model.validate())
        assertEquals(ModelFormat.UNKNOWN, model.revisions.toList().first().format)
        // complete name cannot be tested, as it uses a generated date and we do not know the date
        assertTrue(model.vcsIdentifier.endsWith("test"))
        File gitFile = new File("target/vcs/git/${model.vcsIdentifier}")
        List<String> lines = gitFile.readLines()
        assertEquals(1, lines.size())
        assertEquals("Test", lines[0])
        // ensure the revision and commit message is correct
        ObjectId commit = repository.resolve(Constants.HEAD)
        RevWalk revWalk = new RevWalk(repository)
        RevCommit revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), model.revisions.toList().first().vcsId)
        assertEquals(1, model.revisions.size())
        // we did not specify a commit message, so default should be used
        assertEquals("Import of ${model.vcsIdentifier}", revCommit.getShortMessage())
        assertEquals("Import of ${model.vcsIdentifier}", revCommit.getFullMessage())
        // verify set permissions
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.ADMINISTRATION))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.DELETE))
        Revision revision = model.revisions.toList().first()
        assertTrue(aclUtilService.hasPermission(auth, revision, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, revision, BasePermission.ADMINISTRATION))
        assertTrue(aclUtilService.hasPermission(auth, revision, BasePermission.DELETE))
        assertFalse(aclUtilService.hasPermission(auth, revision, BasePermission.WRITE))
        // importing a model with same name should not be possible
        shouldFail(ModelException) {
            modelService.uploadModel(importFile, meta)
        }
        // importing with invalid model file should not be possible
        meta.name = "test2"
        meta.format = ModelFormat.SBML
        File sbmlFile = new File("target/sbml/sbmlTestFile")
        FileUtils.deleteQuietly(sbmlFile)
        FileUtils.touch(sbmlFile)
        shouldFail(ModelException) {
            modelService.uploadModel(sbmlFile, meta)
        }
        // importing with valid model file should be possible
        sbmlFile = new File("target/sbml/uploadModelValidSbmlFile")
        FileUtils.deleteQuietly(sbmlFile)
        FileUtils.touch(sbmlFile)
        sbmlFile.append('''<?xml version="1.0" encoding="UTF-8"?>
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
</sbml>''')
        model = modelService.uploadModel(sbmlFile, meta)
        assertTrue(model.validate())
        assertEquals(ModelFormat.SBML, model.revisions.toList().first().format)
        // TODO: somehow we need to test the failing cases, which is non-trivial
        // the only solution were to modify comment to make the revision non-validate, but in future it will be a command object which validates
    }

    void testRetrieveModelFile() {
        // first create the VCS
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
        // import a file
        authenticateAsTestUser()
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "test", format: ModelFormat.UNKNOWN)
        File importFile = new File("target/vcs/exchange/import.xml")
        FileUtils.touch(importFile)
        importFile.append("Test\n")
        Model model = modelService.uploadModel(importFile, meta)
        Revision revision = modelService.getLatestRevision(model)
        // Anonymous user should not be allowed to download the revision
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            modelService.retrieveModelFile(revision)
        }
        // User should not be allowed to download the revision
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            modelService.retrieveModelFile(revision)
        }
        // as admin we should get a byte array
        authenticateAsAdmin()
        byte[] bytes = modelService.retrieveModelFile(revision)
        assertEquals("Test\n", new String(bytes))
        // as testuser we should also get the byte array
        authenticateAsTestUser()
        bytes = modelService.retrieveModelFile(revision)
        assertEquals("Test\n", new String(bytes))
        // create a random revision
        Revision rev = new Revision(model: model, vcsId: "2", revisionNumber: 2, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        model.addToRevisions(rev)
        model.save(flush: true)
        aclUtilService.addPermission(rev, "testuser", BasePermission.READ)
        shouldFail(ModelException) {
            modelService.retrieveModelFile(rev)
        }
    }

    void testGrantReadAccess() {
        // create a model with some revisions
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision rev1 = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev2 = new Revision(model: model, vcsId: "2", revisionNumber: 2, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev3 = new Revision(model: model, vcsId: "3", revisionNumber: 3, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev4 = new Revision(model: model, vcsId: "4", revisionNumber: 4, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        Revision rev5 = new Revision(model: model, vcsId: "5", revisionNumber: 5, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev1.validate())
        assertTrue(rev2.validate())
        assertTrue(rev3.validate())
        assertTrue(rev4.validate())
        assertTrue(rev5.validate())
        model.addToRevisions(rev1)
        model.addToRevisions(rev2)
        model.addToRevisions(rev3)
        model.addToRevisions(rev4)
        model.addToRevisions(rev5)
        assertTrue(model.validate())
        model.save()
        // verify that user cannot access the revisions
        authenticateAsTestUser()
        assertEquals(0, modelService.getAllRevisions(model).size())

        // grant read access to the model as admin
        authenticateAsAdmin()
        modelService.grantReadAccess(model, User.findByUsername("testuser"))
        // user should now see all revisions
        authenticateAsTestUser()
        List<Revision> testResults = modelService.getAllRevisions(model)
        assertEquals(5, testResults.size())
        assertSame(rev1, testResults[0])
        assertSame(rev2, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])
        // adding one new Revision to the model
        Revision rev6 = new Revision(model: model, vcsId: "6", revisionNumber: 6, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev6.validate())
        model.addToRevisions(rev6)
        assertTrue(model.validate())
        assertNotNull(model.save(flush: true))
        // verify that testuser does not have access to this revision
        testResults = modelService.getAllRevisions(Model.get(model.id))
        assertEquals(5, testResults.size())
        assertSame(rev1, testResults[0])
        assertSame(rev2, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])
        // verify that user is not seeing the revisions
        authenticateAsUser()
        assertEquals(0, modelService.getAllRevisions(model).size())
        // try granting read permission to user - should not change anything
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.grantReadAccess(model, User.findByUsername("user"))
        }
        authenticateAsUser()
        assertEquals(0, modelService.getAllRevisions(model).size())
        // give admin right to the testuser - this should allow user to grant read access
        authenticateAsAdmin()
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        // if the administration right is not set, the framework throws an exception
        aclUtilService.addPermission(rev1, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev2, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev3, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev4, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev5, "testuser", BasePermission.ADMINISTRATION)
        // grant read permission to user
        authenticateAsTestUser()
        modelService.grantReadAccess(Model.get(model.id), User.findByUsername("user"))
        // user should see same revisions as testuser
        authenticateAsUser()
        testResults = modelService.getAllRevisions(model)
        assertEquals(5, testResults.size())
        assertSame(rev1, testResults[0])
        assertSame(rev2, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])

    }

    void testGrantWriteAccess() {
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision rev1 = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev1.validate())
        model.addToRevisions(rev1)
        assertTrue(model.validate())
        model.save()
        // testuser does not have Write permission on model
        def auth = authenticateAsTestUser()
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        authenticateAsAdmin()
        // grant write access
        modelService.grantWriteAccess(model, User.findByUsername("testuser"))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // testuser is not admin to the model, he should not be allowed to grant write permission
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.grantWriteAccess(model, User.findByUsername("user"))
        }
        // grant admin right to testuser
        authenticateAsAdmin()
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        // verify that user hoes not have the right to write on model
        auth = authenticateAsUser()
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // grant write right to user
        authenticateAsTestUser()
        modelService.grantWriteAccess(model, User.findByUsername("user"))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // TODO: add checks to verify that uploading a new model revision is allowed
    }

    void testRevokeReadAccess() {
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision rev1 = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev1.validate())
        model.addToRevisions(rev1)
        assertTrue(model.validate())
        model.save()
        // user has no right on the model - he is not allowed to revoke read access
        def auth = authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.revokeReadAccess(model, User.findByUsername("user"))
        }
        // add rights to testuser
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        // let's try revoking our own right - should not be possible
        assertFalse(modelService.revokeReadAccess(model, User.findByUsername("testuser")))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        // grant read right to user
        modelService.grantReadAccess(model, User.findByUsername("user"))
        def auth2 = authenticateAsUser()
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        // and revoke again
        authenticateAsTestUser()
        assertTrue(modelService.revokeReadAccess(model, User.findByUsername("user")))
        assertFalse(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        // test the same as admin user
        authenticateAsAdmin()
        // testuser is still admin to the model - right should not be revoked
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertFalse(modelService.revokeReadAccess(model, User.findByUsername("testuser")))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        // remove the admin right and add write right
        aclUtilService.deletePermission(model, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(model, "testuser", BasePermission.WRITE)
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // revoke right, should remove both read and write
        assertTrue(modelService.revokeReadAccess(model, User.findByUsername("testuser")))
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
    }

    void testRevokeWriteAccess() {
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision rev1 = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(rev1.validate())
        model.addToRevisions(rev1)
        assertTrue(model.validate())
        model.save()
        // user has no right on the model - he is not allowed to revoke write access
        def auth = authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            modelService.revokeReadAccess(model, User.findByUsername("user"))
        }
        // add rights to testuser
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        aclUtilService.addPermission(model, "testuser", BasePermission.WRITE)
        // let's try revoking our own right - should not be possible
        assertFalse(modelService.revokeWriteAccess(model, User.findByUsername("testuser")))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // grant write right to user
        modelService.grantReadAccess(model, User.findByUsername("user"))
        modelService.grantWriteAccess(model, User.findByUsername("user"))
        def auth2 = authenticateAsUser()
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.WRITE))
        // and revoke again
        authenticateAsTestUser()
        assertTrue(modelService.revokeWriteAccess(model, User.findByUsername("user")))
        assertFalse(aclUtilService.hasPermission(auth2, model, BasePermission.WRITE))
        // test the same as admin user
        authenticateAsAdmin()
        // testuser is still admin to the model - right should not be revoked
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        assertFalse(modelService.revokeWriteAccess(model, User.findByUsername("testuser")))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // remove the admin right
        aclUtilService.deletePermission(model, "testuser", BasePermission.ADMINISTRATION)
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // revoke right
        assertTrue(modelService.revokeWriteAccess(model, User.findByUsername("testuser")))
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
        // calling again should not change anything
        assertTrue(modelService.revokeWriteAccess(model, User.findByUsername("testuser")))
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.READ))
    }
}
