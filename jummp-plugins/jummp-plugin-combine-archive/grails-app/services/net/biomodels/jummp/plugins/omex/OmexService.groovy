package net.biomodels.jummp.plugins.omex

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.perf4j.aop.Profiled
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType

/**
 * Provides methods to handle the COMBINE archive format.
 * @see net.biomodels.jummp.core.model.FileFormatService
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class OmexService implements FileFormatService {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    @Profiled(tag="omexService.validate")
    public boolean validate(final List<File> model) {
        //TODO delegate the validation to libCombineArchive API
        return areFilesThisFormat(model)
    }

    @Profiled(tag="omexService.extractName")
    public String extractName(final List<File> model) {
        return ""
    }

    @Profiled(tag="omexService.extractDescription")
    public String extractDescription(final List<File> model) {
        return ""
    }

    @Profiled(tag="omexService.getAllAnnotationURNs")
    public List<String> getAllAnnotationURNs(RevisionTransportCommand revision) {
        return []
    }

    @Profiled(tag="omexService.getPubMedAnnotation")
    public List<String> getPubMedAnnotation(RevisionTransportCommand revision) {
        return []
    }

    /**
     * Detects whether the supplied files are in the format supported by this Service
     * @param files a list of files that should be checked
     * @return true if all files are supported, false otherwise.
     * @see net.biomodels.jummp.core.model.FileFormatService#areFilesThisFormat(List files)
     */
    @Profiled(tag="omexService.areFilesThisFormat")
    public boolean areFilesThisFormat(final List<File> files) {
        if (!files) {
            return false
        }
        boolean allGood = true
        def iFiles = files.iterator()
        while (iFiles.hasNext() && allGood) {
            final File theFile = iFiles.next()
            if (!isOmexFormat(theFile)) {
                allGood = false
            }
        }
        return allGood
    }

    @Profiled(tag="omexService.getFormatVersion")
    public String getFormatVersion(RevisionTransportCommand revision) {
        return revision ? "0.1" : ""
    }
    
    @Profiled(tag="omexService.getSearchIndexingContent")
    public String getSearchIndexingContent(RevisionTransportCommand revision) {
    	    return ""
    }

    /*
     * Helper method that checks if a file appears to be a valid COMBINE archive.
     * @param f the file in question
     * @return true if the supplied file is a COMBINE archive, false otherwise
     */
    private boolean isOmexFormat(final File f) {
        if (!f || !f.canRead()) {
            return false
        }
        if (IS_INFO_ENABLED) {
            log.info "Validating ${f.properties}"
        }
        def path = f.toPath()
        //TODO if null, we need a custom FileTypeDetector
        def sherlock = new DefaultDetector()
        String properType = sherlock.detect(new BufferedInputStream(
                        new FileInputStream(f)), new Metadata()).toString()
                
        boolean correctMIME = "application/zip".equals(properType)
        if (!correctMIME) {
            if (IS_INFO_ENABLED) {
                log.info "Not treating ${f.name} as COMBINE archive because of incorrect content type. ${properType}"
            }
            return false
        }
        boolean correctExtension = path.toString().endsWith(".omex")
        if (!correctExtension) {
            if (IS_INFO_ENABLED) {
                log.info "Not treating ${f.name} as COMBINE archive because of incorrect file extension."
            }
            return false
        }
        def pathURI = new URI(new StringBuilder("jar:").append(path.toUri()).toString())
        FileSystem fs
        boolean containsManifest
        try {
            fs = FileSystems.newFileSystem(pathURI, [:])
            final String MANIFEST_LOCATION =  "manifest.xml"
            Path manifestPath = fs.getPath(MANIFEST_LOCATION)
            containsManifest = Files.exists(manifestPath) &&
                        Files.isReadable(manifestPath) && Files.isRegularFile(manifestPath)
        } finally {
            fs?.close()
        }

            if (IS_INFO_ENABLED) {
                StringBuilder msg = new StringBuilder("File ")
                msg.append(f.name).append(" is")
                msg.append(containsManifest ? "" : " not").append(" a COMBINE archive.")
                msg.append(containsManifest ?: " The manifest file is missing.")
                log.info(msg.toString())
            }
        return containsManifest
    }
}
