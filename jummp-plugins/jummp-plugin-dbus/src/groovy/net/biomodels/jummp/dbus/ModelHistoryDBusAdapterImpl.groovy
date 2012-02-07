package net.biomodels.jummp.dbus

import net.biomodels.jummp.dbus.model.DBusModel
import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod

@SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
@DBusAdapter(interfaceName="ModelHistoryDBusAdapter", serviceName="modelHistoryService")
class ModelHistoryDBusAdapterImpl extends AbstractDBusAdapter implements ModelHistoryDBusAdapter {
    /**
     * Dependency Injection of Model History Service
     */
    def modelHistoryService

    @DBusMethod(isAuthenticate=true, collect = "id")
    public List<String> history(String authenticationHash) {
    }

    @DBusMethod(isAuthenticate=true)
    public DBusModel lastAccessedModel(String authenticationHash) {
    }

}
