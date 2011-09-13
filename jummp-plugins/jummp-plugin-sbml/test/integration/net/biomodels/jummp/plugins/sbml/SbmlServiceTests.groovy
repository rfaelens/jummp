package net.biomodels.jummp.plugins.sbml

import grails.test.*
import org.apache.commons.io.FileUtils

class SbmlServiceTests extends GrailsUnitTestCase {
    def sbmlService
    def grailsApplication
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
        // trying to delete the directory causes a crash. Seems like jsbml does not close the files!
        grailsApplication.config.jummp.plugins.sbml.validate = false
    }

    void testValidate() {
        grailsApplication.config.jummp.plugins.sbml.validate = true
        // we do not want to test whether the library works correctly
        // we only need to check that an invalid file is marked as invalid and a valid file is marked as valid.
        // test empty file
        File file = new File("target/sbml/test")
        FileUtils.deleteQuietly(file)
        FileUtils.touch(file)
        assertFalse(sbmlService.validate(file))

        // unknown sbml
        File unknownSbml = new File("target/sbml/unknown")
        FileUtils.deleteQuietly(unknownSbml)
        FileUtils.touch(unknownSbml)
        unknownSbml.append('''<?xml version='1.0' encoding='UTF-8'?>
<sbml level="99" version="1">
  <model/>
</sbml>''')
        assertFalse(sbmlService.validate(unknownSbml))

        // TODO: we need test files for errors reported by jsbml

        // test valid file
        File validFile = new File("target/sbml/valid")
        FileUtils.deleteQuietly(validFile)
        FileUtils.touch(validFile)
        validFile.append('''<?xml version="1.0" encoding="UTF-8"?>
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
        assertTrue(sbmlService.validate(validFile))
    }
}
