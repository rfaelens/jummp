package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.IErrorHandler;
import eu.ddmore.libpharmml.IMarshaller;
import eu.ddmore.libpharmml.dom.PharmML;
import grails.test.mixin.*
import groovy.io.FileType
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import net.biomodels.jummp.plugins.pharmml.PharmMlDetector
import org.apache.xerces.util.XMLCatalogResolver;
import org.junit.*
import org.xml.sax.SAXException;

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

    @Test
    void onlyPharmMLsCanBeValidated() {
        assertFalse service.validate(null)
        def model = []
        assertFalse service.validate(model)
        model = [new File("test/files/iov1_data.txt")]
        assertFalse service.validate(model)

        model = []
        def baseFolder = new File("test/files/")
        baseFolder.eachFileMatch ~/example.*\.xml/, { File f -> model << f; }
        //test them sequentially because the API only handles resources individually
        model.each { pharmML ->
            assertTrue(service.validate([pharmML]))
        }
    }
}
