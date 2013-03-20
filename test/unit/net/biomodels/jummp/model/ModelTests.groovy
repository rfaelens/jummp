package net.biomodels.jummp.model

import grails.test.*
import grails.test.mixin.TestFor

@TestFor(Model)
class ModelTests {

    void testConstraints() {
        mockForConstraintsTests(Model, [new Model(name: "test", vcsIdentifier: "vcs")])
        // test for nullable
        Model model = new Model()
        assertFalse(model.validate())
        assertEquals("nullable", model.errors["vcsIdentifier"])
        assertEquals("nullable", model.errors["name"])
        assertEquals("nullable", model.errors["revisions"])
        // test for blank
        model = new Model()
        model.name = ""
        model.vcsIdentifier = ""
        assertFalse(model.validate())
        assertEquals("blank", model.errors["vcsIdentifier"])
        assertEquals("blank", model.errors["name"])
        assertEquals("nullable", model.errors["revisions"])
        // test for uniqueness
        model = new Model()
        model.name = "test"
        model.vcsIdentifier = "vcs"
        assertFalse(model.validate())
        assertEquals("unique", model.errors["vcsIdentifier"])
        assertEquals("unique", model.errors["name"])
        assertEquals("nullable", model.errors["revisions"])
        // test for the Revisions being empty
        model = new Model()
        model.revisions = []
        assertFalse(model.validate())
        assertEquals("nullable", model.errors["vcsIdentifier"])
        assertEquals("nullable", model.errors["name"])
        assertEquals("validator", model.errors["revisions"])
        // try a valid model
        model = new Model()
        Revision revision = new Revision()
        mockDomain(Revision, [revision])
        model.revisions = [revision] as Set
        model.name = "Model"
        model.vcsIdentifier = "1234"
        assertTrue(model.validate())
    }
}
