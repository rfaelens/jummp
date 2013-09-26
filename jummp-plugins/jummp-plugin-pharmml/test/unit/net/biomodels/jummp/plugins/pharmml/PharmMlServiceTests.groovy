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
        bigModel << new File("test/files/example2.xml")
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
        model = [new File("test/files/example2.xml")]
        assertEquals("Example 2 - simulation continuous PK (Bonate 2012)", service.extractName(model))
        model = []
        def baseFolder = new File("test/files/")
        baseFolder.eachFileMatch FileType.FILES, ~/.*\.xml/, { File f -> model << f; }
        def mergedNames = [ "Example 1 - simulation continuous PK/PD",
                "Example 2 - simulation continuous PK (Bonate 2012)",
                "Example 3 - basic Warfarin PK estimation with covariate W",
                "Example 4 - estimation with IOV1 and with covariates",
                "Example 5 - estimation for growth tumor model (Ribba et al. 2012)"
        ]
        // the order of the files may not be preserved.
        String result = service.extractName(model)
        mergedNames.each { name -> assertTrue result.contains(name) }
    }

    @Test
    void modelDescriptionGetsExtracted() {
        assertEquals "", service.extractDescription(null)
        // only return a description when it relates to the root, not just any element
        assertEquals "", service.extractDescription([new File("test/files/example1.xml")])
        String expected = '''\
based on A Tumor Growth Inhibition Model for Low-Grade Glioma Treated with Chemotherapy or Radiotherapy
        Benjamin Ribba, Gentian Kaloshi, Mathieu Peyre, et al. Clin Cancer Res Published OnlineFirst July 3, 2012.'''
        assertEquals expected, service.extractDescription([new File("test/files/example5.xml")])
    }

    @Test
    void onlyPharmMLsCanBeValidated() {
        assertFalse service.validate(null)
        def model = []
        assertFalse service.validate(model)
        model = [new File("../../test/files/BIOMD0000000272.xml")]
        assertFalse service.validate(model)

        model = [ "test/files/example1.xml",
            "test/files/example2.xml",
            "test/files/example3.xml",
            "test/files/Ribba_CCR2012.xml",
        ]
        model.each { pharmML ->
            println "${pharmML}: ${service.validate([new File(pharmML)])}"
        }
    }
}
