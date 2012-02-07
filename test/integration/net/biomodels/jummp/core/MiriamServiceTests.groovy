package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.miriam.MiriamDatatype
import net.biomodels.jummp.core.miriam.MiriamIdentifier
import net.biomodels.jummp.core.miriam.MiriamResource
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship

class MiriamServiceTests extends JummpIntegrationTest {
    def miriamService

    @Override
    @Before
    void setUp() {
        createUserAndRoles()
    }

    @Override
    @After
    void tearDown() {
    }

    @Test
    void testUpdateMiriamResourcesSecurity() {
        String url = "http://www.ebi.ac.uk/miriam/main/export/xml/"
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            miriamService.updateMiriamResources(url, false)
        }
    }

    @Test
    void testUpdateAllMiriamIdentifiersSecurity() {
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            miriamService.updateAllMiriamIdentifiers()
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            miriamService.updateAllMiriamIdentifiers()
        }
        authenticateAsCurator()
        shouldFail(AccessDeniedException) {
            miriamService.updateAllMiriamIdentifiers()
        }
    }

    @Test
    void testMiriamData() {
        authenticateAsAdmin()
        MiriamDatatype md1 = new MiriamDatatype(identifier: "00000001", name: "BIND", pattern: "^\\d+\$", urn: "urn:miriam:bind")
        assertNotNull(md1.save(flush:true))
        MiriamResource mr1 = new MiriamResource(identifier: "xml", location: "co.mbine", action: "spec")
        mr1.datatype = md1
        assertNotNull(mr1.save(flush:true))
        md1.addToResources(mr1)
        assertNotNull(md1.save(flush:true))
        MiriamIdentifier mi1 = new MiriamIdentifier( identifier: "0031387", name: "MPF complex")
        mi1.datatype = md1
        assertNotNull(mi1.save(flush:true))
        mr1.datatype = md1
        assertNotNull(mr1.save(flush:true))
        md1.addToIdentifiers(mi1)
        md1.addToResources(mr1)
        assertNotNull(md1.save(flush:true))
        assertTrue(mi1.validate())
        MiriamDatatype md2 = new MiriamDatatype(identifier: "00000002", name: "ChEBI", pattern: "^CHEBI:\\d+\$", urn: "urn:miriam:obo.chebi")
        assertNotNull(md2.save(flush:true))
        MiriamIdentifier mi2 = new MiriamIdentifier(identifier: "0000097", name: "catch")
        mi2.datatype = md2
        assertNotNull(mi2.save(flush:true))
        MiriamResource mr2 = new MiriamResource(identifier: "00000053", location: "loop", action: "trace")
        mr2.datatype = md2
        assertNotNull(mr2.save(flush:true))
        md2.addToIdentifiers(mi2)
        md2.addToResources(mr2)
        assertNotNull(md2.save(flush:true))
        assertTrue(mi2.validate())
        Map data = miriamService.miriamData("urn:miriam:bind:00000001")
        assertEquals("co.mbine", data.dataTypeLocation)
        assertEquals("BIND", data.dataTypeName)
        assertEquals("00000001", data.name)
        assertEquals("http://identifiers.org/bind/00000001", data.url.toString())
    }

    void testQueueUrnForIdentifierResolving() {
        authenticateAsAdmin()
        String urn = "urn:miriam:bind"
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision rev = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        model.addToRevisions(rev)
        assertNotNull(model.save())
        miriamService.queueUrnForIdentifierResolving(urn, rev)
    }

    void testCreateGeneOntologyRelationships() {
        authenticateAsAdmin()
        Model model1 = new Model(name: "test1", vcsIdentifier: "test1.xml")
        Revision revision1 = new Revision(model: model1, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision1.validate())
        model1.addToRevisions(revision1)
        assertTrue(model1.validate())
        assertNotNull(model1.save(flush:true))
        assertNotNull(revision1.save(flush:true))
        Model model2 = new Model(name: "test2", vcsIdentifier: "test2.xml")
        Revision revision2 = new Revision(model: model2, vcsId: "2", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision2.validate())
        model2.addToRevisions(revision2)
        model2.save(flush:true)
        assertNotNull(revision2.save(flush:true))
        assertTrue(model2.validate())
        Model model3 = new Model(name: "test3", vcsIdentifier: "test3.xml")
        Revision revision3 = new Revision(model: model3, vcsId: "3", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision3.validate())
        model3.addToRevisions(revision3)
        assertNotNull(model3.save(flush:true))
        assertNotNull(revision3.save(flush:true))
        assertTrue(model3.validate())
        MiriamDatatype md1 = new MiriamDatatype(identifier: "00000001", name: "BIND", pattern: "^\\d+\$", urn: "urn:miriam:bind")
        assertNotNull(md1.save(flush:true))
        MiriamIdentifier mi1 = new MiriamIdentifier( identifier: "0031387", name: "MPF complex")
        mi1.datatype = md1
        assertNotNull(mi1.save(flush:true))
        MiriamResource mr1 = new MiriamResource(identifier: "xml", location: "identifiers.org", action: "bla")
        mr1.datatype = md1
        assertNotNull(mr1.save(flush:true))
        md1.addToIdentifiers(mi1)
        md1.addToResources(mr1)
        assertNotNull(md1.save(flush:true))
        assertTrue(mi1.validate())
        MiriamDatatype md2 = new MiriamDatatype(identifier: "00000002", name: "ChEBI", pattern: "^CHEBI:\\d+\$", urn: "urn:miriam:obo.chebi")
        assertNotNull(md2.save(flush:true))
        MiriamIdentifier mi2 = new MiriamIdentifier(identifier: "0000097", name: "genetics")
        mi2.datatype = md2
        assertNotNull(mi2.save(flush:true))
        MiriamResource mr2 = new MiriamResource(identifier: "00000006", location: "www.java.net", action: "jsbml")
        mr2.datatype = md2
        assertNotNull(mr2.save(flush:true))
        md2.addToIdentifiers(mi2)
        md2.addToResources(mr2)
        assertNotNull(md2.save(flush:true))
        assertTrue(mi2.validate())
        MiriamDatatype md3 = new MiriamDatatype(identifier: "00000003", name: "Ensembl", pattern: "^ENS[A-Z]*[FPTG]\\d{11}\$", urn: "urn:miriam:ensembl")
        assertNotNull(md3.save(flush:true))
        MiriamIdentifier mi3 = new MiriamIdentifier(identifier: "0000077", name: "identify the identification")
        mi3.datatype = md3
        assertNotNull(mi3.save(flush:true))
        MiriamResource mr3 = new MiriamResource(identifier: "00000976", location: "casetest", action: "sbml")
        mr3.datatype = md3
        assertNotNull(mr3.save(flush:true))
        md3.addToIdentifiers(mi3)
        md3.addToResources(mr3)
        assertNotNull(md3.save(flush:true))
        assertTrue(mi3.validate())
        GeneOntology go1 = new GeneOntology()
        go1.addToRevisions(revision1)
        go1.description = mi1
        assertNotNull(go1.save(flush:true))
        GeneOntology go2 = new GeneOntology()
        go2.addToRevisions(revision2)
        go2.description = mi2
        assertNotNull(go2.save(flush:true))
        GeneOntology go3 = new GeneOntology()
        go3.addToRevisions(revision3)
        go3.description = mi3
        assertNotNull(go3.save(flush:true))
        miriamService.createGeneOntologyRelationships(go2, "2")
    }
}
