package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType

/**
 * @short Representation of a File belonging to a ModelVersion.
 *
 * @see net.biomodels.jummp.model.Revision
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130612
 */
class RepositoryFile implements Serializable {
    static belongsTo = [revision:Revision]
    /**
     * Dependency Injection of FileSystemService
     */
    def fileSystemService
    /**
     * The path to the file associated with this file. Although in the database we
     * only store the name of the file, we know the unique vcsIdentifier of the 
     * model folder that contains it. When an instance of this class is wrapped in a 
     * corresponding command object, we work out the absolute location of the file.
     */
    String path
    /**
     * A brief description of the purpose of the file in relation to the model
     */
    String description
    /**
     * Flag to differentiate between the files that should be displayed to the user and those that should not.
     * By default, they are the former.
     */
    boolean hidden = false
    /**
     * Flag for distinguishing the main entry of a submission from the rest. This will be considered the main
     * model file. For instance, in the case of DDMoRe submissions this should be the PharmML file. If set to true,
     * this must be in XML or ZIP format.
     */
    boolean mainFile = false
    /**
     * Set to true if this file was provided by the user during submission, rather than automatically generated.
     */
    boolean userSubmitted = false
    /**
     * The content type of this file as defined in http://www.iana.org/assignments/media-types/
     * 
     */
    String mimeType

    static constraints = {
        path(blank: false, unique: true,
            validator: { p, rf -> 
                String sep  = File.separator
                String pathRegex = "${sep}?([a-zA-Z0-9\\-_]+${sep})+[a-zA-Z0-9\\-_\\.]+".toString()
                if (p == null || !new File(p).exists() || ! p.matches(pathRegex)) {
                    return false
                } 
                def f = new File(p).getCanonicalFile()
                def sherlock = new DefaultDetector()
                String properType = sherlock.detect(new BufferedInputStream(
                        new FileInputStream(f)), new Metadata()).toString()
                p = f.name
                if (!rf.mimeType.equals(properType)) {
                    rf.mimeType = properType
                    return true
                }
            })
        description(blank: true, maxSize:500)
        //content type detection is performed above, when we validate the path of the file
        mimeType(nullable: true, blank: true)
        mainFile(validator: { main, rf ->
            if (main) {
                return !rf.hidden
            }
            return true
        })
    }

    /**
     * Comply with the need to provide the absolute file paths in RepositoryFileTransportCommand objects
     * while also being flexible about the location of the root folder where all models are stored.
     */
    RepositoryFileTransportCommand toCommandObject() {
        final String sep = File.separator
        def realPath = new StringBuffer(fileSystemService.root.absolutePath)
        realPath.append(sep).append(revision.model.vcsIdentifier).append(sep)
        realPath.append(path)
        return new RepositoryFileTransportCommand(
                path: realPath.toString(),
                description: description,
                hidden: hidden,
                mainFile: mainFile,
                userSubmitted: userSubmitted,
                mimeType: mimeType,
                revision: revision ? revision.toCommandObject() : null
        )
    }
}
