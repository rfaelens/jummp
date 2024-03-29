/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Log4j (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Log4j used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.core.JummpException
import net.biomodels.jummp.remote.AbstractRemoteAdapter
import org.apache.log4j.Logger

/**
 * @short Base class for services delegating to the core through synchronous JMS.
 *
 * A service class extending this abstract class can be used to connect to the core
 * web application through JMS from a different component such as the entry-point web application or web services.
 *
 * This class provides methods to send messages with synchronous JMS and validate the return value.
 * All exported methods are executed with synchronous JMS and the implementing class should taken care
 * of the special situations. This means the current Authentication is wrapped into each call and the
 * return value is verified.
 * If an unexpected null value or an Exception is returned, an Exception will be re-thrown to be handled
 * by the application.
 *
 * All returned objects from the core are de-coupled from the database and any changes to the objects are not
 * stored in the database. The appropriate methods of the implementing adapter services have to be called to
 * update objects in the database (and by that ensuring that the business logic is used).
 *
 * An implementing class needs to provide the adapter service name which is used to send all methods to.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractJmsRemoteAdapter extends AbstractRemoteAdapter {
    def jmsSynchronousService
    Logger log = Logger.getLogger(AbstractJmsRemoteAdapter) 

    /**
     * Validates the @p retVal. In case of a @c null value an JummpException is thrown, in case the
     * value is an Exception itself, the Exception gets re-thrown, in case the value is not an instance
     * of @p expectedType an JummpException is thrown.
     * @param retVal The return value to validate
     * @param expectedType The expected type of the value
     * @throws JummpException In case of @p retVal being @c null or not the expected type
     */
    protected void validateReturnValue(def retVal, Class expectedType) throws JummpException {
        if (retVal == null) {
            log.error("Received null value from core.")
            throw new JummpException("Received a null value from core")
        }
        if (retVal instanceof Exception) {
            throw retVal
        }
        if (!expectedType.isInstance(retVal)) {
            throw new JummpException("Expected a value of type ${expectedType.toString()} but received ${retVal.class}")
        }
    }

    /**
     * Convenient overwrite for case of no arguments except Authentication
     * @param method  The name of the method to invoke
     * @return Whatever the core returns
     */
    protected def send(String method) {
        return send(method, null, true)
    }

    /**
     * Convenient overwrite to default to authenticate
     * @param method The name of the method to invoke
     * @param message The arguments which are expected
     * @return Whatever the core returns
     */
    protected def send(String method, def message) {
        return send(method, message, true)
    }

    /**
     * Helper method to send a JMS message to core.
     * @param method The name of the method to invoke
     * @param message The arguments which are expected
     * @param authenticated Whether the Authentication should be prepended to the message
     * @return Whatever the core returns
     */
    protected def send(String method, def message, boolean authenticated) {
        if (authenticated && message) {
            if (message instanceof List) {
                ((List)message).add(0, authenticationToken())
            } else {
                message = [authenticationToken(), message]
            }
        } else if (authenticated && !message) {
            message = authenticationToken()
        }
        return jmsSynchronousService.send([app: "jummp", service: getAdapterServiceName(), method: method],message, [service: getAdapterServiceName(), method: "${method}.response"])
    }

    /**
     *
     * @return Name of the service in core to send messages to
     */
    abstract protected String getAdapterServiceName();
}
