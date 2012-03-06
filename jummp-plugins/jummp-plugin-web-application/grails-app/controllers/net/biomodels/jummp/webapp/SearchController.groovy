package net.biomodels.jummp.webapp

import grails.converters.JSON
import net.biomodels.jummp.core.model.RevisionTransportCommand

class SearchController {
    /**
     * Default action showing a list view
     */
    def list = {
        render(template: "/templates/list")
        return
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

        dataToRender.iTotalRecords = 10 // TODO: real value from core
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords

        List models = modelService.getAllModels(start, length)
        models.each { model ->
            dataToRender.aaData << [
                model.id,
                model.name,
                model.vcsIdentifier,
                model.state,
                model.publication
            ]
        }
        render dataToRender as JSON
    }
}
