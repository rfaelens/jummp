package net.biomodels.jummp.webapp.administration

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import net.biomodels.jummp.core.JummpException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.plugins.security.User

/**
 * @short Controller for user management.
 *
 * This controller is only useful to administrators and allows to manage all users.
 * There is a list of users which is rendered in a dataTable and allows to modify
 * the users' properties.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Secured('ROLE_ADMIN')
class UserAdministrationController {
    /**
     * Dependency Injection of RemoteUserService
     */
    def userService
    /**
     * Dependency Injection of SpringSecurityService
     */
    def springSecurityService

    /**
     * Default action showing the DataTable markup
     */
    def index = {
    }

    /**
     * Action returning the DataTable content as JSON
     */
    def dataTableSource = {
        println "dataTableSource"
        int start = 0
        int length = 10
        if (params.iDisplayStart) {
            start = params.iDisplayStart as int
        }
        if (params.iDisplayLength) {
            length = Math.min(100, params.iDisplayLength as int)
        }
        def dataToRender = [:]
        dataToRender.sEcho = params.sEcho
        dataToRender.aaData = []

        dataToRender.iTotalRecords = 10 // TODO: real value from core
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords

        List users = userService.getAllUsers(start, length)
        users.each { user ->
            dataToRender.aaData << [user.id, user.username, user.userRealName, user.email, user.enabled, user.accountExpired, user.accountLocked, user.passwordExpired]
        }
        render dataToRender as JSON
    }

    /**
     * Action to enable a given user
     */
    def enable = {
        try {
            def data = [success: userService.enableUser(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (UserNotFoundException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to (un)lock a given user
     */
    def lockAccount = {
        try {
            def data = [success: userService.lockAccount(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (UserNotFoundException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to (un)expire a given user
     */
    def expireAccount = {
        try {
            def data = [success: userService.expireAccount(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (UserNotFoundException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to (un)expire the password of a given user 
     */
    def expirePassword = {
        try {
            def data = [success: userService.expirePassword(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (UserNotFoundException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to edit an user
     */
    def show = {
        /*if (!springSecurityService.isAjax(request)) {
            render(template: "/templates/page", model: [link: g.createLink(action: "show", id: params.id), callback: "loadAdminUserCallback"])
            return
        }*/
        [user: userService.getUser(params.id as Long), roles: userService.getAllRoles(), userRoles: userService.getRolesForUser(params.id as Long)]
    }

    /**
     * Action to add a role to a user
     */
    def addRole = { AddRemoveRoleCommand cmd ->
        Map data = [:]
        if (cmd.hasErrors()) {
            data.put("error", g.message(code: "user.administration.userRole.error.general"))
        } else {
            try {
                userService.addRoleToUser(cmd.userId, cmd.id)
                data.put("success", "true")
            } catch (UserNotFoundException e) {
                data.put("error", g.message(code: "user.administration.userRole.error.userNotFound"))
            } catch (RoleNotFoundException e) {
                data.put("error", g.message(code: "user.administration.userRole.error.roleNotFound"))
            }
        }
        render data as JSON
    }

    /**
     * Action to remove a role from a user
     */
    def removeRole = { AddRemoveRoleCommand cmd ->
        Map data = [:]
        if (cmd.hasErrors()) {
            data.put("error", g.message(code: "user.administration.userRole.error.general"))
        } else {
            try {
                userService.removeRoleFromUser(cmd.userId, cmd.id)
                data.put("success", "true")
            } catch (UserNotFoundException e) {
                data.put("error", g.message(code: "user.administration.userRole.error.userNotFound"))
            } catch (RoleNotFoundException e) {
                data.put("error", g.message(code: "user.administration.userRole.error.roleNotFound"))
            }
        }
        render data as JSON
    }

    /**
     * Action to render the view to register a new user as admin
     */
    def register = {
    }

    /**
     * Action to perform the registration of a new user as admin
     */
    def performRegistration = { RegistrationCommand cmd ->
        def data = [:]
        if (cmd.hasErrors()) {
            data.put("error", true)
            data.put("username", resolveErrorMessage(cmd, "username", "User Name"))
            data.put("email", resolveErrorMessage(cmd, "email", "Email"))
            data.put("userRealName", resolveErrorMessage(cmd, "userRealName", "Name"))
        } else {
            try {
                data.put("user", userService.register(cmd.toUser()))
                data.put("success", true)
            } catch (JummpException e) {
                data.clear()
                data.put("error", true)
                data.put("username", e.message)
            }
        }
        render data as JSON
    }

    /**
     * Action for editing non-security relevant user information.
     */
    def editUser = { EditUserCommand cmd ->
        Map data = [:]
        if (cmd.hasErrors()) {
            data.put("error", true)
            data.put("username", resolveErrorMessage(cmd, "username", "Username"))
            data.put("userRealName", resolveErrorMessage(cmd, "userRealName", "Name"))
            data.put("email", resolveErrorMessage(cmd, "email", "Email"))
        } else {
            userService.editUser(cmd.toUser())
            data.put("success", true)
        }
        render data as JSON
    }

    private String resolveErrorMessage(EditUserCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "user.administration.${field}.blank")
            case "email.invalid":
                return g.message(code: "user.administration.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }

    /**
     * Resolves the error message for a field error
     * @param cmd The RegistrationCommand for resolving the errors
     * @param field The field to be tested
     * @param description A descriptive name of the field to be passed to unknown errors
     * @return The resolved error message or @c null if there is no error
     */
    private String resolveErrorMessage(RegistrationCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "user.administration.register.error.${field}.blank")
            case "validator.invalid":
                return g.message(code: "user.administration.register.error.${field}.invalid")
            case "email.invalid":
                return g.message(code: "user.administration.register.error.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }
}

/**
 * @short Command Object for add/remove role actions
 */
class AddRemoveRoleCommand {
    Long id
    Long userId

    static constraints = {
        id(nullable: false)
        userId(nullable: false)
    }
}

/**
 * @short Command object for User registration
 */
class RegistrationCommand {
    String username
    String email
    String userRealName

    static constraints = {
        username(nullable: false, blank: false)
        email(nullable: false, email: true, blank: false)
        userRealName(nullable: false, blank: false)
    }

    User toUser() {
        return new User(username: this.username, email: this.email, userRealName: this.userRealName)
    }
}

/**
 * @short Command Object to validate the user before editing.
 */
class EditUserCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String username
    String userRealName
    String email

    static constraints = {
        username(nullable: false, blank: false)
        userRealName(nullable: false, blank: false)
        email(nullable: false, blank: false, email: true)
    }

    /**
     *
     * @return The command object as a User
     */
    User toUser() {
        return new User(username: this.username, userRealName: this.userRealName, email: this.email)
    }
}