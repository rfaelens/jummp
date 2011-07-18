package net.biomodels.jummp.dbus

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod

@DBusAdapter(interfaceName="GeneOntologyTreeDBusAdapter", serviceName="geneOntologyTreeService")
class GeneOntologyTreeDBusAdapterImpl extends AbstractDBusAdapter implements GeneOntologyTreeDBusAdapter {
    def geneOntologyTreeService

    public GeneOntologyTreeDBusAdapter() {}

    @DBusMethod(isAuthenticate = true, json=true)
    public String treeLevel(String authenticationHash, long id) {
    }
}
