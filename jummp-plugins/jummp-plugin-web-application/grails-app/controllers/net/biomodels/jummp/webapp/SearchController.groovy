/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.webapp

import grails.converters.JSON
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC
import grails.plugins.springsecurity.Secured

class SearchController {
    
     /**
     * Dependency Injection of Spring Security Service
     */
     def springSecurityService
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
    
    def searchRedir = {
    	    redirect action: 'search', params: [query:params.search_block_form]
    }
    
    /**
     * Default action showing a list view
     */
     def search = {
    	   [query:params.query]
    }
    
    @Secured(['ROLE_ADMIN'])
    def regen = {
    	long start=System.currentTimeMillis()
    	modelService.regenerateIndices()
    	[regenTime:System.currentTimeMillis() - start]
    }

        /**
     * Action returning the DataTable content as JSON
     */
    def executeSearch = {
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
            models = models.sort{ m1, m2 -> sortDir * m1.name.compareTo(m2.name)  }
            break
        }
        dataToRender.iTotalRecords = models.size()
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0
        if (start>0 && start<models.size()) {
        	models = models[start..-1]
        }
        if (models.size() > length) {
        	models = models[0..length-1]
        }
        
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
