package net.biomodels.jummp.webapp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.multipart.MultipartFile

/**
 * Command object for validating the files supplied by the user as part of a
 * model submission.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130701
 */
@grails.validation.Validateable
class UploadFilesCommand implements Serializable {
    private static final long serialVersionUID = 1L
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
    private static final boolean IS_INFO_ENABLED  = log.isInfoEnabled()

    List<MultipartFile> mainFile
    /**
     * The additional files belonging to a model.
     */
     List<MultipartFile> extraFiles
     /**
      * The descriptions associated with each additional file
      */
     List<String> description

    /**
     * Provides the additional files along with their descriptions in a map.
     *
     * @return a Map with entries of type Map.MapEntry<MultipartFile, String>.
     */
    public Map<MultipartFile, String> additionalFilesAsMap() {
        if (IS_DEBUG_ENABLED) {
            log.debug(String.format("Providing %s additional files as a map", extraFiles.size()))
        }
        def result = [:]
        extraFiles?.eachWithIndex { f, i ->
            result[f] = description[i]
        }
        result
    }

    public static boolean containsDuplicates(List<MultipartFile> files) {
        if (!files) {
            // deal with null and empty elsewhere
            return false
        } else {
            def fileProperties = []
            files.each { it ->
                final def currentFileProperties = [
                    it.getOriginalFilename(),
                    it.getSize(),
                    it.getContentType()
                ]
                if (fileProperties.contains(currentFileProperties)) {
                    return true
                } else {
                    fileProperties << currentFileProperties
                }
            }
            return false
        }
    }

    static constraints = {
        mainFile(nullable: false,
            validator: { mf ->
                if (IS_INFO_ENABLED) {
                    log.info("\nSubmission started. Inspecting ${mf.inspect()}")
                }
                if (mf.findAll {!it || it.isEmpty()}.size() > 0 ){
                    if (IS_DEBUG_ENABLED) {
                        log.debug("Some main file fields were blank. Current main file(s): ${mf.inspect()}.")
                    }
                }
                mf = mf.findAll{ it && !it.isEmpty() }
                if (mf.size() < 1) {
                    log.error "\tPlease give me some files."
                    return ['mainFile.blank']
                }
                /*boolean duplicatesExist = containsDuplicates(mf)
                if (haveDuplicates) {
                    log.error "${mf.inspect()} contains main files uploaded multiple times."
                    return ['mainFile.duplicate']
                }*/
                return true
            }
        )
        extraFiles(nullable: true,
            validator: { supplements, cmd ->
                if (!supplements) {
                    if (IS_INFO_ENABLED) {
                        log.info "No additional files were provided for ${cmd.inspect()}."
                        return true
                    }
                }
                if (IS_DEBUG_ENABLED) {
                    log.debug("Validating additional files ${supplements.inspect()}.")
                }
                // purge empty files
                supplements = supplements.findAll {it && !it.isEmpty()}
                if (IS_DEBUG_ENABLED) {
                    log.debug(String.format("There are %s supplementary files in this submission: %s.",
                           supplements.size(), supplements.inspect()))
                }
                supplements.eachWithIndex{ file, i ->
                    if (!cmd.description[i]) {
                        if (IS_DEBUG_ENABLED) {
                            log.debug(String.format("Supplementary file %s does not have a description.",
                                        file.getOriginalFilename()))
                        }
                    }
                }
                /*boolean duplicatesExist = containsDuplicates(supplements + cmd.mainFile)
                if (duplicatesExist) {
                    log.error("Found duplicate supplementary files. Rejecting this submission.")
                    return ['additionalFile.duplicate']
                }*/
                return true
            }
        )
    }
}
