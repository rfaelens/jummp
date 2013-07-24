package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.model.Publication

class PubMedServiceTests extends JummpIntegrationTest {
    def pubMedService
    @Override
    @Before
    void setUp() {
    }

    @Override
    @After
    void tearDown() {
    }

    @Ignore @Test
    void testFetchPublicationData() {
        // publication: Science   (ISSN: 0036-8075)   (ESSN: 1095-9203)
        String id = "20488988"
        Publication publication = pubMedService.fetchPublicationData(id)
        assertNotNull(publication)
        publication.validate()
        assertTrue(publication.validate())
        assertEquals("Science", publication.journal)
        assertEquals(2010, publication.year)
        assertEquals("Jun", publication.month)
        assertEquals(11, publication.day)
        assertEquals(328, publication.volume)
        assertEquals(5984, publication.issue)
        assertEquals("1404-8", publication.pages)
        assertEquals("Covering a broad dynamic range: information processing at the erythropoietin receptor.", publication.title)
        assertEquals("Division Systems Biology of Signal Transduction, DKFZ-ZMBH Alliance, German Cancer Research Center, 69120 Heidelberg, Germany.", publication.affiliation)
        assertEquals("Cell surface receptors convert extracellular cues into receptor activation, thereby triggering intracellular signaling networks and controlling cellular decisions. A major unresolved issue is the identification of receptor properties that critically determine processing of ligand-encoded information. We show by mathematical modeling of quantitative data and experimental validation that rapid ligand depletion and replenishment of the cell surface receptor are characteristic features of the erythropoietin (Epo) receptor (EpoR). The amount of Epo-EpoR complexes and EpoR activation integrated over time corresponds linearly to ligand input; this process is carried out over a broad range of ligand concentrations. This relation depends solely on EpoR turnover independent of ligand binding, which suggests an essential role of large intracellular receptor pools. These receptor properties enable the system to cope with basal and acute demand in the hematopoietic system.", publication.synopsis)
        // TODO: add tests for author

        // test for 12974500 - no day specified
        publication = pubMedService.fetchPublicationData("12974500")
        assertNull(publication.day)

        // test for 20955552 - no month and no issue
        publication = pubMedService.fetchPublicationData("20955552")
        assertNull(publication.day)
        assertNull(publication.month)
        assertNull(publication.issue)

        // should cause a SAXParseException
        shouldFail(JummpException) {
        pubMedService.fetchPublicationData("0")
        }
    }
}
