package net.biomodels.jummp.webapp

import java.util.List;
import java.util.Map;

import grails.converters.JSON

class SearchController {
    /**
     * Dependency injection of modelService.
     **/
    def modelService
    /**
     * Dependency injection of modelHistoryService.
    **/
    def modelHistoryService

    def index = {
        redirect action: 'list'
    }

    /**
     * Default action showing a list view
     */
    def list = {
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

        dataToRender.iTotalRecords = modelService.getModelCount()
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords

        List models = modelService.getAllModels(start, length)
        models.each { model ->
            dataToRender.aaData << [
                model.id,
                model.name,
                model.publication
            ]
        }
        render dataToRender as JSON
    }

    def lastAccessedModels = {
        List data = modelHistoryService.history()
        def dataToRender = []
        data.each { model ->
            dataToRender << [id: model.id, name: model.name, submitter: model.submitter]
        }
        render dataToRender as JSON
    }
}
