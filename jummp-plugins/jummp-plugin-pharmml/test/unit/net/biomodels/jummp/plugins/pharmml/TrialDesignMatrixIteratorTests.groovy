package net.biomodels.jummp.plugins.pharmml

import grails.test.mixin.*
import net.biomodels.jummp.plugins.pharmml.TrialDesignMatrix
import net.biomodels.jummp.plugins.pharmml.TrialDesignMatrixIterator
import org.junit.*

public class TrialDesignMatrixIteratorTests {

    @Test
    void nullMatrix() {
        try {
            new TrialDesignMatrixIterator(null)
            fail("Should not be able to pass a null TrialDesignMatrix")
        } catch (IllegalArgumentException e) {
            assertEquals(e.message,
                    "TrialDesignMatrixIterator cannot iterate over an undefined matrix.")
        }
   }

   void coolMatrix() {
        def pharmMlService = mockFor(pharmMlService)
        assertNotNull pharmMlService
   }
}
