package net.biomodels.jummp.plugins.omex

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import net.biomodels.jummp.core.model.FileFormatService
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Provides methods to handle the COMBINE archive format.
 * @see net.biomodels.jummp.core.model.FileFormatService
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class OmexService implements FileFormatService {

    public boolean validate(final List<File> model) {
        //TODO delegate the validation to libCombineArchive API
        return areFilesThisFormat(model)
    }

    public String extractName(final List<File> model) {
        return ""
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

    public String getFormatVersion(RevisionTransportCommand revision) {
        return revision ? "0.1" : ""
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
        def path = f.toPath()
        //TODO if null, we need a custom FileTypeDetector
        boolean correctMIME = "application/zip".equals(Files.probeContentType(path))
        if (!correctMIME) {
            return false
        }
        boolean correctExtension = path.toString().endsWith(".omex")
        if (!correctExtension) {
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

        return containsManifest
    }
}