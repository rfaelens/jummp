package net.biomodels.jummp.core.vcs;

/**
 * Exception indicating that it is tried to access a working copy though it has not been inited yet.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class VcsNotInitedException extends VcsException {
    public VcsNotInitedException() {
        super("The working copy has not been inited yet.");
    }
}
