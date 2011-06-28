package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.core.JummpException

/**
 * @short Exception thrown when updating the MIRIAM Resources fails.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamUpdateException extends JummpException implements Serializable {
    private static final long serialVersionUID = 1L
    public MiriamUpdateException(Throwable cause) {
        super("Error occurred during update of MIRIAM resources", cause)
    }
}
