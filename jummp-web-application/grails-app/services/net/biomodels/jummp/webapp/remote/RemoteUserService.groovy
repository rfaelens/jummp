package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.remote.RemoteUserAdapter

import org.perf4j.aop.Profiled
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.core.user.UserCodeInvalidException
import net.biomodels.jummp.core.user.UserCodeExpiredException

/**
 * @short Service delegating to RemoteUserAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RemoteUserService implements RemoteUserAdapter {

    static transactional = true
    RemoteUserAdapter remoteUserAdapter

    @Profiled(tag="RemoteUserService.changePassword")
    public void changePassword(String oldPassword, String newPassword) throws BadCredentialsException {
        remoteUserAdapter.changePassword(oldPassword, newPassword)
    }

    @Profiled(tag="RemoteUserService.editUser")
    public void editUser(User user) throws UserInvalidException {
        remoteUserAdapter.editUser user
    }

    @Profiled(tag="RemoteUserService.getCurrentUser")
    public User getCurrentUser() {
        return remoteUserAdapter.getCurrentUser()
    }

    @Profiled(tag="RemoteUserService.getUser")
    public User getUser(String username) throws UserNotFoundException {
        return remoteUserAdapter.getUser(username)
    }

    @Profiled(tag="RemoteUserService.getUser")
    public User getUser(Long id) throws UserNotFoundException {
        return remoteUserAdapter.getUser(id)
    }

    @Profiled(tag="RemoteUserService.getAllUsers")
    public List<User> getAllUsers(Integer offset, Integer count) {
        return remoteUserAdapter.getAllUsers(offset, count)
    }

    @Profiled(tag="RemoteUserService.enableUser")
    Boolean enableUser(Long userId, Boolean enable) throws UserNotFoundException {
        return remoteUserAdapter.enableUser(userId, enable)
    }

    @Profiled(tag="RemoteUserService.lockAccount")
    Boolean lockAccount(Long userId, Boolean lock) throws UserNotFoundException {
        return remoteUserAdapter.lockAccount(userId, lock)
    }

    @Profiled(tag="RemoteUserService.expireAccount")
    Boolean expireAccount(Long userId, Boolean expire) throws UserNotFoundException {
        return remoteUserAdapter.expireAccount(userId, expire)
    }

    @Profiled(tag="RemoteUserService.expirePassword")
    Boolean expirePassword(Long userId, Boolean expire) throws UserNotFoundException {
        return remoteUserAdapter.expirePassword(userId, expire)
    }

    @Profiled(tag="RemoteUserService.register")
    Long register(User user) throws RegistrationException, UserInvalidException {
        return remoteUserAdapter.register(user)
    }

    @Profiled(tag="RemoteUserService.validateRegistration")
    void validateRegistration(String username, String code) throws UserManagementException {
        remoteUserAdapter.validateRegistration username, code
    }

    @Profiled(tag="RemoteUserService.validateAdminRegistration")
    void validateAdminRegistration(String username, String code) throws UserManagementException {
        remoteUserAdapter.validateAdminRegistration username, code
    }

    @Profiled(tag="RemoteUserService.validateAdminRegistration")
    void validateAdminRegistration(String username, String code, String password) throws UserManagementException {
        remoteUserAdapter.validateAdminRegistration username, code, password
    }

    @Profiled(tag="RemoteUserService.requestPassword")
    void requestPassword(String username) throws UserNotFoundException {
        remoteUserAdapter.requestPassword username
    }

    @Profiled(tag="RemoteUserService.resetPassword")
    void resetPassword(String code, String username, String password) throws UserNotFoundException, UserCodeInvalidException, UserCodeExpiredException {
        remoteUserAdapter.resetPassword code, username, password
    }

    @Profiled(tag="RemoteUserService.getAllRoles")
    List<Role> getAllRoles() {
        return remoteUserAdapter.getAllRoles()
    }

    @Profiled(tag="RemoteUserService.getRolesForUser")
    List<Role> getRolesForUser(Long id) {
        return remoteUserAdapter.getRolesForUser(id)
    }

    @Profiled(tag="RemoteUserService.addRoleToUser")
    void addRoleToUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        remoteUserAdapter.addRoleToUser userId, roleId
    }

    @Profiled(tag="RemoteUserService.removeRoleFromUser")
    void removeRoleFromUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        remoteUserAdapter.removeRoleFromUser userId, roleId
    }
}
