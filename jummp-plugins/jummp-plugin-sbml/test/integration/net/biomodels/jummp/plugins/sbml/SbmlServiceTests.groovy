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
* Apache Commons, Grails (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, Grails used as well as
* that of the covered work.}
**/





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
