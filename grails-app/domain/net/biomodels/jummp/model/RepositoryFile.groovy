package net.biomodels.jummp.model

/**
 * @short Representation of a File belonging to a ModelVersion.
 *
 * @see net.biomodels.jummp.model.ModelVersion
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130522
 */
class RepositoryFile {

    static hasMany = [versions: ModelVersion]
    static belongsTo = ModelVersion
    /**
     * The path relative to the root folder containing all models
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

    static constraints = {
        path(nullable: false, blank: false, matches:
            "${File.separator}?([a-zA-Z0-9\\-_]+${File.separator})+[a-zA-Z0-9\\-_]+${File.separator}?" as String)
        description(nullable: false, blank: true, maxSize:100)
        versions(nullable: false, validator: { v ->
            return !v.isEmpty()
        })
    }
}
