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
import net.biomodels.jummp.plugins.pharmml.TrialDesignStructure
import org.junit.*

public class TrialDesignStructureTests {

    @Test
    void defaultConstructorIsPrivate() {
        def structure
        try {
            structure = new TrialDesignStructure()
            fail("Should not be able to pass a null TrialDesignMatrix")
        } catch (UnsupportedOperationException e) {
            println("The default constructor for TrialDesignStructure is private.")
            assertNull(structure)
        }
   }

   @Test
   void constructorArgumentsGetFilteredCorrectly() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        assertTrue testFile.exists()
        constructorArgumentsTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        assertTrue testFile.exists()
        constructorArgumentsTestHelper(testFile)
   }

   private void constructorArgumentsTestHelper(File f) {
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        assertNotNull dom
        assertNotNull dom.trialDesign
        def structure = dom.trialDesign.structure
        assertNotNull(structure)
        //test each constructor arg for null and empty values
        List[] args = new List[4]
        args[0] = structure.arm
        args[1] = structure.epoch
        args[2] = structure.cell
        args[3] = structure.segment
        args.each { assertTrue(nonEmptyList(it)) }
        args.eachWithIndex { o, i ->
            def properArg = args[i]
            def td_struct
            args[i] = null
            try {
                td_struct = new TrialDesignStructure(args[0], args[1], args[2], args[3])
                fail("Expected an exception due to null arg${i}.")
            } catch (IllegalArgumentException e) {
                try {
                    assertNull(td_struct)
                    args[i] = []
                    td_struct = new TrialDesignStructure(args[0], args[1], args[2], args[3])
                    fail("Expected an exception due to empty arg${i}.")
                } catch(IllegalArgumentException ex) {
                    assertNull(td_struct)
                }
            } finally {
                args[i] = properArg
                td_struct = new TrialDesignStructure(args[0], args[1], args[2], args[3])
                assertNotNull(td_struct)
            }
        }
   }

   @Test
   void cellMatrixFilledCorrectly() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        cellMatrixTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        cellMatrixTestHelper(testFile)
   }

   private void cellMatrixTestHelper(File f) {
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        def tds = dom.trialDesign.structure
        def structure = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        assertTrue(structure.allCellsDefined())
        def expected = [
                "ep1_a1": ["ta"],
                "ep2_a1": ["wash"],
                "ep3_a1": ["tb"],
                "ep1_a2": ["tb"],
                "ep2_a2": ["wash"],
                "ep3_a2": ["ta"]
        ]
        assertEquals(expected, structure.trialDesignStructure)
   }

   @Test
   void findByEpochWorks() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        findByEpochTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        findByEpochTestHelper(testFile)
   }

   private void findByEpochTestHelper(File f) {
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        def tds = dom.trialDesign.structure
        def structure = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        def expected = [
            "ep1" : ["ta", "tb"],
            "ep2" : ["wash", "wash"],
            "ep3" : ["tb", "ta"]
        ]
        def out     = [:]
        def refOut  = [:]
        expected.each { it ->
            out[it.key]     = structure.findSegmentsByEpoch(it.key).collect{it.oid}
            refOut[it.key]  = structure.findSegmentRefsByEpoch(it.key)
        }
        assertEquals(expected, out)
        assertEquals(expected, refOut)
        assertTrue(structure.findSegmentsByEpoch("").size() == 0)
        assertTrue(structure.findSegmentsByEpoch(null).size() == 0)
   }

   @Test
   void findByArmWorks() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        findByArmTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        findByArmTestHelper(testFile)
   }

   private void findByArmTestHelper(File f) {
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        def tds = dom.trialDesign.structure
        def structure = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        def expected = [
            "a1" : ["ta", "wash", "tb"],
            "a2" : ["tb", "wash", "ta"]
        ]
        def out     = [:]
        def refOut  = [:]
        expected.each { it ->
            out[it.key]     = structure.findSegmentsByArm(it.key).collect{it.oid}
            refOut[it.key]  = structure.findSegmentRefsByArm(it.key)
        }
        assertEquals(expected, out)
        assertEquals(expected, refOut)
        assertTrue(structure.findSegmentsByArm("").size() == 0)
        assertTrue(structure.findSegmentsByArm(null).size() == 0)
   }

   @Test
   void iteratorWorks() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        iteratorTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        iteratorTestHelper(testFile)
   }

   private void iteratorTestHelper(File f) {
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        def tds = dom.trialDesign.structure
        def structure = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        def iStructure = structure.iterator()
        assertNotNull(iStructure)
        assertTrue(iStructure.hasNext())
        def iteratorOutput = []
        while (iStructure.hasNext()) {
            iteratorOutput.add(iStructure.next())
        }
        assertEquals(structure.trialDesignStructure.size(), iteratorOutput.size())
        assertEquals(structure.trialDesignStructure.entrySet().toList(), iteratorOutput)
   }

   @Test
    void getArmsAndEpochsWork() {
        def testFile = new File("test/files/0.2.1/example4.xml")
        getArmsAndEpochsTestHelper(testFile)
        testFile = new File("test/files/0.3/example4.xml")
        getArmsAndEpochsTestHelper(testFile)
   }

   private void getArmsAndEpochsTestHelper(File f) {
        def dom = AbstractPharmMlHandler.getDomFromPharmML(f)
        def tds = dom.trialDesign.structure
        def struct = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        assertEquals(["a1", "a2"] as Set, struct.getArmRefs())
        assertEquals(["ep1", "ep2", "ep3"] as Set, struct.getEpochRefs())
   }

   boolean nonEmptyList(List l) {
       return l && l.size() > 0
   }
}

