package net.biomodels.jummp.webapp.miriam

import net.biomodels.jummp.core.JummpException

/**
 * @short Exception thrown when updating the MIRIAM Resources fails.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamUpdateException extends JummpException {
    public MiriamUpdateException(Throwable cause) {
        super("Error occurred during update of MIRIAM resources", cause)
    }
}
