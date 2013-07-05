package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.plugins.sbml.SbmlService
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.UnknownFormatService
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.core.model.ModelFormatTransportCommand


class ModelFileFormatServiceTests {
    def modelFileFormatService
    def grailsApplication
    @Override
    @Before
    void setUp() {
        FileUtils.deleteDirectory(new File("target/sbml"))
        assertTrue(new File("target/sbml").mkdirs())
    }

    @Override
    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/sbml"))
        grailsApplication.config.jummp.plugins.sbml.validation = false
    }

    @Test
    void testServiceForFormat() {
        // unknown format should return null
        def dontKnowThisFormatService=modelFileFormatService.serviceForFormat(ModelFormat.findByIdentifier("UNKNOWN"))
        assertNotNull(dontKnowThisFormatService)
        // for sbml it needs to be a SbmlService
        def formatService = modelFileFormatService.serviceForFormat(ModelFormat.findByIdentifier("SBML"))
        assertNotNull(formatService)
        assertTrue(formatService instanceof FileFormatService)
        assertTrue(formatService instanceof SbmlService)
        assertTrue(dontKnowThisFormatService instanceof UnknownFormatService)
    }

    @Test
    void testInfer() {
        // for null, there is no format
        assertNull(modelFileFormatService.inferModelFormat(null))

        // an unknown format file
        File validUnknown = new File("target/sbml/unknown")
        validUnknown.setText('What is my name')
        assertEquals(modelFileFormatService.inferModelFormat([validUnknown]).identifier, "UNKNOWN")

        // an SBML file should be detected. Make less restrictive to accept invalid SBML files
        File validSbml = new File("target/sbml/validSbml")
        validSbml.setText('''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')
        assertEquals(modelFileFormatService.inferModelFormat([validSbml]).identifier, "SBML")
    }

    @Test
    void testValidate() {
        grailsApplication.config.jummp.plugins.sbml.validation = true
        // for unknown file type this should evaluate to false
        assertFalse(modelFileFormatService.validate(null, ModelFormat.findByIdentifier("UNKNOWN")))

        // a valid unknown format file
        File validUnknown = new File("target/sbml/unknown")
        validUnknown.setText('What is my name')
        assertTrue(modelFileFormatService.validate([validUnknown], ModelFormat.findByIdentifier("UNKNOWN")))

        // for an invalid sbml file it should also evaluate to false
        File invalidSbml = new File("target/sbml/unknown")
        FileUtils.deleteQuietly(invalidSbml)
        FileUtils.touch(invalidSbml)
        invalidSbml.append('''<?xml version='1.0' encoding='UTF-8'?>
<sbml level="99" version="1">
  <model/>
</sbml>''')
        assertFalse(modelFileFormatService.validate([invalidSbml], ModelFormat.findByIdentifier("SBML")))
        // and for a valid SBML file it should be true
        File validSbml = new File("target/sbml/validSbml")
        FileUtils.deleteQuietly(validSbml)
        FileUtils.touch(validSbml)
        validSbml.append('''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')
        assertTrue(modelFileFormatService.validate([validSbml], ModelFormat.findByIdentifier("SBML")))
    }

    @Test
    void testExtractName() {
        // for unknown format it's empty
        assertEquals("", modelFileFormatService.extractName(null, ModelFormat.findByIdentifier("UNKNOWN")))
        // TODO: in sbmlService it's not yet implemented and needs a test file
    }
}
