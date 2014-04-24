/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
* groovy, Grails, Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of groovy, Grails, Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.remote

import net.biomodels.jummp.core.user.JummpAuthentication
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * @short Base class for all Remote Adapters.
 *
 * Implements methods required by all remote adapters.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractRemoteAdapter {

    /**
     *
     * @return The Authentication Hash of the current Authentication.
     */
    protected String authenticationToken() {
        Authentication auth = SecurityContextHolder.context.authentication
        if (auth instanceof AnonymousAuthenticationToken) {
            return "anonymous"
        } else if (auth instanceof JummpAuthentication) {
            return ((JummpAuthentication)auth).getAuthenticationHash()
        } else {
            return ""
        }
    }

    /**
     * Converts a JSON string into a List with each entry being a Map.
     * This is a convenient method to circumvent the problem that
     * JSON.parse returns a JSON structure and no List.
     * @param json The JSON String
     * @return List of Map entries
     */
    protected List<Map> listOfMapFromJSON(String json) {
        def parsedJSON = JSON.parse(json)
        return jsonToList((JSONArray) parsedJSON)
    }

    /**
     * Converts a JSON string into a Map.
     * This is a convenient method to circumvent the problem that
     * JSON.parse returns a JSON structure and no Map.
     * @param json The JSON String
     * @return Parsed JSON as a Map
     */
    protected Map mapFromJSON(String json) {
        def parsedJSON = JSON.parse(json)
        Map returnMap = [:]
        parsedJSON.keySet().each { key ->
            def value = parsedJSON.get(key)
            if (value == JSONObject.NULL) {
                value = null
            }
            if (value instanceof JSONArray) {
                value = jsonToList(value)
            }
            returnMap.put(key, value)
        }
        return returnMap
    }

    /**
     * Converts a JSON array into a List with each entry being a Map.
     * This method is needed, when a JSON string contains
     * inner arrays.
     * @param array The JSON array
     * @return List of Map entries
     */
    private List<Map> jsonToList(JSONArray array) {
        List<Map> returnList = []
        array.each {
            Map entry = [:]
            if (it.isEmpty()) {
                return
            }
            it.keySet().each { key ->
                def value = it.get(key)
                if (value == JSONObject.NULL) {
                    value = null
                }
                if (value instanceof JSONArray) {
                    value = jsonToList(value)
                }
                entry.put(key, value)
            }
            returnList << entry
        }
        return returnList
    }

    /**
     * Helper method for retrieving all elements of a List, if only ids are returned.
     * The method uses dynamic groovy features to retrieve the data.
     * @param adapter The core's adapter to invoke
     * @param methodName The name of the method to retrieve the single elements
     * @param returnType The name of the class to return, if the returned element is not of that type a "to${Name}" is invoked
     * @param identifiers The List of identifiers for the data to retrieve
     * @param convert An optional class to convert the id element to before retrieving the data, may be @c null
     * @return List of elements in returnType
     */
    protected List retrieveAllElements(def adapter, String methodName, String returnType, List identifiers, Class convert) {
        List returnValues = []
        identifiers.each { id ->
            if (convert) {
                id = id."to${GrailsNameUtils.getShortName(convert)}"()
            }
            def element = adapter."${methodName}"(authenticationToken(), id)
            if (GrailsNameUtils.getShortName(element.class) != returnType) {
                element = element."to${returnType}"()
            }
            returnValues << element
        }
        return returnValues
    }

    /**
     * Overloaded method for convenience.
     * @param adapter
     * @param methodName
     * @param returnType
     * @param identifiers
     * @return
     */
    protected List retrieveAllElements(def adapter, String methodName, String returnType, List identifiers) {
        return retrieveAllElements(adapter, methodName, returnType, identifiers, null)
    }
}
