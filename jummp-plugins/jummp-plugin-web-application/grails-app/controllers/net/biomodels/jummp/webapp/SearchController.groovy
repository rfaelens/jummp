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
     * Default action showing a list view
     */
    def search = {
    	   [query:params.search_block_form]
    }

        /**
     * Action returning the DataTable content as JSON
     */
    def executeSearch = {
        System.out.println("SEARCH QUERY FOR: "+params.id)
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
        dataToRender.modelIDs= []

        List models = new LinkedList()
        models.addAll(modelService.searchModels(params.id))
        
        int sortDir=1;
        if (params.sSortDir_0=="asc") {
        	sortDir=-1;
        }
        
        switch (params.iSortCol_0 as int) {
        case 0:
            models = models.sort{ m1, m2 -> sortDir * m1.name.compareTo(m2.name)  }
            break
        case 1:
            models = models.sort{ m1, m2 -> sortDir * m1.format.name.compareTo(m2.format.name)  }
            break
        case 2:
            models = models.sort{ m1, m2 -> sortDir * m1.submitter.compareTo(m2.submitter)  }
            break
        case 3:
            models = models.sort{ m1, m2 -> sortDir * m1.submissionDate.getTime() - m2.submissionDate.getTime()  }
            break
        case 4:
            models = models.sort{ m1, m2 -> sortDir * m1.lastModifiedDate.getTime() - m2.lastModifiedDate.getTime()  }
            break
        default:
            models = models.sort{ m1, m2 -> sortDir * m1.id - m2.id  }
            break
        }
        
        dataToRender.iTotalRecords = models.size()
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0
        
        models.each { modelTC ->
            dataToRender.modelIDs << [ modelTC.id ]
            dataToRender.aaData << [
                modelTC.name,
                modelTC.format.name,
                modelTC.submitter,
                modelTC.submissionDate.getTime(),
                modelTC.lastModifiedDate.getTime()
            ]
        }
        render dataToRender as JSON
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
        dataToRender.modelIDs= []

        dataToRender.iTotalRecords = modelService.getModelCount(params.sSearch)
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0
        
        
        ModelListSorting sort
        switch (params.iSortCol_0 as int) {
        case 0:
            sort = ModelListSorting.NAME
            break
        case 1:
            sort = ModelListSorting.FORMAT
            break
        case 2:
            sort = ModelListSorting.SUBMITTER
            break
        case 3:
            sort = ModelListSorting.SUBMISSION_DATE
            break
        case 4:
            sort = ModelListSorting.LAST_MODIFIED
            break
        default:
            sort = ModelListSorting.ID
            break
        }
        List models = modelService.getAllModels(start, length, params.sSortDir_0 == "asc", sort, params.sSearch)
        models.each { model ->
            MTC modelTC=model.toCommandObject()
            dataToRender.modelIDs << [ model.id ]
            dataToRender.aaData << [
                model.name,
                modelTC.format.name,
                modelTC.submitter,
                modelTC.submissionDate.getTime(),
                modelTC.lastModifiedDate.getTime()
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
