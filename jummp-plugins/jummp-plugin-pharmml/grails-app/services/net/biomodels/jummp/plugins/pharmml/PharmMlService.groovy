package net.biomodels.jummp.plugins.pharmml

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicBoolean
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.util.JummpXmlUtils

/**
 * Service class containing the logic to handle models encoded in PharmML.
 * @see net.biomodels.jummp.core.model.FileFormatService
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class PharmMlService implements FileFormatService {

    public boolean validate(final List<File> model) {
        //delegate to libPharmML
        return areFilesThisFormat(model)
    }

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

    public String extractDescription(final List<File> model) {
        return ""
    }

    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        return []
    }

    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) {
        return []
    }

    /**
     * Detects whether the supplied files are in the format supported by this Service
     * @param files a list of files that should be checked
     * @return true if all files are supported, false otherwise.
     * @see net.biomodels.jummp.core.model.FileFormatService#areFilesThisFormat(List files)
     */
    public boolean areFilesThisFormat(final List<File> files) {
        if (!files) {
            return false
        }
        def fileQueue = new ConcurrentLinkedQueue(files)
        AtomicBoolean allGood = new AtomicBoolean(true)
        def iFiles = fileQueue.iterator()
        def threadFactory = Executors.defaultThreadFactory()
        while (iFiles.hasNext() && allGood.get()) {
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
            if (!detector.isRecognisedFormat(theFile)) {
                allGood.set(false)
            }
        }
        return allGood.get()
    }

    public String getFormatVersion(RevisionTransportCommand revision) {
        //return PharmML writtenVersion
        return revision ? "0.1" : ""
    }
}
