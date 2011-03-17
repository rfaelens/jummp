package net.biomodels.jummp.webapp.administration

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.core.user.RoleNotFoundException

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
     * Dependency Injection of UserAdapterService
     */
    def userAdapterService
    /**
     * Dependency Injection of SpringSecurityService
     */
    def springSecurityService

    /**
     * Default action showing the DataTable markup
     */
    def index = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "USERADMINLIST"])
        }
    }

    /**
     * Action returning the DataTable content as JSON
     */
    def dataTableSource = {
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

        List users = userAdapterService.getAllUsers(start, length)
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
            def data = [success: userAdapterService.enableUser(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (IllegalArgumentException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to (un)lock a given user
     */
    def lockAccount = {
        try {
            def data = [success: userAdapterService.lockAccount(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (IllegalArgumentException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to (un)expire a given user
     */
    def expireAccount = {
        try {
            def data = [success: userAdapterService.expireAccount(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (IllegalArgumentException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to (un)expire the password of a given user 
     */
    def expirePassword = {
        try {
            def data = [success: userAdapterService.expirePassword(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (IllegalArgumentException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }

    /**
     * Action to edit an user
     */
    def show = {
        if (!springSecurityService.isAjax(request)) {
            redirect(controller: "home", params: [redirect: "USERADMINSHOW", id: params.id])
            return
        }
        [user: userAdapterService.getUser(params.id as Long), roles: userAdapterService.getAllRoles(), userRoles: userAdapterService.getRolesForUser(params.id as Long)]
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
                userAdapterService.addRoleToUser(cmd.userId, cmd.id)
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
                userAdapterService.removeRoleFromUser(cmd.userId, cmd.id)
                data.put("success", "true")
            } catch (UserNotFoundException e) {
                data.put("error", g.message(code: "user.administration.userRole.error.userNotFound"))
            } catch (RoleNotFoundException e) {
                data.put("error", g.message(code: "user.administration.userRole.error.roleNotFound"))
            }
        }
        render data as JSON
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
