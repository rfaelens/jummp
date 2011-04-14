package net.biomodels.jummp.jms

import net.biomodels.jummp.remote.AbstractCoreAdapter

/**
 * @short Abstract base class for all Jms Adapter.
 *
 * This class provides the common base for all individual JMS Adapters such as
 * verification of message and setting and restoring the Authentication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractJmsAdapter extends AbstractCoreAdapter {
    /**
     * Helper function to verify that @p message has correct structure.
     * @param message The message to verify
     * @param classes The structure as List of Class types.
     * @return @c true, if the message structure is valid, @c false otherwise
     */
    protected boolean verifyMessage(def message, List<Class<?>> classes) {
        if (!(message instanceof List)) {
            return false
        }
        if (message.size() != classes.size()) {
            return false
        }
        for (int i=0; i<classes.size(); i++) {
            Class clazz = classes[i]
            if (!clazz.isInstance(message[i])) {
                return false
            }
        }
        return true
    }
}
