package net.biomodels.jummp.plugins.pharmml

import java.nio.file.Files
import java.nio.file.Path
import net.biomodels.jummp.core.RunnableModelFormatDetector
import net.biomodels.jummp.core.util.JummpXmlUtils
import net.biomodels.jummp.plugins.sbml.SbmlService

public class PharmMlDetector implements RunnableModelFormatDetector {
    /**
     * The file to perform the checks on.
     */
    private final File modelFile
    private boolean isPharmML
    private boolean hasBeenRun

    /**
     * Constructs a PharmMlDetector for @p file
     * @param file The file whose model format should be determined.
     */
    public PharmMlDetector(final File file) {
        modelFile = file
    }

    /*
     * Keep default constructor private.
     */
    private PharmMlDetector() {
    }

    /**
     * @see java.lang.Runnable#run()
     */
    void run() {
        isRecognisedFormat(modelFile)
    }

    /**
     * Determines whether @p theFile appears to be a model in PharmML.
     * This method makes no guarantee that the file is a valid PharmML document.
     * @param theFile the file on which to perform the test.
     * @return true if the file contains a PharmML document, false otherwise.
     * @see net.biomodels.jummp.core.RunnableModelFormatDetector#isRecognisedFormat(File)
     */
    public boolean isRecognisedFormat(final File theFile) {
        if (!theFile || !theFile.canRead()) {
            hasBeenRun = true
            isPharmML = false
            return false
        }
        return hasBeenRun ? isPharmML : determineFormat(theFile)
    }

    private boolean determineFormat(final File theFile) {
        //validation has already been done
        assert theFile && theFile.canRead()
        final Path path = theFile.toPath()
        final String CONTENT_TYPE = Files.probeContentType(path)
        if ("application/xml".equals(CONTENT_TYPE)) {
            String acceptedNamespace = JummpXmlUtils.findModelAttribute theFile, "PharmML", "xmlns"
            if (!acceptedNamespace) {
                acceptedNamespace = JummpXmlUtils.findModelAttribute theFile, "sbml", "xmlns"
            }
            if (!acceptedNamespace) {
                if (IS_INFO_ENABLED) {
                    log.info "File ${theFile.name} does not include PharmML or SBML namespace declaration."
                }
                hasBeenRun = true
                isPharmML = false
                return false
            }
        } else if ((!"text/plain".equals(CONTENT_TYPE)) && (!"text/csv".equals(CONTENT_TYPE))) {
            //data sources include CSV and TSV files
            hasBeenRun = true
            isPharmML = false
            return false
        }
        //set these to these true so that we don't need to run multiple times
        hasBeenRun = true
        isPharmML = true
        return true
    }
}
