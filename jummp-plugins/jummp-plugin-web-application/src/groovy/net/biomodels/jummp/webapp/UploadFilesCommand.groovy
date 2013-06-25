package net.biomodels.jummp.webapp

/**
 * Command object for validating the files supplied by the user as part of a
 * model submission.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130619
 */
@grails.validation.Validateable
class UploadFilesCommand implements Serializable {
    private static final long serialVersionUID = 1L

    UploadFile mainFile
    List<UploadFile> additionalFiles
    String[] descriptions

    //TODO refine
    static constraints = {
        mainFile(nullable: true)
        additionalFiles(nullable: true, blank: true)
        descriptions nullable: true
    }
}
