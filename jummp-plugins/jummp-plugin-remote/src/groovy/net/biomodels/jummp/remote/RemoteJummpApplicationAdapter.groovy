/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.remote

import net.biomodels.jummp.core.JummpException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

/**
 * @short Interface describing the Application methods exported by remote adapters.
 *
 * Remote adapters have to implement this interface if they want to provide access
 * to JUMMP's core application functionality. This interface does not describe methods
 * for handling Users or Models, etc. Therefore there are specific interfaces. The only
 * purpose of this interface is to provide common methods to interact with core aspects
 * not matching into a more specific interface.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface RemoteJummpApplicationAdapter {
    /**
     * Tries to authenticate the given @p Authentication in the core.
     * @param authentication The Authentication to test. In most cases a UsernamePasswordAuthenticationToken
     * @return An authenticated user
     * @throws AuthenticationException If the Authentication is not valid
     * @throws JummpException If an error occurred
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException
    /**
     * Retrieves the externalized configuration of the core application.
     * @param appToken The unique application token
     * @return The core's configuration
     */
    ConfigObject getJummpConfig(String appToken)
    /**
     * Retrieves a boolean from the core, indicating whether a user's
     * authentication hash is still valid or already removed.
     * @param authentication The authentication of a user
     * @return true if authentication is valid, false otherwise
     */
    boolean isAuthenticated(Authentication authentication)
}
