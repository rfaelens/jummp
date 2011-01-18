package net.biomodels.jummp.core

import grails.test.*
import net.biomodels.jummp.core.model.ModelFormat
import net.biomodels.jummp.plugins.sbml.SbmlService
import net.biomodels.jummp.core.model.FileFormatService
import org.apache.commons.io.FileUtils

class ModelFileFormatServiceTests extends JummpIntegrationTestCase {
    def modelFileFormatService
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testServiceForFormat() {
        // unknown format should return null
        assertNull(modelFileFormatService.serviceForFormat(ModelFormat.UNKNOWN))
        // for sbml it needs to be a SbmlService
        def formatService = modelFileFormatService.serviceForFormat(ModelFormat.SBML)
        assertNotNull(formatService)
        assertTrue(formatService instanceof FileFormatService)
        assertTrue(formatService instanceof SbmlService)
    }

    void testValidate() {
        // for unknown file type this should evaluate to false
        assertFalse(modelFileFormatService.validate(null, ModelFormat.UNKNOWN))
        // for an invalid sbml file it should also evaluate to false
        File invalidSbml = new File("target/sbml/unknown")
        FileUtils.deleteQuietly(invalidSbml)
        FileUtils.touch(invalidSbml)
        invalidSbml.append('''<?xml version='1.0' encoding='UTF-8'?>
<sbml level="99" version="1">
  <model/>
</sbml>''')
        assertFalse(modelFileFormatService.validate(invalidSbml, ModelFormat.SBML))
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
        assertTrue(modelFileFormatService.validate(validSbml, ModelFormat.SBML))
    }

    void testExtractName() {
        // for unknown format it's empty
        assertEquals("", modelFileFormatService.extractName(null, ModelFormat.UNKNOWN))
        // TODO: in sbmlService it's not yet implemented and needs a test file
    }
}
