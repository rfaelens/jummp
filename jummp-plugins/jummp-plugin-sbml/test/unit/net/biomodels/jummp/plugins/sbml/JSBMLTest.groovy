package net.biomodels.jummp.plugins.sbml

import org.junit.Test
import org.sbml.jsbml.ListOf
import org.sbml.jsbml.Model
import org.sbml.jsbml.SBMLDocument
import org.sbml.jsbml.SBMLReader
import org.sbml.jsbml.ext.fbc.FBCModelPlugin
import org.sbml.jsbml.ext.fbc.FluxBound

import static org.junit.Assert.*

/**
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 * Date: 27/01/15
 */
class JSBMLTest {

    @Test
    void ensureTestFilesExist() {
        File f = new File("test/files/BIOMD0000000272.xml")
        assertTrue f.exists()
        f = new File("test/files/fbc_example1.xml")
        assertTrue f.exists()
    }

    @Test
    void loadL2V4Model() {
        File f = new File("test/files/BIOMD0000000272.xml")
        def is = new BufferedInputStream(new FileInputStream(f))
        Model m = new SBMLReader().readSBMLFromStream(is).getModel()
        assertNotNull m
    }

    @Test
    void loadFBCModel() {
        File f = new File("test/files/fbc_example1.xml")
        SBMLDocument d = new SBMLReader().readSBML(f)
        Model m = d.getModel()
        assertNotNull m
        assertTrue m.isSetPlugin("fbc")
        FBCModelPlugin plugin = (FBCModelPlugin) m.getPlugin("fbc")
        assertNotNull(plugin)
        ListOf<FluxBound> fluxBoundsList = plugin.getListOfFluxBounds()
        assertEquals(1, fluxBoundsList.size())
        FluxBound fb = fluxBoundsList.get(0)
        assertEquals("bound1", fb.getId())
        assertEquals("J0", fb.getReaction())
        assertEquals("EQUAL", fb.getOperation().name())
        assertTrue(10.0d == fb.getValue())
    }
}
