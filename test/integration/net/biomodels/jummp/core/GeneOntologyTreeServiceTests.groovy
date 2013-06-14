package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.core.miriam.MiriamDatatype
import net.biomodels.jummp.core.miriam.GeneOntologyTreeLevel
import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.MiriamIdentifier
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship
import net.biomodels.jummp.core.miriam.GeneOntologyRelationshipType
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.miriam.MiriamResource

class GeneOntologyTreeServiceTests extends JummpIntegrationTest {
    def geneOntologyTreeService

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
    void testTreeLevel() {
        authenticateAsAdmin()
        Model model1 = new Model(name: "test1", vcsIdentifier: "test1.xml")
        Revision revision1 = new Revision(model: model1, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("admin"), minorRevision: false, name:"",description:"", comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision1.validate())
        model1.addToRevisions(revision1)
        assertTrue(model1.validate())
        assertNotNull(model1.save(flush:true))
        assertNotNull(revision1.save(flush:true))
        Model model2 = new Model(name: "test2", vcsIdentifier: "test2.xml")
        Revision revision2 = new Revision(model: model2, vcsId: "2", revisionNumber: 1, owner: User.findByUsername("admin"), minorRevision: false, name:"",description:"", comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision2.validate())
        model2.addToRevisions(revision2)
        assertNotNull(model2.save(flush:true))
        assertNotNull(revision2.save(flush:true))
        assertTrue(model2.validate())
        Model model3 = new Model(name: "test3", vcsIdentifier: "test3.xml")
        Revision revision3 = new Revision(model: model3, vcsId: "3", revisionNumber: 1, owner: User.findByUsername("admin"), minorRevision: false, name:"",description:"", comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision3.validate())
        model3.addToRevisions(revision3)
        assertNotNull(model3.save(flush:true))
        assertNotNull(revision3.save(flush:true))
        assertTrue(model3.validate())
        MiriamDatatype md1 = new MiriamDatatype(identifier: "MIR:00000001", name: "BIND", pattern: "^\\d+\$", urn: "urn:miriam:bind")
        assertNotNull(md1.save(flush:true))
        MiriamIdentifier mi1 = new MiriamIdentifier( identifier: "0031387", name: "MPF complex")
        mi1.datatype = md1
        assertNotNull(mi1.save(flush:true))
        MiriamResource mr1 = new MiriamResource(identifier: "xml", location: "www.test.de", action: "bla")
        mr1.datatype = md1
        assertNotNull(mr1.save(flush:true))
        md1.addToIdentifiers(mi1)
        md1.addToResources(mr1)
        assertNotNull(md1.save(flush:true))
        assertTrue(mi1.validate())
        MiriamDatatype md2 = new MiriamDatatype(identifier: "MIR:00000002", name: "ChEBI", pattern: "^CHEBI:\\d+\$", urn: "urn:miriam:obo.chebi")
        assertNotNull(md2.save(flush:true))
        MiriamIdentifier mi2 = new MiriamIdentifier(identifier: "0000097", name: "shappi komplott")
        mi2.datatype = md2
        assertNotNull(mi2.save(flush:true))
        MiriamResource mr2 = new MiriamResource(identifier: "blubber", location: "www.java.dut.net", action: "inder.ned")
        mr2.datatype = md2
        assertNotNull(mr2.save(flush:true))
        md2.addToIdentifiers(mi2)
        md2.addToResources(mr2)
        assertNotNull(md2.save(flush:true))
        assertTrue(mi2.validate())
        MiriamDatatype md3 = new MiriamDatatype(identifier: "MIR:00000003", name: "Ensembl", pattern: "^ENS[A-Z]*[FPTG]\\d{11}\$", urn: "urn:miriam:ensembl")
        assertNotNull(md3.save(flush:true))
        MiriamIdentifier mi3 = new MiriamIdentifier(identifier: "0000077", name: "identify the identification")
        mi3.datatype = md3
        assertNotNull(mi3.save(flush:true))
        MiriamResource mr3 = new MiriamResource(identifier: "identify yourself!", location: "www.roggnroll.com", action: "inder.ned")
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
        GeneOntologyRelationshipType gort = GeneOntologyRelationshipType.IsA
        GeneOntologyRelationship gor1 = new GeneOntologyRelationship(from: go1, to: go2, type: gort)
        gor1.type = gort
        assertNotNull(gor1.save(flush:true))
        go1.addToRelationships(gor1)
        assertNotNull(go1.save(flush:true))
        GeneOntologyRelationship gor2 = new GeneOntologyRelationship(from: go2, to: go3, type: gort)
        gor2.type = gort
        assertNotNull(gor2.save(flush:true))
        go2.addToRelationships(gor2)
        assertNotNull(go2.save(flush:true))
        GeneOntologyRelationship gor3 = new GeneOntologyRelationship(from: go1, to: go3, type: gort)
        gor3.type = gort
        assertNotNull(gor3.save(flush:true))
        go3.addToRelationships(gor3)
        assertNotNull(go3.save(flush:true))
        assertNotNull(model1.save(flush:true))
        assertNotNull(model2.save(flush:true))
        assertNotNull(model3.save(flush:true))
        // This would work but start a thread by which it cannot be exactly said when it is finished.
        // Long gold = 1
        // GeneOntologyTreeLevel gotl = geneOntologyTreeService.treeLevel(gold)
    }
}

