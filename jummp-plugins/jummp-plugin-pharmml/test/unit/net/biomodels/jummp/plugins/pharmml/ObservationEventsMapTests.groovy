package net.biomodels.jummp.plugins.pharmml

import grails.test.mixin.*
import net.biomodels.jummp.plugins.pharmml.ObservationEventsMap
import net.biomodels.jummp.plugins.pharmml.PharmMlService
import org.junit.*

public class ObservationEventsMapTests {

    @Test
    void constructorArgumentGetsFilteredCorrectly() {
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        ObservationEventsMap oem
       try {
            oem = new ObservationEventsMap(null)
            fail("Expected an exception due to null studyEvent.")
        } catch (IllegalArgumentException e) {
            try {
                assertNull(oem)
                oem = new ObservationEventsMap([])
                fail("Expected an exception due to empty studyEvent.")
            } catch(IllegalArgumentException ex) {
                assertNull(oem)
            }
        } finally {
            oem = new ObservationEventsMap(structure.studyEvent)
            assertNotNull(oem)
        }
   }

    @Test
    void mapGetsPopulatedCorrectly() {
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        ObservationEventsMap oem = new ObservationEventsMap(structure.studyEvent)
        def expected = new TreeMap([
            "ep1_a1" : new TreeMap([ "occ1" : "iov1"]),
            "ep1_a2" : new TreeMap([ "occ1" : "iov1"]),
            "ep3_a1" : new TreeMap([ "occ2" : "iov1"]),
            "ep3_a2" : new TreeMap([ "occ2" : "iov1"])
        ])
        assertEquals(expected, oem.getObservationMap())
   }

    @Test
    void getArmsAndEpochsWork() {
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        ObservationEventsMap oem = new ObservationEventsMap(structure.studyEvent)
        assertEquals(["a1", "a2"], oem.getArms())
        assertEquals(["ep1", "ep3"], oem.getEpochs())
   }
}

