package net.biomodels.jummp.model

import grails.test.*

class PublicationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    @SuppressWarnings('UnusedVariable')
    void testConstraints() {
        mockForConstraintsTests(Publication)
        // test for nullable
        Publication publication = new Publication()
        assertFalse(publication.validate())
        assertEquals("nullable", publication.errors["journal"])
        assertEquals("nullable", publication.errors["title"])
        assertEquals("nullable", publication.errors["affiliation"])
        assertEquals("nullable", publication.errors["synopsis"])
        // test for blank
        publication = new Publication(journal: "", title: "", affiliation: "", synopsis: "")
        assertFalse(publication.validate())
        assertEquals("blank", publication.errors["journal"])
        assertEquals("blank", publication.errors["title"])
        assertEquals("blank", publication.errors["affiliation"])
        assertNull(publication.errors["synopsis"])
        // test for max size of synopsis
        String synopsis = ""
        for (int i=0; i<=5000; i++) {
            synopsis += "1"
        }
        publication.synopsis = synopsis
        assertFalse(publication.validate())
        assertEquals("maxSize", publication.errors["synopsis"])
        // test for allowed values
        publication = new Publication(journal: "journal", title: "title", affiliation: "Affiliation")
        synopsis = ""
        for (int i=1; i<1000; i++) {
            synopsis += "1"
        }
        publication.synopsis = synopsis
        assertTrue(publication.validate())
    }
}
