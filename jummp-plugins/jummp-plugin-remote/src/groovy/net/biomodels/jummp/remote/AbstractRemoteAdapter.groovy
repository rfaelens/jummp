package net.biomodels.jummp.remote

import net.biomodels.jummp.core.user.JummpAuthentication
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON

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
}
