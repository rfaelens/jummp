package net.biomodels.jummp.dbus.remote

import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.remote.RemoteGeneOntologyTreeAdapter
import net.biomodels.jummp.dbus.GeneOntologyTreeDBusAdapter
import org.freedesktop.dbus.DBusConnection
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter

@RemoteDBusAdapter(interfaceName="RemoteGeneOntologyTreeAdapter", dbusAdapterName="geneOntologyTreeDBusAdapter")
class RemoteGeneOntologyTreeDBusAdapterImpl extends AbstractRemoteDBusAdapter implements RemoteGeneOntologyTreeAdapter, InitializingBean {
    private DBusConnection connection
    private GeneOntologyTreeDBusAdapter geneOntologyTreeDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SYSTEM)
        geneOntologyTreeDBusAdapter = (GeneOntologyTreeDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/GOTree", GeneOntologyTreeDBusAdapter.class)
    }
}
