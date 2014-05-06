/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
* Xerces, LibPharmml, Grails, JUnit (or a modified version of that library), containing parts
* covered by the terms of Common Public License, Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Xerces, LibPharmml, Grails, JUnit used as well as
* that of the covered work.}
**/

package net.biomodels.jummp.plugins.pharmml

import grails.test.mixin.*
import net.biomodels.jummp.plugins.pharmml.ObservationEventsMap
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
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
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
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
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
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        ObservationEventsMap oem = new ObservationEventsMap(structure.studyEvent)
        assertEquals(["a1", "a2"], oem.getArms())
        assertEquals(["ep1", "ep3"], oem.getEpochs())
   }
}

