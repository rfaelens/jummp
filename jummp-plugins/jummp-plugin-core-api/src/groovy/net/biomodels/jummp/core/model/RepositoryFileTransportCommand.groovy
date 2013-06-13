package net.biomodels.jummp.core.model


/**
 * @short Wrapper for a RepositoryFile that can be transported through JMS.
 *
 * Small wrapper class to decouple the RepositoryFile from the database.
 * Changes to instances of this class are not populated to the database.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130613
 */
class RepositoryFileTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L

    Long id
    String path
    String description
    boolean hidden
    boolean mainFile
    boolean userSubmitted
    String mimeType
    RevisionTransportCommand revision
}
