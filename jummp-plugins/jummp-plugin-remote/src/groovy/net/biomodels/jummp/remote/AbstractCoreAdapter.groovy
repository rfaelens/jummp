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
* Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.remote

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import net.biomodels.jummp.core.IAuthenticationHashService

/**
 * @short Abstract Base class for all Adapters exporting core functionality.
 *
 * This abstract class bundles all the methods required by all adapters which
 * export parts of the Core API. This means mostly methods to interact with the
 * Authentication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractCoreAdapter {
    /**
     * An anonymous authentication
     */
    private static final ANONYMOUS_AUTH = new AnonymousAuthenticationToken("key", "anonymousUser", [new GrantedAuthorityImpl("ROLE_ANONYMOUS")])
    /**
     * Dependency injection of AuthenticationHashService
     */
    protected IAuthenticationHashService authenticationHashService

    /**
     * Helper function to set the Authentication in the current thread
     * @param authentication
     */
    protected void setAuthentication(String authenticationHash) throws AuthenticationHashNotFoundException {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.context.setAuthentication(authenticationHashService.retrieveAuthentication(authenticationHash))
    }

    /**
     * Helper function to set an anonymous Authentication in the current thread
     */
    protected void setAnonymousAuthentication() {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.context.setAuthentication(ANONYMOUS_AUTH)
    }

    /**
     * Helper function to remove the Authentication from current thread.
     */
    protected void restoreAuthentication() {
        SecurityContextHolder.clearContext()
    }

    /**
     * Setter for Dependency Injection of AuthenticationHashService.
     * @param authenticationHashService
     */
    public void setAuthenticationHashService(IAuthenticationHashService authenticationHashService) {
        this.authenticationHashService = authenticationHashService
    }
}
