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
import net.biomodels.jummp.plugins.security.User

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

    private boolean integerCheck(def input, boolean minValueCheck=false, int minValue=-1) {
        try {
            if (!input) {
                return false
            }
            int value = Integer.parseInt(input)
            if (minValueCheck) {
                return value > minValue
            }
            return true
        }
        catch(Exception e) {
            return false
        }
    }

    private void sanitiseParams() {
        if (!params.sortBy) {
            params.sortBy="modified"
        }
        if (!params.sortDir || params.sortDir!="asc") {
            params.sortDir="desc";
        }
        if (params.sortBy) {
            switch (params.sortBy) {
                case "name":
                case "format":
                case "submitter":
                case "submitted":
                case "modified":
                    break
                default:
                    params.sortBy = "modified";
            }
        }
        else {
            params.sortBy = "modified";
        }
        params.numResults=numResults();
        if (integerCheck(params.offset, true, -1)) {
            params.offset = params.offset ? Integer.parseInt(params.offset) : 0
        }
        else {
            params.offset = 0
        }
    }

    private int numResults() {
        final int MAXRESULTS = 50
        final int MINRESULTS = 10
        User user
        if (!(springSecurityService.principal instanceof String)) {
            user=User.findById(springSecurityService.principal.id)
        }
        Preferences prefs
        if (user) {
            prefs = Preferences.findByUser(user)
        }
        if (!prefs) {
            prefs = Preferences.getDefaults()
        }
        if (integerCheck(params.numResults, true, -1)) {
            prefs.numResults = params.numResults as Integer
            if (prefs.numResults > MAXRESULTS ) {
                prefs.numResults = MAXRESULTS;
            }
            else if (prefs.numResults < MINRESULTS ) {
                prefs.numResults = MINRESULTS;
            }
            if (user) {
                prefs.setUser(user)
                prefs.save(flush:true)
            }
        }
        return prefs.numResults
    }

    /**
     * Default action showing a list view
     */
    def list = {
        sanitiseParams()
        def results = browseCore(params.sortBy, params.sortDir, params.offset, params.numResults)

        if (!params.format || params.format=="html") {
            results["history"] = modelHistoryService.history()
            return results
        }
        respond new BrowseResults(results)
    }

    /**
     * Default action showing a list view
     */
    def archive = {
        sanitiseParams()
        def results = archiveCore(params.sortBy, params.sortDir, params.offset, params.numResults)
        return results
    }

    def searchRedir = {
        redirect action: 'search', params: [query:params.search_block_form]
    }

    /**
     * Default action showing a list view
     */
    def search = {
        sanitiseParams();
        if (!params.query) {
            params.query = ""
        }
        def results = searchCore(params.query, params.sortBy, params.sortDir, params.offset,
                        params.numResults)
        if (!params.format || params.format=="html") {
            return results
        }
        respond new SearchResults(results)
    }

    @Secured(['ROLE_ADMIN'])
    def regen = {
        long start=System.currentTimeMillis()
        modelService.regenerateIndices()
        [regenTime:System.currentTimeMillis() - start]
    }

    private def searchCore(String query, String sortBy, String sortDirection, int offset, int length) {
        List<MTC> models=[]
        if (query?.trim()) {
            models.addAll(modelService.searchModels(query))
        }
        int sortDir = 1
        if (sortDirection && sortDirection == "asc") {
            sortDir = -1
        }
        switch (sortBy) {
            case "name":
                models = models.sort{ m1, m2 -> sortDir * m2.name.compareTo(m1.name) }
                break
            case "format":
                models = models.sort{ m1, m2 -> sortDir * m2.format.name.compareTo(m1.format.name) }
                break
            case "submitter":
                models = models.sort{ m1, m2 -> sortDir * m2.submitter.compareTo(m1.submitter) }
                break
            case "submitted":
                models = models.sort{ m1, m2 ->
                    sortDir * m2.submissionDate.getTime() - m1.submissionDate.getTime()
                }
                break
            case "modified":
                models = models.sort{ m1, m2 ->
                    sortDir * m2.lastModifiedDate.getTime() - m1.lastModifiedDate.getTime()
                }
                break
            default:
                models = models.sort{ m1, m2 -> sortDir * m2.name.compareTo(m1.name) }
                break
        }
        int retval = models.size()
        if (offset > 0 && offset < models.size()) {
            models = models[offset..-1]
        }
        else {
            offset = 0
        }
        if (models.size() > length) {
            models = models[0..length-1]
        }
        return [models: models, matches: retval, sortBy: sortBy, sortDirection: sortDirection,
                    offset: offset, length: length, query: query]
    }

    private def archiveCore(String sortBy, String sortDirection, int offset, int length) {
        int sortDir = 1
        if (sortDirection == "asc") {
            sortDir = -1
        }
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
        List modelsDomain =
                modelService.getAllModels(offset, length, sortDirection == "asc", sort, null, true)
        List models = []
        modelsDomain.each {
            models.add(it.toCommandObject())
        }
        return [models: models, modelsAvailable: modelService.getModelCount(null, true), sortBy: sortBy,
                    sortDirection: sortDirection, offset: offset, length: length]
    }

    private def browseCore(String sortBy, String sortDirection, int offset, int length) {
        int sortDir = 1
        if (sortDirection == "asc") {
            sortDir = -1
        }
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
        return [models: models, modelsAvailable: modelService.getModelCount(), sortBy: sortBy,
                sortDirection: sortDirection, offset: offset, length: length]
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

    def lastAccessedModels = {
        List data = modelHistoryService.history()
        def dataToRender = []
        data.each { model ->
            dataToRender << [id: model.id, name: model.name, submitter: model.submitter]
        }
        render dataToRender as JSON
    }
}
