package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.remote.AbstractRemoteAdapter
import net.biomodels.jummp.remote.RemoteSbmlAdapter
import org.freedesktop.dbus.DBusConnection
import net.biomodels.jummp.dbus.SbmlDBusAdapter
import org.springframework.beans.factory.InitializingBean
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: May 12, 2011
 * Time: 12:49:17 PM
 * To change this template use File | Settings | File Templates.
 */
class RemoteSbmlAdapterDBusImpl extends AbstractRemoteAdapter implements RemoteSbmlAdapter, InitializingBean {
    private DBusConnection connection
    private SbmlDBusAdapter sbmlDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SESSION)
        sbmlDBusAdapter = (SbmlDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/SBML", SbmlDBusAdapter.class)
    }

    String getMetaId(long modelId, int revisionNumber) {
        return sbmlDBusAdapter.getModelMetaId(authenticationToken(), modelId, revisionNumber)
    }

    long getVersion(long modelId, int revisionNumber) {
        return sbmlDBusAdapter.getVersion(authenticationToken(), modelId, revisionNumber)
    }

    long getLevel(long modelId, int revisionNumber) {
        return sbmlDBusAdapter.getLevel(authenticationToken(), modelId, revisionNumber)
    }

    String getNotes(long modelId, int revisionNumber) {
        return sbmlDBusAdapter.getModelNotes(authenticationToken(), modelId, revisionNumber)
    }

    List<Map> getAnnotations(long modelId, int revisionNumber) {
        String json = sbmlDBusAdapter.getModelAnnotations(authenticationToken(), modelId, revisionNumber)
        def parsedJSON = JSON.parse(json)
        List<Map> returnList = []
        parsedJSON.each {
            returnList << it
        }
        return returnList
    }

    List<Map> getParameters(long modelId, int revisionNumber) {
        return mapFromJSON(sbmlDBusAdapter.getParameters(authenticationToken(), modelId, revisionNumber))
    }

    Map getParameter(long modelId, int revisionNumber, String id) {
        def parsedJSON = JSON.parse(sbmlDBusAdapter.getParameter(authenticationToken(), modelId, revisionNumber, id))
        Map entry = [:]
        parsedJSON.keySet().each { key ->
            def value = parsedJSON.get(key)
            if (value == JSONObject.NULL) {
                value = null
            }
            entry.put(key, value)
        }
        return entry
    }

    List<Map> getLocalParameters(long modelId, int revisionNumber) {
        return mapFromJSON(sbmlDBusAdapter.getLocalParameters(authenticationToken(), modelId, revisionNumber))
    }

    public List<Map> getReactions(long modelId, int revisionNumber) {
        return mapFromJSON(sbmlDBusAdapter.getReactions(authenticationToken(), modelId, revisionNumber))
    }

    public Map getReaction(long modelId, int revisionNumber, String id) {
        def parsedJSON = JSON.parse(sbmlDBusAdapter.getReaction(authenticationToken(), modelId, revisionNumber, id))
        Map reaction = [:]
        parsedJSON.keySet().each { key ->
            def value = parsedJSON.get(key)
            if (value == JSONObject.NULL) {
                value = null
            }
            reaction.put(key, value)
        }
        return reaction
    }

    public List<Map> getEvents(long modelId, int revisionNumber) {
        return mapFromJSON(sbmlDBusAdapter.getEvents(authenticationToken(), modelId, revisionNumber))
    }

    public Map getEvent(long modelId, int revisionNumber, String id) {
        def parsedJSON = JSON.parse(sbmlDBusAdapter.getEvent(authenticationToken(), modelId, revisionNumber, id))
        Map event = [:]
        parsedJSON.keySet().each { key ->
            def value = parsedJSON.get(key)
            if (value == JSONObject.NULL) {
                value = null
            }
            event.put(key, value)
        }
        return event
    }
    public List<Map> getRules(long modelId, int revisionNumber) {
        return mapFromJSON(sbmlDBusAdapter.getRules(authenticationToken(), modelId, revisionNumber))
    }

    public Map getRule(long modelId, int revisionNumber, String variable) {
        def parsedJSON = JSON.parse(sbmlDBusAdapter.getRule(authenticationToken(), modelId, revisionNumber, variable))
        Map rule = [:]
        parsedJSON.keySet().each { key ->
            def value = parsedJSON.get(key)
            if (value == JSONObject.NULL) {
                value = null
            }
            rule.put(key, value)
        }
        return rule
    }

    private List<Map> mapFromJSON(String json) {
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
}
