/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


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
