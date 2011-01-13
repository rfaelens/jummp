package net.biomodels.jummp.core

import grails.test.*

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.UserRole
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.plugins.git.GitService

class ModelServiceTests extends GrailsUnitTestCase {
    def authenticationManager
    def aclService
    def objectIdentityRetrievalStrategy
    def aclUtilService
    def springSecurityService
    def modelService
    protected void setUp() {
        super.setUp()
        mockLogging(VcsService, true)
        createUserAndRoles()

        /*File clone = new File("target/vcs/git")
        clone.mkdirs()
        File exchangeDirectory = new File("target/vcs/exchange")
        exchangeDirectory.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()*/
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
    }

    void testGetAllModelsSecurity() {
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // testUser should not see the model
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertTrue(modelService.getAllModels().isEmpty())
        assertEquals(0, modelService.getModelCount())
        // admin should see one model
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertFalse(modelService.getAllModels().isEmpty())
        assertSame(model, modelService.getAllModels().first())
        assertEquals(1, modelService.getModelCount())
        // user 2 should not see the model
        authenticate("user", "verysecret")
        // is no admin
        modelAdminUser(false)
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(0, modelService.getModelCount())
        authenticate("user", "verysecret")
        assertEquals(0, modelService.getModelCount())
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertEquals(0, modelService.getModelCount())
        // create one model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // no auth is set, only admin should see it
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(0, modelService.getModelCount())
        authenticate("user", "verysecret")
        assertEquals(0, modelService.getModelCount())
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertEquals(1, modelService.getModelCount())
        // adding read permission for all users should render one model for all
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        aclUtilService.addPermission(revision, "user", BasePermission.READ)
        aclUtilService.addPermission(revision, "admin", BasePermission.READ)
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(1, modelService.getModelCount())
        authenticate("user", "verysecret")
        assertEquals(1, modelService.getModelCount())
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(1, modelService.getModelCount())
        authenticate("user", "verysecret")
        assertEquals(1, modelService.getModelCount())
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertEquals(11, modelService.getModelCount())
        // there are no permissions set yet - users should only see one model
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(1, modelService.getModelCount())
        authenticate("user", "verysecret")
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
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertEquals(11, modelService.getModelCount())
        // both users should get seven models
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(7, modelService.getModelCount())
        authenticate("user", "verysecret")
        assertEquals(7, modelService.getModelCount())
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertNull(modelService.getLatestRevision(model))
        authenticate("user", "verysecret")
        assertNull(modelService.getLatestRevision(model))
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        // adding permission for the users
        aclUtilService.addPermission(revision, "user", BasePermission.READ)
        aclUtilService.addPermission(revision, "testuser", BasePermission.READ)
        // now our users should see the revision
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        authenticate("user", "verysecret")
        assertNotNull(modelService.getLatestRevision(model))
        assertSame(revision, modelService.getLatestRevision(model))
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertSame(revision, modelService.getLatestRevision(model))
        authenticate("user", "verysecret")
        assertSame(revision, modelService.getLatestRevision(model))
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertSame(rev6, modelService.getLatestRevision(model))
        // let's add some ACL
        aclUtilService.addPermission(rev3, "testuser", BasePermission.READ)
        aclUtilService.addPermission(rev6, "user", BasePermission.READ)
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertSame(rev3, modelService.getLatestRevision(model))
        authenticate("user", "verysecret")
        assertSame(rev6, modelService.getLatestRevision(model))
        // allow rev5 for all users
        aclUtilService.addPermission(rev5, "ROLE_USER", BasePermission.READ)
        authenticate("testuser", "secret")
        assertSame(rev5, modelService.getLatestRevision(model))
        authenticate("user", "verysecret")
        assertSame(rev6, modelService.getLatestRevision(model))
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertSame(rev6, modelService.getLatestRevision(model))
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
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
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
        modelAdminUser(true)
        authenticate("admin", "1234")
        testElements = modelService.getAllModels(28, 5)
        assertEquals(2, testElements.size())
        assertSame(Model.findByName("${28}"), testElements.first())
        assertSame(Model.findByName("${29}"), testElements.last())
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(0, modelService.getAllRevisions(model).size())
        authenticate("user", "verysecret")
        assertEquals(0, modelService.getAllRevisions(model).size())
        modelAdminUser(true)
        authenticate("admin", "1234")
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        // adding permission should user make see it
        aclUtilService.addPermission(revision, "ROLE_USER", BasePermission.READ)
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        authenticate("user", "verysecret")
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        authenticate("user", "verysecret")
        assertEquals(1, modelService.getAllRevisions(model).size())
        assertSame(revision, modelService.getAllRevisions(model).first())
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        testResults = modelService.getAllRevisions(model)
        assertEquals(4, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev5, testResults[3])
        authenticate("user", "verysecret")
        testResults = modelService.getAllRevisions(model)
        assertEquals(5, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev2, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])
        // unchanged for admin
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        testResults = modelService.getAllRevisions(model)
        assertEquals(4, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev3, testResults[2])
        assertSame(rev5, testResults[3])
        authenticate("user", "verysecret")
        testResults = modelService.getAllRevisions(model)
        assertEquals(5, testResults.size())
        assertSame(revision, testResults[0])
        assertSame(rev1, testResults[1])
        assertSame(rev2, testResults[2])
        assertSame(rev4, testResults[3])
        assertSame(rev5, testResults[4])
        // unchanged for admin
        modelAdminUser(true)
        authenticate("admin", "1234")
        testResults = modelService.getAllRevisions(model)
        assertEquals(6, testResults.size())
        for (int i=0; i<6; i++) {
            assertSame(Revision.findByRevisionNumber(i+1), testResults[i])
        }
        // lets' see if we all get the revision for model2
        modelAdminUser(false)
        authenticate("testuser", "secret")
        testResults = modelService.getAllRevisions(model2)
        assertEquals(1, testResults.size())
        assertSame(revision2, testResults[0])
        authenticate("user", "verysecret")
        testResults = modelService.getAllRevisions(model2)
        assertEquals(1, testResults.size())
        assertSame(revision2, testResults[0])
        modelAdminUser(true)
        authenticate("admin", "1234")
        testResults = modelService.getAllRevisions(model2)
        assertEquals(1, testResults.size())
        assertSame(revision2, testResults[0])
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        shouldFail(AccessDeniedException) {
            modelService.addRevision(model, new File("target/test"), null)
        }
        // give user the right to write to the model
        modelAdminUser(true)
        authenticate("admin", "1234")
        modelService.grantReadAccess(model, User.findByUsername("testuser"))
        modelService.grantWriteAccess(model, User.findByUsername("testuser"))
        // model may not be null - test as admin as otherwise will throw AccessDeniedException
        shouldFail(NullPointerException) {
            modelService.addRevision(null, new File("target/test"), "")
        }
        // vcs is not yet setup, adding a revision should fail
        modelAdminUser(false)
        def auth = authenticate("testuser", "secret")
        // model may not be null - as a user this throws an AccessDeniedException
        shouldFail(AccessDeniedException) {
            modelService.addRevision(null, new File("target/test"), "")
        }
        // file may not be null
        shouldFail(NullPointerException) {
            modelService.addRevision(model, null, "")
        }
        // comment may not be null
        shouldFail(NullPointerException) {
            modelService.addRevision(model, new File("target/test"), null)
        }
        // file must exist
        shouldFail(FileNotFoundException) {
            modelService.addRevision(model, new File("target/test"), "")
        }
        // file may not be a directory
        File exchangeDirectory = new File("target/vcs/exchange")
        exchangeDirectory.mkdirs()
        shouldFail(FileNotFoundException) {
            modelService.addRevision(model, exchangeDirectory, "")
        }
        File importFile = new File("target/vcs/exchange/test.xml")
        FileUtils.touch(importFile)
        assertNull(modelService.addRevision(model, importFile, ""))
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
        Revision rev = modelService.addRevision(model, updateFile, "")
        assertEquals(2, rev.revisionNumber)
        assertTrue(aclUtilService.hasPermission(auth, rev, BasePermission.ADMINISTRATION))
        assertTrue(aclUtilService.hasPermission(auth, rev, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth, rev, BasePermission.DELETE))
        File gitFile = new File("target/vcs/git/test.xml")
        List<String> lines = gitFile.readLines()
        assertEquals(1, lines.size())
        assertEquals("Test", lines[0])
        // user should not have received a read right on the revision
        def auth2 = authenticate("user", "verysecret")
        assertFalse(aclUtilService.hasPermission(auth2, rev, BasePermission.READ))
        // grant read access to the user - he should than get right to read the next revision
        modelAdminUser(true)
        authenticate("admin", "1234")
        modelService.grantReadAccess(model, User.findByUsername("user"))
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        modelAdminUser(false)
        authenticate("testuser", "secret")
        updateFile.append("Further Test\n")
        Revision rev2 = modelService.addRevision(model, updateFile, "")
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
        modelAdminUser(true)
        def adminAuth = authenticate("admin", "1234")
        updateFile.append("Admin Test\n")
        Revision rev3 = modelService.addRevision(model, updateFile, "")
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
        modelAdminUser(false)
        authenticate("testuser", "secret")
        assertEquals(0, modelService.getAllRevisions(model).size())

        // grant read access to the model as admin
        modelAdminUser(true)
        authenticate("admin", "1234")
        modelService.grantReadAccess(model, User.findByUsername("testuser"))
        // user should now see all revisions
        modelAdminUser(false)
        authenticate("testuser", "secret")
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
        authenticate("user", "verysecret")
        assertEquals(0, modelService.getAllRevisions(model).size())
        // try granting read permission to user - should not change anything
        authenticate("testuser", "secret")
        shouldFail(AccessDeniedException) {
            modelService.grantReadAccess(model, User.findByUsername("user"))
        }
        authenticate("user", "verysecret")
        assertEquals(0, modelService.getAllRevisions(model).size())
        // give admin right to the testuser - this should allow user to grant read access
        modelAdminUser(true)
        authenticate("admin", "1234")
        aclUtilService.addPermission(model, "testuser", BasePermission.READ)
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        // if the administration right is not set, the framework throws an exception
        aclUtilService.addPermission(rev1, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev2, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev3, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev4, "testuser", BasePermission.ADMINISTRATION)
        aclUtilService.addPermission(rev5, "testuser", BasePermission.ADMINISTRATION)
        modelAdminUser(false)
        // grant read permission to user
        authenticate("testuser", "secret")
        modelService.grantReadAccess(Model.get(model.id), User.findByUsername("user"))
        // user should see same revisions as testuser
        authenticate("user", "verysecret")
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
        def auth = authenticate("testuser", "secret")
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        modelAdminUser(true)
        authenticate("admin", "1234")
        // grant write access
        modelService.grantWriteAccess(model, User.findByUsername("testuser"))
        assertTrue(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // testuser is not admin to the model, he should not be allowed to grant write permission
        modelAdminUser(false)
        authenticate("testuser", "secret")
        shouldFail(AccessDeniedException) {
            modelService.grantWriteAccess(model, User.findByUsername("user"))
        }
        // grant admin right to testuser
        modelAdminUser(true)
        authenticate("admin", "1234")
        aclUtilService.addPermission(model, "testuser", BasePermission.ADMINISTRATION)
        // verify that user hoes not have the right to write on model
        modelAdminUser(false)
        auth = authenticate("user", "verysecret")
        assertFalse(aclUtilService.hasPermission(auth, model, BasePermission.WRITE))
        // grant write right to user
        authenticate("testuser", "secret")
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
        modelAdminUser(false)
        def auth = authenticate("testuser", "secret")
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
        def auth2 = authenticate("user", "verysecret")
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        // and revoke again
        authenticate("testuser", "secret")
        assertTrue(modelService.revokeReadAccess(model, User.findByUsername("user")))
        assertFalse(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        // test the same as admin user
        modelAdminUser(true)
        authenticate("admin", "1234")
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
        modelAdminUser(false)
        def auth = authenticate("testuser", "secret")
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
        def auth2 = authenticate("user", "verysecret")
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.READ))
        assertTrue(aclUtilService.hasPermission(auth2, model, BasePermission.WRITE))
        // and revoke again
        authenticate("testuser", "secret")
        assertTrue(modelService.revokeWriteAccess(model, User.findByUsername("user")))
        assertFalse(aclUtilService.hasPermission(auth2, model, BasePermission.WRITE))
        // test the same as admin user
        modelAdminUser(true)
        authenticate("admin", "1234")
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

    private void createUserAndRoles() {
        User user = new User(username: "testuser",
                password: springSecurityService.encodePassword("secret"),
                userRealName: "Test",
                email: "test@test.com",
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false)
        assertNotNull(user.save())
        assertNotNull(new AclSid(sid: user.username, principal: true).save(flush: true))
        User user2 = new User(username: "user",
                password: springSecurityService.encodePassword("verysecret"),
                userRealName: "Test2",
                email: "test2@test.com",
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false)
        assertNotNull(user2.save())
        assertNotNull(new AclSid(sid: user2.username, principal: true).save(flush: true))
        User admin = new User(username: "admin",
                password: springSecurityService.encodePassword("1234"),
                userRealName: "Administrator",
                email: "admin@test.com",
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false)
        assertNotNull(admin.save())
        assertNotNull(new AclSid(sid: admin.username, principal: true).save(flush: true))
        Role userRole = new Role(authority: "ROLE_USER")
        assertNotNull(userRole.save())
        UserRole.create(user, userRole, false)
        UserRole.create(user2, userRole, false)
        UserRole.create(admin, userRole, false)
        Role adminRole = new Role(authority: "ROLE_ADMIN")
        assertNotNull(adminRole.save())
        UserRole.create(admin, adminRole, false)
    }

    private def authenticate(String username, String password) {
        def authToken = new UsernamePasswordAuthenticationToken(username, password)
        def auth = authenticationManager.authenticate(authToken)
        SecurityContextHolder.getContext().setAuthentication(auth)
        return auth
    }

    private void modelAdminUser(boolean admin) {
        SpringSecurityUtils.metaClass.'static'.ifAnyGranted = { String parameter ->
            return admin
        }
    }
}
