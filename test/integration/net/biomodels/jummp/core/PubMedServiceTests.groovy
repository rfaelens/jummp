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
* JUnit (or a modified version of that library), containing parts
* covered by the terms of Common Public License, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JUnit used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.model.Publication

class PubMedServiceTests extends JummpIntegrationTest {
    def pubMedService
    
    @Before
    void setUp() {
    }

    @After
    void tearDown() {
    }

    @Test
    void testFetchPublicationData() {
        // publication: Science   (ISSN: 0036-8075)   (ESSN: 1095-9203)
        String id = "20488988"
        Publication publication = pubMedService.fetchPublicationData(id)
        assertNotNull(publication)
        publication.validate()
        assertTrue(publication.validate())
        assertEquals("Science (New York, N.Y.)", publication.journal)
        assertEquals(2010, publication.year)
        assertEquals("6", publication.month)
        //assertEquals(11, publication.day) DONT ALWAYS GET BACK DAY FROM NEW PUBMED SERVICE
        assertEquals(328, publication.volume)
        assertEquals(5984, publication.issue)
        assertEquals("1404-1408", publication.pages)
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
        assertEquals("0",publication.month) 
        assertNull(publication.issue)

        // should cause a SAXParseException
        shouldFail(JummpException) {
        pubMedService.fetchPublicationData("0")
        }
    }
}
