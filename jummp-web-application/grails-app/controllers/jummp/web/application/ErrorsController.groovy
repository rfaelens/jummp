package jummp.web.application

import grails.converters.JSON

class ErrorsController {
    def springSecurityService

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
