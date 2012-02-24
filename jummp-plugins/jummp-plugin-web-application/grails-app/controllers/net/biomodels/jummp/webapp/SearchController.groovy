package net.biomodels.jummp.webapp

//import net.biomodels.jummp.core.model.ModelListSorting
//import grails.converters.JSON

class SearchController {
    /**
     * Dependency injection of springSecurityService
     */
    def springSecurityService
    /**
     * Dependency injection of modelService
     */
    def modelService

    def overlay = {
        render(template: "/templates/overlay")
    }

    /**
     * Default action showing a list view
     */
    def list = {
        //if (!springSecurityService.isAjax(request)) {
        render(template: "/templates/list")//, model: [link: g.createLink(action: "list"), callback: "loadModelListCallback"])
        return
        //}
        //[offset: params.offset, sort: params.sort, dir: params.dir]
    }

    /**
     * AJAX action to get all Models from the core the current user has access to.
     * Returns a JSON data structure for consumption by a jQuery DataTables.
     */
/*    def dataTableSource = {
        // input validation
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

        dataToRender.iTotalRecords = remoteModelService.getModelCount()
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0

        ModelListSorting sort
        switch (params.iSortCol_0 as int) {
            case 1:
                sort = ModelListSorting.NAME
                break
            case 2:
                sort = ModelListSorting.PUBLICATION
                break
            case 3:
                sort = ModelListSorting.LAST_MODIFIED
                break
            case 4:
                sort = ModelListSorting.FORMAT
                break
            case 0: // id column is the default
            default:
                sort = ModelListSorting.ID
                break
        }
        List models = modelService.getAllModels(start, length, params.sSortDir_0 == "asc", sort)
        models.each { model ->
            Map publication = [:]
            if (model.publication) {
                publication.put("link", model.publication.link)
                publication.put("linkProvider", model.publication.linkProvider.toString())
                publication.put("compactTitle", jummp.compactPublicationTitle(publication: model.publication))
            }
            dataToRender.aaData << [
                model.id,
                model.name,
                publication,
                model.lastModifiedDate,
                model.format.name
            ]
        }
        render dataToRender as JSON
    }*/
}
