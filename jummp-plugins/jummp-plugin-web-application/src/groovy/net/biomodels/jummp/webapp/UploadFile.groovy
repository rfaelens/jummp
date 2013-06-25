package net.biomodels.jummp.webapp

import org.springframework.web.multipart.MultipartFile

/**
 * Representation of a file that was uploaded by the user
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130619
 */
@grails.validation.Validateable
class UploadFile implements Serializable {
    private static final long serialVersionUID = 1L
    MultipartFile bytes
    String description
    boolean mainFile = false

    //TODO expand
    static constraints = {
        description blank: false
    }
}
