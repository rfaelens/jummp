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

import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.core.user.UserCodeInvalidException
import net.biomodels.jummp.core.user.UserCodeExpiredException

/**
 * @short Interface describing how to access the remote User Service.
 *
 * This interface defines all the methods which has to be implemented by a
 * remote adapter to the core's UserService.
 *
 * Each remote adapter exporting the UserService needs to implement this
 * interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface RemoteUserAdapter {

    /**
     * Changes the password of the currently logged in user.
     * @param oldPassword The old password for verification
     * @param newPassword The new password to be used
     * @throws BadCredentialsException if @p oldPassword is incorrect
     */
    public void changePassword(String oldPassword, String newPassword) throws BadCredentialsException
    /**
     * Edit the non-security related parts of a user.
     * @param user The User with the updated fields
     * @throws UserInvalidException If the modified user does not validate
     */
    public void editUser(User user) throws UserInvalidException
    /**
     *
     * @return The current (security sanitized) user
     */
    public User getCurrentUser()
    /**
     * Retrieves a User object for the given @p username.
     * @param username The login identifier of the user to be retrieved
     * @return The (security sanitized) user
     * @throws UserNotFoundException Thrown if there is no User for @p username
     */
    public User getUser(String username) throws UserNotFoundException
    /**
     * Retrieves a User object for the given @p username.
     * @param id The user Id
     * @return The (security sanitized) user
     * @throws UserNotFoundException Thrown if there is no User for @p username
     */
    public User getUser(Long id) throws UserNotFoundException
    /**
     * Retrieves list of users.
     * This method is only for administrative purpose. It does not sanitize the
     * returned Users, that is it includes all (also security relevant) elements.
     * The method only exists in a paginated version
     * @param offset Offset in the list
     * @param count Number of Users to return, Maximum is 100
     * @return List of Users ordered by Id
     */
    public List<User> getAllUsers(Integer offset, Integer count)
    /**
     * Enables/Disables the user identified by @p userId
     * @param userId The unique id of the user
     * @param enable if @c true the user is enabled, if @c false the user is disabled
     * @return @c true, if the enable state was changed, @c false if the user was already in @p enable state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    public Boolean enableUser(Long userId, Boolean enable) throws UserNotFoundException
    /**
     * (Un)Locks the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param lock if @c true the account is locked, if @c false the account is unlocked
     * @return @c true, if the account locked state was changed, @c false if the user was already in @p lock state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    public Boolean lockAccount(Long userId, Boolean lock) throws UserNotFoundException
    /**
     * (Un)Expires the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the account is expired, if @c false the account is un-expired
     * @return @c true, if the account expired state was changed, @c false if the user was already in @p expire state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    public Boolean expireAccount(Long userId, Boolean expire) throws UserNotFoundException
    /**
     * (Un)Expires the password for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the password is expired, if @c false the password is un-expired
     * @return @c true, if the password expired state was changed, @c false if the password was already in @p expire state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    public Boolean expirePassword(Long userId, Boolean expire) throws UserNotFoundException
    /**
     * Registers a new User.
     * @param user The new User to register
     * @return Id of new created user
     * @throws RegistrationException In case a user with same name already exists
     * @throws UserInvalidException In case the new user does not validate
     */
    public Long register(User user) throws RegistrationException, UserInvalidException
    /**
     * Validates the registration code of a new user.
     * @param username The name of the new user
     * @param code The validation code
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    public void validateRegistration(String username, String code) throws UserManagementException
    /**
     * Validates the registration code of a new user registered by an admin.
     * @param username The name of the new user
     * @param code The validation code
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    public void validateAdminRegistration(String username, String code) throws UserManagementException
    /**
     * Validates the registration code of a new user registered by an admin.
     * @param username The name of the new user
     * @param code The validation code
     * @param password The user's new password
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    public void validateAdminRegistration(String username, String code, String password) throws UserManagementException
    /**
     * Request a new password for user identified by @p username.
     * @param username The login id of the user whose password should be reset.
     * @throws UserNotFoundException Thrown if there is no user with @p username
     */
    public void requestPassword(String username) throws UserNotFoundException
    /**
     * Resets the Password of the user.
     * @param code The Password Reset Code
     * @param username The Login Id of the User
     * @param password The new Password
     * @throws UserNotFoundException Thrown in case user is not found
     * @throws UserCodeInvalidException Thrown in case code is not valid
     * @throws UserCodeExpiredException Thrown in case code expired
     */
    public void resetPassword(String code, String username, String password) throws UserNotFoundException, UserCodeInvalidException, UserCodeExpiredException
    /**
     * Retrieves all available roles from the Core Jummp Instance.
     * As this is an admin method it does not provide a paginated version
     * @return List of all Roles
     */
    public List<Role> getAllRoles()
    /**
     * Retrieves the Roles for the User identified by @p id.
     *
     * In case there is no user with @p id an empty list is returned.
     * @param id The user id
     * @return List of Roles assigned to the user
     */
    public List<Role> getRolesForUser(Long id)
    /**
     * Adds a Role to the user.
     * If the user already has the role, the user is not changed and no feedback for this situation is
     * provided. The method only ensures that the user has the role after execution.
     * @param userId The id of the user who should receive a new role
     * @param roleId The id or the role to be added to the user
     * @throws UserNotFoundException In case there is no user with @p userId
     * @throws RoleNotFoundException In case there is no role with @p roleId
     */
    public void addRoleToUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException
    /**
     * Removes a Role from the user.
     * If the user does not have the role, the user is not changed and no feedback for this situation is
     * provided. The method only ensures that the user does not have the role after execution.
     * @param userId The id of the user from whom the role should be removed
     * @param roleId The id or the role to be removed from the user
     * @throws UserNotFoundException In case there is no user with @p userId
     * @throws RoleNotFoundException In case there is no role with @p roleId
     */
    public void removeRoleFromUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException
}
