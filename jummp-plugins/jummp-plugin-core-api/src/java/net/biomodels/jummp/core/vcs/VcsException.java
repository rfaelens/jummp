package net.biomodels.jummp.core.vcs;

import net.biomodels.jummp.core.JummpException;

/**
 * Base exception for all VCS exceptions.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 **/
public class VcsException extends JummpException {
    public VcsException() {
        this("VCS exception");
    }

    public VcsException(String message) {
        super(message);
    }

    public VcsException(String message, Throwable cause) {
        super(message, cause);
    }

    public VcsException(Throwable cause) {
        super("VCS exception", cause);
    }
}
