/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Grails, Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Grails, Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.webapp

import grails.converters.JSON
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC
import grails.plugins.springsecurity.Secured
import net.biomodels.jummp.webapp.rest.search.SearchResults
import net.biomodels.jummp.webapp.rest.search.BrowseResults

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
    	if (!params.format || params.format=="html") {
     	 	 return []
     	 }
		 respond new BrowseResults(browseCore(params.sortBy, 
		 									  params.sortDir, 
		 									  params.offset ? Integer.parseInt(params.offset):0, 10))     
    }
    
    def searchRedir = {
    	    redirect action: 'search', params: [query:params.search_block_form]
    }
    
    /**
     * Default action showing a list view
     */
     def search = {
    	 
     	 if (!params.format || params.format=="html") {
     	 	 return [query:params.query]
     	 }
		 respond new SearchResults(searchCore(params.query, 
		 									  params.sortBy, 
		 									  params.sortDir, 
		 									  params.offset ? Integer.parseInt(params.offset):0, 10))     	 
    }
    
    @Secured(['ROLE_ADMIN'])
    def regen = {
    	long start=System.currentTimeMillis()
    	modelService.regenerateIndices()
    	[regenTime:System.currentTimeMillis() - start]
    }

    
    private def searchCore(String query, String sortBy, String sortDirection, int offset, int length) {
    	List<MTC> models=[]
    	models.addAll(modelService.searchModels(query))
        if (sortBy) {
        	int sortDir=1
        	if (sortDirection=="asc") {
        		sortDir=-1;
        	}
        	switch (sortBy) {
        		case "name":
        			models = models.sort{ m1, m2 -> sortDir * m2.name.compareTo(m1.name)  }
        			break
        		case "format":
        			models = models.sort{ m1, m2 -> sortDir * m2.format.name.compareTo(m1.format.name)  }
        			break
        		case "submitter":
        			models = models.sort{ m1, m2 -> sortDir * m2.submitter.compareTo(m1.submitter)  }
        			break
        		case "submitted":
        			models = models.sort{ m1, m2 -> sortDir * m2.submissionDate.getTime() - m1.submissionDate.getTime()  }
        			break
        		case "modified":
        			models = models.sort{ m1, m2 -> sortDir * m2.lastModifiedDate.getTime() - m1.lastModifiedDate.getTime()  }
        			break
        		default:
        			models = models.sort{ m1, m2 -> sortDir * m2.name.compareTo(m1.name)  }
        			break
        	}
        }
        int retval=models.size()
        if (offset>0 && offset<models.size()) {
        	models = models[offset..-1]
        }
        if (models.size() > length) {
        	models = models[0..length-1]
        }
        return [models: models, 
        		matches: retval,
        		sortBy: sortBy,
        		sortDirection: sortDirection,
        		offset: offset,
        		length: length]
        
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
        String sortBy="name"
        switch (params.iSortCol_0 as int) {
        case 0:
            sortBy="name"
            break
        case 1:
            sortBy="format"
            break
        case 2:
			sortBy="submitter"            
        	break
        case 3:
            sortBy="submitted"
        	break
        case 4:
        	sortBy="modified"
        	break
        default:
            sortBy="name"
        	break
        }
        //List<MTC> models=new LinkedList<MTC>()
        def results=searchCore(params.id, sortBy, params.sSortDir_0, start, length)
        def dataToRender = [:]
        dataToRender.sEcho = params.sEcho
        dataToRender.aaData = []
        dataToRender.modelIDs= []
        dataToRender.iTotalRecords = results.matches
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0
        results.models.each { modelTC ->
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

    
    private def browseCore(String sortBy, String sortDirection, int offset, int length) {
    	ModelListSorting sort
        switch (sortBy) {
        case "name":
            sort = ModelListSorting.NAME
            break
        case "format":
            sort = ModelListSorting.FORMAT
            break
        case "submitter":
            sort = ModelListSorting.SUBMITTER
            break
        case "submitted":
            sort = ModelListSorting.SUBMISSION_DATE
            break
        case "modified":
            sort = ModelListSorting.LAST_MODIFIED
            break
        default:
            sort = ModelListSorting.ID
            break
        }
        List modelsDomain = modelService.getAllModels(offset, length, sortDirection == "asc", sort)
        List models = []
        modelsDomain.each {
        	models.add(it.toCommandObject())
        }
        return [models: models, 
        		modelsAvailable: modelService.getModelCount(),
        		sortBy: sortBy,
        		sortDirection: sortDirection,
        		offset: offset,
        		length: length]
    }
    
    
    private String getSortColumn(int sc) {
    	String sortBy="name"
    	switch (sc) {
    		case 0:
            	sortBy="name"
            	break
            case 1:
            	sortBy="format"
            	break
            case 2:
				sortBy="submitter"            
				break
			case 3:
            	sortBy="submitted"
            	break
            case 4:
        		sortBy="modified"
        		break
        }
        return sortBy
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
        String sortBy=getSortColumn(params.iSortCol_0 as int)
        
        def results = browseCore(sortBy, params.sSortDir_0, start, length)
        def dataToRender = [:]
        dataToRender.sEcho = params.sEcho
        dataToRender.aaData = []
        dataToRender.modelIDs= []

        dataToRender.iTotalRecords = results.modelsAvailable
        dataToRender.iTotalDisplayRecords = dataToRender.iTotalRecords
        dataToRender.offset = start
        dataToRender.iSortCol_0 = params.iSortCol_0
        dataToRender.sSortDir_0 = params.sSortDir_0
        
        
        results.models.each { modelTC ->
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

    def lastAccessedModels = {
        List data = modelHistoryService.history()
        def dataToRender = []
        data.each { model ->
            dataToRender << [id: model.id, name: model.name, submitter: model.submitter]
        }
        render dataToRender as JSON
    }
}
