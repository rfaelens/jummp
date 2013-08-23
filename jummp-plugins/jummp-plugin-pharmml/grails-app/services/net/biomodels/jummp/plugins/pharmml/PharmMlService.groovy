package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.*
import eu.ddmore.libpharmml.impl.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.util.JummpXmlUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled
import org.apache.xerces.util.XMLCatalogResolver;
import org.xml.sax.SAXException;


/**
 * Service class containing the logic to handle models encoded in PharmML.
 * @see net.biomodels.jummp.core.model.FileFormatService
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMlService implements FileFormatService {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    @Profiled(tag="pharmMlService.validate")
    public boolean validate(List<File> model) {
        if (!model) {
            if (IS_INFO_ENABLED) {
                log.info "Refusing to validate an undefined list of files as a PharmML submission."
            }
            return false
        }
        model = model.findAll{it && it.canRead()}
        if (IS_INFO_ENABLED) {
            log.info "Validating ${model.inspect()} as a PharmML submission."
        }

        //temporarily ignore everything else apart from the PharmML file as current libPharmML implementation can't cope
        def fileQueue = new ConcurrentLinkedQueue(model)
        AtomicBoolean stillLooking = new AtomicBoolean(true)
        ThreadFactory threadFactory = Executors.defaultThreadFactory()
        def iFiles = fileQueue.iterator()
        File pharmMlFile
        while (iFiles.hasNext() && stillLooking.get()) {
            final File file = iFiles.next()
            PharmMlDetector detective = new PharmMlDetector(file)
            Thread detectiveThread = threadFactory.newThread(detective)
            if (detectiveThread) {
                detectiveThread.setName("pharmML validation for ${file.name}")
                detectiveThread.start()
                detectiveThread.join()
                if (detective.isRecognisedFormat(file)) {
                    stillLooking.set(true)
                    pharmMlFile = file
                }
            } else {
                log.error "pharmMlService.validate: Cannot start detection thread for file ${file.name}"
                throw new RuntimeException("Something went wrong when we tried to validate ${file.name}")
                return false
            }
        }

        if (!pharmMlFile) {
            if (IS_INFO_ENABLED) {
                log.info "No PharmML to validate in ${model.inspect()}"
            }
            return false
        }
        if (IS_INFO_ENABLED) {
            log.info "Asking libPharmML to validate ${pharmMlFile}."
        }
        assert pharmMlFile!= null && pharmMlFile.exists() && pharmMlFile.canRead()
        LibPharmMLImpl api = PharmMlFactory.getInstance().createLibPharmML()
        def stream = null
        IPharmMLResource resource = null 
        try {
            stream = new BufferedInputStream(new FileInputStream(pharmMlFile))
            resource = api.createDomFromResource(stream)
        } catch(IOException x) {
            log.error(x.message, x)
            throw new RuntimeException(x.message, x)
        } finally {
            stream?.close()
        }
        IValidationReport report = resource.getCreationReport()
        if (IS_INFO_ENABLED) {
            final int ERR_COUNT = report.numErrors()
            if (ERR_COUNT) {
                def err = []
                Iterator<IValidationError> iErr = report.errorIterator()
                while (iErr.hasNext()) {
                    IValidationError e = iErr.next()
                    err << new StringBuffer(e.getRuleId()).append(':').append(e.getErrorMsg()).
                            append(System.getProperty("line.separator")).toString()
                }
                log.info "err.inspect()"
            }
        }
        return report.isValid()
    }

    @Profiled(tag="pharmMlService.extractName")
    public String extractName(List<File> model) {
        StringBuffer theName = new StringBuffer()
        if (!model) {
            return ""
        }
        model = model.findAll{ f -> f.canRead() && "application/xml".equals(Files.probeContentType(f.toPath())) }
        model.each {
            String mName = JummpXmlUtils.findModelAttribute(it, "PharmML", "name")
            if (mName) {
                if (theName) {
                    log.info "${model.inspect()} already has name set to ${theName}. Appending ${mName} to it."
                    theName.append(" ")
                }
                theName.append(mName)
            }
        }

        return theName.toString()
    }

    @Profiled(tag="pharmMlService.extractDescription")
    public String extractDescription(final List<File> model) {
        return ""
    }

    @Profiled(tag="pharmMlService.getAllAnnotationURNs")
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        return []
    }

    @Profiled(tag="pharmMlService.getPubMedAnnotation")
    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) {
        return []
    }

    /**
     * Detects whether the supplied files are in the format supported by this Service
     * @param files a list of files that should be checked
     * @return true if all files are supported, false otherwise.
     * @see net.biomodels.jummp.core.model.FileFormatService#areFilesThisFormat(List files)
     */
    @Profiled(tag="pharmMlService.areFilesThisFormat")
    public boolean areFilesThisFormat(List<File> files) {
        if (!files) {
            return false
        }
        files = files.findAll {it && it.canRead()}
        def fileQueue = new ConcurrentLinkedQueue(files)
        def outcomes = new ConcurrentSkipListMap<File, Boolean>()
        def iFiles = fileQueue.iterator()
        def threadFactory = Executors.defaultThreadFactory()
        while (iFiles.hasNext()) {
            final File theFile = iFiles.next()
            final PharmMlDetector detector = new PharmMlDetector(theFile)
            def modelDetectorThread = threadFactory.newThread(detector)
            if (modelDetectorThread) {
                modelDetectorThread.setName("pharmML detector for ${theFile.name}")
                modelDetectorThread.start()
            } else {
                //todo retry or at least log
                println "cannot start a detector thread for $theFile"
            }
            modelDetectorThread.join()
            outcomes.put(theFile, detector.isRecognisedFormat(theFile))
        }
        List<Boolean> outcomeValues = outcomes.values().toList()
        boolean pharmmlFound = !!outcomeValues.find{it}
        if (!pharmmlFound) {
            if (IS_INFO_ENABLED) {
                log.info("No PharmML file was found in ${files.inspect()}")
            }
            return false
        }
        // see if non-PharmML files are in SBML or clinical trial data
        if (outcomeValues.find{!it}) {
            def iOutcomes = outcomes.entrySet().iterator()
            while (iOutcomes.hasNext()) {
                def entry = iOutcomes.next()
                if (!entry.getValue()) {
                    final File NON_PHARMML_FILE = entry.getKey()
                    final String CONTENT = Files.probeContentType(NON_PHARMML_FILE.toPath())
                    if ("application/xml".equals(CONTENT)) {
                        if (JummpXmlUtils.findModelAttribute(theFile, "sbml", "xmlns")) {
                            entry.setValue(true)
                        }
                    } else if (["text/plain", "text/csv"].contains(CONTENT)) {
                        entry.setValue(true)
                    }
                }
            }
        }
        //we're okay as long as no value is set to false
        return !(outcomes.values().find{!it})
    }

    @Profiled(tag="pharmMlService.getFormatVersion")
    public String getFormatVersion(RevisionTransportCommand revision) {
        //return PharmML writtenVersion
        return revision ? "0.1" : ""
    }
}
