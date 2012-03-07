package net.biomodels.jummp.webapp

import grails.converters.JSON
import net.biomodels.jummp.core.model.RevisionTransportCommand

class SearchController {
    /**
     * Dependency injection of modelService.
     **/
    def modelService

    def index = {
        redirect action: 'list'
    }

    /**
     * Default action showing a list view
     */
    def list = {
    }

    def overlay = {
        render(template: "/templates/list", model: [link: g.createLink(action: "overlay", id: params.id)])
    }

    def summary = {
            render(template: "/templates/overlay", model: [link: g.createLink(action: "show", id: params.id), callback: "loadModelTabCallback"])
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
}