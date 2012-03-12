package net.biomodels.jummp.webapp

import grails.converters.JSON
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand

class SearchController {
    /**
     * Dependency injection of modelService.
     **/
    def modelService
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService
    /**
     * Dependency injection of sbmlService.
     */
    def sbmlService

    def index = {
        redirect action: 'list'
    }

    /**
     * Default action showing a list view
     */
    def list = {
    }

    def model = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [revision: rev]
    }

    /**
     * Display basic information about the model
     */
    def summary = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [publication: modelDelegateService.getPublication(params.id as Long), revision: rev, notes: sbmlService.getNotes(rev), annotations: sbmlService.getAnnotations(rev)]
    }

    /**
     * Renders html snippet with Publication information for the current Model identified by the id.
     */
   def publication = {
       PublicationTransportCommand publication = modelDelegateService.getPublication(params.id as Long)
       [publication: publication]
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
