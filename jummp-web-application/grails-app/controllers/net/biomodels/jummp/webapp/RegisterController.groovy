package net.biomodels.jummp.webapp

import grails.plugins.springsecurity.Secured
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.JummpException
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.user.UserManagementException

/**
 * @short Controller for registering new user.
 *
 * This controller provides the logic for registering a new user.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Secured(["isAnonymous()"])
class RegisterController {
    /**
     * Dependency injection of user adapter Service
     */
    def userAdapterService
    /**
     * Dependency injection of spring security service
     */
    def springSecurityService

    /**
     * Default action rendering the Registration Dialog markup
     */
    def index = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "REGISTER"])
            return
        }
        if (!ConfigurationHolder.config.jummpCore.security.anonymousRegistration) {
            throw new AccessDeniedException(g.message(code: "user.register.disabled"))
        }
        [password: ConfigurationHolder.config.jummpCore.security.registration.ui.userPassword]
    }

    /**
     * User registration action.
     * TODO: allow admin to register users
     */
    def register = { RegistrationCommand cmd ->
        def data = [:]
        if (!ConfigurationHolder.config.jummpCore.security.anonymousRegistration) {
            data.put("error", g.message(code: "user.register.disabled"))
            render data as JSON
            return
        }
        if (cmd.hasErrors()) {
            data.put("error", true)
            data.put("username", resolveErrorMessage(cmd, "username", "User Name"))
            data.put("password", resolveErrorMessage(cmd, "password", "Password"))
            data.put("verifyPassword", resolveErrorMessage(cmd, "verifyPassword", "Password Verification"))
            data.put("email", resolveErrorMessage(cmd, "email", "Email"))
            data.put("userRealName", resolveErrorMessage(cmd, "userRealName", "Name"))
        } else {
            try {
                userAdapterService.register(cmd.toUser())
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
     * Action rendering the markup for account validation.
     */
    def validate = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "VALIDATE", id: params.id])
        }
        [code: params.id]
    }

    def validateRegistration = {
        def data = [:]
        try {
            userAdapterService.validateRegistration(params.username, params.code)
            data.put("success", true)
        } catch (UserManagementException e) {
            data.put("error", e.message)
        }
        render data as JSON
    }

    /**
     * Action rendering the markup for account confirmation after registration by admin
     */
    def confirmRegistration = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "CONFIRMREGISTRATION", id: params.id])
        }
        [code: params.id, password: ConfigurationHolder.config.jummpCore.security.registration.ui.userPassword]
    }

    /**
     * Action for performing the actual account confirmation after registration by admin
     */
    def performConfirmRegistration = { ConfirmRegistrationCommand cmd ->
        def data = [:]
        if (cmd.hasErrors()) {
            data.put("error", true)
            data.put("username", resolveErrorMessage(cmd, "username", "Username"))
            data.put("code", resolveErrorMessage(cmd, "code", "Confirmation Code"))
            data.put("password", resolveErrorMessage(cmd, "password", "Password"))
            data.put("verifyPassword", resolveErrorMessage(cmd, "verifyPassword", "Password Verification"))
        } else {
            try {
                if (cmd.password) {
                    userAdapterService.validateAdminRegistration(cmd.username, cmd.code, cmd.password)
                } else {
                    userAdapterService.validateAdminRegistration(cmd.username, cmd.code)
                }
                data.put("success", true)
            } catch (UserManagementException e) {
                data.clear()
                data.put("error", e.message)
            }
        }
        render data as JSON
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
                return g.message(code: "user.register.error.${field}.blank")
            case "validator.invalid":
                return g.message(code: "user.register.error.${field}.invalid")
            case "email.invalid":
                return g.message(code: "user.register.error.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }

    private String resolveErrorMessage(ConfirmRegistrationCommand cmd, String field, String description) {
        if (cmd.errors.getFieldError(field)) {
            switch (cmd.errors.getFieldError(field).code) {
            case "blank":
                return g.message(code: "user.register.confirm.error.${field}.blank")
            case "validator.invalid":
                return g.message(code: "user.register.confirm.error.${field}.invalid")
            default:
                return g.message(code: "error.unknown", args: [description])
            }
        }
        return null
    }
}

/**
 * Command Object to be used in the register action.
 */
class RegistrationCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String username
    String password
    String verifyPassword
    String email
    String userRealName

    static constraints = {
        username(nullable: false, blank: false)
        password(nullable: true, blank: false, validator: { password ->
            if (ConfigurationHolder.config.jummpCore.security.registration.ui.userPassword) {
                return password != null
            } else {
                return password == null
            }
        })
        verifyPassword(nullable: true, validator: { verifyPassword, cmd ->
            return cmd.password == verifyPassword
        })
        email(nullable: false, blank: false, email: true)
        userRealName(nullable: false, blank: false)
    }

    public User toUser() {
        return new User(username: this.username,
                password: this.password,
                email: this.email,
                userRealName: this.userRealName)
    }
}

/**
 * @short Command object for performConfirmRegistration action.
 */
class ConfirmRegistrationCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String username
    String code
    String password
    String verifyPassword


    static constraints = {
        username(nullable: false, blank: false)
        code(nullable: false, blank: false)
        password(nullable: true, blank: false, validator: { password ->
            if (ConfigurationHolder.config.jummpCore.security.registration.ui.userPassword) {
                return password != null
            } else {
                return password == null
            }
        })
        verifyPassword(nullable: true, validator: { verifyPassword, cmd ->
            return cmd.password == verifyPassword
        })
    }
}
