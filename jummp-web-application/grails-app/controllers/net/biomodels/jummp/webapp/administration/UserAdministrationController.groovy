package net.biomodels.jummp.webapp.administration

import grails.plugins.springsecurity.Secured
import grails.converters.JSON

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
     * Dependency Injection of CoreAdapterService
     */
    def coreAdapterService
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

        List users = coreAdapterService.getAllUsers(start, length)
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
            def data = [success: coreAdapterService.enableUser(params.id as Long, Boolean.parseBoolean(params.value))]
            render data as JSON
        } catch (IllegalArgumentException e) {
            def data = [error: true, message: e.message]
            render data as JSON
        }
    }
}
