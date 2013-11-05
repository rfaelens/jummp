package net.biomodels.jummp.plugins.pharmml

import grails.test.mixin.*
import net.biomodels.jummp.plugins.pharmml.TrialDesignStructure
import net.biomodels.jummp.plugins.pharmml.PharmMlService
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
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
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
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
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
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
        def tds = dom.trialDesign.structure
        def structure = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        def expected = [
            "ep1" : ["ta", "tb"],
            "ep2" : ["wash", "wash"],
            "ep3" : ["tb", "ta"]
        ]
        def out = [:]
        expected.each { it ->
            out[it.key] = structure.findByEpoch(it.key).collect{it.oid}
        }
        assertEquals(expected, out)
        assertTrue(structure.findByEpoch("").size() == 0)
        assertTrue(structure.findByEpoch(null).size() == 0)
   }

   @Test
   void findByArmWorks() {
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
        def tds = dom.trialDesign.structure
        def structure = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        def expected = [
            "a1" : ["ta", "wash", "tb"],
            "a2" : ["tb", "wash", "ta"]
        ]
        def out = [:]
        expected.each { it ->
            out[it.key] = structure.findByArm(it.key).collect{it.oid}
        }
        assertEquals(expected, out)
        assertTrue(structure.findByArm("").size() == 0)
        assertTrue(structure.findByArm(null).size() == 0)
   }

   @Test
   void iteratorWorks() {
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
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
        def pharmMlService = new PharmMlService()
        def testFile = new File("test/files/example4.xml")
        def dom = pharmMlService.getDomFromPharmML(testFile)
        def tds = dom.trialDesign.structure
        def struct = new TrialDesignStructure(tds.arm, tds.epoch, tds.cell, tds.segment)
        assertEquals(["a1", "a2"] as Set, struct.getArmRefs())
        assertEquals(["ep1", "ep2", "ep3"] as Set, struct.getEpochRefs())
   }

   boolean nonEmptyList(List l) {
       return l && l.size() > 0
   }
}

