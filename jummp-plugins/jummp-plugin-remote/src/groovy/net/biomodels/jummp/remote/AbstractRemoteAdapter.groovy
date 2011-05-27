package net.biomodels.jummp.remote

import net.biomodels.jummp.core.user.JummpAuthentication
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import grails.util.GrailsNameUtils

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
        List<Map> returnList = []
        parsedJSON.each {
            Map entry = [:]
            it.keySet().each { key ->
                def value = it.get(key)
                if (value == JSONObject.NULL) {
                    value = null
                }
                entry.put(key, value)
            }
            returnList << entry
        }
        return returnList
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
            returnMap.put(key, value)
        }
        return returnMap
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
