package net.biomodels.jummp.model

import grails.test.*
import net.biomodels.jummp.plugins.security.User

class RevisionTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    @SuppressWarnings('UnusedVariable')
    void testConstraints() {
        Model model = new Model(vcsIdentifier: "test", name: "test")
        User owner = new User(username: "testUser", password: "secret", userRealName: "Test User", email: "test@user.org", enabled: true, accountExpired: false, accountLocked: false, passwordExpired: false)
        mockForConstraintsTests(Revision, [new Revision(vcsId: "1", revisionNumber: 1, minorRevision: false, uploadDate: new Date(), comment: '', owner: owner, model: model)])
        // verify nullable
        Revision revision = new Revision()
        assertFalse(revision.validate())
        assertEquals("nullable", revision.errors["vcsId"])
        assertEquals("nullable", revision.errors["revisionNumber"])
        assertEquals("nullable", revision.errors["minorRevision"])
        assertEquals("nullable", revision.errors["uploadDate"])
        assertEquals("nullable", revision.errors["name"])
        assertEquals("nullable", revision.errors["description"])
        assertEquals("nullable", revision.errors["comment"])
        assertEquals("nullable", revision.errors["owner"])
        assertEquals("nullable", revision.errors["format"])

        // verify vcsId uniqueness constraint
        revision = new Revision(vcsId: "1")
        assertFalse(revision.validate())
        assertEquals("unique", revision.errors["vcsId"])

        // verify revisionNumber uniqueness for a single model
        revision = new Revision(vcsId: "2", model: model, revisionNumber: 1)
        assertFalse(revision.validate())
        assertEquals("unique", revision.errors["revisionNumber"])

        // verify the comment constraints
        String comment = ""
        for (int i=0; i<=1000; i++) {
            comment += "1"
        }
        revision = new Revision(comment: comment)
        assertFalse(revision.validate())
        assertEquals("maxSize", revision.errors["comment"])
        // a comment may be blank
        revision = new Revision(comment: '')
        assertFalse(revision.validate())
        assertNull(revision.errors["comment"])
        // a comment may be smaller than 1000
        comment = ""
        for (int i=0; i<1000; i++) {
            comment += "1"
        }
        revision = new Revision(comment: comment)
        assertFalse(revision.validate())
        assertNull(revision.errors["comment"])

        
        // verify description constraints
        // a description may be blank
        revision = new Revision(description: '')
        assertFalse(revision.validate())
        assertNull(revision.errors["description"])

        
        // verify name constraints
        // a name may be blank
        revision = new Revision(name: '')
        assertFalse(revision.validate())
        assertNull(revision.errors["name"])

        
        // verify that a correct Revision is valid
        revision = new Revision(model: model, vcsId: "2", revisionNumber: 2, owner: owner, minorRevision: true, uploadDate: new Date(), name:'',description:'', comment: '', format: new ModelFormat(identifier: "UNKNOWN", name: "unknown"))
        assertTrue(revision.validate())
    }
}
