package net.biomodels.jummp.plugins.sbml

import org.junit.Test

import static org.junit.Assert.*

/**
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 * Date: 27/01/15
 */
class JSBMLTest {

    @Test
    void jsbmlDoesNotThrowErrors() {
        File f = new File("test/files/BIOMD0000000272.xml")
        assertTrue f.exists()
    }
}
