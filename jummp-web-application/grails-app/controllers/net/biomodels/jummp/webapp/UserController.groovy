package net.biomodels.jummp.webapp

import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.authentication.BadCredentialsException
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import net.biomodels.jummp.core.JummpException

/**
 * @short Controller for editing user information.
 *
 * This controller allows a user to change the user editible parts of his user
 * object such as password.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Secured('ROLE_USER')
class UserController {
    def userAdapterService
    def springSecurityService

    /**
     * Standard Action rendering default view
     */
    def index = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "USER"])
        }
        [user: userAdapterService.getCurrentUser(), changePassword: ConfigurationHolder.config.jummpCore.security.ui.changePassword]
    }

    /**
     * Action for changing a password
     */
    def changePassword = { ChangePasswordCommand cmd ->
        Map data = [:]
        if (!ConfigurationHolder.config.jummpCore.security.ui.changePassword) {
            data.put("error", g.message(code: 'user.change.password.disabled'))
            render data as JSON
            return
        }
        if (cmd.hasErrors()) {
            data.put("error", true)
            data.put("oldPassword", resolveErrorMessage(cmd, "oldPassword", "Old Password"))
            data.put("newPassword", resolveErrorMessage(cmd, "newPassword", "New Password"))
            data.put("verifyPassword", resolveErrorMessage(cmd, "verifyPassword", "Password Verification"))
        } else {
            try {
                userAdapterService.changePassword(cmd.oldPassword, cmd.newPassword)
                data.put("success", true)
            } catch (BadCredentialsException e) {
                data.put("error", true)
                data.put("oldPassword", g.message(code: "user.change.oldPassword.incorrect"))
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
            userAdapterService.editUser(cmd.toUser())
            data.put("success", true)
        }
        render data as JSON
    }

    /**
     * Action to show the view to request a new password.
     */
    @Secured('isAnonymous()')
    def passwordForgotten = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "PASSWORDFORGOTTEN"])
        }
    }

    /**
     * Action for requesting a new password
     */
    @Secured('isAnonymous()')
    def requestPassword = { RequestPasswordCommand cmd ->
        def data = [:]
        if (cmd.hasErrors()) {
            data.put("error", g.message(code: "user.resetPassword.error.username.blank"))
        } else {
            try {
                userAdapterService.requestPassword(params.username)
                data.put("success", true)
            } catch (JummpException e) {
                data.put("error", g.message(code: "user.resetPassword.error.userNotFound"))
            }
        }
        render data as JSON
    }

    /**
     * Action to show the view to reset a new password.
     */
    @Secured('isAnonymous()')
    def resetPassword = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "RESETPASSWORD", id: params.id])
        }
        [code: params.id]
    }

    /**
     * Action to change the password with the code.
     */
    @Secured('isAnonymous()')
    def performResetPassword = { ResetPasswordCommand cmd ->
        def data = [:]
        if (cmd.hasErrors()) {
            data.put("error", true)
            data.put("username", resolveErrorMessage(cmd, "username", "User Name"))
            data.put("password", resolveErrorMessage(cmd, "password", "Password"))
            data.put("verifyPassword", resolveErrorMessage(cmd, "verifyPassword", "Password Verification"))
            data.put("code", resolveErrorMessage(cmd, "code", "Reset Password Code"))
        } else {
            try {
                userAdapterService.resetPassword(cmd.code, cmd.username, cmd.password)
                data.put("success", true)
            } catch (JummpException e) {
                data.put("error", e.message)
            }
        }
        render data as JSON
    }

    /**
     * Resolves the error message for a field error
     * @param cmd The ChangePasswordCommand for resolving the errors
     * @param field The field to be tested
     * @param description A descriptive name of the field to be passed to unknown errors
     * @return The resolved error message or @c null if there is no error
     */
    private String resolveErrorMessage(ChangePasswordCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "user.change.${field}.blank")
            case "validator.invalid":
                return g.message(code: "user.change.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }

    private String resolveErrorMessage(EditUserCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "user.edit.${field}.blank")
            case "email.invalid":
                return g.message(code: "user.edit.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }

    private String resolveErrorMessage(ResetPasswordCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "user.resetPassword.error.${field}.blank")
            case "validator.invalid":
                // validator invalid only for password verification - reuse change password message
                return g.message(code: "user.change.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }
}

/**
 * @short Command Object to validate the change password fields.
 */
class ChangePasswordCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String oldPassword
    String newPassword
    String verifyPassword

    static constraints = {
        oldPassword(nullable: false, blank: false)
        newPassword(nullable: false, blank: false)
        verifyPassword(validator: { verifyPassword, cmd ->
            return (verifyPassword == cmd.newPassword)
        })
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

/**
 * @short Command object for requesting a password
 */
class RequestPasswordCommand implements Serializable {
    String username

    static constraints = {
        username(nullable: false, blank: false)
    }
}

/**
 * @short Command Object for resetting a password.
 */
class ResetPasswordCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String code
    String username
    String password
    String verifyPassword

    static constraints = {
        code(nullable: false, blank: false)
        username(nullable: false, blank: false)
        password(nullable: false, blank: false)
        verifyPassword(validator: { verifyPassword, cmd ->
            return (verifyPassword == cmd.password)
        })
    }
}
