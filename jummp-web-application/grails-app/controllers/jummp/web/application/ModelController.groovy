package jummp.web.application

import grails.converters.JSON
import net.biomodels.jummp.core.model.ModelListSorting

/**
 * @short Controller providing basic access to Models.
 *
 * This controller communicates with the coreAdapterService to retrieve Models and
 * Model information from the core application.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelController {
    /**
     * Dependency injection of coreAdapterService
     */
    def coreAdapterService

    /**
     * Default action showing a list view
     */
    def index = { }

    /**
     * AJAX action to get all Models from the core the current user has access to.
     * Returns a JSON data structure for consumption by a jQuery DataTables. 
     */
    def dataTableSource = {
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

        dataToRender.iTotalRecords = coreAdapterService.getModelCount()
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords

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
        case 0: // id column is the default
        default:
            sort = ModelListSorting.ID
            break
        }
        List models = coreAdapterService.getAllModels(start, length, params.sSortDir_0 == "asc", sort)
        models.each { model ->
            // TODO: add the publication data
            dataToRender.aaData << [model.id, model.name, "TODO", model.lastModifiedDate]
        }
        render dataToRender as JSON
    }
}
