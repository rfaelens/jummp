package net.biomodels.jummp.core;

/**
 * Base exception for all exceptions thrown in Jummp.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 **/
public class JummpException extends Exception {
    public JummpException() {
        this("unknown");
    }

    public JummpException(String message) {
        super(message);
    }

    public JummpException(String message, Throwable cause) {
        super(message, cause);
    }

    public JummpException(Throwable cause) {
        super("unknown", cause);
    }
}
