package net.biomodels.jummp.plugins.pharmml

import grails.test.mixin.*
import net.biomodels.jummp.plugins.pharmml.ObservationEventsMap
import net.biomodels.jummp.plugins.pharmml.PharmMlService
import org.junit.*

public class ObservationEventsMapTests {

    @Test
    void constructorArgumentGetsFilteredCorrectly() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        constructorArgumentHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        constructorArgumentHelper(testFile)
   }

   private void constructorArgumentHelper(File f) {
        def pharmMlService = new PharmMlService()
        def dom = pharmMlService.getDomFromPharmML(f)
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
        def testFile = new File("test/files/0.2.1/example4.xml")
        mapPopulationHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        mapPopulationHelper(testFile)
   }

   private void mapPopulationHelper(File f) {
        def pharmMlService = new PharmMlService()
        def dom = pharmMlService.getDomFromPharmML(f)
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        ObservationEventsMap oem = new ObservationEventsMap(structure.studyEvent)
        def expected = new TreeMap([
            "ep1_a1" : new TreeMap([ "occ1" : "iov"]),
            "ep1_a2" : new TreeMap([ "occ1" : "iov"]),
            "ep3_a1" : new TreeMap([ "occ2" : "iov"]),
            "ep3_a2" : new TreeMap([ "occ2" : "iov"])
        ])
        assertEquals(expected, oem.getObservationMap())
   }

    @Test
    void getArmsAndEpochsWork() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        armsEpochsTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        armsEpochsTestHelper(testFile)
   }

   private void armsEpochsTestHelper(File f) {
        def pharmMlService = new PharmMlService()
        def dom = pharmMlService.getDomFromPharmML(f)
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        ObservationEventsMap oem = new ObservationEventsMap(structure.studyEvent)
        assertEquals(["a1", "a2"], oem.getArms())
        assertEquals(["ep1", "ep3"], oem.getEpochs())
   }
}

