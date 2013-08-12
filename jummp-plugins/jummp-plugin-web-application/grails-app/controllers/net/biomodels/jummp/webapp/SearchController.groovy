package net.biomodels.jummp.webapp

import grails.converters.JSON
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC

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

        dataToRender.iTotalRecords = modelService.getModelCount(params.sSearch)
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0

        ModelListSorting sort
        switch (params.iSortCol_0 as int) {
        case 1:
            sort = ModelListSorting.NAME
            break
        case 0: // id column is the default
        default:
            sort = ModelListSorting.ID
            break
        }

        List models = modelService.getAllModels(start, length, params.sSortDir_0 == "asc", sort, params.sSearch)
        models.each { model ->
            MTC modelTC=model.toCommandObject()
            dataToRender.aaData << [
                model.id,
                model.name,
                modelTC.submitter,
                modelTC.submissionDate.getTime()
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
