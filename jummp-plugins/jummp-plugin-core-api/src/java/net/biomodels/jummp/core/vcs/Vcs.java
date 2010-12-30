package net.biomodels.jummp.core.vcs;

/**
 * @short Interface for a service provided by a vcs backend plugin.
 *
 * The interface only provides a getter to the concrete VcsManager.
 * It is the task of the implementing class to setup the VcsManager so that
 * the core does not have to bother with the specific settings of the vcs system.
 * @see VcsManager
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface Vcs {

    /**
     * Creates a concrete VcsManager and returns a reference to it.
     * If the VcsManager has already been created, just the reference will be returned.
     * @return Reference to the concrete VcsManager.
     * @throws VcsNotInitedException If the vcs could not be inited.
     */
    VcsManager vcsManager() throws VcsNotInitedException;

    /**
     * Returns whether the version control system is configured properly.
     * @return @c true if the Vcs is configured properly, @c false otherwise.
     */
    boolean isValid();
}
