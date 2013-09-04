package net.biomodels.jummp.core.model
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
/**
 * @short Wrapper for a Revision to be transported through JMS.
 *
 * Small wrapper class to decouple the Revision from the Database.
 * Changes to instances of this class are not populated to the database.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class RevisionTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long id
    /**
     * Revision number in reference to the Model and not to the VCS.
     */
    Integer revisionNumber
    /**
     * The real name of the user who uploaded the Revision.
     */
    String owner
    /**
     * Whether the revision is a minor change or not.
     */
    Boolean minorRevision
    /**
     * Whether the revision has been validated
     */
    Boolean validated
    /**
     * The name of this revision.
     */
    String name
    /**
     * The description of this revision.
     */
    String description
    /**
     * The "commit message" of this revision.
     */
    String comment
    /**
     * The date when the Revision was uploaded.
     */
    Date uploadDate
    /**
     * The format of the file in the VCS.
     */
    ModelFormatTransportCommand format
    /**
     * The model the revision belongs to
     */
    ModelTransportCommand model
    /**
     * The list of files associated with this revision
     */
     List<RepositoryFileTransportCommand> files = null;
     
     List<RepositoryFileTransportCommand> getFiles() {
     	     if (!files) {
     	     	     def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
     	     	     files=ctx.modelDelegateService.retrieveModelFiles(this)
     	     }
     	     return files
     }
     
     void setFiles(List<RepositoryFileTransportCommand> f) {
     	     files=f;
     }
}
