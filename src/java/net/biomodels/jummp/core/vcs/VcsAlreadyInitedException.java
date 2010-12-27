package net.biomodels.jummp.core.vcs;

/**
 * Exception indicating that an attempt is made to init the working copy although it has been inited before.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 **/
public class VcsAlreadyInitedException extends VcsException {
    public VcsAlreadyInitedException() {
        super("The working copy has already been inited");
    }
}
