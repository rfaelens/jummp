package net.biomodels.jummp.model

/**
 * @short Representation of a File belonging to a ModelVersion.
 *
 * @see net.biomodels.jummp.model.Revision
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130522
 */
class RepositoryFile {
    static belongsTo = [revision:Revision]
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
        path(nullable: false, blank: false, unique: true,
            validator: { p -> 
                String sep  = File.separator
                String pathRegex = "${sep}?([a-zA-Z0-9\\-_]+${sep})+[a-zA-Z0-9\\-_\\.]+".toString()
                return p != null && new File(p).exists() && p.matches(pathRegex)
            }
        )
        description(nullable: false, blank: true, maxSize:100)
    }
}
