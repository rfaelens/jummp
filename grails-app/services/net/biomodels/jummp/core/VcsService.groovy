package net.biomodels.jummp.core

import org.springframework.security.access.prepost.PreAuthorize
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsManager
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision

/**
 * @short Service providing access to the version control system.
 *
 * This service allows the core to access the version control system provided by a plugin.
 * The service is only for internal use in the core by other services and should not be
 * made part of the external API.
 * @internal
 * @see VcsManager
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class VcsService {
    static transactional = true
    @SuppressWarnings('GrailsStatelessService')
    VcsManager vcsManager
    @SuppressWarnings('GrailsStatelessService')
    def grailsApplication

    /**
     * Checks whether the Version Control System is configured properly
     * @return @c true if the vcs system is configured properly, @c false otherwise
     */
    boolean isValid() {
        return (vcsManager != null)
    }

    /**
    * Updates a Model file previously imported to the VCS.
    * Copies @p file into the working copy of the VCS and updates the existing file in the
    * VCS and the remote location of the VCS.
    * Use this method if the file had been imported previously.
    * @param model The Model representing the file in the VCS.
    * @param file The file to update
    * @param commitMessage The commit message to be used for the update.
    * @return Revision number of updated file.
    * @throws VcsException passes along the VcsException thrown by VcsManager
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    String updateModel(final Model model, final List<File> files, final String commitMessage) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }

        if (commitMessage == null || commitMessage.isEmpty()) {
            return vcsManager.updateModel(new File(model.vcsIdentifier), files, "Update of $model.name")
        } else {
            return vcsManager.updateModel(new File(model.vcsIdentifier), files, commitMessage)
        }
    }

    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    String updateModel(final Model model, final File file, final String commitMessage) throws VcsException {
        return updateModel(model, [file], commitMessage);
    }
    /**
     * Imports a new Model file into the VCS.
     * Copies @p file into the working copy of the VCS and performs an initial import.
     * Use this method if the file has not been imported previously.
     * @param model The Model representing the new file in the VCS
     * @param file The file to import
     * @return Revision number of imported file.
     * @throws VcsException passes along the VcsException thrown by VcsManager
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    String importModel(final Model model, final List<File> files) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }

        return vcsManager.updateModel(new File(model.vcsIdentifier), files, "Import of ${model.name}")
    }

    /**
     * Imports a new Model file into the VCS.
     * Copies @p file into the working copy of the VCS and performs an initial import.
     * Use this method if the file has not been imported previously.
     * @param model The Model representing the new file in the VCS
     * @param file The file to import
     * @return Revision number of imported file.
     * @throws VcsException passes along the VcsException thrown by VcsManager
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    String importModel(final Model model, final File file) throws VcsException {
        return importModel(model, [file])
    }

    /**
     * Retrieves Model File from VCS.
     * @param revision The Revision for which the Model file should be retrieved
     * @return File Handler to the retrieved file in the exchange directory.
     * It's the responsibility of the caller to delete the file when it is not needed any more.
     * @throws VcsException passes along the VcsException thrown by VcsManager
     */
    @PreAuthorize("hasPermission(#revision, read) or hasRole('ROLE_ADMIN')")
    List<File> retrieveFiles(final Revision revision) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }
        def latestRevId = Revision.createCriteria().get {
            eq("model.id", revision.model.id)
            projections {
                max("revisionNumber")
            }
        }
        if (revision.revisionNumber == latestRevId) {
            return vcsManager.retrieveModel(new File(revision.model.vcsIdentifier))
        }

        return vcsManager.retrieveModel(new File(revision.model.vcsIdentifier), revision.vcsId)
    }
    // TODO: implement required methods
}
