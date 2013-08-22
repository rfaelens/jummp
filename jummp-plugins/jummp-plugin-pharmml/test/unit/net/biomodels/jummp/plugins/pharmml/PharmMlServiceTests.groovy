package net.biomodels.jummp.plugins.pharmml

import grails.test.mixin.*
import groovy.io.FileType
import org.junit.*
import net.biomodels.jummp.plugins.pharmml.PharmMlDetector

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
        def baseFolder = new File("test/files/")
        baseFolder.eachFileMatch ~/example.*\.xml/, { File f -> bigModel << f; }
        assertTrue service.areFilesThisFormat(bigModel)
        bigModel = []
        // contains a couple of models in SBML
        baseFolder.eachFileMatch ~/.*.xml/, { File f -> bigModel << f; }
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
        bigModel << new File("test/files/example9.xml")
        assertTrue service.areFilesThisFormat(bigModel)
    }

    @Test
    void pharmMLsCanIncludeSbmlAndTxt() {
        def bigModel = []
        def baseFolder = new File("test/files/")
        //includes two sbml files
        baseFolder.eachFileMatch FileType.FILES, ~/.*\.xml/, { File f -> bigModel << f; }
        bigModel << new File("test/files/iov1_data.txt")
        assertTrue service.areFilesThisFormat(bigModel)
        bigModel << new File("test/files/warfarin_conc_pca.csv")
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
        //falls-back to empty when no name is provided
        def model = [new File("test/files/pdmodel_sbml.xml"), new File("test/files/iov1_data.txt")]
        assertEquals "", service.extractName(model)
        model = [new File("test/files/example2.xml")]
        assertEquals "CTS1 example - continuous PK/PD", service.extractName(model)
        model = []
        def baseFolder = new File("test/files/")
        baseFolder.eachFileMatch FileType.FILES, ~/.*\.xml/, { File f -> model << f; }
        def mergedNames = [ "IOV1 with covariates",
                "CTS1 example - continuous PK/PD", 
                "Warfarin example Corresponds to WP3 PK_PRED use case",
                "Chan, Nutt, Holford 2005 Parkinson paper",
                "IOV1 with covariates",
                "BradshawPierce"
        ]
        // the order of the files may not be preserved.
        String result = service.extractName(model)
        mergedNames.each { name ->
            assertTrue result.contains(name)
        }
    }
}
