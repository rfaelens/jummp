package net.biomodels.jummp.webapp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.multipart.MultipartFile

/**
 * Command object for validating the files supplied by the user as part of a
 * model submission.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130628
 */
@grails.validation.Validateable
class UploadFilesCommand implements Serializable {
    private static final long serialVersionUID = 1L
    private static final Log log = LogFactory.getLog(this)

    List<MultipartFile> mainFile
    Map<MultipartFile, String> extra

    static constraints = {
        mainFile(nullable: false,
            validator: { mf ->
                    if (log.isInfoEnabled()) {
                        log.info("\nSubmission started. Inspecting ${mf.inspect()}")
                    }
                    mf = mf.findAll{ it && !it.isEmpty() }
                    if (log.isDebugEnabled()) {
                        log.debug("Some main file fields were blank. Current main file(s): ${mf.inspect()}.")
                    }
                    if (mf.size() < 1) {
                        log.error "\tPlease give me some files."
                        return ['mainFile.blank']
                    } else {
                        boolean areValid = true
                        //list of tuples of the form (file_name, file_size, file_content_type)
                        def fileProperties = []
                        //filter out the ones that the user left blank
                        def iter = mf.iterator()
                        while (areValid && iter.hasNext()) {
                            final MultipartFile currentFile = iter.next()
                            def currentFileProperties = [
                                currentFile.getOriginalFilename(),
                                currentFile.getSize(),
                                currentFile.getContentType()
                            ]
                            if (fileProperties.contains(currentFileProperties)) {
                                log.error "${currentFile.getOriginalFilename()} uploaded multiple times."
                                return ['mainFile.duplicate']
                            } else {
                                fileProperties << currentFileProperties
                            }
                        }
                        return areValid
                    }
                    //should never happen
                    return false
            }
        )
        extra nullable: true
    }
}
