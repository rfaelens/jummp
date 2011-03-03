package jummp.web.application

import grails.converters.JSON

class ErrorsController {
    def springSecurityService

    def error403 = {
        if (springSecurityService.isAjax(request)) {
            def data = [error: 403, authenticated: springSecurityService.isLoggedIn()]
            render data as JSON
            return
        } else {
            [authenticated: springSecurityService.isLoggedIn()]
        }
    }

    def error404 = {
        if (springSecurityService.isAjax(request)) {
            def data = [error: 404, resource: request.forwardURI]
            render data as JSON
            return
        } else {
            [resource: request.forwardURI]
        }
    }

    def error500 = {
        def exception = request.getAttribute('exception')
        String digest = ''
        if (exception) {
            if (exception.message) {
                digest = exception.message
            }
            exception.stackTrace.each {
                digest += it.toString()
            }
        }
        digest = digest.encodeAsMD5()
        if (springSecurityService.isAjax(request)) {
            def data = [error: 500, code: digest]
            render data as JSON
            return
        } else {
            [code: digest]
        }
    }
}
