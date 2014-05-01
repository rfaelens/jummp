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
import groovy.io.FileType
import net.biomodels.jummp.plugins.pharmml.PharmMlDetector
import org.junit.*

@TestFor(PharmMlService)
class PharmMlServiceTests {

    @Test
    void dummyIsNotAFormat() {
        assertFalse service.areFilesThisFormat(null)
        assertFalse service.areFilesThisFormat([])
        assertFalse service.areFilesThisFormat([new File("this_file_does_not_exist")])
    }

    @Test
    void pharmMLsGetDetected() {
        def bigModel = []
        def baseFolder = new File("test/files/0.2.1/")
        baseFolder.eachFileMatch ~/example.*\.xml/, { File f -> bigModel << f }
        assertTrue service.areFilesThisFormat(bigModel)
        bigModel = []
        // contains a model in SBML
        bigModel << new File("../../test/files/BIOMD0000000272.xml")
        baseFolder.eachFileMatch ~/.*.xml/, { File f -> bigModel << f }
        assertTrue service.areFilesThisFormat(bigModel)
        bigModel = []
        baseFolder = new File("test/files/0.3/")
        baseFolder.eachFileMatch ~/example.*\.xml/, { File f -> bigModel << f }
        assertTrue service.areFilesThisFormat(bigModel)
    }

    @Test
    void iWontBeHacked() {
        def hackFile = new File("nonexistent")
        def hack = new PharmMlDetector(hackFile)
        def hackThread = new Thread(hack)
        hackThread.start()
        hackThread.join()
        assertFalse hack.isRecognisedFormat(hackFile)
    }

    @Test
    void youShallNotPass() {
        def bigModel = [ new File("test/files/iov1_data.txt"),
            new File("test/files/pkmodel_sbml.xml"),
            new File("test/files/pdmodel_sbml.xml")
        ]
        assertFalse service.areFilesThisFormat(bigModel)
        bigModel = [new File("../../test/files/BIOMD0000000272.xml")]
        assertFalse service.areFilesThisFormat(bigModel)

        bigModel << new File("test/files/0.2.1/example2.xml")
        assertTrue service.areFilesThisFormat(bigModel)

        bigModel = [new File("test/files/0.3/example2.xml")]
        assertTrue service.areFilesThisFormat(bigModel)
    }

    @Test
    void modelNameHandlesGarbage() {
        assertEquals "", service.extractName(null)
        assertEquals "", service.extractName([])
        def noModel = [new File("test/files/iov1_data.txt"), new File("test/files/warfarin_conc_pca.csv")]
        assertEquals "", service.extractName(noModel)
    }

    @Test
    void modelNameGetsRetrived() {
        //falls back to empty when no name is provided
        def model = [new File("test/files/pdmodel_sbml.xml"), new File("test/files/iov1_data.txt")]
        assertEquals "", service.extractName(model)
        model = [new File("test/files/0.2.1/example2.xml")]
        assertEquals("Example 2 - simulation continuous PK (Bonate 2012)", service.extractName(model))
        model = []
        def baseFolder = new File("test/files/0.2.1/")
        baseFolder.eachFileMatch FileType.FILES, ~/.*\.xml/, { File f -> model << f }
        def mergedNames = [ "Example 1 - simulation continuous PK/PD",
                "Example 2 - simulation continuous PK (Bonate 2012)",
                "Example 3 - basic Warfarin PK estimation with covariate W",
                "Example 4 - estimation with IOV1 and with covariates",
                "Example 5 - estimation for growth tumor model (Ribba et al. 2012)"
        ]
        // the order of the files may not be preserved.
        String result = service.extractName(model)
        mergedNames.each { name -> assertTrue result.contains(name) }

        model = []
        baseFolder = new File("test/files/0.3/")
        baseFolder.eachFileMatch FileType.FILES, ~/.*\.xml/, { File f -> model << f }
        result = service.extractName(model)
        mergedNames[4] = "Example 5 - estimation for growth tumor model"
        mergedNames.each { name -> assertTrue result.contains(name) }

    }

    @Test
    void modelDescriptionGetsExtracted() {
        assertEquals "", service.extractDescription(null)
        String expected = '''\
based on A Tumor Growth Inhibition Model for Low-Grade Glioma Treated with Chemotherapy or Radiotherapy
        Benjamin Ribba, Gentian Kaloshi, Mathieu Peyre, et al. Clin Cancer Res Published OnlineFirst July 3, 2012.'''
        assertEquals expected, service.extractDescription([new File("test/files/0.2.1/example5.xml")])
        assertEquals expected, service.extractDescription([new File("test/files/0.3/example5.xml")])
        assertEquals '', service.extractDescription([new File("test/files/0.2.1/example1.xml")])
    }

    @Test
    void onlyPharmMLsCanBeValidated() {
        assertFalse service.validate(null)
        def model = []
        assertFalse service.validate(model)
        model = [new File("../../test/files/BIOMD0000000272.xml")]
        assertFalse service.validate(model)

        model = [ "test/files/0.2.1/example1.xml",
            "test/files/0.2.1/example2.xml",
            "test/files/0.2.1/example3.xml",
            "test/files/0.2.1/example5.xml",
        ]
        model.each { pharmML ->
            assertTrue service.validate([new File(pharmML)])
        }

        model = []
        def baseFolder = new File("test/files/0.3/")
        baseFolder.eachFileMatch ~/example.*\.xml/, { File f -> model << f }
        model.each { pharmML ->
            assertTrue service.validate([pharmML])
        }
    }

    @Test
    void modellingStepsAreNotCompulsory() {
        def model = new File("test/files/0.2.1/parameterModel_specExamples.xml")
        assertTrue(model.exists())
        def dom = AbstractPharmMlHandler.getDomFromPharmML(model)
        assertNull(dom.modellingSteps)
        assertEquals([], service.getCommonModellingSteps(dom.modellingSteps, dom.writtenVersion))
        // now try one that does have modellingSteps
        model = new File("test/files/0.2.1/example1.xml")
        assertTrue(model.exists())
        dom = AbstractPharmMlHandler.getDomFromPharmML(model)
        assertNotNull(dom.modellingSteps)
        assertEquals(1, service.getCommonModellingSteps(dom.modellingSteps, dom.writtenVersion).size())
    }
}
