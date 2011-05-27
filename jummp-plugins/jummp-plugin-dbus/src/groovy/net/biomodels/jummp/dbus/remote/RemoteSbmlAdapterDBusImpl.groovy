package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.remote.AbstractRemoteAdapter
import net.biomodels.jummp.remote.RemoteSbmlAdapter
import org.freedesktop.dbus.DBusConnection
import net.biomodels.jummp.dbus.SbmlDBusAdapter
import org.springframework.beans.factory.InitializingBean

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
        return listOfMapFromJSON(sbmlDBusAdapter.getModelAnnotations(authenticationToken(), modelId, revisionNumber))
    }

    List<Map> getParameters(long modelId, int revisionNumber) {
        return listOfMapFromJSON(sbmlDBusAdapter.getParameters(authenticationToken(), modelId, revisionNumber))
    }

    Map getParameter(long modelId, int revisionNumber, String id) {
        return mapFromJSON(sbmlDBusAdapter.getParameter(authenticationToken(), modelId, revisionNumber, id))
    }

    List<Map> getLocalParameters(long modelId, int revisionNumber) {
        return listOfMapFromJSON(sbmlDBusAdapter.getLocalParameters(authenticationToken(), modelId, revisionNumber))
    }

    public List<Map> getReactions(long modelId, int revisionNumber) {
        return listOfMapFromJSON(sbmlDBusAdapter.getReactions(authenticationToken(), modelId, revisionNumber))
    }

    public Map getReaction(long modelId, int revisionNumber, String id) {
        return mapFromJSON(sbmlDBusAdapter.getReaction(authenticationToken(), modelId, revisionNumber, id))
    }

    public List<Map> getEvents(long modelId, int revisionNumber) {
        return listOfMapFromJSON(sbmlDBusAdapter.getEvents(authenticationToken(), modelId, revisionNumber))
    }

    public Map getEvent(long modelId, int revisionNumber, String id) {
       return mapFromJSON(sbmlDBusAdapter.getEvent(authenticationToken(), modelId, revisionNumber, id))
    }
    public List<Map> getRules(long modelId, int revisionNumber) {
        return listOfMapFromJSON(sbmlDBusAdapter.getRules(authenticationToken(), modelId, revisionNumber))
    }

    public Map getRule(long modelId, int revisionNumber, String variable) {
        return mapFromJSON(sbmlDBusAdapter.getRule(authenticationToken(), modelId, revisionNumber, variable))
    }

    public List<Map> getFunctionDefinitions(long modelId, int revisionNumber) {
        return listOfMapFromJSON(sbmlDBusAdapter.getFunctionDefinitions(authenticationToken(), modelId, revisionNumber))
    }

    public Map getFunctionDefinition(long modelId, int revisionNumber, String id) {
        return mapFromJSON(sbmlDBusAdapter.getFunctionDefinition(authenticationToken(), modelId, revisionNumber, id))
    }
}
